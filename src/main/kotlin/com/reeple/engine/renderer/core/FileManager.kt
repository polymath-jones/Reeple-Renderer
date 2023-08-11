package com.reeple.engine.renderer.core


import org.apache.tomcat.util.http.fileupload.FileUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.servlet.http.Part

sealed class FileManager {

    companion object {
        const val ROOT = "tmp"

        fun setupTaskDirectories(id: String) {

            val parentDir = "$ROOT/tasks/task_$id"
            val resourceDir = "$ROOT/tasks/task_$id/resources"
            val exportDir = "$ROOT/tasks/task_$id/export"
            val audioDir = "$ROOT/tasks/task_$id/resources/audio"
            val imagesDir = "$ROOT/tasks/task_$id/resources/images"
            val videoDir = "$ROOT/tasks/task_$id/resources/background"

            val taskDirectory = File(parentDir)
            if (taskDirectory.mkdirs()) {
                println("New task directory created with id:$id")
                val resourceDirectory = File(resourceDir)
                val exportDirectory = File(exportDir)

                if (resourceDirectory.mkdir()) {
                    val audioDirectory = File(audioDir)
                    val imagesDirectory = File(imagesDir)
                    val videoDirectory = File(videoDir)

                    audioDirectory.mkdir()
                    imagesDirectory.mkdir()
                    videoDirectory.mkdir()
                    println("New resource directory created in task directory with id:$id")
                } else {
                    println("Couldn't create resource directory in task directory with id:$id")
                }
                if (exportDirectory.mkdir()) {
                    println("New export directory created in task directory with id:$id")
                } else {
                    println("Couldn't create resource directory in task directory with id:$id")
                }
            } else {
                println("Couldn't create task directory with id:$id")
                throw Exception("Couldn't create task directory with id:$id")
            }
        }

        fun removeTaskDirectory(id: String) {

            try {
                val folder = File("$ROOT/tasks/task_$id")
                FileUtils.deleteDirectory(folder)
                println("Deleted task directory with id:$id")
            } catch (e: Exception) {
                println("folder does not exist")
                e.printStackTrace()

            }
        }

        fun createVideoContainer(id: String): String {
            val path = "$ROOT/tasks/task_$id/export/video.mp4"
            val videoContainer = File(path)
            videoContainer.createNewFile()
            return path
        }

        fun getResource(path: String?): File {
            return File(path)
        }

        fun getExportBytes(id: String): InputStream {
            return Files.newInputStream(Paths.get("$ROOT/tasks/task_$id/export/video.mp4"))
        }

        fun getExport(id: String): File {
            return File("$ROOT/tasks/task_$id/export/video.mp4")
        }

        fun getBackgroundUrl(id: String, file: String): String {
            return "$ROOT/tasks/task_$id/resources/background/${file}"
        }

        fun saveResource(type: String, id: String, resource: Part): String? {

            val filePath: Path
            try {
                when (type) {
                    "audio" -> {
                        val path = "$ROOT/tasks/task_$id/resources/audio"
                        filePath = Paths.get(path, "audio.${resource.submittedFileName.substringAfterLast(".")}")
                        try {
                            val os = Files.newOutputStream(filePath)
                            os.write(resource.inputStream.readBytes())
                            os.close()
                            return filePath.toString()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    "image" -> {
                        val path = "$ROOT/tasks/task_$id/resources/images"
                        filePath = Paths.get(path, resource.submittedFileName)
                        try {
                            val os = Files.newOutputStream(filePath)
                            os.write(resource.inputStream.readBytes())
                            os.close()
                            return filePath.toString()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    "background" -> {
                        val path = "$ROOT/tasks/task_$id/resources/background"
                        filePath = Paths.get(path, resource.submittedFileName)
                        try {
                            val os = Files.newOutputStream(filePath)
                            os.write(resource.inputStream.readBytes())
                            os.close()
                            return filePath.toString()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    else -> {
                        println("invalid resource type passed: $type")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }
}