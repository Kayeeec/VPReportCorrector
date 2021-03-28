package org.vpreportcorrector.sync

import tornadofx.FXTask

interface SyncService {
    /**
     * A two way synchronisation of the working directory to the remote repository.
     *
     * @param fxTask    a background task the method is run on (useful for updating task status message)
     */
    fun sync(fxTask: FXTask<*>)

    /**
     * Whether the sync service class should be reinitialized and
     * whether the its [dispose] method should be executed first.
     */
    fun shouldReinitializeAndOrDispose(): ReinitializationParameters

    /**
     * Sync service cleanup method.
     * Usually ran before reinitialization of the sync service when the settings have changed too much.
     * E.g.: Git - removing .git directory if the working directory has been changed.
     */
    fun dispose()
}


data class ReinitializationParameters(
    val reinitialize: Boolean,
    val dispose: Boolean
)
