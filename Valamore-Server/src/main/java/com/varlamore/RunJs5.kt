package com.varlamore

import io.xeros.AssetLoader
import org.jire.js5server.Js5Server

object RunJs5 {

    fun init() {
        val ports = arrayOf(443, 43594, 50000)
        Js5Server.init(AssetLoader.getFolder("cache").absolutePath, ports.toIntArray(), version = 217, false)
    }
}
