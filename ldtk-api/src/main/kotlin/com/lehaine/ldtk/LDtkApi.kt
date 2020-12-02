package com.lehaine.ldtk

import com.squareup.moshi.Moshi

object LDtkApi {

    private val moshi = Moshi.Builder().build()
    private val projectAdapter = moshi.adapter(ProjectJson::class.java)

    fun parseLDtkFile(json: String): ProjectJson? {
        return projectAdapter.fromJson(json)
    }

}