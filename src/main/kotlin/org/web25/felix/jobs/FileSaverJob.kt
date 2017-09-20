package org.web25.felix.jobs

class FileSaverJob (val processableFile: ProcessableFile, parent: Job<Any>? = null, val jobCreator: JobCreator?) : AbstractJob<Unit>(parent) {

    override val name: String = "write-file"

    override fun execute(jobContext: JobContext) {
        processableFile.write()

        if(jobCreator != null) {
            jobContext.jobManager.addJob(jobCreator.invoke(processableFile))
        }
    }

}

fun write(processableFile: ProcessableFile, jobCreator: JobCreator? = null): FileSaverJob {
    return FileSaverJob(processableFile, jobCreator = jobCreator)
}