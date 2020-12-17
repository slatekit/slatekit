package slatekit.apis.services

import slatekit.apis.ApiRequest
import slatekit.apis.ApiResult
import slatekit.apis.ApiServer
import slatekit.apis.Middleware
import slatekit.apis.support.RewriteSupport
import slatekit.common.Ignore
import slatekit.common.validations.ValidationFuncs
import slatekit.results.Outcome
import slatekit.results.builders.Outcomes

class Restify : RewriteSupport, Middleware {

    private val verbGet = "get"
    private val verbPost = "post"
    private val verbPut = "put"
    private val verbDelete = "delete"
    private val verbPatch = "patch"

    /**
     * Rewrites restful routes and maps them to SlateKit API routes
     */
    @Ignore
    override suspend fun process(req: ApiRequest, next:suspend(ApiRequest) -> Outcome<ApiResult>): Outcome<ApiResult> {

        // Get the first and second part
        val parts = req.request.parts
        val verb = req.request.verb.toLowerCase()
        val container = req.host

        val result = if (verb == verbGet && parts[2] == "") {
            Outcomes.of { rewrite(req, rename(container,"getAll")) }
        } else if (verb == verbGet && ValidationFuncs.isNumeric(parts[2])) {
            Outcomes.of { rewriteWithParam(req, rename(container,"getById"), "id", parts[2]) }
        } else if (verb == verbPost && parts[2] == "") {
                Outcomes.of { rewrite(req, rename(container,"create")) }
        } else if (verb == verbPut && parts[2] == "") {
                Outcomes.of { rewrite(req, rename(container,"update")) }
        } else if (verb == verbPatch && ValidationFuncs.isNumeric(parts[2])) {
                Outcomes.of { rewriteWithParam(req, rename(container,"patch"), "id", parts[2]) }
        } else if (verb == verbDelete && parts[2] == "") {
                Outcomes.of { rewrite(req, rename(container,verbDelete)) }
        } else if (verb == verbDelete && ValidationFuncs.isNumeric(parts[2])) {
                Outcomes.of { rewriteWithParam(req, rename(container,"deleteById"), "id", parts[2]) }
        } else {
            next(req)
        }
        return result
    }

    /**
     * Provides access to naming conventions used for actions
     */
    fun rename(server:ApiServer, text: String): String = server.settings.naming?.rename(text) ?: text

    companion object {
    }
}
