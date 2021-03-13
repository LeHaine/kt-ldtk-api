package com.lehaine.gdx

import com.badlogic.gdx.math.Vector2

object LDtkUtil {

    @JvmStatic
    fun toGdxPos(cx: Int, cy: Int, gridSize: Int, levelHeight: Int): Vector2 {
        return Vector2((cx * gridSize).toFloat(), -(cy * gridSize - levelHeight).toFloat())
    }

    @JvmStatic
    fun toGdxPos(cx: Int, cy: Int, gridSize: Int, levelHeight: Int, out: Vector2) {
        out.x = (cx * gridSize).toFloat()
        out.y = -(cy * gridSize - levelHeight).toFloat()
    }
}


fun Vector2.convertToGdxPos(cx: Int, cy: Int, gridSize: Int, levelHeight: Int) {
    LDtkUtil.toGdxPos(cx, cy, gridSize, levelHeight, this)
}

fun Vector2.getGdxPos(cx: Int, cy: Int, gridSize: Int, levelHeight: Int): Vector2 {
    return LDtkUtil.toGdxPos(cx, cy, gridSize, levelHeight)
}