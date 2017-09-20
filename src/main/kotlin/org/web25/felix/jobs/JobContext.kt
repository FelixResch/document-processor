package org.web25.felix.jobs

import org.web25.felix.jobs.simple.SimpleJobContext

interface JobContext {

    val jobManager: JobManager

    companion object {

        fun createForThread(): JobContext {
            return SimpleJobContext()
        }
    }
}

