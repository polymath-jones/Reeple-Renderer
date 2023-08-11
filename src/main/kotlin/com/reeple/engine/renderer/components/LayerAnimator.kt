package com.reeple.engine.renderer.components

import com.reeple.engine.renderer.core.FileManager
import com.reeple.engine.renderer.core.RenderContext
import com.reeple.engine.renderer.types.ShapeType
import com.reeple.engine.renderer.types.ImageDto
import com.reeple.engine.renderer.types.ShapeDto
import com.reeple.engine.renderer.types.TextDto
import com.reeple.engine.renderer.utils.drawText
import com.reeple.engine.renderer.utils.fastResizeImage
import java.awt.AlphaComposite
import java.awt.Graphics2D
import javax.imageio.ImageIO

class LayerAnimator(val context: RenderContext) {

    private val shapeRenderer = ShapeRenderer()

    fun render(g2d: Graphics2D) {
        tick()
        draw(g2d)
    }

    private fun tick() {
        for (layer in context.animatedLayers) {
            when (layer) {
                is ImageDto -> {

                    var model = context.animations[layer.animationModel]!!

                    model.posX?.let {
                        layer.posX = it.interpolate(context.meta.video.optimisation!!)
                    }

                    model.posY?.let {
                        layer.posY = it.interpolate(context.meta.video.optimisation!!)
                    }

                    model.opacity?.let {
                        layer.opacity = it.interpolate(context.meta.video.optimisation!!).toInt()
                    }

                }
                is ShapeDto -> {

                    var model = context.animations[layer.animationModel]!!

                    model.posX?.let {
                        layer.posX = it.interpolate(context.meta.video.optimisation!!)
                    }

                    model.posY?.let {
                        layer.posY = it.interpolate(context.meta.video.optimisation!!)
                    }

                    model.opacity?.let {
                        layer.opacity = it.interpolate(context.meta.video.optimisation!!).toInt()
                    }
                }
                is TextDto -> {

                    var model = context.animations[layer.animationModel]!!

                    model.posX?.let {
                        layer.posX = it.interpolate(context.meta.video.optimisation!!)
                    }

                    model.posY?.let {
                        layer.posY = it.interpolate(context.meta.video.optimisation!!)
                    }

                    model.opacity?.let {
                        layer.opacity = it.interpolate(context.meta.video.optimisation!!).toInt()
                    }
                }

            }
        }
    }

    private fun draw(g2d: Graphics2D) {
        for (layer in context.animatedLayers) {
            when (layer) {
                is ImageDto -> {
                    var source = ImageIO.read(FileManager.getResource(layer.file))

                    if (layer.width != 0.0 || layer.height != 0.0) {
                        source =fastResizeImage(source, layer.width!!, layer.height!!)
                    }
                    g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (layer.opacity!! / 100f))
                    g2d.drawImage(source, null, layer.posX!!.toInt(), layer.posY!!.toInt())
                    g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
                }
                is ShapeDto -> {
                    if (layer.shapeType != ShapeType.SVG) shapeRenderer.drawBasicShape(layer, g2d)
                    else shapeRenderer.drawVectorShape(layer, g2d)
                }
                is TextDto -> {
                    drawText(layer, g2d)
                }
            }
        }
    }


}