package com.reeple.engine.renderer.components

import com.reeple.engine.renderer.core.FileManager
import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes
import ws.schild.jave.info.MultimediaInfo

sealed class AudioConverter {

    companion object {
        fun convert(path: String, isOptimized: Boolean?): String? {
            val source = FileManager.getResource(path)
            val target = FileManager.getResource(path.substringBefore(".") + "_converted.mp3")

            try {
                val bitrate = if (isOptimized !== null && isOptimized) 256000 else 512000
                val mmObject = MultimediaObject(source)
                val info: MultimediaInfo = mmObject.info
                val audio = AudioAttributes()
                audio.setCodec("libmp3lame")
                audio.setBitRate(bitrate)
                audio.setChannels(2)
                audio.setSamplingRate(44100)
                val attrs = EncodingAttributes()
                attrs.setInputFormat(info.format);
                attrs.setAudioAttributes(audio)
                val encoder = Encoder()
                encoder.encode(MultimediaObject(source), target, attrs, null)
                return target.absolutePath

            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("An error occurred while converting audio file:: ${e.message}");
            }
        }

    }
}