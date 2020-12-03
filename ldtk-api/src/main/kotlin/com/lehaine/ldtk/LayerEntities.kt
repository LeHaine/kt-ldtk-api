package com.lehaine.ldtk

open class LayerEntities(json: LayerInstanceJson) : Layer(json) {

    private val _entities = mutableListOf<Entity>()
    val entities get() = _entities.toList()

    init {
        json.entityInstances.forEach { entityInstanceJson ->
            val entity = instantiateEntity(entityInstanceJson)
            entity?.let { _entities.add(it) }
        }
    }

    protected open fun instantiateEntity(json: EntityInstanceJson): Entity? {
        return null
    }
}