package org.web25.felix.jobs

import java.io.File

class FileWalkerJob(val dir: ProcessableFile, val jobCreator: JobCreator? = null, parent: Job<Any>? = null): AbstractJob<Unit>(parent) {

    override val name: String = "file-walker"


    override fun execute(jobContext: JobContext) {
        val walker = dir.src.walk().iterator()
        walker.forEach {
            if(it.extension == "md") {
                if(jobCreator != null) {
                    jobContext.jobManager.addJob(jobCreator.invoke(ProcessableFile(it)))
                }
            }
        }
    }

}

fun fileWalker(dir: File, jobCreator: JobCreator? = null): Job<Unit> {
    return FileWalkerJob(ProcessableFile(dir), jobCreator)
}