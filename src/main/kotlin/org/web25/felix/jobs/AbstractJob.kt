package org.web25.felix.jobs

abstract class AbstractJob<T: Any>(override val parent: Job<Any>?) : Job<T> {

    override var state: JobState = JobState.WAITING
    override fun toString(): String {
        return "${this::class.simpleName}(state=$state,name=$name,done=$done)"
    }


}