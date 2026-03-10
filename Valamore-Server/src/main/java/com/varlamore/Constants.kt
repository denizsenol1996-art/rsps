package com.varlamore

import com.varlamore.custom.PackDats
import com.varlamore.custom.PackSprites
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.cache.tools.tasks.impl.PackMaps
import dev.openrune.cache.tools.tasks.impl.PackModels
import dev.openrune.cache.tools.tasks.impl.defs.PackObjects
import io.xeros.AssetLoader
import java.io.File


val REV : Int = 221

val tasks : Array<CacheTask> = arrayOf(
    PackSprites(AssetLoader.getFolder("raw-cache/sprites/")),
    PackDats(AssetLoader.getFolder("raw-cache/dats/")),
    PackModels(AssetLoader.getFolder("raw-cache/models/")),
    PackMaps(AssetLoader.getFolder("raw-cache/maps/"),AssetLoader.getFile("cache","xteas.json")),
    PackObjects(AssetLoader.getFolder("raw-cache/definitions/objects/"))
)