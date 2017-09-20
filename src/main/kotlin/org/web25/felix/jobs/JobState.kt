package org.web25.felix.jobs

/**
 * Defines the state a job currently is in. A job might change it's state any time, but should refrain from
 * updating it's state to FINISHED, as this indicates that the job can be removed from the job queue.
 */
enum class JobState {
    /**
     * Initial state of any job, can be set by the job to indicate the job is waiting on a different job or a
     * background task to complete.
     */
    WAITING,
    /**
     * Indicates that the job is currently active. Every job automatically changes it's state to ACTIVE when execution
     * begins.
     */
    ACTIVE,
    /**
     * Indicates that the job has finished it's work and can be removed from the job queue. Even if a job manager
     * doesn't remove jobs from the queue when they state that they are finished, users might think that a job is no
     * longer active and cancel the execution of the whole execution queue.
     */
    FINISHED,
    /**
     * Indicates that the job has failed with a foreseeable reason (e.g. no connection to the internet, files not found).
     * This should be used for occasions when a job checks for preconditions and they aren't met or behaviour that can
     * be expected is perceived that disables correct execution of the job.
     */
    FAILED,
    /**
     * Indicates that unexpected behaviour lead to the wrong execution of the job. This state should be used with an
     * exception so the problem can be solved.
     */
    ERRORED
}