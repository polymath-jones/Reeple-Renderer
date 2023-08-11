package com.reeple.engine.renderer.types

import java.awt.Graphics2D

interface CanRender {
    fun render(g2d: Graphics2D)
    fun render(ampData: ArrayList<FloatArray>, currentPoint: Int, g2d: Graphics2D)
}