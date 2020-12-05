package com.lehaine.gdx

import com.lehaine.ldtk.IntGridValue
import com.lehaine.ldtk.LayerInstanceJson
import com.lehaine.ldtk.LayerIntGridAutoLayer
import com.lehaine.ldtk.TilesetDefJson

class GdxLayerIntGridAutoLayer(
    tilesetDefJson: TilesetDefJson?,
    intGridValues: List<IntGridValue>, json: LayerInstanceJson
) : LayerIntGridAutoLayer(tilesetDefJson, intGridValues, json) {
}