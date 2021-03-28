package org.vpreportcorrector.sync

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.vpreportcorrector.settings.Settings
import org.vpreportcorrector.sync.SyncConstants.CREDENTIALS_DIR_NAME
import org.vpreportcorrector.sync.SyncConstants.GOOGLE_DRIVE_CREDENTIALS_PATH
import org.vpreportcorrector.sync.SyncConstants.GOOGLE_DRIVE_TOKENS_FILE
import org.vpreportcorrector.utils.AppConstants.APP_HOME_FOLDER
import org.vpreportcorrector.utils.deleteDirectoryStream
import org.vpreportcorrector.utils.getUserHomeDirectory
import org.vpreportcorrector.utils.list
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths


class GoogleDriveSettingsView : View("Google Drive Settings") {
    private val vm: GoogleDriveSettingsViewModel by inject()

    init {
        vm.checkForStoredTokens()
    }

    override val root = fieldset(t("remoteRepository")) {
        field {
            button("Log in with Google") {
                // TODO KB: enable switching accounts?
                enableWhen { vm.hasStoredTokensProperty.not() }
                action {
                    vm.logIn()
                }
            }
        }
        field {
            button("Log files") {
                enableWhen { vm.hasStoredTokensProperty }
                action {
                    vm.logFiles()
                }
            }
        }
    }
}

class GoogleDriveSettingsViewModel: ItemViewModel<Settings>() {
    val hasStoredTokensProperty = SimpleBooleanProperty(false)

    val credential = SimpleObjectProperty<Credential>(null)
    val driveService = SimpleObjectProperty<Drive>(null)
    val userString = SimpleStringProperty("")

    private val SCOPES = listOf(DriveScopes.DRIVE_METADATA_READONLY)
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    private val APPLICATION_NAME = t("appName")
    private val TOKENS_DIRECTORY_PATH = initTokensFilePath()
    private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()

    fun initializeData() {
        checkForStoredTokens()
        if (hasStoredTokensProperty.value) {
            credential.value = authorizeOrGetPersistedCredential()
            driveService.value = getDriveService()
            initUserString()
        }
    }

    private fun initUserString() {
        if (driveService.value != null) {
            val about = driveService.value.About().get().execute()
            userString.value = "${about.user.displayName} (${about.user.emailAddress})"
        }
    }

    fun checkForStoredTokens() {
        val tokenDir = Paths.get(TOKENS_DIRECTORY_PATH)
        hasStoredTokensProperty.value = Files.exists(tokenDir) && tokenDir.list().isNotEmpty()
    }

    fun logIn() {
        if (credential.value == null) {
            credential.value = authorizeOrGetPersistedCredential()
            driveService.value = getDriveService()
            initUserString()
            checkForStoredTokens()
        }
    }

    fun logOut() {
        if (credential.value != null) {
            credential.value = null
            driveService.value = null
            userString.value = null
            val tokenDir = Paths.get(TOKENS_DIRECTORY_PATH)
            deleteDirectoryStream(tokenDir)
            checkForStoredTokens()
        }
    }
    
    fun switchAccount() {
        // TODO: 28.03.21  will also need selected folder to clear it
    }
    
    fun selectRemoteFolder() {
        // TODO KB: 
    }

    fun logFiles() {
        driveService.value?.let {
            runAsync {
                driveService.value.files().list().setPageSize(10).setFields("nextPageToken, files(id, name)").execute()
            } ui { results ->
                results.files.forEachIndexed {index, file ->
                    println("${index + 1} ${file.name} (${file.id})")
                }
            }
        }
    }

    private fun getDriveService(): Drive? {
        return Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
            .setApplicationName(APPLICATION_NAME).build()
    }

    private fun getCredential(): Credential? {
        return credential.value ?: authorizeOrGetPersistedCredential()
    }

    private fun authorizeOrGetPersistedCredential(): Credential? {
        // Load client secrets.
        val inStream = getCredentialsStream()
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inStream))
        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline").build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return CustomAuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    private fun getCredentialsStream(): InputStream {
        try {
            return resources.stream(GOOGLE_DRIVE_CREDENTIALS_PATH)
        } catch (e: NullPointerException) {
            kotlin.error("Resource not found: ${GOOGLE_DRIVE_CREDENTIALS_PATH}")
        }
    }

    private fun initTokensFilePath(): String {
        val userHomeAbsPath = getUserHomeDirectory()?.absolutePath ?: ""
        log.info("$ home null ${Paths.get("", APP_HOME_FOLDER, CREDENTIALS_DIR_NAME, GOOGLE_DRIVE_TOKENS_FILE).toAbsolutePath().toString()}")
        log.info("$ home null ${Paths.get(userHomeAbsPath, APP_HOME_FOLDER, CREDENTIALS_DIR_NAME, GOOGLE_DRIVE_TOKENS_FILE).toAbsolutePath().toString()}")
        return Paths.get(userHomeAbsPath, APP_HOME_FOLDER, CREDENTIALS_DIR_NAME, GOOGLE_DRIVE_TOKENS_FILE)
            .toAbsolutePath().toString()
    }

}
