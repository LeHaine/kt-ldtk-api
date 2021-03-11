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

    /** Pivot X coord (0-1) **/
    val pivotX: Float = if (json.__pivot.isNullOrEmpty()) 0f else json.__pivot[0]

    /** Pivot Y coord (0-1) **/
    val pivotY: Float = if (json.__pivot.isNullOrEmpty()) 0f else json.__pivot[1]

    /** Width in pixels **/
    val width: Int = json.width

    /** Height in pixels**/
    val height: Int = json.height

    /** Tile infos if the entity has one (it could have been overridden by a Field value, such as Enums) **/
    val tileInfosJson: TileInfo? = if (json.__tile == null) {
        null
    } else {
        TileInfo(
            tilesetUid = json.__tile.tilesetUid,
            x = json.__tile.srcRect[0],
            y = json.__tile.srcRect[1],
            w = json.__tile.srcRect[2],
            h = json.__tile.srcRect[3]
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

