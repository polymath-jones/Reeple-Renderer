package com.reeple.engine.renderer.utils

import com.reeple.engine.renderer.core.FileManager
import org.springframework.core.io.Resource
import org.springframework.util.ResourceUtils
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File
import javax.servlet.http.Part
import kotlin.math.floor

fun readBytes(part: Part?): ByteArray {
    return part?.inputStream?.readBytes()!!
}

fun deriveImageUrl(imageName:String, id:String): String{
   return "${FileManager.ROOT}/tasks/task_${id}/resources/images/${imageName}"
}

inline fun <reified T> arraySampler(array: ArrayList<T>, sampleSize: Int): ArrayList<T> {

    if (sampleSize > array.size) {
        return array
    }
    val result: ArrayList<T> = ArrayList<T>()
    val totalItems = array.size
    val interval = totalItems.toDouble() / sampleSize

    for (i in 0 until sampleSize) {
        val evenIndex = floor(i * interval + interval / 2).toInt()
        result.add(array[evenIndex])
    }
    return result
}

fun arraySampler(array: FloatArray, sampleSize: Int): FloatArray {

    if (sampleSize > array.size) {
        return array
    }
    val result: FloatArray = FloatArray(sampleSize)
    val totalItems = array.size
    val interval = totalItems.toDouble() / sampleSize

    for (i in 0 until sampleSize) {
        val evenIndex = floor(i * interval + interval / 2).toInt()
        result[i] = (array[evenIndex])
    }
    return result
}

var bootsrapped = false
fun bootstrapApplication(fontResources: Array<Resource>?): Boolean {
    if(!bootsrapped){
        System.getProperties().setProperty("sun.java2d.opengl", "true")
        System.getProperties().setProperty("sun.java2d.accthreshold", "0");
        System.getProperties().setProperty("sun.java2d.renderer", "org.marlin.pisces.MarlinRenderingEngine");

        nu.pattern.OpenCV.loadShared();
        nu.pattern.OpenCV.loadLocally();

        try {
            var count = 0
            val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
            fontResources?.forEach {
                val font = it.inputStream;
                graphicsEnvironment.registerFont(Font.createFont(Font.TRUETYPE_FONT, font))
                count++
            }
            println("Loaded $count fonts")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        println("Bootstrapped application successfully")
        bootsrapped = true
        return true

    }
    return false
}

