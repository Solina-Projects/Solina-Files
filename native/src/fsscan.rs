//! Fast directory listing and folder statistics.
//!
//! `list_dir` reads one directory and packs every entry into a single
//! byte buffer so the JVM side needs one JNI call instead of several
//! `java.io.File` syscalls per child. `dir_stats` walks a tree without
//! following symlinks and can be cancelled from another thread.

use std::collections::{HashMap, HashSet};
use std::fs;
use std::io;
use std::path::Path;
use std::sync::Mutex;

pub const FLAG_DIR: u8 = 1;
pub const FLAG_SYMLINK: u8 = 2;
pub const FLAG_HIDDEN: u8 = 4;
pub const FLAG_READABLE: u8 = 8;

static CANCELLED: Mutex<Option<HashSet<i64>>> = Mutex::new(None);

pub fn cancel(token: i64) {
    if token == 0 {
        return;
    }
    if let Ok(mut guard) = CANCELLED.lock() {
        guard.get_or_insert_with(HashSet::new).insert(token);
    }
}

fn is_cancelled(token: i64) -> bool {
    if token == 0 {
        return false;
    }
    CANCELLED
        .lock()
        .map(|g| g.as_ref().map_or(false, |s| s.contains(&token)))
        .unwrap_or(false)
}

fn clear_token(token: i64) {
    if token == 0 {
        return;
    }
    if let Ok(mut guard) = CANCELLED.lock() {
        if let Some(set) = guard.as_mut() {
            set.remove(&token);
        }
    }
}

fn mtime_millis(meta: &fs::Metadata) -> i64 {
    meta.modified()
        .ok()
        .and_then(|t| t.duration_since(std::time::UNIX_EPOCH).ok())
        .map(|d| d.as_millis() as i64)
        .unwrap_or(0)
}

/// Layout (little endian):
/// `u32 count`, then per entry
/// `u8 flags, i64 size, i64 mtime_ms, u16 name_len, name bytes (UTF-8)`.
/// A symlink reports the metadata of its target when that resolves (so a
/// link to a folder is a folder, like `java.io.File`), otherwise the link
/// itself. Entries whose name is longer than `u16::MAX` bytes are skipped.
pub fn list_dir(path: &str) -> io::Result<Vec<u8>> {
    let mut out: Vec<u8> = Vec::with_capacity(4096);
    out.extend_from_slice(&0u32.to_le_bytes());
    let mut count: u32 = 0;
    for entry in fs::read_dir(path)? {
        let entry = match entry {
            Ok(e) => e,
            Err(_) => continue,
        };
        let name_os = entry.file_name();
        // Java's File.listFiles() also exposes undecodable Unix names through
        // replacement characters. Do not silently drop those directory rows.
        let name = name_os.to_string_lossy();
        if name.len() > u16::MAX as usize {
            continue;
        }
        let link_meta = match entry.metadata() {
            Ok(m) => m,
            Err(_) => continue,
        };
        let is_symlink = link_meta.file_type().is_symlink();
        let meta = if is_symlink {
            fs::metadata(entry.path()).unwrap_or_else(|_| link_meta.clone())
        } else {
            link_meta
        };
        let mut flags = 0u8;
        if meta.is_dir() {
            flags |= FLAG_DIR;
        }
        if is_symlink {
            flags |= FLAG_SYMLINK;
        }
        if name.starts_with('.') {
            flags |= FLAG_HIDDEN;
        }
        if readable(&meta) {
            flags |= FLAG_READABLE;
        }
        let size = if meta.is_dir() { 0 } else { meta.len() as i64 };
        out.push(flags);
        out.extend_from_slice(&size.to_le_bytes());
        out.extend_from_slice(&mtime_millis(&meta).to_le_bytes());
        out.extend_from_slice(&(name.len() as u16).to_le_bytes());
        out.extend_from_slice(name.as_bytes());
        count += 1;
    }
    out[0..4].copy_from_slice(&count.to_le_bytes());
    Ok(out)
}

#[cfg(unix)]
fn readable(meta: &fs::Metadata) -> bool {
    use std::os::unix::fs::MetadataExt;
    // Approximation from mode bits; the app only uses it to dim rows.
    meta.mode() & 0o444 != 0
}

#[cfg(not(unix))]
fn readable(_meta: &fs::Metadata) -> bool {
    true
}

#[derive(Debug, Default, PartialEq, Eq)]
pub struct Stats {
    pub bytes: i64,
    pub files: i64,
    pub dirs: i64,
}

