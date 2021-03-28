package org.vpreportcorrector.sync.git.exceptions

class GitSyncServiceException(override val message: String?, throwable: Throwable?): Exception(message, throwable)
