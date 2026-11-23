//! Guards against the JNI symbol/package mismatch: every `external fun` in
//! the Kotlin bridges must have a matching `#[no_mangle]` Rust export.

use std::fs;
use std::path::Path;

fn rust_exports() -> String {
    let mut all = String::new();
    for f in ["lib.rs", "pdfium.rs", "rar.rs", "crypt.rs", "hexio.rs", "fsscan.rs"] {
        if let Ok(s) = fs::read_to_string(Path::new(env!("CARGO_MANIFEST_DIR")).join("src").join(f)) {
            all.push_str(&s);
        }
    }
    all
}

fn externals(kotlin: &str) -> Vec<String> {
    kotlin
        .lines()
        .filter_map(|l| {
            let l = l.trim();
            let rest = l.strip_prefix("private external fun ").or_else(|| l.strip_prefix("external fun "))?;
            Some(rest.split('(').next()?.trim().to_string())
        })
        .collect()
}

fn check(kotlin_rel: &str, class_path: &str) {
    let root = Path::new(env!("CARGO_MANIFEST_DIR")).join("..");
    let kt = fs::read_to_string(root.join(kotlin_rel)).expect("kotlin bridge file");
    let rs = rust_exports();
    let names = externals(&kt);
    assert!(!names.is_empty(), "no external funs found in {kotlin_rel}");
    for n in names {
        let symbol = format!("Java_{class_path}_{n}");
        assert!(rs.contains(&symbol), "missing Rust export {symbol} for {kotlin_rel}");
    }
}

#[test]
fn native_bridge_symbols_match_kotlin() {
    check(
        "app/src/main/java/ir/solina/files/core/nativebridge/NativeBridge.kt",
        "ir_solina_files_core_nativebridge_NativeBridge",
    );
}

#[test]
fn pdfium_symbols_match_kotlin() {
    check(
        "app/src/main/java/ir/solina/files/core/pdf/view/Pdfium.kt",
        "ir_solina_files_core_pdf_view_Pdfium",
    );
}
