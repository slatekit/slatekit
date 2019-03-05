package slatekit.server.sample

import slatekit.common.DateTime
import slatekit.common.Field

data class SampleMovie(
        val id :Long = 0L,


        @property:Field(required = true, length = 50)
        val title :String = "",


        @property:Field(length = 20)
        val category :String = "",


        @property:Field(required = true)
        val playing :Boolean = false,


        @property:Field(required = true)
        val cost:Int,


        @property:Field(required = true)
        val rating: Double,


        @property:Field(required = true)
        val released: DateTime
)