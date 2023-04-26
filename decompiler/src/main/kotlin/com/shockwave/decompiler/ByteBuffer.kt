package com.shockwave.decompiler

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

fun ByteBuffer.zlibDecompress(compressedLength: Int): ByteBuffer {
    val inflater = Inflater()
    inflater.setInput(this)
    val decompressed = ByteArray(compressedLength * 10) // initial size guess
    val length = inflater.inflate(decompressed)
    inflater.end()
    return ByteBuffer.wrap(decompressed.copyOf(length))
}

fun ByteBuffer.getCString(): String = buildString {
    var char = get().toInt()
    while (char != 0) {
        append(char.toChar())
        char = get().toInt()
    }
}