package com.reeple.engine.renderer.core

import com.reeple.engine.renderer.components.*
import com.reeple.engine.renderer.types.WaveformVariant
import com.reeple.engine.renderer.utils.applyQualityRenderingHints
import com.reeple.engine.renderer.utils.createStaticImage
import com.reeple.engine.renderer.utils.fastResizeImage
import com.reeple.engine.renderer.utils.renderTrackProgress
import com.xuggle.mediatool.IMediaWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class VideoRenderer(
    private val freqAmpData: ArrayList<FloatArray>,
    private val sigAmpData: ArrayList<FloatArray>,
    private val context: RenderContext,
    private val writer: IMediaWriter,
    private val hookService: HooksManager
) {

    private var frameGrabber: FrameGrabber? = null
    private var animator: LayerAnimator? = null

    private val w = context.meta.video.width!!.toInt()
    private val h = context.meta.video.height!!.toInt()
    private val bgX = context.background.posX!!
    private val bgY = context.background.posY!!

    private var fps = 30.0
    private val startTime = System.currentTimeMillis()
    private val totalPoints = freqAmpData.size
    private var index = 1
    private var progress = 0
    private var currentPoint = 0

    private val staticImage = createStaticImage(context)
    private val alphaBuffer = BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR)
    private val g2d: Graphics2D = alphaBuffer.createGraphics().also { applyQualityRenderingHints(it) }
    private val plotters = WaveformPlotter().getPlotters(context)
    private val effectsManager = EffectsRenderer(context)

    init {
        if (context.meta.video.optimisation!!) fps = 24.0
        if (context.bgIsDefined) frameGrabber = FrameGrabber(context, fps.toInt())
        if (context.animatedLayers.isNotEmpty()) animator = LayerAnimator(context)
    }

    suspend fun start() {
        hookService.updateStatus("RENDERING")
        while (TaskManager.isRunning(context.id)) {

            if (currentPoint < totalPoints) {

                clear()
                drawGrabbed()
                drawDynamic()
                drawStatic()
                drawWaveforms()
                drawTracker()
                writeToOutput()
                increment()
                sleep()

            } else break
        }
        cleanup()
    }



    private fun clear() {
        g2d.clearRect(0, 0, w, h)
    }

    private suspend fun sleep() {
        withContext(Dispatchers.IO) { Thread.sleep(0) }
    }

    private fun increment() {
        currentPoint++
        index++
    }

    private fun cleanup() {
        writer.close()
        writer.flush()
        Runtime.getRuntime().gc()
        System.gc()
        println("Render completed in ${(System.currentTimeMillis() - startTime) / 1000.0} secs")
        hookService.updateStatus("FINISHED")
        hookService.uploadVideo()
    }

    private fun writeToOutput() {
        if (context.meta.video.optimisation!!) {
            writer.encodeVideo(
                0,
                fastResizeImage(alphaBuffer, 0.5),
                ((1000000000.0 / fps) * index).roundToLong(),
                TimeUnit.NANOSECONDS
            )
        } else {
            writer.encodeVideo(
                0,
                alphaBuffer,
                ((1000000000.0 / fps) * index).roundToLong(),
                TimeUnit.NANOSECONDS
            )
        }
        alphaBuffer.flush()
    }

    // Draw functions
    private fun drawStatic() {
        g2d.drawRenderedImage(staticImage, null)
    }

    private fun drawDynamic() {
        animator?.render(g2d)
        effectsManager.render(freqAmpData, currentPoint, g2d)
    }

    private fun drawGrabbed() {
        frameGrabber?.let { g2d.drawImage(it.grabNext(), null, bgX, bgY) }
    }

    private fun drawWaveforms() {
        for (plotter in plotters) {
            if (plotter.waveform.type == WaveformVariant.SAD)
                plotter.plot(sigAmpData, currentPoint, g2d)
            else
                plotter.plot(freqAmpData, currentPoint, g2d)
        }
    }

    private fun drawTracker() {
        val trackProgress = (currentPoint / totalPoints.toDouble()) * 100

        if (trackProgress.roundToInt() != progress) {
            println("task with id:${context.id} at $progress%")
            progress = trackProgress.roundToInt()
        }
        if (context.meta.tracker.display!!)
            renderTrackProgress(trackProgress, g2d, context.meta.tracker)
    }

}
