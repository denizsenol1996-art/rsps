package com.varlamore

import dev.openrune.cache.tools.Builder
import dev.openrune.cache.tools.tasks.TaskType
import io.xeros.AssetLoader

object BuildCache {
    fun init() {
        val builder = Builder(type = TaskType.BUILD, revision = REV, AssetLoader.getFolder("cache"))
        builder.extraTasks(*tasks).build().initialize()
    }

}

fun main(args: Array<String>) {
    AssetLoader.initCache()
    BuildCache.init()
}
