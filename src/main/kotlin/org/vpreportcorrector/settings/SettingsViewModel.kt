package org.vpreportcorrector.settings

import javafx.beans.property.SimpleStringProperty
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.utils.AppConstants.KEY_WORKING_DIRECTORY
import org.vpreportcorrector.utils.AppConstants.PREFERENCES_NODE
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class SettingsViewModel : ItemViewModel<Settings>() {
    val workingDirectory = bind(Settings::workingDirectoryProperty)

    init {
        var settings: Settings? = null
        preferences(PREFERENCES_NODE) {
            sync()
            settings = Settings(
                workingDirectory = get(KEY_WORKING_DIRECTORY, "")
            )
        }
        item = settings
    }

    override fun onCommit() {
        preferences(PREFERENCES_NODE) {
            put(KEY_WORKING_DIRECTORY, item.workingDirectory ?: "")
            flush()
        }
        fire(SettingsChanged)
    }
}

class Settings(workingDirectory: String = "") {
    val workingDirectoryProperty = SimpleStringProperty(workingDirectory)
    var workingDirectory: String by workingDirectoryProperty
}

