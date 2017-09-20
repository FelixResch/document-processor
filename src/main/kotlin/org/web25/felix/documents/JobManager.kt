package org.web25.felix.documents

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class JobManager<T: Any>(val threadCount: Int) {

    private val executorService by lazy {
        Executors.newScheduledThreadPool(threadCount)
    }

    private val futures = mutableListOf<Future<T>>()

    fun submit(callable: Callable<T>) {
        synchronized(futures) {
            futures.add(executorService.submit(callable))
        }
    }

    fun shutdown() {
        while (synchronized(futures) {futures.any { !it.isDone }});
        futures.forEach {
            it.get()
        }
        executorService.shutdown()
    }
}