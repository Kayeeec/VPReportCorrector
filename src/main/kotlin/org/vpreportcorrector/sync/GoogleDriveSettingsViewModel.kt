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
import javafx.beans.property.SimpleObjectProperty
import org.vpreportcorrector.settings.Settings
import org.vpreportcorrector.utils.AppConstants
import org.vpreportcorrector.utils.deleteDirectoryStream
import org.vpreportcorrector.utils.getUserHomeDirectory
import org.vpreportcorrector.utils.t
import tornadofx.ItemViewModel
import tornadofx.onChange
import tornadofx.stringBinding
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths

class GoogleDriveSettingsViewModel: ItemViewModel<Settings>() {
    val credential = SimpleObjectProperty<Credential>(null)
    val driveService = SimpleObjectProperty<Drive>(null)
    val loginFieldLabelProperty = stringBinding(this, driveService) {
        getUserString()
    }

    private val SCOPES = listOf(DriveScopes.DRIVE_METADATA_READONLY)
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    private val APPLICATION_NAME = t("appName")
    private val TOKENS_DIRECTORY_PATH = initTokensDirPath()
    private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()

    private var receiver: LocalServerReceiver? = null

    init {
        credential.onChange { initDriveService() }
    }


    fun initializeData() {
        if (hasStoredTokens()) {
            credential.value = loadPersistedCredentials()
        }
    }

    private fun getUserString(): String {
        if (driveService.value != null) {
            val about = driveService.value.about().get()
                .setFields("user")
                .execute()
            return "${about.user.displayName} (${about.user.emailAddress})"
        }
        return t("signedOut")
    }

    private fun hasStoredTokens(): Boolean {
        val tokenFile = Paths.get(TOKENS_DIRECTORY_PATH, SyncConstants.GOOGLE_DRIVE_TOKENS_FILENAME)
        return Files.exists(tokenFile)
    }

    fun logIn() {
        if (credential.value == null) {
            credential.value = authorizeOrGetPersistedCredential()
        }
    }

    fun logOut() {
        if (credential.value != null) {
            val tokenDir = Paths.get(TOKENS_DIRECTORY_PATH)
            deleteDirectoryStream(tokenDir)
            credential.value = null
            driveService.value = null
        }
    }

    fun selectRemoteFolder() {
        if (driveService.value == null) return
        val allFolders = driveService.value.files().list()
            .setQ("mimeType = 'application/vnd.google-apps.folder'")
            .setSupportsAllDrives(true)
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

    private fun initDriveService() {
        if (credential.value != null) {
            driveService.value = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential.value)
                .setApplicationName(APPLICATION_NAME).build()
        }
    }

    private fun authorizeOrGetPersistedCredential(): Credential? {
        val flow = getFlow()
        receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return CustomAuthorizationCodeInstalledApp(
            flow,
            receiver!!
        ).authorize(SyncConstants.GOOGLE_DRIVE_CREDENTIALS_USER_ID)
    }

    private fun getFlow(): GoogleAuthorizationCodeFlow {
        // Load client secrets.
        val inStream = getCredentialsStream()
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inStream))
        // Build flow and trigger user authorization request.
        return GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline").build()
    }

    /**
     * Tries to load credentials using a persisted token.
     * @return Credential if there is a usable persisted token, null otherwise (user is considered signed out)
     */
    private fun loadPersistedCredentials(): Credential? {
        val flow = getFlow()
        val credential: Credential? = flow.loadCredential(SyncConstants.GOOGLE_DRIVE_CREDENTIALS_USER_ID)
        if (credential != null && (
                    credential.refreshToken != null
                            || credential.expiresInSeconds == null
                            || credential.expiresInSeconds > 60
                    )
        ) {
            return credential
        }
        return null
    }

    private fun getCredentialsStream(): InputStream {
        try {
            return resources.stream(SyncConstants.GOOGLE_DRIVE_CREDENTIALS_PATH)
        } catch (e: NullPointerException) {
            kotlin.error("Resource not found: ${SyncConstants.GOOGLE_DRIVE_CREDENTIALS_PATH}")
        }
    }

    private fun initTokensDirPath(): String {
        val userHomeAbsPath = getUserHomeDirectory()?.absolutePath ?: ""
        return Paths.get(
            userHomeAbsPath,
            AppConstants.APP_HOME_FOLDER,
            SyncConstants.CREDENTIALS_DIR_NAME,
            SyncConstants.GOOGLE_DRIVE_TOKENS_DIR
        )
            .toAbsolutePath().toString()
    }

}
