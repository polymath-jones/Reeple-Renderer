package com.reeple.engine.renderer.core

import com.reeple.engine.renderer.utils.bootstrapApplication
import com.reeple.engine.renderer.utils.external.classes.CopyOnWriteLinkedHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class TaskManager {

    companion object {

        private const val MAX = 2
        private var tasks = CopyOnWriteLinkedHashMap<String, Task>()
            .also {
            CoroutineScope(Dispatchers.Default).launch {

                while (true) {
                    delay(10)
                    val parent = launch {
                        var count = 0
                        for (task in it) {
                            launch { task.value.render(); it.remove(task.key) }
                            count++
                            if (count >= MAX)
                                break
                        }
                    }
                    parent.join()
                }
            }
        }

        fun new(context: RenderContext) {
            tasks[context.id] = Task(context)
        }

        fun cancel(id: String) {
            tasks.remove(id)
        }

        fun isRunning(id: String): Boolean {
           return  tasks[id] !== null;
        }
    }
}