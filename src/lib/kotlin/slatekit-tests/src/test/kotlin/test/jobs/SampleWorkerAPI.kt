package test.jobs

import slatekit.apis.*
import slatekit.apis.core.Requests
import slatekit.common.Source
import slatekit.common.CommonContext
import slatekit.common.utils.Random
import slatekit.core.queues.AsyncQueue
import slatekit.integration.common.ApiQueueSupport
import slatekit.results.*


@Api(area = "samples", name = "workerqueue", desc = "sample api to integrating workers, queues, apis")
class SampleWorkerAPI(val ctx: CommonContext, val queues:List<AsyncQueue<String>> = listOf())
    : ApiQueueSupport, slatekit.apis.Handler {

    var _lastResult = ""

    override fun queues(): List<AsyncQueue<String>> = queues


    @Action(tags = ["queued"])
    fun test1(s: String, b: Boolean, i: Int): String {
        _lastResult = "$s, $b, $i"
        return _lastResult
    }


    /**
     * Converts a request for an action that is queued, to an actual queue
     */
    override suspend fun process(req:ApiRequest, next:suspend(ApiRequest) -> Outcome<ApiResult>): Outcome<ApiResult>  {
        // Coming in as http request ? and mode is queued ?
        return if(req.source != Source.Queue && req.target?.action?.tags?.contains("queued") == true){
            // Convert from web request to Queued request
            val queuedReq = Requests.toJsonAsQueued(req.request)
            enueue(Random.guid().toString(), req.request.fullName, queuedReq,  "api-queue")
            Success("Request processed as queue")
        }
        else {
            next(req)
        }
    }
}
