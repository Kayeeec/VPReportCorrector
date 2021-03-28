package org.vpreportcorrector.settings

import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import org.vpreportcorrector.utils.t
import tornadofx.ItemViewModel
import tornadofx.error

class GeneralSettingsViewModel : ItemViewModel<GeneralSettingsModel>(GeneralSettingsModel())
    , WithLoading by LoadingLatch(), Saveable {
    val workingDirectory = bind(GeneralSettingsModel::workingDirectoryProperty)

    init {
        try {
            item.load()
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
            error(
                title = t("error.defaultTitle"),
                header = "Failed to load general settings.",
                content = t("error.content", e.message)
            )
        }
    }

    override fun save() {
        try {
            item.save()
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
            error(
                title = t("error.defaultTitle"),
                header = "Failed to save general settings.",
                content = t("error.content", e.message)
            )
        }
    }
}
