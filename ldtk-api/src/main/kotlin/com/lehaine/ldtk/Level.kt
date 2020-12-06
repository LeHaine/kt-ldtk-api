package com.lehaine.ldtk

import com.lehaine.ldtk.LDtkApi.LAYER_PREFIX

open class Level(val classPath: String, val project: Project, val json: LevelJson) {
    enum class NeighborDirection {
        North,
        South,
        West,
        East;

        companion object {
            fun fromDir(dir: String): NeighborDirection {
                return when (dir.toLowerCase()) {
                    "n" -> North
                    "e" -> East
                    "s" -> South
                    "w" -> West
                    else -> {
                        System.err.println("WARNING: unknown neighbor level direction: $dir")
                        North
                    }
                }
            }
        }
    }

    data class Neighbor(val levelUid: Int, val dir: NeighborDirection)

    val uid = json.uid
    val identifier = json.identifier
    val pxWidth = json.pxWid
    val pxHeight = json.pxHei
    val worldX = json.worldX
    val worldY = json.worldY
    val bgColor = Project.hexToInt(json.__bgColor)

    private val _allUntypedLayers = mutableListOf<Layer>()
    val allUntypedLayers get() = _allUntypedLayers

    private val _neighbors = mutableListOf<Neighbor>()
    val neighors get() = _neighbors.toList()

    init {
        json.layerInstances.forEach { layerInstanceJson ->
            instantiateLayer(classPath, layerInstanceJson)?.also { _allUntypedLayers.add(it) }
        }
        json.__neighbours?.forEach {
            _neighbors.add(Neighbor(it.levelUid, NeighborDirection.fromDir(it.dir)))
        }
    }

    fun resolveLayer(id: String): Layer {
        return allUntypedLayers.find { it.identifier == id } ?: error("Unable to find $id layer")
    }

    private fun instantiateLayer(classPath: String, json: LayerInstanceJson): Layer? {
        val clazz = Class.forName("$classPath\$$LAYER_PREFIX${json.__identifier}")
        return when {
            LayerIntGridAutoLayer::class.java.isAssignableFrom(clazz) -> {
                val intGridValues = project.getLayerDef(json.layerDefUid)?.intGridValues
                val tilesetDef = project.getTilesetDef(json.__tilesetDefUid)
                clazz.getDeclaredConstructor(
                    TilesetDefJson::class.java, List::class.java,
                    LayerInstanceJson::class.java
                ).newInstance(
                    tilesetDef, intGridValues,
                    json
                ) as Layer
            }
            LayerIntGrid::class.java.isAssignableFrom(clazz) -> {
                val intGridValues = project.getLayerDef(json.layerDefUid)?.intGridValues
                clazz.getDeclaredConstructor(
                    List::class.java,
                    LayerInstanceJson::class.java
                ).newInstance(intGridValues, json) as Layer
            }
            LayerEntities::class.java.isAssignableFrom(clazz) -> {
                val entitiesLayer = clazz.getDeclaredConstructor(LayerInstanceJson::class.java).newInstance(json) as
                        LayerEntities
                entitiesLayer.entities.forEach {
                    val allListField = clazz.getDeclaredField("_all${it.identifier.capitalize()}")
                    allListField.isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    val allList = allListField.get(entitiesLayer) as MutableList<Any>
                    allList.add(it)
                }
                entitiesLayer
            }
            LayerTiles::class.java.isAssignableFrom(clazz) -> {
                val tilesetDef = project.getTilesetDef(json.__tilesetDefUid)
                clazz.getDeclaredConstructor(
                    TilesetDefJson::class.java, LayerInstanceJson::class.java
                ).newInstance(tilesetDef, json) as
                        Layer
            }
            LayerAutoLayer::class.java.isAssignableFrom(clazz) -> {
                val tilesetDef = project.getTilesetDef(json.__tilesetDefUid)
                clazz.getDeclaredConstructor(
                    TilesetDefJson::class.java, LayerInstanceJson::class.java
                ).newInstance(tilesetDef, json) as
                        Layer
            }
            else -> {
                null
            }
        }
    }

    override fun toString(): String {
        return "Level(project=$project, uid=$uid, identifier='$identifier', pxWidth=$pxWidth, pxHeight=$pxHeight, worldX=$worldX, worldY=$worldY, bgColor=$bgColor, _allUntypedLayers=$_allUntypedLayers, _neighbors=$_neighbors)"
    }


}