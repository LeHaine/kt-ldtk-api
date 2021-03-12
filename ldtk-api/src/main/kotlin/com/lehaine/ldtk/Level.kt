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

    data class CropRect(val x: Float, val y: Float, val w: Float, val h: Float);

    data class LevelBgImage(
        val relFilePath: String,
        val topLeftX: Int,
        val topLeftY: Int,
        val scaleX: Float,
        val scaleY: Float,
        val cropRect: CropRect
    )

    var uid = json.uid
        private set
    var identifier = json.identifier
        private set
    var pxWidth = json.pxWid
        private set
    var pxHeight = json.pxHei
        private set
    var worldX = json.worldX
        private set
    var worldY = json.worldY
        private set
    var bgColor = Project.hexToInt(json.__bgColor)
        private set
    val hasBgImage: Boolean
        get() = bgImageInfos != null
    var bgImageInfos: LevelBgImage? = if (json.bgRelPath.isNullOrEmpty() || json.__bgPos == null) {
        null
    } else {
        LevelBgImage(
            relFilePath = json.bgRelPath,
            topLeftX = json.__bgPos.topLeftPx[0],
            topLeftY = json.__bgPos.topLeftPx[1],
            scaleX = json.__bgPos.scale[0],
            scaleY = json.__bgPos.scale[1],
            cropRect = CropRect(
                x = json.__bgPos.cropRect[0],
                y = json.__bgPos.cropRect[1],
                w = json.__bgPos.cropRect[2],
                h = json.__bgPos.cropRect[3]
            )
        )
    }
        private set

    private val _allUntypedLayers = mutableListOf<Layer>()
    val allUntypedLayers get() = _allUntypedLayers

    private val _neighbors = mutableListOf<Neighbor>()
    val neighors get() = _neighbors.toList()

    /**
     * Only exists if levels are stored in separate level files.
     */
    private var externalRelPath = json.externalRelPath

    init {
        json.layerInstances?.forEach { layerInstanceJson ->
            instantiateLayer(classPath, layerInstanceJson)?.also { _allUntypedLayers.add(it) }
        }
        json.__neighbours?.forEach {
            _neighbors.add(Neighbor(it.levelUid, NeighborDirection.fromDir(it.dir)))
        }
    }

    fun isLoaded() = externalRelPath == null || !allUntypedLayers.isNullOrEmpty()

    fun load(): Boolean {
        if (isLoaded()) {
            return true
        }
        val relPath = externalRelPath ?: return false
        val json =
            LDtkApi.parseLDtkLevelFile(String(project.getAsset(relPath))) ?: error("Unable to parse Level JSON")

        uid = json.uid
        identifier = json.identifier
        pxWidth = json.pxWid
        pxHeight = json.pxHei
        worldX = json.worldX
        worldY = json.worldY
        bgColor = Project.hexToInt(json.__bgColor)

        bgImageInfos = if (json.bgRelPath.isNullOrEmpty() || json.__bgPos == null) {
            null
        } else {
            LevelBgImage(
                relFilePath = json.bgRelPath,
                topLeftX = json.__bgPos.topLeftPx[0],
                topLeftY = json.__bgPos.topLeftPx[1],
                scaleX = json.__bgPos.scale[0],
                scaleY = json.__bgPos.scale[1],
                cropRect = CropRect(
                    x = json.__bgPos.cropRect[0],
                    y = json.__bgPos.cropRect[1],
                    w = json.__bgPos.cropRect[2],
                    h = json.__bgPos.cropRect[3]
                )
            )
        }
        externalRelPath = json.externalRelPath

        json.layerInstances?.forEach { layerInstanceJson ->
            instantiateLayer(classPath, layerInstanceJson)?.also { _allUntypedLayers.add(it) }
        }
        json.__neighbours?.forEach {
            _neighbors.add(Neighbor(it.levelUid, NeighborDirection.fromDir(it.dir)))
        }

        return true
    }

    fun resolveLayer(id: String): Layer {
        load()
        return allUntypedLayers.find { it.identifier == id } ?: error("Unable to find $id layer")
    }

    private fun instantiateLayer(classPath: String, json: LayerInstanceJson): Layer? {
//        return null
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