/// Sum regular files below `root` without following symlinks.
/// Returns `Ok(None)` when cancelled.
pub fn dir_stats(root: &str, token: i64) -> io::Result<Option<Stats>> {
    let mut stats = Stats::default();
    let mut stack = vec![Path::new(root).to_path_buf()];
    let mut seen: u64 = 0;
    let result = (|| {
        while let Some(dir) = stack.pop() {
            let rd = match fs::read_dir(&dir) {
                Ok(rd) => rd,
                Err(e) if dir.as_path() == Path::new(root) => return Err(e),
                Err(_) => continue,
            };
            for entry in rd {
                let entry = match entry {
                    Ok(e) => e,
                    Err(_) => continue,
                };
                seen += 1;
                if seen % 256 == 0 && is_cancelled(token) {
                    return Ok(None);
                }
                let ft = match entry.file_type() {
                    Ok(t) => t,
                    Err(_) => continue,
                };
                if ft.is_symlink() {
                    continue;
                }
                if ft.is_dir() {
                    stats.dirs += 1;
                    stack.push(entry.path());
                } else if ft.is_file() {
                    stats.files += 1;
                    if let Ok(m) = entry.metadata() {
                        stats.bytes += m.len() as i64;
                    }
                }
            }
        }
        Ok(Some(()))
    })();
    clear_token(token);
    match result {
        Ok(Some(())) => Ok(Some(stats)),
        Ok(None) => Ok(None),
        Err(e) => Err(e),
    }
}


