package slatekit.workers

/**
 * Settings for each worker
 * @param errorLimit: The percentage of errors allows before this worker is paused
 *
 */
data class WorkerSettings(
    val errorLimit: Double = .2,
    val enableRestart: Boolean = true,
    val batchSize: Int = 10,
    val isOngoing: Boolean = false,
    val waitTimeInSeconds: Int = 5,
    val pauseTimeInSeconds: Int = 5,
    val stopTimeInSeconds: Int = 30
)
