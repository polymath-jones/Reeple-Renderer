package com.reeple.engine.renderer.utils

import com.jhlabs.image.GrayscaleFilter
import com.jhlabs.image.NoiseFilter
import com.reeple.engine.renderer.core.RenderContext
import com.reeple.engine.renderer.components.ShapeRenderer
import com.reeple.engine.renderer.core.FileManager
import com.reeple.engine.renderer.types.*
import com.twelvemonkeys.image.ConvolveWithEdgeOp
import org.imgscalr.Scalr
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.DataBufferByte
import java.awt.image.Kernel
import javax.imageio.ImageIO
import kotlin.math.*


fun applyQualityRenderingHints(g2d: Graphics2D) {
    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
    g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
}

fun fastResizeImage(sourceIn: BufferedImage, width: Double, height: Double): BufferedImage {
    val source = convertToType(sourceIn, BufferedImage.TYPE_3BYTE_BGR)
    val scale: Double
    val scaledWidth: Double
    val scaledHeight: Double

    if (width >= height) {
        scale = width / source.width
        scaledWidth = width
        scaledHeight = source.height * scale
    } else {
        scale = height / source.height
        scaledHeight = height
        scaledWidth = source.width * scale
    }
    val pixels = (source.raster.dataBuffer as DataBufferByte).data
    val matImg = Mat(source.height, source.width, CvType.CV_8UC3)
    matImg.put(0, 0, pixels)

    val resizeImage = Mat()
    val sz = Size(scaledWidth, scaledHeight)

    Imgproc.resize(matImg, resizeImage, sz)
    return HighGui.toBufferedImage(resizeImage) as BufferedImage
}

fun fastResizeImage(sourceIn: BufferedImage, scale: Double): BufferedImage {
    val source = convertToType(sourceIn, BufferedImage.TYPE_3BYTE_BGR)
    val scaledWidth: Double
    val scaledHeight: Double


    scaledWidth = source.width * scale
    scaledHeight = source.height * scale

    val pixels = (source.raster.dataBuffer as DataBufferByte).data
    val matImg = Mat(source.height, source.width, CvType.CV_8UC3)
    matImg.put(0, 0, pixels)

    val resizeImage = Mat()
    val sz = Size(scaledWidth, scaledHeight)

    Imgproc.resize(matImg, resizeImage, sz)
    return HighGui.toBufferedImage(resizeImage) as BufferedImage
}

fun convertToType(sourceImage: BufferedImage, targetType: Int): BufferedImage {

    val image: BufferedImage
    if (sourceImage.type == targetType) return sourceImage
    else {
        image = BufferedImage(sourceImage.width,
            sourceImage.height, targetType)
        image.graphics.drawImage(sourceImage, 0, 0, null)
    }

    return image
}

fun screenImage(source: BufferedImage, fill: String) {

    val g2d = source.createGraphics()
    val screen = Rectangle(0, 0, source.width, source.height)
    val fill = Color.decode(fill)
    val opacity = (255 * (50 / 100.0)).toInt()

    g2d.color = Color(fill.red, fill.green, fill.blue, opacity)
    g2d.fill(screen)
}

fun rotateImage(img: BufferedImage, angle: Double): BufferedImage {

    val rads = Math.toRadians(angle)
    val sin = abs(sin(rads))
    val cos = abs(cos(rads))
    val w = img.width
    val h = img.height
    val newWidth = floor(w * cos + h * sin).toInt()
    val newHeight = floor(h * cos + w * sin).toInt()

    val rotated = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)

    val at = AffineTransform()
    val x = w / 2
    val y = h / 2
    at.rotate(rads, x.toDouble(), y.toDouble())

    val g2d = rotated.createGraphics()
    applyQualityRenderingHints(g2d)
    g2d.transform = at
    g2d.drawRenderedImage(img, null)
    g2d.dispose()

    return rotated
}

fun blurFilter(radius: Int, horizontal: Boolean): ConvolveWithEdgeOp {
    if (radius < 1) {
        throw IllegalArgumentException("Radius must be >= 1")
    }

    val size = radius * 2 + 1
    val data = FloatArray(size)

    val sigma = radius / 3.0f
    val twoSigmaSquare = 2.0f * sigma * sigma
    val sigmaRoot = sqrt(twoSigmaSquare * Math.PI).toFloat()
    var total = 0.0f

    for (i in -radius..radius) {
        val distance = (i * i).toFloat()
        val index = i + radius
        data[index] = exp((-distance / twoSigmaSquare).toDouble()).toFloat() / sigmaRoot
        total += data[index]
    }

    for (i in data.indices) {
        data[i] /= total
    }

    var kernel: Kernel?
    kernel = if (horizontal) {
        Kernel(size, 1, data)
    } else {
        Kernel(1, size, data)
    }
    return ConvolveWithEdgeOp(kernel, ConvolveWithEdgeOp.EDGE_REFLECT, null)
}

