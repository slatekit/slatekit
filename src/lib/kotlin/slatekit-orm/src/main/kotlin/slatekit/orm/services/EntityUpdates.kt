package slatekit.orm.services

import slatekit.query.IQuery
import slatekit.orm.core.Entity
import slatekit.orm.core.ServiceSupport
import slatekit.meta.Reflector
import slatekit.meta.kClass
import kotlin.reflect.KProperty

interface EntityUpdates<TId, T> : ServiceSupport<TId, T> where TId: kotlin.Comparable<TId>, T:Entity<TId> {

    /**
     * updates the entity in the datastore
     * @param entity
     * @return
     */
    fun update(entity: T): T {
        val finalEntity = applyFieldData(2, entity)
        return entityRepo().update(finalEntity)
    }

    /**
     * updates the entity field in the datastore
     * @param id: id of the entity
     * @param field: the name of the field
     * @param value: the value to set on the field
     * @return
     */
    fun update(id: TId, field: String, value: String) {
        val item = entityRepo().get(id)
        item?.let { entity ->
            Reflector.setFieldValue(entity.kClass, entity, field, value)
            update(entity)
        }
    }

    /**
     * updates items based on the field name
     * @param prop: The property reference
     * @param value: The value to check for
     * @return
     */
    fun updateByField(prop: KProperty<*>, value: Any): Int {
        return entityRepo().updateByField(prop.name, value)
    }

    /**
     * updates items by a stored proc
     */
    fun updateByProc(name: String, args: List<Any>? = null): Int {
        return entityRepo().updateByProc(name, args)
    }

    /**
     * updates items using the query
     */
    fun updateByQuery(query: IQuery): Int {
        return entityRepo().updateByQuery(query)
    }
}