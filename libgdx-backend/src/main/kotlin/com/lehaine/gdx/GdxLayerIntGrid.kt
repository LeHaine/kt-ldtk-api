package com.lehaine.gdx

import com.lehaine.ldtk.IntGridValueDefinition
import com.lehaine.ldtk.LayerInstance
import com.lehaine.ldtk.LayerIntGrid
import com.lehaine.ldtk.Project

open class GdxLayerIntGrid(
    project: Project,
    intGridValues: List<IntGridValueDefinition>,
    json: LayerInstance
) : LayerIntGrid(project, intGridValues, json) {
}