package ir.solina.files.feature.termux

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.solina.files.core.termux.AndroidTermux
import ir.solina.files.core.termux.TermuxSession

class TermuxViewModel(app: Application) : AndroidViewModel(app) {
    val session = TermuxSession(AndroidTermux(app), viewModelScope)
}
