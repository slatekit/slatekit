package slatekit.jobs

import slatekit.common.Status
import slatekit.common.ids.Identity
import java.util.concurrent.atomic.AtomicReference

interface Workable<T> {

    /**
     * Identity of this worker
     */
    val id: Identity


    /**
     * Stats on this worker
     */
    val stats:WorkerStats


    /**
     * Current work status of this worker
     */
    fun status(): Status


    /**
     * Get key/value pairs representing information about this worker.
     * e.g. such as settings
     */
    fun info():List<Pair<String, String>> = listOf()


    /**
     * Life-cycle hook to allow for initialization
     */
    fun init() {
    }


    /**
     * Performs the work
     * This assumes that this work manages it's own work load/queue/source
     */
    fun work(): WorkState {
       return work(Task.owned)
    }


    /**
     * Performs the work
     * @param task: The task to perform.
     * NOTE: If this worker manages it's own work load/queue/source, then this task is
     * provided by the work() method and assigned Task.owned
     */
    fun work(task:Task): WorkState


    /**
     * Life-cycle hook to allow for completion
     */
    fun done() {
    }


    /**
     * Life-cycle hook to allow for failure
     */
    fun fail(err:Throwable?) {
        notify("Errored: " + err?.message, null)
    }


    /**
     * Transition current status to the one supplied
     */
    fun transition(state: Status) {
        notify(state.name, null)
    }


    /**
     * Send out notifications
     */
    fun notify(desc:String?, extra:List<Pair<String,String>>?){
    }
}