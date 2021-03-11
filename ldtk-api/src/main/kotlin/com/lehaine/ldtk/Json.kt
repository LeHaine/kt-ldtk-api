package com.lehaine.ldtk

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProjectJson(
    /** File format version **/
    val jsonVersion: String,

    /** Project background color **/
    val bgColor: String,

    /** Default background color of levels **/
    @Added("0.6.0")
    val defaultLevelBgColor: String,

    /**
    An enum that describes how levels are organized in this project (ie. linearly or in a 2D space). Possible values are: Free, GridVania, LinearHorizontal and LinearVertical,
     **/
    @Added("0.6.0")
    val worldLayout: WorldLayout,

    /** Width of the world grid in pixels. **/
    @Only(["'GridVania' layouts"])
    @Added("0.6.0")
    val worldGridWidth: Int,

    /** Height of the world grid in pixels. **/
    @Only(["'GridVania' layouts"])
    @Added("0.6.0")
    val worldGridHeight: Int,

    /** A structure containing all the definitions of this project **/
    val defs: DefinitionJson,

    /** All levels. The order of this List is only relevant in `LinearHorizontal` and `linearVertical` world layouts (see `worldLayout` value). Otherwise, you should refer to the `worldX`,`worldY` coordinates of each Level. **/
    val levels: List<LevelJson>
)


@JsonClass(generateAdapter = true)
data class LevelJson(
    /** Unique Int identifier **/
    val uid: Int,

    /** Unique String identifier **/
    val identifier: String,

    /** World X coordinate in pixels **/
    val worldX: Int,

    /** World Y coordinate in pixels **/
    val worldY: Int,

    /** Width of the level in pixels **/
    val pxWid: Int,

    /** Height of the level in pixels **/
    val pxHei: Int,

    /** Background color of the level (same as `bgColor`, except the default value is automatically used here if its value is `null`) **/
    val __bgColor: String,

    val layerInstances: List<LayerInstanceJson>,

    val fieldInstances: List<FieldInstanceJson>,
    /**
     * This value is not null if the project option "*Save levels separately*" is enabled. In this case, this **relative** path points to the level Json file.
     */
    val externalRelPath: String?,

    /** A list of all other levels touching this one on the world map. The `dir` is a single lowercase character tipping on the level location (`n`orth, `s`outh, `w`est, `e`ast). In "linear" world layouts, this List is populated with previous/next levels in List, and `dir` depends on the linear horizontal/vertical layout. **/
    val __neighbours: List<NeighbourLevel>?,

    /**
     * The *optional* relative path to the level background image.
     */
    val bgRelPath: String?,

    /**
     * Position informations of the background image, if there is one.
     */
    val __bgPos: LevelBgPosInfos?
)

/**
 * Neighboring level info
 */
@JsonClass(generateAdapter = true)
data class NeighbourLevel(
    val levelUid: Int,
    /**
     * A single lowercase character tipping on the level location (`n`orth, `s`outh, `w`est, `e`ast).
     */
    val dir: String
)

/**
 * Level background image position info
 */
@JsonClass(generateAdapter = true)
data class LevelBgPosInfos(
    /**
     * A list containing the `[x,y]` pixel coordinates of the top-left corner of the **cropped** background image, depending on `bgPos` option.
     */
    val topLeftPx: List<Int>,
    /**
     * A list containing the `[scaleX,scaleY]` values of the **cropped** background image, depending on `bgPos` option.
     */
    val scale: List<Float>,
    /**
     * An array of 4 float values describing the cropped sub-rectangle of the displayed background image.
     * This cropping happens when original is larger than the level bounds. List format: `[ cropX, cropY, cropWidth, cropHeight ]`
     */
    val cropRect: List<Float>
)

