package org.umlreviewer.sync.git.settings

import javafx.beans.property.SimpleBooleanProperty
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.settings.SettingsViewModel
import org.umlreviewer.utils.t
import tornadofx.objectBinding
import tornadofx.rebind

class GitSettingsViewModel : SettingsViewModel<GitSettingsModel>(GitSettingsModel()) {
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

    override fun load() {
        try {
            startLoading()
            val m = GitSettingsModel()
            m.load()
            rebind { item = m }
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
            tornadofx.error(
                title = t("error.defaultTitle"),
                header = "Failed to load Git repository settings.",
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
            tornadofx.error(
                title = t("error.defaultTitle"),
                header = "Failed to save Git repository settings.",
                content = t("error.content", e.message)
            )
        } finally {
            endLoading()
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
