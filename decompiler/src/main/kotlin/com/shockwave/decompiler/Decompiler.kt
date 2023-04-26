package com.shockwave.decompiler

import java.io.File
import java.nio.ByteBuffer

/**
 * @author Jordan Abraham
 */
class Decompiler(
    private val resource: File
) {
    fun decompile() {
        // Allows read of file resource up to 2GB.
        val buffer = ByteBuffer.wrap(resource.readBytes())
        println(buffer.capacity())
    }
}