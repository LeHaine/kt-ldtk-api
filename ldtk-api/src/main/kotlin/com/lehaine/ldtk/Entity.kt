package com.lehaine.ldtk

open class Entity(val json: EntityInstanceJson) {
    val identifier: String = json.__identifier

    /** Grid-based X coordinate **/
    val cx: Int = json.__grid[0]

    /** Grid-based Y coordinate **/
    val cy: Int = json.__grid[1]

    /** Pixel-based X coordinate **/
    val pixelX: Int = json.px[0]

    /** Pixel-based Y coordinate **/
    val pixelY: Int = json.px[1]

    /** Tile infos if the entity has one (it could have been overridden by a Field value, such as Enums) **/
    val tileInfosJson: TileInfo? = if (json.__tileJson == null) {
        null
    } else {
        TileInfo(
            tilesetUid = json.__tileJson.tilesetUid,
            x = json.__tileJson.srcRect[0],
            y = json.__tileJson.srcRect[1],
            w = json.__tileJson.srcRect[2],
            h = json.__tileJson.srcRect[3]
        )
    }

    data class TileInfo(val tilesetUid: Int, val x: Int, val y: Int, val w: Int, val h: Int)

    override fun toString(): String {
        return "Entity(identifier='$identifier', cx=$cx, cy=$cy, pixelX=$pixelX, pixelY=$pixelY, tileInfosJson=$tileInfosJson)"
    }

    protected fun entityInfoString(): String {
        return "identifier='$identifier', cx=$cx, cy=$cy, pixelX=$pixelX, pixelY=$pixelY, tileInfosJson=$tileInfosJson"
    }


}

