package org.web25.felix.jobs

import org.slf4j.LoggerFactory
import org.web25.felix.documents.singleton
import java.util.concurrent.Callable

interface Job<T: Any> : Callable<T> {

    val name: String

    val parent: Job<Any>?

    fun execute(jobContext: JobContext) : T

    var state: JobState

    override fun call(): T {
        val logger = LoggerFactory.getLogger(Job::class.java)
        logger.debug("Requesting context")
        val context by singleton(threadLocal = true) {
            JobContext.createForThread()
        }
        return synchronized(lock = this) {
            state = JobState.ACTIVE
            try {
                logger.debug("Starting job $name")
                this.execute(context)
            } catch (t: Throwable) {
                state = JobState.ERRORED
                throw t
            } finally {
                state = if(state == JobState.ACTIVE) JobState.FINISHED else state
                logger.debug("Job finished execution with state $state")
            }
        }
    }

    val done: Boolean
    get() {
        return state in listOf(JobState.FINISHED, JobState.FAILED, JobState.ERRORED)
    }


}

