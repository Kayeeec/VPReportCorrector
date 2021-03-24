package org.vpreportcorrector.utils

import org.vpreportcorrector.utils.AppConstants.KEY_WORKING_DIRECTORY
import org.vpreportcorrector.utils.AppConstants.PREFERENCES_NODE
import tornadofx.Component
import tornadofx.get
import java.nio.file.Path
import java.nio.file.Paths
import java.text.MessageFormat

/**
 * Extension function for easier internationalization. Created to easily support parameters given as [args].
 * @param key   message key
 * @param args  arguments to fill the message pattern
 */
fun Component.t(key: String, vararg args: Any?): String {
    return MessageFormat.format(this.messages[key], *args.filterNotNull().toTypedArray())
}

/**
 * Returns the current user-defined working directory from preferences.
 * @return Path a Path object representing the working directory, null if it is not defined
 */
fun Component.getWorkingDirectory(): Path? {
    var workingDir = ""
    preferences(PREFERENCES_NODE) {
        sync()
        workingDir = get(KEY_WORKING_DIRECTORY, "")
    }
    return if (workingDir.isNotEmpty()) Paths.get(workingDir) else null
}
