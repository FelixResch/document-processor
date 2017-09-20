package org.web25.felix.jobs.simple

import org.slf4j.LoggerFactory
import org.web25.felix.jobs.Job
import org.web25.felix.jobs.JobManager
import java.util.concurrent.Executors
import java.util.concurrent.Future

class SimpleJobManager(val threadCount: Int = 4) : JobManager {

    val logger = LoggerFactory.getLogger(JobManager::class.java)

    private val executorService by lazy {
        Executors.newScheduledThreadPool(threadCount)
    }

    private val jobs = mutableListOf<Job<*>>()

    override fun <T : Any> addJob(job: Job<T>): Future<T> {
        return synchronized(jobs) {
            jobs.add(job)
            executorService.submit(job)
        }
    }

    override fun waitForShutdown() {
        dump()
        while (synchronized(jobs) {jobs.any { !it.done }});
        logger.debug("Shutting down SimpleJobManager. Job List:")
        dump()
        executorService.shutdown()
    }

    fun dump() {
        logger.debug("Dump of jobs:")
        logger.debug(jobs.toString())
    }

    override val busy: Boolean
        get() = jobs.any { !it.done}
}