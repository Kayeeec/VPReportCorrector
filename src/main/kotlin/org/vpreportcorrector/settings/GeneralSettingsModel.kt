package org.vpreportcorrector.settings

import javafx.beans.property.SimpleStringProperty
import org.vpreportcorrector.utils.AppConstants
import org.vpreportcorrector.utils.preferencesHelper
import tornadofx.getValue
import tornadofx.setValue

class GeneralSettingsModel {
    val workingDirectoryProperty = SimpleStringProperty("")
    var workingDirectory: String? by workingDirectoryProperty


    fun load() {
        preferencesHelper {
            sync()
            workingDirectory = get(AppConstants.KEY_WORKING_DIRECTORY, "")
        }
    }

    fun save() {
        preferencesHelper {
            put(AppConstants.KEY_WORKING_DIRECTORY, workingDirectory ?: "")
            flush()
        }
    }
}
