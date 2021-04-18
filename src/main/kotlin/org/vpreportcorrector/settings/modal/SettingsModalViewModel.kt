package org.vpreportcorrector.settings.modal

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.ReadOnlyObjectProperty
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import org.vpreportcorrector.settings.general.GeneralSettingsViewModel
import org.vpreportcorrector.sync.RemoteRepo
import org.vpreportcorrector.sync.git.settings.GitSettingsViewModel
import tornadofx.ViewModel
import tornadofx.finally
import tornadofx.success

class SettingsModalViewModel : ViewModel(), WithLoading by LoadingLatch() {
    private val generalVm: GeneralSettingsViewModel by inject()
    private val gitVm: GitSettingsViewModel by inject()

    val isAnyLoading: BooleanBinding = isLoading.or(generalVm.isLoading).or(gitVm.isLoading)
    val isAnyDirty: BooleanBinding = generalVm.dirty.or(gitVm.dirty)
    val isSaveEnabled: BooleanBinding = generalVm.valid.and(gitVm.valid).and(isAnyDirty)
    val repositoryType: ReadOnlyObjectProperty<RemoteRepo> = generalVm.remoteRepository

    fun save(onSuccess: () -> Unit) {
        val doSave = generalVm.commit() && gitVm.commit()
        if (doSave) {
            startLoading()
            runAsync {
                generalVm.save()
                if (repositoryType.value == RemoteRepo.GIT) gitVm.save()
            } success {
                onSuccess()
            } finally {
                endLoading()
                fire(SettingsChanged)
            }
        }
    }

    fun reset() {
        generalVm.rollback()
        gitVm.rollback()
    }

    fun initialize() {
        runAsync {
            startLoading()
            generalVm.load()
            gitVm.load()
        } finally {
            endLoading()
        }
    }
}
