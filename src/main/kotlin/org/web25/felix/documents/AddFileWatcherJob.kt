package org.web25.felix.documents

import org.web25.felix.jobs.*

class AddFileWatcherJob(val processableFile: ProcessableFile, parent: Job<Any>? = null, val jobCreator: JobCreator?) :
        AbstractJob<Unit>(parent) {

    override val name: String = "add-file-watcher"

    override fun execute(jobContext: JobContext) {
        val config = FileWatcherConfig(processableFile.src.name, stage = "add")
        CommandLineProgressPrinter.addFileWatcherConfig(config)
        processableFile.addConfig(config)
        jobCreator?.let {
            jobContext.jobManager.addJob(it(processableFile))
        }
    }
}

fun addFileWatcher(processableFile: ProcessableFile, jobCreator: JobCreator? = null): AddFileWatcherJob {
    return AddFileWatcherJob(processableFile, jobCreator = jobCreator)
}

data class FileWatcherConfig(val fileName: String, var name: String? = null, var state: FileState = FileState.WAIT, var done: Boolean = false,
                             var stage: String, var unmodified: Boolean = false, var errored: Boolean = false) : Config {
}

enum class FileState {
    WAIT, PARSED, RENDERED
}
