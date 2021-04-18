package org.vpreportcorrector.sync

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import org.vpreportcorrector.app.RefreshFilesExplorer
import org.vpreportcorrector.app.RequestSync
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.app.errorhandling.errorWithStacktrace
import org.vpreportcorrector.sync.git.GitSyncService
import org.vpreportcorrector.utils.Helpers.cleanDataDirectory
import org.vpreportcorrector.utils.Helpers.getRemoteRepositoryType
import tornadofx.Controller
import tornadofx.TaskStatus
import tornadofx.fail
import tornadofx.finally
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
                cleanDataDirectory()
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
            when(val syncTo = getRemoteRepositoryType()) {
                RemoteRepo.NONE -> {
                    if (syncServiceProperty.value !== null) {
                        syncServiceProperty.value!!.dispose()
                        syncServiceProperty.value = null
                    }
                }
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
            null -> { // user added sync service
                syncServiceProperty.value = T::class.createInstance()
            }
            !is T -> { // user changed sync service type
                syncServiceProperty.value!!.dispose()
                syncServiceProperty.value = T::class.createInstance()
            }
            else -> { // same service type but settings have presumably changed
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