@JsonClass(generateAdapter = true)
data class LayerInstanceJson(
    /** Unique String identifier **/
    val __identifier: String,

    /** Layer type (possible values: IntGrid, Entities, Tiles or AutoLayer) **/
    val __type: String,

    /** Grid-based width **/
    val __cWid: Int,

    /** Grid-based height **/
    val __cHei: Int,

    /** Grid size **/
    val __gridSize: Int,

    /** Layer opacity as Float [0-1] **/
    val __opacity: Float,

    /** Total layer X pixel offset, including both instance and definition offsets. **/
    val __pxTotalOffsetX: Int,

    /** Total layer Y pixel offset, including both instance and definition offsets. **/
    val __pxTotalOffsetY: Int,

    /** The definition UID of corresponding Tileset, if any. **/
    @Only(["Tile layers", "Auto-layers"])
    @Added("0.6.0")
    val __tilesetDefUid: Int?,

    /** The relative path to corresponding Tileset, if any. **/
    @Only(["Tile layers", "Auto-layers"])
    @Added("0.6.0")
    val __tilesetRelPath: String?,

    /** Reference to the UID of the level containing this layer instance **/
    val levelId: Int,

    /** Reference the Layer definition UID **/
    val layerDefUid: Int,

    /**
     * Layer instance visibility
     */
    val visible: Boolean,

    /** X offset in pixels to render this layer, usually 0 (IMPORTANT: this should be added to the `LayerDef` optional offset, see `__pxTotalOffsetX`) **/
    @Changed("0.5.0")
    val pxOffsetX: Int,

    /** Y offset in pixels to render this layer, usually 0 (IMPORTANT: this should be added to the `LayerDef` optional offset, see `__pxTotalOffsetY`)**/
    @Changed("0.5.0")
    val pxOffsetY: Int,

    /** Random seed used for Auto-Layers rendering **/
    @Only(["Auto-layers"])
    val seed: Int,

    /**
     * The list of IntGrid values, stored using coordinate ID system (refer to online documentation for more info about "Coordinate IDs")
     */
    @Only(["IntGrid layers"])
    val intGrid: List<IntGridValueInstance>,

    /**
     * A list of all values in the IntGrid layer, stored from left to right, and top to bottom (ie. first row from left to right, followed by second row, etc).
     */
    val intGridCsv: List<Int>,

    @Only(["Tile layers"])
    val gridTiles: List<Tile>,

    /**
     * This layer can use another tileset by overriding the tileset UID here.
     */
    val overrideTilesetUid: Int?,

    /**
     * An List containing all tiles generated by Auto-layer rules. The List is already sorted in display order (ie. 1st tile is beneath 2nd, which is beneath 3rd etc.).
     * Note: if multiple tiles are stacked in the same cell as the result of different rules, all tiles behind opaque ones will be discarded.
     **/
    @Only(["Auto-layers"])
    @Added("0.4.0")
    val autoLayerTiles: List<Tile>,

    @Only(["Entity layers"])
    val entityInstances: List<EntityInstanceJson>
)

@JsonClass(generateAdapter = true)
data class IntGridValueInstance(
    /** Coordinate ID in the layer grid **/
    val coordId: Int,
    /** IntGrid value **/
    val v: Int
)

@Added("0.4.0")
@JsonClass(generateAdapter = true)
data class Tile(
    /** Pixel coordinates of the tile in the **layer** (`[x,y]` format). Don't forget optional layer offsets, if they exist! **/
    @Changed("0.5.0")
    val px: List<Int>,

    /** Pixel coordinates of the tile in the **tileset** (`[x,y]` format) **/
    val src: List<Int>,

    /**
     * "Flip bits", a 2-bits integer to represent the mirror transformations of the tile.
     * - Bit 0 = X flip
     * - Bit 1 = Y flip
     * Examples: f=0 (no flip), f=1 (X flip only), f=2 (Y flip only), f=3 (both flips)
     **/
    val f: Int,

    /**
     *The *Tile ID* in the corresponding tileset.
     **/
    @Added("0.6.0")
    val t: Int,

    /**
     * Internal data used by the editor.
     * For auto-layer tiles: `[ruleId, coordId]`.
     * For tile-layer tiles: `[coordId]`.
     **/
    @Changed("0.6.0")
    val d: List<Int>
)