fun blurImage(bufferedImage: BufferedImage): BufferedImage {
    var bi = blurFilter(200, true).filter(bufferedImage, null)
    bi = blurFilter(200, false).filter(bi, null)
    return bi
}

fun maskToCircle(img: BufferedImage): BufferedImage {

    val width = img.width
    val height = img.height
    var diameter = 0
    var oval: Area?
    diameter = if (width > height || width == height) {
        height
    } else {
        width
    }
    oval = if (width > height) {
        Area(Ellipse2D.Double((width - diameter.toDouble()) / 2, 0.0, diameter.toDouble(), diameter.toDouble()))
    } else {
        Area(Ellipse2D.Double((width - diameter.toDouble()) / 2, 0.0, diameter.toDouble(), diameter.toDouble()))
    }
    var masked = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = masked.createGraphics()
    applyQualityRenderingHints(g2d)
    g2d.clip(oval)
    g2d.drawRenderedImage(img, null)
    g2d.dispose()
    return masked
}

fun createStaticImage(context: RenderContext): BufferedImage {

    val shapeRenderer = ShapeRenderer()
    val layers: List<Layer> = context.staticLayers.sortedWith(compareBy { it.zIndex })
    val bufferedImage = BufferedImage(context.meta.video.width!!.toInt(), context.meta.video.height!!.toInt(), BufferedImage.TYPE_INT_ARGB)
    val g2d = bufferedImage.createGraphics()
    applyQualityRenderingHints(g2d)


    for (layer in layers) {

        when (layer) {
            is ImageDto -> {
                var source = ImageIO.read(FileManager.getResource(layer.file))
                if (layer.width != 0.0 || layer.height != 0.0) {
                    source = Scalr.resize(source, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, layer.width!!.toInt(), layer.height!!.toInt(), Scalr.OP_ANTIALIAS)
                }

                if (layer.imageEffect != ImageEffect.NONE) {
                    when (layer.imageEffect) {

                        ImageEffect.BLUR -> {
                            source = blurImage(source)
                        }
                        ImageEffect.MONOCHROME -> {
                            var effect = GrayscaleFilter()
                            val dest = effect.createCompatibleDestImage(source, ColorModel.getRGBdefault())
                            effect.filter(source, dest)
                            source = dest
                            dest.flush()
                        }
                        ImageEffect.JITTER -> {
                            var effect = NoiseFilter()
                            val dest = effect.createCompatibleDestImage(source, ColorModel.getRGBdefault())
                            effect.filter(source, dest)
                            source = dest
                            dest.flush()
                        }
                    }
                }
                if (layer.filter != FilterType.NONE) {
                    when (layer.filter) {
                        FilterType.SCREEN -> {
                            screenImage(source, layer.filterFill!!)
                        }

                    }
                }
                when (layer.align) {
                    ImageAlign.CENTER -> layer.posX = (context.meta.video.width!! - source.width) / 2
                    ImageAlign.RIGHT -> layer.posX = (context.meta.video.width!! - source.width) * 3 / 4
                    ImageAlign.LEFT -> layer.posX = (context.meta.video.width!! - source.width) / 4
                }
                if (layer.mask != MaskType.NONE) {
                    when (layer.mask) {
                        MaskType.CIRCLE -> {
                            source = maskToCircle(source)
                        }
                        MaskType.SQUARE -> {

                        }
                    }
                }
                if (layer.transform != null && layer.transform != "none") {
                    val degree = layer.transform!!.substringAfterLast(":").trim().toDouble()
                    source = rotateImage(source, degree)
                }

                g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (layer.opacity!! / 100f))
                g2d.drawImage(source, null, layer.posX!!.toInt(), layer.posY!!.toInt())
                g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)

                if (layer.frame != FrameType.NONE && layer.mask != MaskType.CIRCLE) {
                    val frameColor = Color.decode(layer.frameColor)
                    var frameWidth = 0f
                    when (layer.frame) {
                        FrameType.THIN -> {
                            frameWidth = 2f
                        }
                        FrameType.NORMAL -> {
                            frameWidth = 5f
                        }
                        FrameType.SOLID -> {
                            frameWidth = 10f
                        }
                    }
                    g2d.color = frameColor
                    g2d.stroke = BasicStroke(frameWidth)
                    g2d.drawRect(layer.posX!!.roundToInt() + frameWidth.toInt() / 2, layer.posY!!.roundToInt(), source.width, source.height)
                }
            }
            is TextDto -> {
                drawText(layer, g2d)
            }
            is ShapeDto -> {
                if (layer.shapeType != ShapeType.SVG)
                    shapeRenderer.drawBasicShape(layer, g2d)
                else
                    shapeRenderer.drawVectorShape(layer, g2d)
            }
        }

    }

    return bufferedImage
}
