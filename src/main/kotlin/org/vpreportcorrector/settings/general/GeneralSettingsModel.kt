package org.vpreportcorrector.settings.general

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.vpreportcorrector.settings.LoadableAndSavable
import org.vpreportcorrector.settings.SettingsPreferencesKey
import org.vpreportcorrector.sync.RemoteRepo
import org.vpreportcorrector.utils.enumValueOrNull
import org.vpreportcorrector.utils.preferencesHelper
import tornadofx.getValue
import tornadofx.setValue

class GeneralSettingsModel: LoadableAndSavable {
    /**
     * To choose which remote repository to use.
     * Statically set to RemoteRepo.GIT as it is the only remote repository implemented.
     */
    val remoteRepositoryProperty = SimpleObjectProperty(RemoteRepo.default)
    var remoteRepository: RemoteRepo by remoteRepositoryProperty

    val workingDirectoryProperty = SimpleStringProperty("")
    var workingDirectory: String? by workingDirectoryProperty


    override fun load() {
        preferencesHelper {
            sync()
            workingDirectory = get(SettingsPreferencesKey.WORKING_DIRECTORY, "")
            remoteRepository = enumValueOrNull(get(SettingsPreferencesKey.REMOTE_REPOSITORY, RemoteRepo.default.name))
                ?: RemoteRepo.default
        }
    }

    override fun save() {
        preferencesHelper {
            put(SettingsPreferencesKey.WORKING_DIRECTORY, workingDirectory ?: "")
            put(SettingsPreferencesKey.REMOTE_REPOSITORY, remoteRepository.name)
            flush()
        }
    }
}
