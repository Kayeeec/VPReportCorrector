package org.umlreviewer.sync.git.settings

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.umlreviewer.settings.LoadableAndSavable
import org.umlreviewer.utils.PreferencesHelper.preferencesHelper
import tornadofx.getValue
import tornadofx.setValue
import java.nio.file.Files
import java.nio.file.Paths

class GitSettingsModel: LoadableAndSavable {
    val configUserNameProperty = SimpleStringProperty("")
    var configUserName: String by configUserNameProperty

    val configUserEmailProperty = SimpleStringProperty("")
    var configUserEmail: String by configUserEmailProperty

    val repoUrlProperty = SimpleStringProperty("")
    var repoUrl: String by repoUrlProperty

    val passphraseProperty = SimpleStringProperty("")
    var passphrase: String by passphraseProperty

    val privateKeyPathProperty = SimpleStringProperty("")
    var privateKeyPath: String by privateKeyPathProperty

    val passwordProperty = SimpleStringProperty("")
    var password: String by passwordProperty

    val usernameProperty = SimpleStringProperty("")
    var username: String by usernameProperty

    val methodProperty = SimpleObjectProperty<GitProtocol>(GitProtocol.HTTPS)
    var method: GitProtocol by methodProperty

    override fun load() {
        // TODO KB: decrypt values
        preferencesHelper {
            sync()
            repoUrl = get(GitPreferencesKey.URL, "")
            method = enumValueOf(get(GitPreferencesKey.METHOD, GitProtocol.HTTPS.name))
            configUserName = get(GitPreferencesKey.CONFIG_USER_NAME, "")
            configUserEmail = get(GitPreferencesKey.CONFIG_USER_EMAIL, "")

            passphrase = get(GitPreferencesKey.PASSPHRASE, "")
            privateKeyPath = get(GitPreferencesKey.PRIVATE_KEY_PATH, "")

            password = get(GitPreferencesKey.PASSWORD, "")
            username = get(GitPreferencesKey.USERNAME, "")
        }
    }

    override fun save() {
        // TODO encrypt values
        preferencesHelper {
            put(GitPreferencesKey.URL, repoUrl.trim())
            put(GitPreferencesKey.METHOD, method.name)
            put(GitPreferencesKey.CONFIG_USER_NAME, configUserName.trim())
            put(GitPreferencesKey.CONFIG_USER_EMAIL, configUserEmail.trim())
            if (method == GitProtocol.HTTPS) {
                put(GitPreferencesKey.PASSWORD, password.trim())
                put(GitPreferencesKey.USERNAME, username.trim())
                put(GitPreferencesKey.PRIVATE_KEY_PATH, "")
                put(GitPreferencesKey.PASSPHRASE, "")
            } else if (method == GitProtocol.SSH) {
                put(GitPreferencesKey.PASSWORD, "")
                put(GitPreferencesKey.USERNAME, "")
                put(GitPreferencesKey.PRIVATE_KEY_PATH, privateKeyPath.trim())
                put(GitPreferencesKey.PASSPHRASE, passphrase.trim())
            }
            flush()
        }
    }

    fun hasCorrectRemoteRepoSettings(): Boolean {
        return repoUrl.isNotBlank() && (hasCorrectHttpsSettings() || hasCorrectSshSettings())
    }

    private fun hasCorrectHttpsSettings(): Boolean {
        return method == GitProtocol.HTTPS && username.isNotEmpty() && password.isNotEmpty()
    }

    private fun hasCorrectSshSettings(): Boolean {
        return method == GitProtocol.SSH && Files.exists(Paths.get(privateKeyPath))
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is GitSettingsModel
                && configUserName == other.configUserName
                && configUserEmail == other.configUserEmail
                && repoUrl == other.repoUrl
                && passphrase == other.passphrase
                && privateKeyPath == other.privateKeyPath
                && password == other.password
                && username == other.username
                && method == other.method
    }

    override fun hashCode(): Int {
        var result = configUserNameProperty.hashCode()
        result = 31 * result + configUserEmailProperty.hashCode()
        result = 31 * result + repoUrlProperty.hashCode()
        result = 31 * result + passphraseProperty.hashCode()
        result = 31 * result + privateKeyPathProperty.hashCode()
        result = 31 * result + passwordProperty.hashCode()
        result = 31 * result + usernameProperty.hashCode()
        result = 31 * result + methodProperty.hashCode()
        return result
    }
}
