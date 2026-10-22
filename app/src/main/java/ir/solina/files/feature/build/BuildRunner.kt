package ir.solina.files.feature.build

import ir.solina.files.R
import ir.solina.files.core.build.BuildListener
import ir.solina.files.core.build.BuildRun
import ir.solina.files.core.build.BuildTracker
import ir.solina.files.core.dex.SmaliSyntaxException
import ir.solina.files.core.i18n.tr
import ir.solina.files.core.sound.UiSound
import ir.solina.files.core.sound.UiSounds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> runBuild(
    title: String,
    subject: String,
    plan: List<String>,
    publish: (BuildRun) -> Unit,
    summary: (T) -> String,
    work: suspend (BuildListener) -> T,
): Result<T> {
    val tracker = BuildTracker(title, subject, plan, onChange = publish)
    val result = try {
        Result.success(withContext(Dispatchers.Default) { work(tracker) })
    } catch (e: CancellationException) {
        tracker.fail(tr(R.string.build_stopped))
        throw e
    } catch (e: Throwable) {
        if (e is VirtualMachineError && e !is OutOfMemoryError) throw e
        Result.failure(e)
    }
    result.onSuccess {
        tracker.finish(summary(it))
        UiSounds.play(UiSound.SUCCESS)
    }
    result.onFailure { e ->
        tracker.fail(e, details(e))
        UiSounds.play(UiSound.ERROR)
    }
    return result
}

fun details(e: Throwable): List<String> = when (e) {
    is SmaliSyntaxException -> e.errors.map { it.toString() }
    else -> emptyList()
}
