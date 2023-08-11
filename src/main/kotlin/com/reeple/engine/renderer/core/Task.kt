package com.reeple.engine.renderer.core

import com.reeple.engine.renderer.components.AudioConverter
import com.reeple.engine.renderer.components.HooksManager
import com.reeple.engine.renderer.utils.arraySampler
import com.reeple.engine.renderer.utils.external.classes.MediaWriterMod
import com.reeple.engine.renderer.utils.external.analysis.FFT
import com.xuggle.xuggler.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

class Task(private var context: RenderContext) {
    private val hookService = HooksManager(context);
    private var frameWidth = context.meta.video.width!!.toInt()
    private var frameHeight = context.meta.video.height!!.toInt()
    private var audioUrl: String = ""
    private var videoUrl: String = FileManager.createVideoContainer(context.id)
    private var writer = MediaWriterMod(videoUrl)
    private val isOptimized = context.meta.video.optimisation!!

    private val maxValue = 1.0f / java.lang.Short.MAX_VALUE
    private val size = 1024
    private val sampleRate = 44100f
    private val fft = FFT(size, sampleRate)

    private val monoSamples: FloatArray = FloatArray(size)
    private var freqAmpData: ArrayList<FloatArray> = ArrayList()
    private var sigAmpData: ArrayList<FloatArray> = ArrayList()
    private var smooth = FloatArray(6) { _ -> 1f }
    var fps = 30.0

    init {
        try {
            if (isOptimized) {
                fps = 24.0
                frameHeight = (frameHeight * 0.5).toInt()
                frameWidth = (frameWidth * 0.5).toInt()
            }

            val quality = context.meta.video.quality
            var bitrate = 5000000
            if (quality != null && quality <= 10) bitrate = quality * 1000000

            writer.addVideoStreamWithBitRate(
                    0,
                    0,
                    ICodec.ID.CODEC_ID_H264,
                    IRational.make(fps),
                    bitrate,
                    frameWidth,
                    frameHeight
            )
            writer.addAudioStream(1, 1, 2, 44100)

        } catch (e: Exception) {
            hookService.callErrorHook(e.message!!)
            throw Exception(e.message!!)
        }
    }

    suspend fun render() {
        try {
            audioUrl = AudioConverter.convert(context.audioUrl,isOptimized)!!
            decode()
            VideoRenderer(freqAmpData, sigAmpData, context, writer, hookService).start()
        } catch (e: Exception) {
            e.printStackTrace()
            hookService.callErrorHook(e.message!!)
        }
    }

    private fun decode() {
        hookService.updateStatus("CONVERTED_AUDIO")
        println("Successfully converted audio file");

        val ampData: ArrayList<ArrayList<Float>> = ArrayList()
        val audioContainer: IContainer = IContainer.make()
        audioContainer.open(this.audioUrl, IContainer.Type.READ, null)
        context.trackLength = audioContainer.duration / 1000000.0

        val stream = audioContainer.getStream(0)
        val coder: IStreamCoder = stream.streamCoder
        coder.open()

        val packet: IPacket = IPacket.make()

        for (i in 0..size / 2) {
            ampData.add(ArrayList<Float>())
        }
        val inputSamples = IAudioSamples.make(512, coder.channels.toLong(), IAudioSamples.Format.FMT_S32)
        while (audioContainer.readNextPacket(packet) >= 0) {

            var offset = 0
            while (offset < packet.size) {
                val bytesDecoded = coder.decodeAudio(inputSamples, packet, offset)
                if (bytesDecoded < 0) {
                    throw RuntimeException("could not detect audio")
                }
                offset += bytesDecoded
                if (inputSamples.isComplete) {


                    for (index in 0 until size) {
                        val amp1 = inputSamples.getSample(index.toLong(), 0, IAudioSamples.Format.FMT_S16) * maxValue
                        val amp2 = inputSamples.getSample(index.toLong(), 1, IAudioSamples.Format.FMT_S16) * maxValue
                        val monoAmp = (amp1 + amp2) / 2
                        monoSamples[index] = monoAmp
                    }


                    fft.forward(monoSamples)
                    var array = FloatArray(6)

                    array[0] = 10 * Math.log(fft.calcAvg(20f, 80f) * 1.0).toFloat() * 3
                    array[1] = 10 * Math.log(fft.calcAvg(80f, 200f) * 1.0).toFloat() * 3
                    array[2] = 10 * Math.log(fft.calcAvg(200f, 1000f) * 1.0).toFloat() * 3
                    array[3] = 20 * Math.log(fft.calcAvg(1000f, 2000f) * 1.0).toFloat() * 3
                    array[4] = 20 * Math.log(fft.calcAvg(2000f, 4000f) * 1.0).toFloat() * 3
                    array[5] = 20 * Math.log(fft.calcAvg(4000f, 20000f) * 1.0).toFloat() * 3

                    for (i in array.indices) {
                        smooth[i] = 0.35f * array[i] + 0.65f * smooth[i]
                        if (smooth[i] == Float.NEGATIVE_INFINITY) {
                            smooth[i] = 0f
                        }

                        if (smooth[i] < 0.0f) {
                            smooth[i] = 0f
                        }
                    }

                    freqAmpData.add(smooth.clone())
                    val array2 = arraySampler(monoSamples, 6)

                    for (i in array2.indices) {
                        val value = abs(array2[i] * 110)
                        smooth[i] = 0.35f * value + 0.65f * smooth[i]
                        if (smooth[i] == Float.NEGATIVE_INFINITY) {
                            smooth[i] = 0f
                        }

                        if (smooth[i] < 0.0f) {
                            smooth[i] = 0f
                        }
                    }
                    sigAmpData.add(smooth.clone())

                    writer.encodeAudio(1, inputSamples)
                }
            }
        }
        freqAmpData = arraySampler(freqAmpData, (fps * context.trackLength!!).roundToInt())
        sigAmpData = arraySampler(sigAmpData, (fps * context.trackLength!!).roundToInt())
        hookService.updateStatus("DECODED_AUDIO")
        println("Audio source decoded")
    }



}


