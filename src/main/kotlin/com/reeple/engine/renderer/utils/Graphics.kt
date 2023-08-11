package com.reeple.engine.renderer.utils

import com.reeple.engine.renderer.types.*
import com.reeple.engine.renderer.types.Point
import com.reeple.engine.renderer.utils.external.classes.ShadowFactory
import com.reeple.engine.renderer.utils.external.classes.TextFormat
import com.reeple.engine.renderer.utils.external.classes.TextRenderer
import java.awt.*
import java.awt.font.TextAttribute
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage

private val factory1 = ShadowFactory(5, 1f, Color.white)
private val factory2 = ShadowFactory(5, 1f, Color.white)


fun drawText(layer: TextDto, g2d: Graphics2D) {
    val attributes = HashMap<TextAttribute, Any>()
    attributes[TextAttribute.POSTURE] = if (layer.fontStyle == FontStyle.ITALIC) TextAttribute.POSTURE_OBLIQUE else TextAttribute.POSTURE_REGULAR
    attributes[TextAttribute.SIZE] = layer.fontSize!!
    attributes[TextAttribute.TRACKING] = layer.spacing!!

    when (layer.fontWeight) {
        FontWeight.BOLD -> attributes[TextAttribute.WEIGHT] = TextAttribute.WEIGHT_BOLD
        FontWeight.NORMAL -> attributes[TextAttribute.WEIGHT] = TextAttribute.WEIGHT_REGULAR
        FontWeight.THIN -> attributes[TextAttribute.WEIGHT] = TextAttribute.WEIGHT_LIGHT
    }
    var color = Color.decode(layer.color)
    TextRenderer.drawString(
        g2d,
        layer.value,
        Font.decode(layer.font!!).deriveFont(attributes),
        Color(color.red, color.green, color.blue, (255 * (layer.opacity!! / 100.0)).toInt()),
        Rectangle(layer.posX!!.toInt(), layer.posY!!.toInt(), layer.width!!, 100),
        layer.align,
        TextFormat.FIRST_LINE_VISIBLE
    )
}

fun drawCurve(points: java.util.ArrayList<Point>, path: GeneralPath, inBend: Int, outBend: Int) {

    /*control points*/
    var cpOneX: Double
    var cpOneY: Double
    var cpTwoX: Double
    var cpTwoY: Double

    path.moveTo(points[0].x, points[0].y)
    for (point in 1 until points.size) {

        val cpx = points[point].x
        val cpy = points[point].y


        if (point == 1) {

            //sp will be the same as move coordinates

            val spx = points[0].x
            val spy = points[0].y

            val npx = points[2].x
            val npy = points[2].y

            cpOneX = spx + (cpx - spx) / outBend
            cpOneY = spy + (cpy - spy) / inBend

            cpTwoX = cpx - (npx - spx) / outBend
            cpTwoY = cpy - (npy - spy) / inBend

            path.curveTo(cpOneX, cpOneY, cpTwoX, cpTwoY, cpx, cpy)

        } else if (point > 1 && point <= points.size - 2) {

            var pp0x: Double
            var pp0y: Double

            if (point == 2) {
                pp0x = points[0].x
                pp0y = points[0].y
            } else {
                pp0x = points[point - 2].x
                pp0y = points[point - 2].y
            }

            val ppx = points[point - 1].x
            val ppy = points[point - 1].y

            val npx = points[point + 1].x
            val npy = points[point + 1].y

            cpOneX = ppx + (cpx - pp0x) / outBend
            cpOneY = ppy + (cpy - pp0y) / inBend

            cpTwoX = cpx - (npx - ppx) / outBend
            cpTwoY = cpy - (npy - ppy) / inBend

            path.curveTo(cpOneX, cpOneY, cpTwoX, cpTwoY, cpx, cpy)

        } else {
            val pp0x = points[point - 2].x
            val pp0y = points[point - 2].y

            val ppx = points[point - 1].x
            val ppy = points[point - 1].y



            cpOneX = ppx + (cpx - pp0x) / outBend
            cpOneY = ppy + (cpy - pp0y) / inBend

            cpTwoX = cpx - (cpx - ppx) / outBend
            cpTwoY = cpy - (cpy - ppy) / inBend

            path.curveTo(cpOneX, cpOneY, cpTwoX, cpTwoY, cpx, cpy)

        }
    }
}

