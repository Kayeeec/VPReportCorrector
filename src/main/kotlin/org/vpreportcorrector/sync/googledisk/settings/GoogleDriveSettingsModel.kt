package org.vpreportcorrector.sync.googledisk.settings

import javafx.beans.property.SimpleStringProperty
import org.vpreportcorrector.settings.LoadableAndSavable
import org.vpreportcorrector.sync.googledisk.GoogleDrivePreferencesKeys.REMOTE_DIR_ID
import org.vpreportcorrector.utils.Helpers.preferencesHelper
import tornadofx.getValue
import tornadofx.setValue

class GoogleDriveSettingsModel: LoadableAndSavable {
    val remoteFolderIdProperty = SimpleStringProperty()
    var remoteFolderId: String? by remoteFolderIdProperty

    override fun load() {
        preferencesHelper {
            sync()
            remoteFolderId = get(REMOTE_DIR_ID, "")
        }
    }

    override fun save() {
        preferencesHelper {
            put(REMOTE_DIR_ID, remoteFolderId ?: "")
            flush()
        }
    }
}
