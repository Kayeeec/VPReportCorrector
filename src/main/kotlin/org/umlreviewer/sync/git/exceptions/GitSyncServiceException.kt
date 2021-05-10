package org.umlreviewer.sync.git.exceptions

class GitSyncServiceException(override val message: String?, throwable: Throwable?): Exception(message, throwable)
