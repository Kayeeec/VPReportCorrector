package org.vpreportcorrector.sync.googledisk

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import org.vpreportcorrector.sync.SyncConstants
import org.vpreportcorrector.utils.AppConstants
import org.vpreportcorrector.utils.getUserHomeDirectory
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger

class GoogleDriveApiHelpers(val APPLICATION_NAME: String) {
    private val log by lazy { Logger.getLogger(this.javaClass.name) }
    val SCOPES = listOf(DriveScopes.DRIVE_METADATA_READONLY)
    val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    val TOKENS_DIRECTORY_PATH = initTokensDirPath()
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()

    fun getFlow(): GoogleAuthorizationCodeFlow {
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
    fun loadPersistedCredentials(): Credential? {
        val flow = getFlow()
        val credential: Credential? = flow.loadCredential(GoogleDriveConstants.GOOGLE_DRIVE_CREDENTIALS_USER_ID)
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

    fun getCredentialsStream(): InputStream {
        try {
            return this.javaClass.getResourceAsStream(GoogleDriveConstants.GOOGLE_DRIVE_CREDENTIALS_PATH)
        } catch (e: NullPointerException) {
            kotlin.error("Resource not found: ${GoogleDriveConstants.GOOGLE_DRIVE_CREDENTIALS_PATH}")
        }
    }

    private fun initTokensDirPath(): String {
        val userHomeAbsPath = getUserHomeDirectory()?.absolutePath ?: ""
        return Paths.get(
            userHomeAbsPath,
            AppConstants.APP_HOME_FOLDER,
            SyncConstants.CREDENTIALS_DIR_NAME,
            GoogleDriveConstants.GOOGLE_DRIVE_TOKENS_DIR
        ).toAbsolutePath().toString()
    }

    fun getDriveService(credential: Credential?): Drive? {
        if (credential != null) {
            return Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build()
        }
        return null
    }

    fun hasStoredTokens(): Boolean {
        val tokenFile = Paths.get(TOKENS_DIRECTORY_PATH, GoogleDriveConstants.GOOGLE_DRIVE_TOKENS_FILENAME)
        return Files.exists(tokenFile)
    }

    fun isValidFolderId(folderId: String?, service: Drive?): Boolean {
        if (folderId == null || service == null) return false
        return try {
            val result = service.files().get(folderId)
                .setSupportsAllDrives(true)
                .setFields("files(mimeType, trashed)")
                .execute()
            !result.trashed && result.mimeType == "application/vnd.google-apps.folder"
        } catch (e: GoogleJsonResponseException) {
            if (e.details.code != 404) log.info(e.stackTraceToString())
            false
        } catch (e: Throwable) {
            log.info(e.stackTraceToString())
            false
        }
    }
}
