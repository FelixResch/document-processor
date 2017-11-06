package org.web25.felix.jobs

import java.io.File

class FileSaverJob (val processableFile: ProcessableFile, val destFiles: List<File>, parent: Job<Any>? = null, val jobCreator: JobCreator?) : AbstractJob<Unit>(parent) {

    override val name: String = "write-file"

    override fun execute(jobContext: JobContext) {
        destFiles.forEach {
            processableFile.write(it)
        }

        if(jobCreator != null) {
            jobContext.jobManager.addJob(jobCreator.invoke(processableFile))
        }
    }

}

fun write(processableFile: ProcessableFile, filenameCreator: ProcessableFile.() -> List<File?> = { listOf(dst) },  jobCreator: JobCreator? = null): FileSaverJob {
    val files: List<File>
    try {
        files = processableFile.filenameCreator().filter { it != null }.map {
            it!!
        }
    } catch (t: Throwable) {
        files = listOf(processableFile.dst)
    }
    return FileSaverJob(processableFile, files, jobCreator = jobCreator)
}