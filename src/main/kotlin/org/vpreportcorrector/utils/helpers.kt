package org.vpreportcorrector.utils

import org.vpreportcorrector.settings.SettingsPreferencesKey
import org.vpreportcorrector.sync.RemoteRepo
import org.vpreportcorrector.utils.AppConstants.PREFERENCES_NODE
import java.nio.file.Path
import java.nio.file.Paths
import java.util.prefs.Preferences

fun preferencesHelper(op: Preferences.() -> Unit) {
    val node = Preferences.userRoot().node(PREFERENCES_NODE)
    op(node)
}

fun getWorkingDirectory(): Path? {
    var workingDir = ""
    preferencesHelper {
        sync()
        workingDir = get(SettingsPreferencesKey.WORKING_DIRECTORY, "")
    }
    return if (workingDir.isNotEmpty()) Paths.get(workingDir) else null
}

fun getSyncTo(): RemoteRepo? {
    var result = ""
    preferencesHelper {
        sync()
        result = get(SettingsPreferencesKey.SYNC_TO, RemoteRepo.default.name)
    }
    return enumValueOrNull(result)
}