/// Calculate statistics for every direct child directory in one tree walk.
/// The root directory is opened once; descendants are assigned to their
/// top-level child, avoiding one recursive walk per visible folder row.
/// Symlinks below each child are not followed. A direct child symlink to a
/// directory is followed to preserve the existing single-folder behavior.
pub fn dir_stats_children(
    root: &str,
    child_names: &[String],
    token: i64,
) -> io::Result<Option<HashMap<String, Stats>>> {
    let wanted: HashSet<&str> = child_names.iter().map(String::as_str).collect();
    let mut stats: HashMap<String, Stats> = HashMap::with_capacity(child_names.len());

    let result = (|| {
        if is_cancelled(token) {
            return Ok(None);
        }
        let mut stack: Vec<(std::path::PathBuf, String, bool)> = Vec::new();
        let mut seen: u64 = 0;
        for entry in fs::read_dir(root)? {
            let entry = match entry {
                Ok(e) => e,
                Err(_) => continue,
            };
            seen += 1;
            if seen % 256 == 0 && is_cancelled(token) {
                return Ok(None);
            }
            let name = match entry.file_name().to_str() {
                Some(n) if wanted.contains(n) => n.to_owned(),
                _ => continue,
            };
            // Follow a symlink only when it is one of the explicitly requested
            // direct child folders. Descendant symlinks are always skipped.
            if fs::metadata(entry.path()).map(|m| m.is_dir()).unwrap_or(false) {
                stats.insert(name.clone(), Stats::default());
                stack.push((entry.path(), name, true));
            }
        }

        while let Some((dir, owner, is_child_root)) = stack.pop() {
            let rd = match fs::read_dir(&dir) {
                Ok(rd) => rd,
                Err(_) => {
                    // Match the old per-folder behavior: if the direct child
                    // itself cannot be scanned, fall back to Kotlin for it.
                    if is_child_root {
                        stats.remove(&owner);
                    }
                    continue;
                }
            };
            for entry in rd {
                let entry = match entry {
                    Ok(e) => e,
                    Err(_) => continue,
                };
                seen += 1;
                if seen % 256 == 0 && is_cancelled(token) {
                    return Ok(None);
                }
                let ft = match entry.file_type() {
                    Ok(t) => t,
                    Err(_) => continue,
                };
                if ft.is_symlink() {
                    continue;
                }
                if ft.is_dir() {
                    if let Some(s) = stats.get_mut(&owner) {
                        s.dirs += 1;
                    }
                    stack.push((entry.path(), owner.clone(), false));
                } else if ft.is_file() {
                    if let Some(s) = stats.get_mut(&owner) {
                        s.files += 1;
                        if let Ok(meta) = entry.metadata() {
                            s.bytes += meta.len() as i64;
                        }
                    }
                }
            }
        }
        Ok(Some(()))
    })();

    clear_token(token);
    match result {
        Ok(Some(())) => Ok(Some(stats)),
        Ok(None) => Ok(None),
        Err(e) => Err(e),
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::Write;

    fn tmp(name: &str) -> std::path::PathBuf {
        let p = std::env::temp_dir().join(format!("solina_fsscan_{}_{}", name, std::process::id()));
        let _ = fs::remove_dir_all(&p);
        fs::create_dir_all(&p).unwrap();
        p
    }

    fn decode(buf: &[u8]) -> Vec<(u8, i64, String)> {
        let n = u32::from_le_bytes(buf[0..4].try_into().unwrap()) as usize;
        let mut pos = 4;
        let mut v = Vec::new();
        for _ in 0..n {
            let flags = buf[pos];
            let size = i64::from_le_bytes(buf[pos + 1..pos + 9].try_into().unwrap());
            let len = u16::from_le_bytes(buf[pos + 17..pos + 19].try_into().unwrap()) as usize;
            let name = String::from_utf8(buf[pos + 19..pos + 19 + len].to_vec()).unwrap();
            pos += 19 + len;
            v.push((flags, size, name));
        }
        assert_eq!(pos, buf.len());
        v.sort_by(|a, b| a.2.cmp(&b.2));
        v
    }

    #[test]
    fn lists_files_dirs_and_hidden() {
        let d = tmp("list");
        fs::create_dir(d.join("sub")).unwrap();
        fs::File::create(d.join("a.txt")).unwrap().write_all(b"hello").unwrap();
        fs::File::create(d.join(".hid")).unwrap();
        let got = decode(&list_dir(d.to_str().unwrap()).unwrap());
        assert_eq!(got.len(), 3);
        let hid = got.iter().find(|e| e.2 == ".hid").unwrap();
        assert_ne!(hid.0 & FLAG_HIDDEN, 0);
        let a = got.iter().find(|e| e.2 == "a.txt").unwrap();
        assert_eq!(a.1, 5);
        assert_eq!(a.0 & FLAG_DIR, 0);
        let s = got.iter().find(|e| e.2 == "sub").unwrap();
        assert_ne!(s.0 & FLAG_DIR, 0);
        let _ = fs::remove_dir_all(&d);
    }

    #[test]
    fn unicode_names_round_trip() {
        let d = tmp("uni");
        fs::File::create(d.join("فایل ۱.txt")).unwrap();
        let got = decode(&list_dir(d.to_str().unwrap()).unwrap());
        assert_eq!(got[0].2, "فایل ۱.txt");
        let _ = fs::remove_dir_all(&d);
    }

    #[cfg(unix)]
    #[test]
    fn invalid_utf8_names_are_not_silently_dropped() {
        use std::os::unix::ffi::OsStrExt;
        let d = tmp("invalid-utf8");
        let raw_name = std::ffi::OsStr::from_bytes(b"bad-\\xff-name");
        fs::File::create(d.join(raw_name)).unwrap();
        let got = decode(&list_dir(d.to_str().unwrap()).unwrap());
        assert_eq!(got.len(), 1);
        assert!(got[0].2.contains('�'));
        let _ = fs::remove_dir_all(&d);
    }

    #[test]
    fn missing_dir_is_error() {
        assert!(list_dir("/definitely/not/here").is_err());
        assert!(dir_stats("/definitely/not/here", 0).is_err());
    }

    #[cfg(unix)]
    #[test]
    fn stats_skip_symlinks_and_count() {
        let d = tmp("stats");
        fs::create_dir_all(d.join("x/y")).unwrap();
        fs::File::create(d.join("x/y/f1")).unwrap().write_all(&[0u8; 10]).unwrap();
        fs::File::create(d.join("f2")).unwrap().write_all(&[0u8; 5]).unwrap();
        std::os::unix::fs::symlink(d.join("x"), d.join("loop")).unwrap();
        let s = dir_stats(d.to_str().unwrap(), 0).unwrap().unwrap();
        assert_eq!(s, Stats { bytes: 15, files: 2, dirs: 2 });
        let _ = fs::remove_dir_all(&d);
    }

    #[test]
    fn cancelled_walk_returns_none() {
        let d = tmp("cancel");
        for i in 0..600 {
            fs::File::create(d.join(format!("f{i}"))).unwrap();
        }
        cancel(777);
        assert_eq!(dir_stats(d.to_str().unwrap(), 777).unwrap(), None);
        // The token is cleared afterwards, so the same token works again.
        assert!(dir_stats(d.to_str().unwrap(), 777).unwrap().is_some());
        let _ = fs::remove_dir_all(&d);
    }

    #[test]
    fn batch_stats_walks_children_once_and_keeps_them_separate() {
        let d = tmp("batch");
        fs::create_dir_all(d.join("a/nested")).unwrap();
        fs::create_dir_all(d.join("b")).unwrap();
        fs::File::create(d.join("a/one")).unwrap().write_all(&[0u8; 10]).unwrap();
        fs::File::create(d.join("a/nested/two")).unwrap().write_all(&[0u8; 5]).unwrap();
        fs::File::create(d.join("b/three")).unwrap().write_all(&[0u8; 7]).unwrap();

        let names = vec!["a".to_string(), "b".to_string()];
        let got = dir_stats_children(d.to_str().unwrap(), &names, 0).unwrap().unwrap();
        assert_eq!(got.get("a").unwrap().bytes, 15);
        assert_eq!(got.get("a").unwrap().files, 2);
        assert_eq!(got.get("a").unwrap().dirs, 1);
        assert_eq!(got.get("b").unwrap().bytes, 7);
        assert_eq!(got.get("b").unwrap().files, 1);
        assert_eq!(got.get("b").unwrap().dirs, 0);
        let _ = fs::remove_dir_all(&d);
    }

}
