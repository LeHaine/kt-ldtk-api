package com.lehaine.gdx

import com.lehaine.ldtk.Project

open class GdxProject(projectFilePath: String) : Project(projectFilePath) {

    fun dispose() {
        assetCache.clear()
    }
}