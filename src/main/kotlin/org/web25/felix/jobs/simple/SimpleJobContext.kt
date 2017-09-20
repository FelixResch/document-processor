package org.web25.felix.jobs.simple

import org.web25.felix.documents.singleton
import org.web25.felix.jobs.JobContext
import org.web25.felix.jobs.JobManager

class SimpleJobContext : JobContext {


    override val jobManager: JobManager by singleton {
        SimpleJobManager()
    }

}

