/**
 * <slate_header>
 * url: www.slatekit.com
 * git: www.github.com/code-helix/slatekit
 * org: www.codehelix.co
 * author: Kishore Reddy
 * copyright: 2016 CodeHelix Solutions Inc.
 * license: refer to website and/or github
 * about: A tool-kit, utility library and server-backend
 * mantra: Simplicity above all else
 * </slate_header>
 */

package slatekit.core.cmds

import slatekit.common.DateTime
import slatekit.common.args.Args
import slatekit.core.common.FunctionInfo
import slatekit.core.common.FunctionResult
import slatekit.results.Failure
import slatekit.results.Result
import slatekit.results.Success
import slatekit.results.builders.Tries

/**
 * The result of command ( cmd ) that was run
 * @param info     : The command info
 * @param result   : Result of execution of the command
 * @param request  : Total time in milliseconds
 * @param response : Response of the command ( converted
 * @param started  : Start time of the command
 * @param ended    : End time of the command
 */
data class CommandResult(
        val request: CommandRequest,
        val response: CommandResponse<*>,
        override val info: FunctionInfo,
        override val result: Result<*, *>,
        override val started: DateTime,
        override val ended: DateTime,
        override val totalMs: Long
) : FunctionResult {


    companion object {
        fun empty(info: FunctionInfo): CommandResult {
            val result = Tries.errored<Any>("Not started")
            val request = CommandRequest.empty()
            val response = CommandResponse(request, result)
            val start = DateTime.now()
            return CommandResult(request, response, info, result, start, start, 0L)
        }
    }
}