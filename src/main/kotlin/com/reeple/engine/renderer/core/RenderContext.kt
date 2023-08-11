package com.reeple.engine.renderer.core

import com.google.gson.Gson
import com.reeple.engine.renderer.components.AudioConverter
import com.reeple.engine.renderer.types.*
import com.reeple.engine.renderer.utils.*
import javax.servlet.http.Part

class RenderContext(parts: Collection<Part>) {

    // fields
    var id: String
        private set
    var hooks: HooksDto
        private set

    lateinit var audioUrl: String
    lateinit var meta: Meta
        private set
    lateinit var background: BackgroundDto
        private set

    val bgIsDefined
        get() = this::background.isInitialized
    // collections
    var staticLayers: ArrayList<Layer> = ArrayList()
        private set
    var animatedLayers: ArrayList<Layer> = ArrayList()
        private set
    var waveforms: ArrayList<WaveformDto> = ArrayList()
        private set
    var effects: ArrayList<EffectDto> = ArrayList()
        private set
    var animations: LinkedHashMap<String, AnimationModel> = LinkedHashMap()
        private set
    var trackLength: Double? = null

    init {
        try {
            val modelRaw = readBytes(parts.find { it -> it.name.substringBefore("_") == "model" })
            val model = Gson().fromJson(String(modelRaw), RenderModelDto().javaClass)

            if (model !== null) {
                this.id = model.id!!
                this.hooks = model.hooks!!

                model.animations?.forEach { a -> this.animations[a.id!!] = a }
                model.images?.forEach { i ->
                    i.file = deriveImageUrl(i.file!!, this.id)
                    if (i.animated!!) this.animatedLayers.add(i) else this.staticLayers.add(i)
                }
                model.shapes?.forEach { s ->
                    if (s.animated!!) this.animatedLayers.add(s) else this.staticLayers.add(s)
                }
                model.texts?.forEach { t ->
                    if (t.animated!!) this.animatedLayers.add(t) else this.staticLayers.add(t)
                }

                model.background?.let {
                    it.file = FileManager.getBackgroundUrl(id, it.file!!)
                    this.background = it
                }
                model.effects?.let { this.effects.addAll(it) }
                model.waveforms?.let {
                    this.waveforms.addAll(it)
                    this.meta = model.meta!!
                }
                FileManager.setupTaskDirectories(this.id)

                parts.forEach {
                    if ("audio" in it.contentType || it.name == "audio")
                        this.audioUrl= FileManager.saveResource("audio", this.id, it)!!
                     else if ("video" in it.contentType || it.name == "background")
                            FileManager.saveResource("background", this.id, it)!!
                    else if ("image" in it.contentType || it.name == "image")
                            FileManager.saveResource("image", this.id, it)
                }
            } else throw Exception("missing render model")
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Missing files or invalid render model:  ${e.message}")
        }
    }
}
