package slatekit.jobs.support

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slatekit.common.Status
import slatekit.jobs.Job
import slatekit.jobs.JobAction
import slatekit.jobs.Workers
import slatekit.jobs.events.JobEvents

/**
 * Dispatches control and management requests on all the workers
 */
class JobDispatch(val job: Job, val workers: Workers, val events: JobEvents, val scope: CoroutineScope) {

    suspend fun start(launch: Boolean) = transition(JobAction.Start, Status.Running, launch)
    suspend fun stop(launch: Boolean) = transition(JobAction.Stop, Status.Stopped, launch)
    suspend fun resume(launch: Boolean) = transition(JobAction.Resume, Status.Running, launch)
    suspend fun process(launch: Boolean) = transition(JobAction.Process, Status.Running, launch)
    suspend fun pause(launch: Boolean, seconds: Long) = transition(JobAction.Pause, Status.Paused, launch, seconds)
    suspend fun delayed(launch: Boolean, seconds: Long) = transition(JobAction.Start, Status.Paused, launch, seconds)

    /**
     * Transitions all workers to the new status supplied
     */
    private suspend fun transition(action: JobAction, newStatus: Status, launch: Boolean, seconds: Long = 0) {
        JobUtils.perform(job, action, job.status(), launch, scope) {
            // logger.log(Info, "Job:", listOf(nameKey, "move" to newStatus.name))
            job.setStatus(newStatus)

            scope.launch {
                events.notify(job)
            }

            workers.all.forEach {
                val (id, uuid) = job.nextIds()
                val req = Command.WorkerCommand(id, uuid.toString(), action, it.id, seconds, "")
                job.request(req)
            }
        }
    }
}
