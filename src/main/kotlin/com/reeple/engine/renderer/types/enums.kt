package com.reeple.engine.renderer.types

enum class WaveformDesign {
    DEFAULT, SPECTRAL_FLUX, ARC_REACTOR, SPECTROGRAM, MORPH_STACK, PELICAN_GRID, RAIN_BARS
}

enum class WaveformVariant {
    FAD, SAD
}

enum class MaskType {
    NONE, CIRCLE, SQUARE
}

enum class FrameType {
    NONE, THIN, NORMAL, SOLID
}

enum class FontWeight {
    THIN, NORMAL, BOLD
}

enum class FontStyle {
    ITALIC
}

enum class AudioGramSpacing {
    TIGHT, NORMAL, LOOSE
}

enum class ImageAlign {
    CENTER, LEFT, RIGHT
}

enum class ScaleMode {
    UP_SCALING, EQUI_SCALING, DOWN_SCALING
}

enum class TrackerType {
    HORIZONTAL_BAR
}

enum class EffectType {
    PARTICLE
}

enum class EffectMode {
    DEFAULT, ALPHA, BETA, GAMMA
}

enum class ShapeType {
    BOX, CIRCLE, LINE, SVG
}

enum class FilterType {
    NONE, SCREEN,
}

enum class ImageEffect {
    NONE, BLUR, JITTER, MONOCHROME
}

enum class FillMode {
    MONO, GRADIENT_MID, GRADIENT_LR, GRADIENT_RL, TRIAD
}

enum class AnimationDirection {
    FORWARD, REVERSE, CIRCLE
}

enum class AnimationInterpolation {
    LINEAR, EASE_IN, EASE_OUT
}

enum class BackgroundType {
    GIF, VIDEO
}