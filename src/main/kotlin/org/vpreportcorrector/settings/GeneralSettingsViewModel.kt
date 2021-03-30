package org.vpreportcorrector.settings

import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import tornadofx.ItemViewModel
import tornadofx.finally

class GeneralSettingsViewModel : ItemViewModel<GeneralSettingsModel>(GeneralSettingsModel())
    , WithLoading by LoadingLatch() {
    val workingDirectory = bind(GeneralSettingsModel::workingDirectoryProperty)

    init {
        item.load()
    }

    fun save() {
        runAsync {
            startLoading()
            item.save()
        } finally {
            endLoading()
        }
    }
}

