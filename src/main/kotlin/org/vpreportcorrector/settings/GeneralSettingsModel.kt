package org.vpreportcorrector.settings

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.vpreportcorrector.sync.RemoteRepo
import org.vpreportcorrector.utils.enumValueOrNull
import org.vpreportcorrector.utils.preferencesHelper
import tornadofx.getValue
import tornadofx.setValue

class GeneralSettingsModel: Saveable, Loadable {
    /**
     * To choose which remote repository to use.
     * Statically set to RemoteRepo.GIT as it is the only remote repository implemented.
     */
    val syncToProperty = SimpleObjectProperty(RemoteRepo.default)
    var syncTo: RemoteRepo by syncToProperty

    val workingDirectoryProperty = SimpleStringProperty("")
    var workingDirectory: String? by workingDirectoryProperty


    override fun load() {
        preferencesHelper {
            sync()
            workingDirectory = get(SettingsPreferencesKey.WORKING_DIRECTORY, "")
            syncTo = enumValueOrNull(get(SettingsPreferencesKey.SYNC_TO, RemoteRepo.default.name)) ?: RemoteRepo.default
        }
    }

    override fun save() {
        preferencesHelper {
            put(SettingsPreferencesKey.WORKING_DIRECTORY, workingDirectory ?: "")
            put(SettingsPreferencesKey.SYNC_TO, syncTo.name)
            flush()
        }
    }
}
