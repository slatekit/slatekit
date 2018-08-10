package slatekit.core.workers

import slatekit.common.status.*
import slatekit.core.common.AppContext
import slatekit.core.workers.core.QueueInfo
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.TimeUnit


/**
 * This is the top-level class in the worker system and supports the following use cases:
 * TODO: Document design goal for this class
 * 1. register a new worker into a group
 * 2. get a specific group in the system
 * 3. get a specific group.worker in the system
 * 4. run the life-cycle methods on groups/workers ( e..g init, end )
 * 5. control a specific group  life-cycle methods ( start, stop, pause, resume )
 * 6. control a specific worker life-cycle methods ( start, stop, pause, resume )
 */
open class System(val ctx:AppContext,
                  val queueInfos:List<QueueInfo>,
                  service:ExecutorService? = null,
                  val settings:SystemSettings = SystemSettings())
    : Runnable
{

    /**
     * Queues built from queue infos with priorities, creating "weighted" queues.
     */
    val queues = Queues(queueInfos)
    private var manager:Manager? = null

    private val _runState = AtomicReference<RunState>(RunStateNotStarted)
    private var _thread:Thread? = null

    /**
     * You can extend the work system and
     */
    open val svc = service ?: Executors.newFixedThreadPool(3)


    /**
     * organizes all the workers into groups
     */
    private val workers = mutableMapOf<String, Worker<*>>()


    /**
     * register a worker into the default group
     */
    fun <T> register(worker:Worker<T>) {
        workers[worker.about.name] = worker
    }


    /**
     * Gets the worker with the supplied name
     */
    fun get(name:String):Worker<*>? = workers[name]


    fun getWorkers():List<Worker<*>> = workers.values.toList()


    /**
     * runs all the workers in the groups within the work-flow of
     * init, exec, end
     */
    override fun run():Unit {

        // Initialize
        moveToState(RunStateInitializing)
        init()

        // Work
        moveToState(RunStateRunning)
        var state = _runState.get()
        while(state != RunStateComplete) {

            // Prevent paused/stopped status from executing logic
            if(state == RunStateRunning) {
                exec()
            }

            // Enable pause ?
            //if(settings.pauseBetweenCycles) {
            //    Thread.sleep(settings.pauseTimeInSeconds * 1000L)
            //}
            state = _runState.get()
        }

        // Ending/Complete
        moveToState(RunStateComplete)
        end()
    }


    /**
     * initialize the system.
     * NOTE: This is open to allow derived classes to self register
     * all workers and groups and have them ready to be run later
     */
    open fun init() {
        workers.forEach( { _, worker -> worker.init() })
    }


    /**
     * performs the core logic of executing all the workers in all the groups.
     * NOTE: This is open to allow derived classes more fine grained
     * control and to handle custom execution of all the groups/workers
     */
    open fun exec():Unit {
        init()
        workers.forEach { it.value.moveToState(RunStateRunning)}
        (1..200).forEach( {
            if(manager == null) {
                manager = Manager(this)
            }
            manager?.manage()
        })
        end()
    }


    /**
     * stops the system.
     * NOTE: This is open to allow derived classes to handle
     * any shutdown / end steps
     */
    open fun end() {
        workers.forEach( { _, worker -> worker.end() })
    }


    fun start() {
        _thread = Thread(this)
        _thread?.isDaemon = true
        _thread?.start()
    }


    /**
     * pauses the system
     */
    fun pause() = {
        moveToState(RunStatePaused)
    }


    /**
     * resumes the system
     */
    fun resume() = {
        moveToState(RunStateRunning)
    }


    /**
     * stops the system
     */
    fun stop() = {
        moveToState(RunStateStopped)
    }


    /**
     * pauses the system
     */
    fun done() {
        moveToState(RunStateComplete)

        // Graceful shutdown
        svc.shutdown()
        try {
            if (!svc.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                svc.shutdownNow()
            }
        } catch (e: InterruptedException) {
            svc.shutdownNow()
        }
    }


    /**
     * starts the worker
     */
    fun startWorker(worker:String) = get(worker)?.start()


    /**
     * pauses the worker
     */
    fun pauseWorker(worker:String) = get(worker)?.pause()


    /**
     * resumes the worker
     */
    fun resumeWorker(worker:String) =  get(worker)?.resume()


    /**
     * stops the worker
     */
    fun stopWorker(worker:String) =  get(worker)?.stop()


    /**
     * Sends the worker to work by using the ThreadPool
     */
    fun sendToWork(worker:Worker<*>) {
        //svc.execute(worker)
    }


    /**
     * moves the current state to the name supplied and performs a status update
     *
     * @param state
     * @return
     */
    fun moveToState(state: RunState): RunState {
        _runState.set(state)
        return state
    }
}
