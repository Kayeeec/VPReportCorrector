package org.vpreportcorrector.utils

import org.vpreportcorrector.settings.SettingsPreferencesKey
import org.vpreportcorrector.sync.RemoteRepo
import org.vpreportcorrector.utils.AppConstants.PREFERENCES_NODE
import java.nio.file.Files
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

/**
 * Removes all files in the '.data' directory that don't have and equivalent PDF file in the working directory.
 */
fun cleanDataDirectory() {
    val workDir = getWorkingDirectory() ?: return
    val dataDir = Paths.get(workDir.toFile().canonicalPath, AppConstants.DATA_FOLDER_NAME)
    if (!Files.exists(dataDir)) return

    Files.walk(dataDir).use { walk ->
        walk.sorted(Comparator.reverseOrder())
            .filter { !hasEquivalentPdfOrDirectory(it, workDir, dataDir) }
            .forEach {
                if (it !== dataDir) it.toFile().delete()
            }
    }
}

private fun hasEquivalentPdfOrDirectory(path: Path?, workDir: Path, dataDir: Path): Boolean {
    if (path == null || path == dataDir) return true
    val filename = if (path.toFile().isDirectory) path.toFile().nameWithoutExtension
    else "${path.toFile().nameWithoutExtension}.pdf"
    val relParentPath = path.parent.toFile().relativeTo(dataDir.toFile())
    val ekvFile = Paths.get(workDir.toFile().canonicalPath, relParentPath.toString(), filename)
    return fileExistsNameCaseInsensitive(ekvFile)
}
