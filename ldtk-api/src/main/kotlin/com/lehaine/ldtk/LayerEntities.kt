package com.lehaine.ldtk

open class LayerEntities(json: LayerInstanceJson) : Layer(json) {

    private val _entities = mutableListOf<Entity>()
    val entities get() = _entities.toList()

    init {

    }

    protected fun instantiateEntities(classPath: String) {
        json.entityInstances.forEach { entityInstanceJson ->
            instantiateEntity(classPath, entityInstanceJson).also { _entities.add(it) }
        }
    }

    private fun instantiateEntity(classPath: String, json: EntityInstanceJson): Entity {
        val clazz = Class.forName("$classPath\$Entity_${json.__identifier}")
        val entity =
            clazz.getDeclaredConstructor(EntityInstanceJson::class.java).newInstance(json)
                    as Entity
        val arrayReg = "Array<(.*)>".toRegex()
        json.fieldInstances.forEach { fieldJson ->
            val isArray = arrayReg.matches(fieldJson.__type)
            val typeName =
                if (isArray) arrayReg.find(fieldJson.__type)!!.groupValues[1] else fieldJson.__type
            if (isArray) {
                val arrList = fieldJson.__value as MutableList<*>
                val newList = when (typeName) {
                    "Int" -> {
                        arrList.map { (it as Double).toInt() }
                    }
                    "Double", "Boolean", "String" -> {
                        arrList
                    }
                    "Color" -> {
                        arrList.map {
                            val hex = it as String
                            val value = Project.hexToInt(hex)
                            Color(value, hex)
                        }
                    }
                    "Point" -> {
                        arrList.map {
                            val map = it as Map<*, *>
                            val cx = (map["cx"] as Double).toInt()
                            val cy = (map["cy"] as Double).toInt()
                            Point(cx, cy)
                        }
                    }
                    else -> {
                        if ("LocalEnum" in typeName) {
                            val type = typeName.substring(typeName.indexOf(".") + 1)
                            val enumClass = Class.forName("$classPath\$$type")
                            val valueOf = enumClass.getMethod("valueOf", String::class.java)
                            arrList.map {
                                val enumString = it as String
                                valueOf.invoke(null, enumString)
                            }
                        } else {
                            fieldJson.__value
                        }
                    }
                }

                val listField = clazz.getDeclaredField("_${fieldJson.__identifier}")
                listField.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val list = listField.get(entity) as MutableList<Any?>
                list.addAll(newList)
            } else if ("LocalEnum" in typeName) {
                val type = typeName.substring(typeName.indexOf(".") + 1)
                val field = clazz.getDeclaredField(fieldJson.__identifier)
                val setter = clazz.getDeclaredMethod(
                    "set${fieldJson.__identifier.capitalize()}",
                    Class.forName("$classPath\$$type")
                )
                val valueOf = field.type.getMethod("valueOf", String::class.java)
                val value = valueOf.invoke(null, fieldJson.__value)
                setter.invoke(entity, value)
            } else {
                val classType = when (typeName) {
                    "Int" -> Int::class.java
                    "Bool" -> Boolean::class.java
                    "Float" -> Double::class.java
                    "String" -> String::class.java
                    "Color" -> Color::class.java
                    "Point" -> Point::class.java
                    else -> {
                        System.err.println("Unsupported type: $typeName")
                        null
                    }
                }
                if (classType != null) {
                    val setter = clazz.getDeclaredMethod(
                        "set${fieldJson.__identifier.capitalize()}",
                        classType
                    )
                    val value = when (typeName) {
                        "Int" -> {
                            (fieldJson.__value as Double).toInt()
                        }
                        "Color" -> {
                            val value = Project.hexToInt(fieldJson.__value as String)
                            val hexValue = fieldJson.__value
                            Color(value, hexValue)
                        }
                        "Point" -> {
                            if (fieldJson.__value != null) {
                                val map = fieldJson.__value as Map<*, *>
                                val cx = (map["cx"] as Double).toInt()
                                val cy = (map["cy"] as Double).toInt()
                                Point(cx, cy)
                            } else {
                                null
                            }
                        }
                        else -> {
                            fieldJson.__value
                        }
                    }
                    setter.invoke(entity, value)
                }
            }
        }
        return entity
    }

    override fun toString(): String {
        return "LayerEntities(entities=$entities)"
    }

}