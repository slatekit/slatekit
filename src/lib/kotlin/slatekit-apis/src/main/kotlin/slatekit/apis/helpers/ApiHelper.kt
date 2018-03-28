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

package slatekit.apis.helpers


import slatekit.apis.*
import slatekit.apis.core.Auth
import slatekit.common.*
import slatekit.common.auth.AuthFuncs
import slatekit.common.encrypt.Encryptor
import slatekit.common.results.ResultFuncs.ok
import slatekit.common.results.ResultFuncs.unAuthorized
import slatekit.meta.Deserializer

object ApiHelper {

    val _typeDefaults = mapOf(
            "String" to "",
            "Boolean" to false,
            "Int" to 0,
            "Long" to 0L,
            "Double" to 0.0,
            "DateTime" to DateTime.now()
    )


    /**
     * String match factoring in the wildcard "*"
     */
    fun isValidMatch(actual: String, expected: String): Boolean {
        return if (actual.isNullOrEmpty() || actual == "*")
            true
        else
            actual == expected
    }


    /**
     * Builds arguments supplied into the Inputs class which
     * is the base class for inputs in the request.
     */
    fun buildArgs(inputs: List<Pair<String, Any>>?): InputArgs {

        // fill args
        val rawArgs = inputs?.let { all -> all.toMap() } ?: mapOf()
        val args = InputArgs(rawArgs)
        return args
    }


    /**
     * builds the request
     */
    fun buildCliRequest(path: String,
                        inputs: List<Pair<String, Any>>?,
                        headers: List<Pair<String, Any>>?): Request {

        val tokens = path.split('.').toList()
        val args = buildArgs(inputs)
        val opts = buildArgs(headers)
        val apiCmd = Request(path, tokens, ApiConstants.SourceCLI, "get", args, opts,
                null, "", "", ApiConstants.Version, DateTime.now())
        return apiCmd
    }


    /**
     *  Checks the action and api to ensure the current request (cmd) is authorizated to
     *  make the call
     */
    fun isAuthorizedForCall(cmd: Request, apiRef: ApiRef, auth: Auth?): Result<Boolean> {
        val noAuth = auth == null

        // CASE 1: No auth for action
        return if (noAuth && (apiRef.action.roles == ApiConstants.RoleGuest || apiRef.action.roles.isNullOrEmpty())) {
            ok()
        }
        // CASE 2: No auth for parent
        else if (noAuth && apiRef.action.roles == ApiConstants.RoleParent
                && apiRef.api.roles == ApiConstants.RoleGuest) {
            ok()
        }
        // CASE 3: No auth and action requires roles!
        else if (noAuth) {
            unAuthorized(msg = "Unable to authorize, authorization provider not set")
        }
        else {
            // auth-mode, action roles, api roles
            auth?.isAuthorized(cmd, apiRef.api.auth, apiRef.action.roles, apiRef.api.roles)
                    ?: unAuthorized(msg = "Unable to authorize, authorization provider not set")
        }
    }


    fun isWebProtocol(primaryValue: String, parentValue: String): Boolean {
        val finalValue = AuthFuncs.getReferencedValue(primaryValue, parentValue)
        return when(finalValue) {
            ApiConstants.SourceAny -> true
            ApiConstants.SourceWeb -> true
            else                     -> false
        }
    }


    fun fillArgs(deserializer: Deserializer, apiRef:ApiRef, cmd: Request, args: Inputs, allowLocalIO: Boolean = false,
                 enc: Encryptor? = null): Array<Any?> {
        val action = apiRef.action
        // Check 1: No args ?
        return if (!action.hasArgs)
            arrayOf()
        // Check 2: 1 param with default and no args
        else if (action.isSingleDefaultedArg() && args.size() == 0) {
            val argType = action.paramsUser[0].type.toString()
            val defaultVal = if (_typeDefaults.contains(argType)) _typeDefaults[argType] else null
            arrayOf<Any?>(defaultVal ?: "")
        }
        else {
            deserializer.deserialize(action.member.parameters, args, cmd.meta, cmd)
        }
    }


    /**
     * copies the annotation taking into account the overrides
     *
     * @param ano
     * @param roles
     * @param auth
     * @param protocol
     * @return
     */
    fun buildApiInfo(ano: Api, reg: ApiReg): ApiReg {

        val finalRoles    = reg.roles.nonEmptyOrDefault(ano.roles)
        val finalAuth     = reg.auth.nonEmptyOrDefault(ano.auth)
        val finalProtocol = reg.protocol.nonEmptyOrDefault(ano.protocol)
        return reg.copy(
                area     = ano.area,
                name     = ano.name,
                desc     = ano.desc,
                roles    = finalRoles,
                auth     = finalAuth,
                verb     = ano.verb,
                protocol = finalProtocol
        )
    }


    /**
     * copies the annotation taking into account the overrides
     *
     * @param ano
     * @param roles
     * @param auth
     * @param protocol
     * @return
     */
    fun buildApiInfo(reg: ApiReg): ApiReg {

        val name = reg.cls.simpleName!!.removeSuffix("Controller").removeSuffix("Api").removeSuffix("API")
        val finalArea     = reg.area.nonEmptyOrDefault("")
        val finalName     = reg.name.nonEmptyOrDefault(name)
        val finalRoles    = reg.roles.nonEmptyOrDefault("?")
        val finalAuth     = reg.auth.nonEmptyOrDefault(ApiConstants.AuthModeAppKey)
        val finalProtocol = reg.protocol.nonEmptyOrDefault("*")
        val finalVerb     = reg.verb.nonEmptyOrDefault("*")
        return reg.copy(
                area     = finalArea,
                name     = finalName,
                roles    = finalRoles,
                auth     = finalAuth,
                verb     = finalVerb,
                protocol = finalProtocol
        )
    }
}
