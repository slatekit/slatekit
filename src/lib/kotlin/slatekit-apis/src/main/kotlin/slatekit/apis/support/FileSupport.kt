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

package slatekit.apis.support

import java.io.File
import slatekit.context.Context
import slatekit.common.Ignore
import slatekit.common.crypto.EncryptSupport
import slatekit.common.log.LogSupport
import slatekit.common.ext.subStringPair

interface FileSupport : EncryptSupport, LogSupport {

    val context: Context

    @Ignore
    fun interpretUri(path: String): String? {
        val pathParts = path.subStringPair("://")
        return pathParts?.let { parts ->
            val uri = parts.first
            val loc = parts.second
            this.context.dirs?.let { dirs ->
                when (uri) {
                    "usr://" -> File(System.getProperty("user.home"), loc).toString()
                    "tmp://" -> File(System.getProperty("java.io.tmpdir"), loc).toString()
                    "abs://" -> File(loc).toString()
                    "inputs://" -> File(dirs.pathToInputs, loc).toString()
                    "outputs://" -> File(dirs.pathToOutputs, loc).toString()
                    "logs://" -> File(dirs.pathToLogs, loc).toString()
                    "cache://" -> File(dirs.pathToCache, loc).toString()
                    else -> path
                }
            }
        }
    }

    @Ignore
    fun writeToFile(
        msg: Any?,
        fileNameLocal: String,
        count: Int,
        contentFetcher: (Any?) -> String
    ): String? {
        return msg?.let { _ ->
            val finalFileName = if (count == 0) fileNameLocal else fileNameLocal + "_" + count
            val path = interpretUri(finalFileName)
            val content = contentFetcher(msg)
            File(path.toString()).writeText(content)
            "File content written to : " + path
        } ?: "No items available"
    }
}
