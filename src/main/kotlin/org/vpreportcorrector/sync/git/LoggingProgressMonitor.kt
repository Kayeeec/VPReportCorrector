package org.vpreportcorrector.sync.git

import org.eclipse.jgit.lib.ProgressMonitor
import java.util.logging.Logger

class LoggingProgressMonitor: ProgressMonitor {
    private val log by lazy { Logger.getLogger(this.javaClass.name) }
    private var taskTitle = ""
    private var totalTaskWork = 0
    private var completedTaskWork = 0

    override fun start(totalTasks: Int) { // never called for push/pull - report bug?
        log.info("Starting work on $totalTasks tasks.")
    }

    override fun beginTask(title: String?, totalWork: Int) {
        totalTaskWork = totalWork
        taskTitle = title ?: ""
        log.info("Starting task: $title, total work: $totalTaskWork")
    }

    override fun update(completed: Int) {
        completedTaskWork += completed
        log.info("${taskTitle}: completed $completedTaskWork out of $totalTaskWork tasks.")
    }

    override fun endTask() {
        completedTaskWork = 0
        log.info("Task `$taskTitle` done.")
    }

    override fun isCancelled(): Boolean {
        return false
    }
}
