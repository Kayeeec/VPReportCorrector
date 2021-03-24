package org.vpreportcorrector.settings

import javafx.beans.property.SimpleStringProperty
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.utils.KEY_WORKING_DIRECTORY
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class SettingsModel : ItemViewModel<Settings>() {
    val workingDirectory = bind(Settings::workingDirectoryProperty)

    init {
        var settings: Settings? = null
        preferences {
            sync()
            settings = Settings(
                workingDirectory = get(KEY_WORKING_DIRECTORY, "")
            )
        }
        item = settings
    }

    override fun onCommit() {
        preferences {
            put(KEY_WORKING_DIRECTORY, item.workingDirectory ?: "")
            flush()
        }
        fire(SettingsChanged)
    }
}

class Settings(workingDirectory: String = "") {
    val workingDirectoryProperty = SimpleStringProperty(workingDirectory)
    var workingDirectory by workingDirectoryProperty
}

