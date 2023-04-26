package com.shockwave.decompiler

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.Inflater

/**
 * @author Jordan Abraham
 */
tailrec fun ByteBuffer.getVarInt(
    current: Int = get().toInt() and 0xff,
    result: Int = 0
): Int {
    val nextResult = (result shl 7) or (current and 0x7f)
    if (current shr 7 == 0) return nextResult
    return getVarInt(result = nextResult)
}

fun ByteBuffer.zlibDecompress(): String {
    val inflater = Inflater()
    val outputStream = ByteArrayOutputStream()

    return outputStream.use {
        val buffer = ByteArray(1024)

        inflater.setInput(this)

        var count = -1
        while (count != 0) {
            count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }

        inflater.end()
        outputStream.toString()
    }
}