package com.lehaine.ldtk

open class Level(val project: Project, val json: LevelJson) {
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
            val layer = instantiateLayer(layerInstanceJson)
            layer?.let { _allUntypedLayers.add(it) }
        }
        json.__neighbours?.forEach {
            _neighbors.add(Neighbor(it.levelUid, NeighborDirection.fromDir(it.dir)))
        }

    }

    fun resolveLayer(id: String): Layer? {
        return allUntypedLayers.find { it.identifier == id }
    }

    protected open fun instantiateLayer(json: LayerInstanceJson): Layer? {
        return null
    }

    override fun toString(): String {
        return "Level(project=$project, uid=$uid, identifier='$identifier', pxWidth=$pxWidth, pxHeight=$pxHeight, worldX=$worldX, worldY=$worldY, bgColor=$bgColor, _allUntypedLayers=$_allUntypedLayers, _neighbors=$_neighbors)"
    }


}