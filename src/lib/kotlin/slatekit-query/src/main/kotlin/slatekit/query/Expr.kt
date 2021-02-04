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

package slatekit.query

import slatekit.common.data.Command

/**
 * Expression interface to represent conditions, logical and/or, etc.
 */
interface Expr {
    fun toStringQuery(): String
}


interface Stmt {
    fun build(): Command
}


/**
 * Represents a set expression category = 1 for Update statements
 */
data class Set(@JvmField val field: String, @JvmField val fieldValue: Any?) : Expr {

    override fun toStringQuery(): kotlin.String {
        return ""
    }
}


/**
 * Represents a condition clause e.g. "category" > 20
 * @param field : "category"
 * @param op: e.g ">"
 * @param value : 20
 */
class Condition(
    @JvmField val field: Any,
    @JvmField val op: String,
    @JvmField val value: Any
) : Expr {

    /**
     * string represention of condition
     * @return
     */
    override fun toStringQuery(): String = toStringQueryWithOptions(false, "[", "]")

    /**
     * Returns a String representation of this instance.
     * @param surround : True to surround alias with text.
     * @param left : Left surrounding text
     * @param right : Right surrounding text
     * @return
     */
    fun toStringQueryWithOptions(surround: Boolean = false, left: String = "", right: String = ""): String {
        val fieldName = QueryEncoder.ensureField(this.field.toString())
        val col = if (surround) left + fieldName + right else fieldName
        val comp = QueryEncoder.ensureCompare(op)
        val fieldVal = this.value
        val result = QueryEncoder.convertVal(fieldVal)

        return "$col $comp $result"
    }
}


/**
 * Represents 2 conditions to form a logical expression in the form of
 * 1. ( a AND b )
 * 2. ( a OR  b )
 * 3. ( a AND ( b OR c ) )
 */
data class LogicalExpr(
    @JvmField val left: Expr,
    @JvmField val op: String,
    @JvmField val right: Expr
) : Expr {

    override fun toStringQuery(): String = getString(left) + " " + op + " " + getString(right)

    private fun getString(item: Any?): String =
        if (item == null) {
            ""
        } else (item as? Expr)?.toStringQuery() ?: item.toString()
}