@JsonClass(generateAdapter = true)
data class EntityInstanceJson(
    /** Unique String identifier **/
    val __identifier: String,

    /** Grid-based coordinates (`[x,y]` format) **/
    @Changed("0.4.0")
    val __grid: List<Int>,

    /**
     * Pivot coordinates  (`[x,y]` format, values are from 0 to 1) of the Entity
     */
    val __pivot: List<Float>,

    /** Entity width in pixels. For non-resizable entities, it will be the same as Entity definition. **/
    val width: Int,

    /** Entity height in pixels. For non-resizable entities, it will be the same as Entity definition. **/
    val height: Int,

    /**
     * Optional Tile used to display this entity (it could either be the default Entity tile, or some tile provided by a field value, like an Enum).
     **/
    @Added("0.4.0")
    val __tile: EntityInstanceTile?,

    /** Reference of the **Entity definition** UID **/
    val defUid: Int,

    /** Pixel coordinates (`[x,y]` format). Don't forget optional layer offsets, if they exist! **/
    @Changed("0.4.0")
    val px: List<Int>,

    /** An array of all custom fields and their values. **/
    val fieldInstances: List<FieldInstanceJson>,
)

/**
 * Tile data in an Entity instance
 **/
@JsonClass(generateAdapter = true)
data class EntityInstanceTile(
    /** Tileset ID **/
    val tilesetUid: Int,

    /** An List of 4 Int values that refers to the tile in the tileset image: `[ x, y, width, height ]` **/
    val srcRect: List<Int>
)

@JsonClass(generateAdapter = true)
data class FieldInstanceJson(
    /** Unique String identifier **/
    val __identifier: String,

    /**
    Actual value of the field instance. The value type may vary, depending on `__type` (Integer, Boolean, String etc.)
    It can also be an `List` of various types.
     **/
    val __value: Any?,

    /** Type of the field, such as Int, Float, Enum(enum_name), Boolean, etc. **/
    val __type: String,

    /** Reference of the **Field definition** UID **/
    val defUid: Int,
)

@JsonClass(generateAdapter = true)
data class DefinitionJson(
    val layers: List<LayerDefJson>,
    /** All entities, including their custom fields **/
    val entities: List<EntityDefJson>,
    val tilesets: List<TilesetDefJson>,
    val enums: List<EnumDefJson>,
    /**
     * A list containing all custom fields available to all levels.
     */
    val levelFields: List<FieldDefJson>,

    /**
     * Note: external enums are exactly the same as `enums`, except they have a `relPath` to point to an external source file.
     **/
    val externalEnums: List<EnumDefJson>
)

@JsonClass(generateAdapter = true)
data class LayerDefJson(
    /** Unique String identifier **/
    val identifier: String,

    /** Type of the layer (*IntGrid, Entities, Tiles or AutoLayer*) **/
    val __type: String,

    /** Type of the layer **/
    val type: String,

    /** Unique Int identifier **/
    val uid: Int,

    /** Width and height of the grid in pixels **/
    val gridSize: Int,

    /** X offset of the layer, in pixels (IMPORTANT: this should be added to the `LayerInstance` optional offset) **/
    @Added("0.5.0")
    val pxOffsetX: Int,

    /** Y offset of the layer, in pixels (IMPORTANT: this should be added to the `LayerInstance` optional offset) **/
    @Added("0.5.0")
    val pxOffsetY: Int,

    /** Opacity of the layer (0 to 1.0) **/
    val displayOpacity: Float,

    @Only(["IntGrid layer"])
    val intGridValues: List<IntGridValue>,

    /** Reference to the Tileset UID being used by this auto-layer rules **/
    @Only(["Auto-layers"])
    val autoTilesetDefUid: Int?,

    @Only(["Auto-layers"])
    val autoSourceLayerDefUid: Int?,

    /** Reference to the Tileset UID being used by this tile layer **/
    @Only(["Tile layers"])
    val tilesetDefUid: Int?
)

@JsonClass(generateAdapter = true)
data class IntGridValue(val identifier: String?, val color: String)

