package org.vpreportcorrector.sync

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import org.vpreportcorrector.app.RefreshFilesExplorer
import org.vpreportcorrector.app.RequestSync
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.app.errorhandling.errorWithStacktrace
import org.vpreportcorrector.sync.git.GitSyncService
import org.vpreportcorrector.utils.getSyncTo
import tornadofx.*
import kotlin.error
import kotlin.reflect.full.createInstance

class SyncController: Controller() {
    private val syncServiceProperty = SimpleObjectProperty<SyncService?>(null)
    val syncTaskStatus = TaskStatus()
    val initTaskStatus = TaskStatus()

    val isAnyTaskRunning: BooleanBinding = syncTaskStatus.running.or(initTaskStatus.running)
    val isSyncServiceInitialized: BooleanBinding = syncServiceProperty.isNotNull

    init {
        chooseAndInitSyncClass()
        subscribe<SettingsChanged> {
            chooseAndInitSyncClass()
        }
        subscribe<RequestSync> {
            sync()
        }
    }

    private fun sync() {
        if (isAnyTaskRunning.value) return
        syncServiceProperty.value?.let {
            runAsync(syncTaskStatus) {
                updateMessage("Sync in progress...")
                it.sync(this)
            } fail {
                log.severe(it.stackTraceToString())
                errorWithStacktrace("Sync to remote location failed.", it)
            } finally {
                fire(RefreshFilesExplorer)
            }
        }
    }

    /**
     * chooses proper sync class according to the settings
     */
    private fun chooseAndInitSyncClass() {
        if (isAnyTaskRunning.value) return
        runAsync(initTaskStatus) {
            updateMessage("Sync service initialization in progress...")
            when(val syncTo = getSyncTo()) {
                RemoteRepo.GIT -> {
                    log.info("Initializing Git sync service.")
                    initializeService<GitSyncService>()
                }
                null -> error("No repository type set. Please check if it is set correctly in application settings.")
                else -> error("No service initialization implemented for repository type '$syncTo'.")
            }
        } fail {
            log.severe(it.stackTraceToString())
            errorWithStacktrace("Failed to initialize synchronisation service.", it)
        } finally {
            fire(RefreshFilesExplorer)
        }
    }

    private inline fun <reified T : SyncService> initializeService() {
        when (syncServiceProperty.value) {
            null, !is T -> { // user added sync service or changed its type
                syncServiceProperty.value = T::class.createInstance()
            }
            else -> { // same service type but settings have changed
                val service = syncServiceProperty.value as T
                val (reinitialize, dispose) = service.shouldReinitializeAndOrDispose()
                if (reinitialize) {
                    if (dispose) service.dispose()
                    syncServiceProperty.value = T::class.createInstance()
                }
            }
        }
    }
}
