package com.reeple.engine.renderer.components

import com.reeple.engine.renderer.core.RenderContext
import com.reeple.engine.renderer.types.ScaleMode
import com.reeple.engine.renderer.types.BackgroundType
import com.reeple.engine.renderer.utils.external.classes.GifDecoder
import com.reeple.engine.renderer.utils.fastResizeImage
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameUtils
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import kotlin.math.roundToInt


class FrameGrabber(context: RenderContext, fpsOut: Int) {

    private var data = context
    private var grabberCounter: Int = 0
    private var outputCounter: Int = 0
    private var currentFrameIndex: Int = 0

    private var fpsIn = 0
    private var type = context.background.type
    private var mode = ScaleMode.EQUI_SCALING

    private lateinit var gifDecoder: GifDecoder
    private lateinit var scaleArray: IntArray
    private lateinit var frameGrabber: FFmpegFrameGrabber
    private var outputImage: BufferedImage? = null


    init {

        when (type) {
            BackgroundType.GIF -> {
                gifDecoder = GifDecoder()
                val inputStream = FileInputStream(File(context.background.file!!))
                val out = gifDecoder.read(inputStream)
                fpsIn = (1000.0 / gifDecoder.getDelay(1)).roundToInt()

            }
            BackgroundType.VIDEO -> {

                frameGrabber = FFmpegFrameGrabber(File(context.background.file!!))
                frameGrabber.start()
                fpsIn = frameGrabber.videoFrameRate.roundToInt()

            }
        }
        when {
            fpsOut > fpsIn -> {
                mode = ScaleMode.UP_SCALING
                scaleArray = findIndexes(fpsOut, fpsIn)

            }
            fpsOut < fpsIn -> {
                mode = ScaleMode.DOWN_SCALING
                scaleArray = findIndexes(fpsIn, fpsOut)
            }
            else -> mode = ScaleMode.EQUI_SCALING
        }
    }

    private fun findIndexes(n: Int, r: Int): IntArray {

        val values = IntArray(n) { 0 }
        val offset = r - 1

        for (i in 0 until r) {
            val pos = (offset + (i * n)) / r
            values[pos % n] = 1
        }
        return values
    }

    fun grabNext(): BufferedImage? {
        when (type) {
            BackgroundType.GIF -> {
                if (grabberCounter == gifDecoder.frameCount) {
                    gifDecoder.restart()
                    grabberCounter = 0
                }

                when (mode) {
                    ScaleMode.DOWN_SCALING -> {
                        if (currentFrameIndex >= scaleArray.size - 1) {
                            currentFrameIndex = 0
                        }
                        for (i in currentFrameIndex until scaleArray.size) {
                            if (scaleArray[i] != 0) {
                                gifDecoder.grabImage()?.let { outputImage = it }
                                currentFrameIndex = i + 1
                                break
                            }
                        }
                    }
                    ScaleMode.UP_SCALING -> {
                        if (currentFrameIndex >= scaleArray.size - 1) {
                            currentFrameIndex = 0
                        }

                        if (scaleArray[currentFrameIndex] != 0) {
                            gifDecoder.grabImage()?.let { outputImage = it }
                            currentFrameIndex++
                            grabberCounter++

                        } else {
                            currentFrameIndex++
                        }
                    }
                    ScaleMode.EQUI_SCALING -> {
                        outputImage = gifDecoder.grabImage()
                        grabberCounter++
                    }
                }

            }
            BackgroundType.VIDEO -> {
                if (grabberCounter == frameGrabber.lengthInVideoFrames) {
                    frameGrabber.restart()
                    grabberCounter = 0
                }
                when (mode) {
                    ScaleMode.EQUI_SCALING -> {
                        val frame = frameGrabber.grabImage()
                        if (frame != null)
                            outputImage = Java2DFrameUtils.toBufferedImage(frame)
                        grabberCounter++
                    }
                    ScaleMode.DOWN_SCALING -> {

                        if (currentFrameIndex >= scaleArray.size - 1) {
                            currentFrameIndex = 0
                        }
                        for (i in currentFrameIndex until scaleArray.size) {

                            if (scaleArray[i] != 0) {
                                val frame = frameGrabber.grabImage()
                                if (frame != null)
                                    outputImage = Java2DFrameUtils.toBufferedImage(frame)
                                currentFrameIndex = i + 1
                                break
                            }
                            frameGrabber.grabImage()
                        }
                    }
                    ScaleMode.UP_SCALING -> {

                        if (currentFrameIndex >= scaleArray.size - 1) {
                            currentFrameIndex = 0
                        }

                        if (scaleArray[currentFrameIndex] != 0) {
                            val frame = frameGrabber.grabImage()
                            if (frame != null)
                                outputImage = Java2DFrameUtils.toBufferedImage(frame)
                            currentFrameIndex++
                            grabberCounter++

                        } else {
                            currentFrameIndex++
                        }
                    }
                }
            }
        }


        outputCounter++
        if(outputImage !== null)
        return fastResizeImage(outputImage!!, data.background.width!!, data.background.height!!)
        return null
    }



}