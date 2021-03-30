package org.vpreportcorrector.settings

import javafx.beans.binding.BooleanBinding
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import org.vpreportcorrector.sync.googledisk.settings.GoogleDriveSettingsViewModel
import tornadofx.ViewModel

class SettingsViewModel : ViewModel(), WithLoading by LoadingLatch() {
    private val generalVm: GeneralSettingsViewModel by inject()
    private val googleDiskVm: GoogleDriveSettingsViewModel by inject()

    val isAnyLoading: BooleanBinding = isLoading.or(googleDiskVm.isLoading)
    val isAnyDirty: BooleanBinding = generalVm.dirty.or(googleDiskVm.dirty)
    val isSaveEnabled: BooleanBinding = generalVm.valid.and(googleDiskVm.valid)
        .and(isAnyDirty)


    // TODO: this could be refactored to only save dirty view models (minor)
    fun save(onSuccess: () -> Unit) {
        val doSave = generalVm.commit() && googleDiskVm.commit()
        if (doSave) {
            generalVm.save()
            googleDiskVm.save()
            fire(SettingsChanged)
            onSuccess()
        }
    }

    fun reset() {
        generalVm.rollback()
        googleDiskVm.rollback(googleDiskVm.remoteFolderId)
    }
}
