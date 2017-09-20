package org.web25.felix.documents

import org.web25.felix.jobs.*
import java.io.File
import java.security.MessageDigest
import java.util.*

class FileChangeCheckerJob(val file: ProcessableFile, val parentDir: File, parent: Job<Any>? = null, val jobCreator: JobCreator?): AbstractJob<Unit>(parent) {

    override val name: String = "check-for-modification"

    val fileStateTable by singleton { FileStateTable(parentDir) }

    override fun execute(jobContext: JobContext) {
        if(!fileStateTable.unmodified(file)) {
            //fileStateTable.update(file)
            jobCreator?.let { jobCreator ->
                jobContext.jobManager.addJob(jobCreator(file))
            }
        } else {
            val fileWatcherConfig = file.getConfig(FileWatcherConfig::class)
            fileWatcherConfig.name = fileStateTable[this.file].title
            fileWatcherConfig.unmodified = true
            fileWatcherConfig.done = true
        }
    }


}

fun checkForChanges(processableFile: ProcessableFile, parentDir: File, jobCreator: JobCreator? = null): FileChangeCheckerJob {
    return FileChangeCheckerJob(processableFile, parentDir, jobCreator = jobCreator)
}

