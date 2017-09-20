package org.web25.felix.jobs

import java.util.concurrent.Future

interface JobManager {

    fun <T: Any> addJob(job : Job<T>): Future<T>

    fun waitForShutdown()
    val busy: Boolean
}