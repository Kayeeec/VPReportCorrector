package org.vpreportcorrector.settings

import javafx.beans.binding.BooleanBinding
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import org.vpreportcorrector.sync.git.settings.GitSettingsViewModel
import tornadofx.ViewModel
import tornadofx.finally
import tornadofx.success

class SettingsViewModel : ViewModel(), WithLoading by LoadingLatch() {
    private val generalVm: GeneralSettingsViewModel by inject()
    private val gitVm: GitSettingsViewModel by inject()

    val isAnyLoading: BooleanBinding = isLoading.or(gitVm.isLoading)
    val isAnyDirty: BooleanBinding = generalVm.dirty.or(gitVm.dirty)
    val isSaveEnabled: BooleanBinding = generalVm.valid.and(gitVm.valid).and(isAnyDirty)

    fun save(onSuccess: () -> Unit) {
        val doSave = generalVm.commit() && gitVm.commit()
        if (doSave) {
            startLoading()
            runAsync {
                generalVm.save()
                gitVm.save()
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
}