@JsonClass(generateAdapter = true)
data class EntityDefJson(
    /** Unique String identifier **/
    val identifier: String,

    /** Unique Int identifier **/
    val uid: Int,

    /** Pixel width **/
    val width: Int,

    /** Pixel height **/
    val height: Int,

    /** Base entity color **/
    val color: String,

    /** Tileset ID used for optional tile display **/
    val tilesetId: Int?,

    /** Tile ID used for optional tile display **/
    val tileId: Int?,

    /** Pivot X coordinate (from 0 to 1.0) **/
    val pivotX: Float,

    /** Pivot Y coordinate (from 0 to 1.0) **/
    val pivotY: Float,

    /** Array of field definitions **/
    val fieldDefs: List<FieldDefJson>
)

@JsonClass(generateAdapter = true)
data class FieldDefJson(
    /** Unique String identifier **/
    val identifier: String,

    /** Unique Intidentifier **/
    val uid: Int,

    /** Human readable value type (eg. `Int`, `Float`, `Point`, etc.). If the field is an array, this field will look like `Array<...>` (eg. `Array<Int>`, `Array<Point>` etc.) **/
    val __type: String,

    /** Internal type enum **/
    val type: Any,

    /** TRUE if the value is an array of multiple values **/
    val isArray: Boolean,

    /** TRUE if the value can be null. For arrays, TRUE means it can contain null values (exception: array of Points can't have null values). **/
    val canBeNull: Boolean,

    /** Array min length **/
    @Only(["Array"])
    val arrayMinLength: Int?,

    /** Array max length **/
    @Only(["Array"])
    val arrayMaxLength: Int?,

    /** Min limit for value, if applicable **/
    @Only(["Int, Float"])
    val min: Float?,

    /** Max limit for value, if applicable **/
    @Only(["Int, Float"])
    val max: Float?,

    /** Optional list of accepted file extensions for FilePath value type. Includes the dot: `.ext`**/
    @Only(["FilePath"])
    val acceptFileTypes: List<String>?,

    /** Default value if selected value is null or invalid. **/
    val defaultOverride: DefaultOverrideInfo?,
)

@JsonClass(generateAdapter = true)
data class DefaultOverrideInfo(val id: String, val params: List<Any>)

@JsonClass(generateAdapter = true)
data class TilesetDefJson(
    /** Unique String identifier **/
    val identifier: String,

    /** Unique Intidentifier **/
    val uid: Int,

    /** Path to the source file, relative to the current project JSON file **/
    val relPath: String,

    /** Image width in pixels **/
    val pxWid: Int,

    /** Image width in pixels **/
    val pxHei: Int,

    val tileGridSize: Int,

    /** Space in pixels between all tiles **/
    val spacing: Int,

    /** Distance in pixels from image borders **/
    val padding: Int,

    /** Array of group of tiles selections, only meant to be used in the editor **/
    val savedSelections: List<SaveSelectionInfo>
)

@JsonClass(generateAdapter = true)
data class SaveSelectionInfo(
    val ids: List<Int>,
    val mode: Any
)

@JsonClass(generateAdapter = true)
data class EnumDefJson(
    /** Unique Int identifier **/
    val uid: Int,

    /** Unique String identifier **/
    val identifier: String,

    /** All possible enum values, with their optional Tile infos. **/
    val values: List<EnumDefValues>,

    /** Tileset UID if provided **/
    val iconTilesetUid: Int?,

    /** Relative path to the external file providing this Enum **/
    val externalRelPath: String?
)

@JsonClass(generateAdapter = true)
data class EnumDefValues(
    /** Enum value **/
    val id: String,

    /** The optional ID of the tile **/
    val tileId: Int?,

    /** An array of 4 Int values that refers to the tile in the tileset image: `[ x, y, width, height ]` **/
    @Added("0.4.0")
    val __tileSrcRect: List<Int>?,
)

enum class WorldLayout {
    Free,
    GridVania,
    LinearHorizontal,
    LinearVertical
}