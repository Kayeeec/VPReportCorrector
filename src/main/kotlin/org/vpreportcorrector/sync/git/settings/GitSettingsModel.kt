package org.vpreportcorrector.sync.git.settings

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.vpreportcorrector.settings.Loadable
import org.vpreportcorrector.settings.Saveable
import org.vpreportcorrector.utils.preferencesHelper
import java.nio.file.Files
import java.nio.file.Paths
import tornadofx.*

class GitSettingsModel: Saveable, Loadable {
    val configUserNameProperty = SimpleStringProperty("")
    var configUserName: String by configUserNameProperty

    val configUserEmailProperty = SimpleStringProperty("")
    var configUserEmail: String by configUserEmailProperty

    val repoUrlProperty = SimpleStringProperty("")
    var repoUrl by repoUrlProperty

    val passphraseProperty = SimpleStringProperty("")
    var passphrase by passphraseProperty

    val privateKeyPathProperty = SimpleStringProperty("")
    var privateKeyPath by privateKeyPathProperty

    val passwordProperty = SimpleStringProperty("")
    var password by passwordProperty

    val usernameProperty = SimpleStringProperty("")
    var username by usernameProperty

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
            put(GitPreferencesKey.URL, repoUrl ?: "")
            put(GitPreferencesKey.METHOD, method.name)
            put(GitPreferencesKey.CONFIG_USER_NAME, configUserName)
            put(GitPreferencesKey.CONFIG_USER_EMAIL, configUserEmail)
            if (method == GitProtocol.HTTPS) {
                put(GitPreferencesKey.PASSWORD, password ?: "")
                put(GitPreferencesKey.USERNAME, username ?: "")
                put(GitPreferencesKey.PRIVATE_KEY_PATH, "")
                put(GitPreferencesKey.PASSPHRASE, "")
            } else if (method == GitProtocol.SSH) {
                put(GitPreferencesKey.PASSWORD, "")
                put(GitPreferencesKey.USERNAME, "")
                put(GitPreferencesKey.PRIVATE_KEY_PATH, privateKeyPath ?: "")
                put(GitPreferencesKey.PASSPHRASE, passphrase ?: "")
            }
            flush()
        }
    }

    fun hasCorrectRemoteRepoSettings(): Boolean {
        return repoUrl != null && (hasCorrectHttpsSettings() || hasCorrectSshSettings())
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
}
