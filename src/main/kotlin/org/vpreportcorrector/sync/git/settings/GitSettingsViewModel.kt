package org.vpreportcorrector.sync.git.settings

import javafx.beans.property.SimpleBooleanProperty
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import org.vpreportcorrector.settings.Saveable
import org.vpreportcorrector.utils.t
import tornadofx.ItemViewModel
import tornadofx.objectBinding

class GitSettingsViewModel : ItemViewModel<GitSettingsModel>(GitSettingsModel()), WithLoading by LoadingLatch(),
    Saveable {
    val configUserName = bind(GitSettingsModel::configUserNameProperty)
    val configUserEmail = bind(GitSettingsModel::configUserEmailProperty)
    val repoUrl = bind(GitSettingsModel::repoUrlProperty)
    val passphrase = bind(GitSettingsModel::passphraseProperty)
    val privateKeyPath = bind(GitSettingsModel::privateKeyPathProperty)
    val password = bind(GitSettingsModel::passwordProperty)
    val username = bind(GitSettingsModel::usernameProperty)
    val method = bind(GitSettingsModel::methodProperty)

    val showPassword = SimpleBooleanProperty(false)
    val showPasswordGraphic = objectBinding(this, showPassword) {
        selectShowGraphic(showPassword.value)
    }
    val showPassphrase = SimpleBooleanProperty(false)
    val showPassphraseGraphic = objectBinding(this, showPassphrase) {
        selectShowGraphic(showPassphrase.value)
    }

    init {
        try {
            item.load()
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
            tornadofx.error(
                title = t("error.defaultTitle"),
                header = "Failed to load Git repository settings.",
                content = t("error.content", e.message)
            )
        }
    }

    override fun save() {
        try {
            item.save()
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
            tornadofx.error(
                title = t("error.defaultTitle"),
                header = "Failed to save Git repository settings.",
                content = t("error.content", e.message)
            )
        }
    }

    private fun selectShowGraphic(show: Boolean): FontIcon {
        return if (show) {
            FontIcon(FontAwesomeRegular.EYE_SLASH)
        } else {
            FontIcon(FontAwesomeRegular.EYE)
        }
    }
}
