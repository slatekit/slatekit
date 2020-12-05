package slatekit.jobs.support

import kotlinx.coroutines.CoroutineScope
import slatekit.common.Identity
import slatekit.common.ids.Paired
import slatekit.common.log.Logger
import slatekit.common.log.LoggerConsole
import slatekit.common.paged.Pager
import slatekit.core.common.Backoffs
import slatekit.core.common.DefaultScheduler
import slatekit.core.common.Scheduler
import slatekit.jobs.Jobs
import slatekit.jobs.Queue
import slatekit.jobs.support.Command
import slatekit.jobs.workers.WorkRequest
import slatekit.jobs.workers.WorkResult
import slatekit.jobs.workers.Worker
import slatekit.policy.Policy

/**
 * @param id        : Identity of the job e.g. signup.alerts.job.qat.ABC123
 * @param channel   : Channel to communicate commands to a job and between job/workers
 * @param workers   : List of all @see[Worker] involved in this job
 * @param logger    : Logger used inside the job
 * @param policies  : Policies that control or respond to work/task execution
 * @param backoffs  : Exponential backoff times during idling or pauses
 * @param notifier  : Notifier to emit job/work events to subscribers
 * @param ids       : Id generator used for creating ids for commands
 * @param scheduler : Scheduler used to continue resuming work
 */
data class JobContext(val id: Identity,
                      val channel: slatekit.core.common.Coordinator<Command>,
                      val workers: List<Worker<*>>,
                      val logger : Logger = LoggerConsole(),
                      val queue  : Queue? = null,
                      val scope  : CoroutineScope = Jobs.scope,
                      val policies: List<Policy<WorkRequest, WorkResult>> = listOf(),
                      val backoffs: () -> Pager<Long> = { Backoffs.times() },
                      val notifier: Notifier = Notifier(),
                      val ids: Paired = Paired(),
                      val scheduler: Scheduler = DefaultScheduler()
)
