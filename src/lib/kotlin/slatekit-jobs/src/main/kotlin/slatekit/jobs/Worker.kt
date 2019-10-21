package slatekit.jobs

import slatekit.common.Status
import slatekit.common.Identity
import java.util.concurrent.atomic.AtomicReference


/**
 * Base class for Workers
 */
open class Worker<T>(override val id: Identity,
                     override val stats: WorkerStats = WorkerStats.of(id),
                     val operation: ((Task) -> WorkState)? = null) : Workable<T> {

    private val _status = AtomicReference<Status>(Status.InActive)


    /**
     * Transition current status to the one supplied
     */
    override suspend fun transition(state: Status) {
        _status.set(state)
        notify(state.name, null)
    }


    override fun status(): Status = _status.get()


    /**
     * Performs the work
     * @param task: The task to perform.
     * NOTE: If this worker manages it's own work load/queue/source, then this task is
     * provided by the work() method and assigned Task.owned
     */
    override suspend fun work(task: Task): WorkResult {
        return when (operation) {
            null -> WorkResult(WorkState.Done)
            else -> WorkResult(operation.invoke(task))
        }
    }
}