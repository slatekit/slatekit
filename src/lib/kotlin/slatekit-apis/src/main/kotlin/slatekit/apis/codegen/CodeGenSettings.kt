package slatekit.apis.codegen

import slatekit.apis.ApiHost
import slatekit.common.requests.Request

data class CodeGenSettings(
        val host: ApiHost,
        val req: Request,
        val templatesFolder: String,
        val outputFolder: String,
        val packageName: String,
        val classFile: String = "",
        val methodFile: String = "",
        val modelFile: String = "",
        val lang: String,
        val extension: String,
        val declaredMethodsOnly: Boolean = true
)