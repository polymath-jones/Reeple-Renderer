package com.reeple.engine.controllers

import com.reeple.engine.renderer.core.FileManager
import com.reeple.engine.renderer.core.RenderContext
import com.reeple.engine.renderer.core.TaskManager
import com.reeple.engine.renderer.utils.bootstrapApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeType
import org.springframework.util.ResourceUtils
import org.springframework.web.bind.annotation.*
import java.awt.GraphicsEnvironment
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController()
class MainControllers {

    @Autowired
    private val request: HttpServletRequest? = null

    @Value("classpath:static/fonts/*")
    private val fontResources: Array<Resource>? = null

    @GetMapping("/fonts")
    fun geFonts(response: HttpServletResponse): ResponseEntity<Any>{
        val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val data = object {
            val fontFamilies = graphicsEnvironment.availableFontFamilyNames
        }
        return ResponseEntity(data,null,HttpStatus.OK);
    }

    @GetMapping("/video/{id}")
    @ResponseBody
    fun getVideoController(@PathVariable id: String, response: HttpServletResponse): ResponseEntity<FileSystemResource> {
        val vid = FileManager.getExport(id)
        val length = vid.length()

        val headers = HttpHeaders()
        headers.contentType = MediaType.asMediaType(MimeType.valueOf("video/mp4"))
        headers.contentLength = length
        headers.setContentDispositionFormData("attachment", "video_$id.mp4")

        return ResponseEntity(FileSystemResource(vid), headers, HttpStatus.OK)

    }

    @PutMapping("/cancel/{id}")
    @CrossOrigin
    fun cancelController(@PathVariable id: String): ResponseEntity<Any> {

        CoroutineScope(Dispatchers.IO).launch {
            TaskManager.cancel(id)
        }
        return ResponseEntity("canceled task with id:$id", HttpStatus.OK)
    }

    @PostMapping("/render")
    @CrossOrigin
    fun createNewRenderTask(): ResponseEntity<Any> {

        if (request !== null) {
            try {
                bootstrapApplication(fontResources);
                TaskManager.new(RenderContext(request.parts));

            } catch (e: Exception) {
                return ResponseEntity("Failed to initialize Render: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
            }
            return ResponseEntity("Render task has been successfully initialized", HttpStatus.OK)
        }
        return ResponseEntity("Failed to initialize Render", HttpStatus.BAD_REQUEST)

    }
}





