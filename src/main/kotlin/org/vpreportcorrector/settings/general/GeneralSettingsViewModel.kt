package org.vpreportcorrector.settings.general

import org.vpreportcorrector.settings.SettingsViewModel
import org.vpreportcorrector.utils.t
import tornadofx.error
import tornadofx.rebind


class GeneralSettingsViewModel : SettingsViewModel<GeneralSettingsModel>(GeneralSettingsModel()) {
    val workingDirectory = bind(GeneralSettingsModel::workingDirectoryProperty)
    val remoteRepository = bind(GeneralSettingsModel::remoteRepositoryProperty)

    override fun load() {
        try {
            startLoading()
            val m = GeneralSettingsModel()
            m.load()
            rebind { item = m }
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
            error(
                title = t("error.defaultTitle"),
                header = "Failed to load general settings.",
                content = t("error.content", e.message)
            )
        } finally {
            endLoading()
        }
    }

    override fun save() {
        try {
            startLoading()
            item.save()
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
            error(
                title = t("error.defaultTitle"),
                header = "Failed to save general settings.",
                content = t("error.content", e.message)
            )
        } finally {
            endLoading()
        }
    }
}
