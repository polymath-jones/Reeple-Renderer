package com.reeple.engine.renderer.types


import com.reeple.engine.renderer.utils.external.classes.TextAlignment
import kotlin.math.pow
import kotlin.math.roundToInt

class Meta {
    lateinit var video: Video
    lateinit var tracker: AudioTracker
}

class RenderModelDto {
    var id: String? = null
    var images: Array<ImageDto>? = null
    var texts: Array<TextDto>? = null
    var shapes: Array<ShapeDto>? = null
    var waveforms: Array<WaveformDto>? = null
    var animations: Array<AnimationModel>? = null
    var effects: Array<EffectDto>? = null
    var background: BackgroundDto? = null
    var meta: Meta? = null
    var hooks: HooksDto? = null
}

class HooksDto {
    var updateHook: String? = null
    var errorHook: String? = null
    var finishHook: String? = null
}

class BackgroundDto {
    var type: BackgroundType? = null
    var posX: Int? = null
    var posY: Int? = null
    var width: Double? = null
    var height: Double? = null
    var file: String? = null
}

class ShapeDto : Layer {
    var animated: Boolean? = null
    var animationModel: String? = null
    var shapeType: ShapeType? = null
    var posX: Double? = null
    var posY: Double? = null
    var width: Int? = null
    var height: Int? = null
    val fill: String? = null
    var svg: String? = null
    var outline: Boolean? = false
    var outlineWidth: Int? = null
    var outlineColor: String? = null
    override var zIndex: Int? = null
    var opacity: Int? = null
}

class ImageDto : Layer {
    var animated: Boolean? = null
    var animationModel: String? = null
    var file: String? = null
    var frame: FrameType? = null
    var frameColor: String? = null
    var width: Double? = null
    var height: Double? = null
    var mask: MaskType? = null
    var transform: String? = null
    var posX: Double? = null
    var posY: Double? = null
    override var zIndex: Int? = null
    var align: ImageAlign? = null
    var imageEffect: ImageEffect? = null
    var filter: FilterType? = null
    var filterFill: String? = null
    var opacity: Int? = null
}

class TextDto : Layer {
    var animated: Boolean? = null
    var animationModel: String? = null
    var value: String? = null
    var font: String? = null
    var fontSize: Int? = null
    var fontStyle: FontStyle? = null
    var fontWeight: FontWeight? = null
    var color: String? = null
    var posX: Double? = null
    var posY: Double? = null
    override var zIndex: Int? = null
    var align: TextAlignment? = null
    var width: Int? = null
    var spacing: Double? = null
    var opacity: Int? = null
}

class EffectDto {
    var effectType: EffectType? = null
    var effectMode: EffectMode? = null
    var posX: Int? = null
    var posY: Int? = null
    var width: Double? = null
    var height: Double? = null
    val fill: String? = null
}


class Video {
    val fill: String? = null
    var width: Double? = null
    var height: Double? = null
    var quality: Int? = null
    var optimisation: Boolean? = false
}

class WaveformDto {
    var type: WaveformVariant? = null
    var design: WaveformDesign? = null
    var fillMode: FillMode? = null
    var fill1: String? = null
    var fill2: String? = null
    var fill3: String? = null
    var stroke: Boolean? = null
    var strokeFill: String? = null
    var strokeWidth: Double? = null
    var strokeOpacity: Int? = null
    var width: Double? = null
    var height: Double? = null
    var posX: Double? = null
    var posY: Double? = null
    var opacity: Int? = null
}

class AnimationModel {
    var id: String? = null
    var posX: AnimationParameter? = null
    var posY: AnimationParameter? = null
    var opacity: AnimationParameter? = null
}

class AnimationParameter {
    var start: Double? = null
    var end: Double? = null
    var duration: Double? = null
    var delay: Double? = null
    var direction: AnimationDirection? = null
    var interpolation: AnimationInterpolation? = null

    private var frameRange = 0.0
    private var frameDelay = 0.0
    private var frameCount = 0
    private var absoluteProgress = 0.0
    private var range = 0.0
    private var foward = true
    private var fps = 30.0
    private var initiated = false

    private fun init(optimised: Boolean) {
        if (optimised) fps = 24.0
        frameDelay = (delay!! * fps) / 1000
        frameRange = ((duration!! * fps) / 1000)
        range = end!! - start!!
    }

    private fun easeIn(x: Double): Double {
        return x * x * x
    }

    private fun easeOut(x: Double): Double {
        return 1 - (1 - x).pow(3)
    }

    fun interpolate(optimised: Boolean): Double {
        if (!initiated) init(optimised).also { initiated = true }
        if (frameCount >= frameDelay) absoluteProgress = (frameCount - frameDelay) / frameRange
        if (frameCount >= (frameRange + frameDelay).roundToInt()) absoluteProgress = 1.0


        var value = 0.0
        when (direction) {
            AnimationDirection.FORWARD -> {
                when (interpolation) {
                    AnimationInterpolation.EASE_IN -> value = start!! + range * easeIn(absoluteProgress)
                    AnimationInterpolation.EASE_OUT -> value = start!! + range * easeOut(absoluteProgress)
                    AnimationInterpolation.LINEAR -> value = start!! + range * absoluteProgress
                }
            }
            AnimationDirection.REVERSE -> {
                when (interpolation) {
                    AnimationInterpolation.EASE_IN -> value = end!! - range * easeIn(absoluteProgress)
                    AnimationInterpolation.EASE_OUT -> value = end!! - range * easeOut(absoluteProgress)
                    AnimationInterpolation.LINEAR -> value = end!! - range * absoluteProgress
                }
            }
            AnimationDirection.CIRCLE -> {
                when (interpolation) {
                    AnimationInterpolation.EASE_IN -> {
                        if (foward) {
                            value = start!! + range * easeIn(absoluteProgress)
                            if (absoluteProgress == 1.0) {
                                foward = false
                                frameDelay = frameCount.toDouble()
                            }
                        } else {
                            value = end!! - range * easeIn(absoluteProgress)
                            if (absoluteProgress == 1.0) {
                                foward = true
                                frameDelay = frameCount.toDouble()
                            }
                        }
                    }
                    AnimationInterpolation.EASE_OUT -> {
                        if (foward) {
                            value = start!! + range * easeOut(absoluteProgress)
                            if (absoluteProgress == 1.0) {
                                foward = false
                                frameDelay = frameCount.toDouble()
                            }
                        } else {
                            value = end!! - range * easeOut(absoluteProgress)
                            if (absoluteProgress == 1.0) {
                                foward = true
                                frameDelay = frameCount.toDouble()
                            }
                        }
                    }
                    AnimationInterpolation.LINEAR -> {
                        if (foward) {
                            value = start!! + range * absoluteProgress
                            if (absoluteProgress == 1.0) {
                                foward = false
                                frameDelay = frameCount.toDouble()
                            }
                        } else {
                            value = end!! - range * absoluteProgress
                            if (absoluteProgress == 1.0) {
                                foward = true
                                frameDelay = frameCount.toDouble()
                            }
                        }
                    }

                }
            }

        }

        frameCount++
        return value
    }
}

class AudioTracker {
    var display: Boolean? = null
    var type: TrackerType? = null
    var fill: String? = null
    var posX: Double? = null
    var posY: Double? = null
    var opacity: Int? = null
    var length: Double? = null
}

class Point(x: Double, y: Double) {
    val x = x
    val y = y
}

interface Layer {
    var zIndex: Int?

}