fun fillGradient(g2d: Graphics2D, shape: Shape, fill1: Color, fill2: Color, fill3: Color?, mode: Int) {

    var shapePath = GeneralPath(shape)
    var x = shapePath.bounds.x.toFloat()
    var y = shapePath.bounds.y.toFloat()
    var height = shapePath.bounds.height
    var width = shapePath.bounds.width

    var gradientPaint: GradientPaint = GradientPaint(0f, 0f, fill1, 0f, 0f, fill2)

    if (fill3 == null) {

        when (mode) {
            1 -> gradientPaint = GradientPaint(x + width / 2, y, fill1, x + width / 2, y + height, fill2)
            2 -> gradientPaint = GradientPaint(x, y, fill1, x + width, y + height, fill2)
            3 -> gradientPaint = GradientPaint(x + width, y, fill1, x, y + height, fill2)

        }

    } else {
        when (mode) {
            1 -> {
                g2d.paint = GradientPaint(x + width / 2, y, fill1, x + width / 2, y + height * 0.66f, fill2)
                g2d.fill(shape)
                gradientPaint = GradientPaint(x + width / 2, y + height * 0.66f, Color(fill2.red, fill2.green, fill2.blue, 0), x + width / 2, y + height, fill3)
            }
            2 -> {
                g2d.paint = GradientPaint(x, y, fill1, x + width, y + height * 0.66f, fill2)
                g2d.fill(shape)
                gradientPaint = GradientPaint(x + width, y + height * 0.66f, Color(fill2.red, fill2.green, fill2.blue, 0), x, y + height, fill3)
            }
            3 -> {
                g2d.paint = GradientPaint(x + width, y, fill1, x, y + height * 0.66f, fill2)
                g2d.fill(shape)
                gradientPaint = GradientPaint(x, y + height * 0.66f, Color(fill2.red, fill2.green, fill2.blue, 0), x + width, y + height, fill3)
            }
        }
    }

    g2d.paint = gradientPaint
    g2d.fill(shape)
}

fun generateGlow(shape: Shape, g2: Graphics2D, color: Color, size: Int, opacity: Float) {


    val x = shape.bounds.x.toDouble()
    val y = shape.bounds.y.toDouble()

    val shape2 = GeneralPath(shape)
    shape2.transform(AffineTransform.getTranslateInstance(-x, -y))

    val buffer = BufferedImage(shape2.bounds.width, shape2.bounds.height, BufferedImage.TYPE_INT_ARGB)
    val graphics2D = buffer.createGraphics()

    graphics2D.color = color
    graphics2D.stroke = BasicStroke(5f)
    graphics2D.fill(shape2)

    factory1.color = Color.white
    factory1.opacity = opacity
    factory1.size = size

    factory2.color = color
    factory2.size = size

    val glowLayer =  factory2.createShadow( factory1.createShadow(buffer))
    val deltaX = x - (glowLayer.width - shape.bounds.width) / 2.0
    val deltaY = y - (glowLayer.height - shape.bounds.height) / 2.0

    g2.drawImage(glowLayer, AffineTransform.getTranslateInstance(deltaX, deltaY), null)

}

fun renderTrackProgress(percent: Double, g2d: Graphics2D, meta: AudioTracker) {
    when (meta.type) {
        TrackerType.HORIZONTAL_BAR -> {
            val bar = RoundRectangle2D.Double(meta.posX!!, meta.posY!!, meta.length!! * (percent / 100), 10.0, 0.0, 0.0)
            val color = Color.decode(meta.fill)
            val opacity = (255 * (meta.opacity!! / 100.0)).toInt()

            g2d.color = Color(color.red, color.green, color.blue, opacity)
            g2d.fill(bar)
        }
    }
}