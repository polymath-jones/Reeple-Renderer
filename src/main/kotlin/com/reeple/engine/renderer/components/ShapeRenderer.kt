package com.reeple.engine.renderer.components

import com.kitfox.svg.SVGUniverse
import com.reeple.engine.renderer.types.ShapeType
import com.reeple.engine.renderer.types.ShapeDto
import com.reeple.engine.renderer.utils.applyQualityRenderingHints
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.nio.charset.StandardCharsets


class ShapeRenderer {

    private var universe = SVGUniverse()
    private var shapeCount = 0

    fun drawBasicShape(shape: ShapeDto, g2d: Graphics2D) {

        if (shape.shapeType != ShapeType.SVG) {
            when (shape.shapeType) {
                ShapeType.BOX -> {
                    var box = Rectangle(shape.posX!!.toInt(), shape.posY!!.toInt(), shape.width!!, shape.height!!)
                    if (shape.outline!!) {
                        g2d.color = Color.decode(shape.outlineColor)
                        g2d.stroke = BasicStroke(shape.outlineWidth!!.toFloat())
                        g2d.draw(box)


                        val color = Color.decode(shape.fill)
                        g2d.color = Color(color.red, color.green, color.blue, (255 * (shape.opacity!! / 100.0)).toInt())
                        g2d.fill(box)
                        g2d.stroke = BasicStroke(0f)
                        shapeCount++
                    } else {
                        val color = Color.decode(shape.fill)
                        g2d.color = Color(color.red, color.green, color.blue, (255 * (shape.opacity!! / 100.0)).toInt())
                        g2d.fill(box)
                        shapeCount++
                    }
                }
                ShapeType.CIRCLE -> {
                    val ellipse = Ellipse2D.Double(shape.posX!!, shape.posY!!, shape.width!!.toDouble(), shape.height!!.toDouble())
                    if (shape.outline!!) {
                        g2d.color = Color.decode(shape.outlineColor)
                        g2d.stroke = BasicStroke(shape.outlineWidth!!.toFloat())
                        g2d.draw(ellipse)
                        val color = Color.decode(shape.fill)
                        g2d.color = Color(color.red, color.green, color.blue, (255 * (shape.opacity!! / 100.0)).toInt())
                        g2d.fill(ellipse)
                        g2d.stroke = BasicStroke(0f)
                        shapeCount++
                    } else {
                        val color = Color.decode(shape.fill)
                        g2d.color = Color(color.red, color.green, color.blue, (255 * (shape.opacity!! / 100.0)).toInt())
                        g2d.fill(ellipse)
                        shapeCount++
                    }
                }
                ShapeType.LINE -> {
                    val x1 = shape.posX!!
                    val x2 = x1 + shape.width!!.toDouble()
                    val y1 = shape.posY!!
                    val y2 = y1

                    val line = Line2D.Double(x1, y1, x2, y2)
                    val color = Color.decode(shape.fill)
                    g2d.color = Color(color.red, color.green, color.blue, (255 * (shape.opacity!! / 100.0)).toInt())
                    g2d.stroke = BasicStroke(shape.outlineWidth!!.toFloat())
                    g2d.draw(line)
                    g2d.stroke = BasicStroke(0f)
                    shapeCount++
                }
            }
        } else {
            println("Invalid basic shape passed to the render")
        }
    }


    fun drawVectorShape(audiogramShape: ShapeDto, g2d: Graphics2D) {

        val diagram = universe.getDiagram(universe.loadSVG(audiogramShape.svg!!.byteInputStream(StandardCharsets.UTF_8), "shape_${++shapeCount}"))
        val svgBuffer = BufferedImage(diagram.width.toInt(), diagram.height.toInt(), BufferedImage.TYPE_INT_ARGB)

        var g2dSvgBuffer = svgBuffer.createGraphics()
        applyQualityRenderingHints(g2dSvgBuffer)
        g2dSvgBuffer.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (audiogramShape.opacity!! / 100f))

        diagram.render(g2dSvgBuffer)
        g2d.drawImage(svgBuffer, null, audiogramShape.posX!!.toInt(), audiogramShape.posY!!.toInt())
    }

    override fun toString(): String {
        return "${super.toString()} :: Total number of shapes rendered: $shapeCount"
    }
}