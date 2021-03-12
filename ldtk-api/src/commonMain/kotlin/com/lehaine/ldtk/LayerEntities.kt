package com.lehaine.ldtk

open class LayerEntities(json: LayerInstance) : Layer(json) {

    private val _entities = mutableListOf<Entity>()
    val entities get() = _entities.toList()

    protected fun instantiateEntities() {
        json.entityInstances.forEach { entityInstanceJson ->
            instantiateEntity(entityInstanceJson)?.also { _entities.add(it) }
        }
    }

    /**
     * This function will be overridden in the ProjectProcessor if used.
     */
    protected open fun instantiateEntity(json: EntityInstance): Entity? {
        return null
    }

    override fun toString(): String {
        return "LayerEntities(entities=$entities)"
    }

}