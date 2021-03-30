package org.vpreportcorrector.sync.googledisk.settings

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.services.drive.Drive
import javafx.beans.property.SimpleObjectProperty
import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import org.vpreportcorrector.sync.SyncConstants
import org.vpreportcorrector.sync.googledisk.*
import org.vpreportcorrector.utils.deleteDirectoryStream
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.nio.file.Paths

class GoogleDriveSettingsViewModel: ItemViewModel<GoogleDriveSettingsModel>(GoogleDriveSettingsModel()),
    WithLoading by LoadingLatch() {
    val remoteFolderId = bind(GoogleDriveSettingsModel::remoteFolderIdProperty)

    val credential = SimpleObjectProperty<Credential>(null)
    val driveService = SimpleObjectProperty<Drive>(null)
    val loginFieldLabelProperty = stringBinding(this, driveService) {
        getUserString()
    }
    private val driveHelpers = GoogleDriveApiHelpers(t("appName"))

    init {
        item.load()
        credential.onChange {
            driveService.value = driveHelpers.getDriveService(credential.value)
            remoteFolderId.value = null
        }
    }

    fun initializeData() {
        if (driveHelpers.hasStoredTokens()) {
            credential.value = driveHelpers.loadPersistedCredentials()
        }
    }

    fun save() {
        runAsync {
            startLoading()
            item.save()
        } finally {
            endLoading()
        }
    }

    fun logIn() {
        if (credential.value == null) {
            credential.value = authorizeOrGetPersistedCredential()
        }
    }

    fun logOut() {
        if (credential.value != null) {
            val tokenDir = Paths.get(driveHelpers.TOKENS_DIRECTORY_PATH)
            deleteDirectoryStream(tokenDir)
            credential.value = null
            driveService.value = null
        }
    }

    fun selectRemoteFolder() {
        if (driveService.value == null) return
        runAsync {
            startLoading()
            collectAllRemoteFolders().toList()
        } ui {
            val folders = it
            val selectModel = SelectRemoteFolderViewModel(SelectRemoteFolderModel(folders, remoteFolderId.value))
            val selectScope = Scope()
            setInScope(selectModel, selectScope)
            find<SelectRemoteFolderModal>(selectScope).openModal(block = true)
            selectModel.item.selectedFolderId?.let {
                remoteFolderId.value = selectModel.item.selectedFolderId
                markDirty(remoteFolderId)
            }
        } fail {
            log.severe(it.stackTraceToString())
            error(
                title = t("error.defaultTitle"),
                header = "Failed to collect folder information form Google Disk.",
                content = "Please enter the remote Disk folder ID manually."
            )
        } finally {
            endLoading()
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

    private fun collectAllRemoteFolders(): MutableSet<com.google.api.services.drive.model.File> {
        var pageToken: String? = null
        val allFolders = mutableSetOf<com.google.api.services.drive.model.File>()
        do {
            val result = driveService.value.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                .setSpaces("drive")
                .setSupportsAllDrives(true)
                .setFields("nextPageToken, files(id, name, iconLink)")
                .execute()
            allFolders.addAll(result.files)
            pageToken = result.nextPageToken
        } while (pageToken != null)
        return allFolders
    }

    private fun authorizeOrGetPersistedCredential(): Credential? {
        val flow = driveHelpers.getFlow()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return CustomAuthorizationCodeInstalledApp(
            flow,
            receiver!!
        ).authorize(SyncConstants.GOOGLE_DRIVE_CREDENTIALS_USER_ID)
    }
}

