package com.reeple.engine.renderer.components

import com.reeple.engine.renderer.core.FileManager
import com.reeple.engine.renderer.core.RenderContext
import khttp.structures.files.FileLike

class HooksManager(val context: RenderContext) {
    private val hooks = context.hooks

    fun updateStatus(status: String) {
        try {
            val res = khttp.put(
                url = hooks.updateHook!!,
                json = mapOf("status" to status)
            )
            println(res)
        } catch (e: Exception) {
            println("Failed to call update hook")
        }
    }

    fun uploadVideo() {
        try {
            val res = khttp.post(
                url = hooks.finishHook!!,
                files = listOf(
                    FileLike("video", FileManager.getExport(context.id))
                )
            )
            println(res)
        } catch (e: Exception) {
            println("Failed to call upload hook")
        }
    }

    fun callErrorHook(errorMsg: String) {
        try {
            val res = khttp.post(
                url = hooks.errorHook!!,
                json = mapOf("message" to errorMsg)
            )
            println(res)
        } catch (e: Exception) {
            println("Failed to call error hook")
        }

    }

}