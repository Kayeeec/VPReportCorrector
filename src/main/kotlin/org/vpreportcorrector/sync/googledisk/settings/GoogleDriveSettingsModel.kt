package org.vpreportcorrector.sync.googledisk.settings

import javafx.beans.property.SimpleStringProperty
import org.vpreportcorrector.utils.AppConstants
import org.vpreportcorrector.utils.preferencesHelper
import tornadofx.getValue
import tornadofx.setValue

class GoogleDriveSettingsModel {
    val remoteFolderIdProperty = SimpleStringProperty()
    var remoteFolderId: String? by remoteFolderIdProperty

    fun load() {
        preferencesHelper {
            sync()
            remoteFolderId = get(AppConstants.KEY_DISK_REMOTE_DIR_ID, "")
        }
    }

    fun save() {
        preferencesHelper {
            put(AppConstants.KEY_DISK_REMOTE_DIR_ID, remoteFolderId ?: "")
            flush()
        }
    }
}
