package com.shockwave.decompiler

import java.nio.ByteBuffer
import java.util.zip.Inflater

/**
 * @author Jordan Abraham
 */
internal fun ByteBuffer.readUByte(): Int = get().toInt() and 0xff
internal fun ByteBuffer.readUShort(): Int = short.toInt() and 0xffff

internal fun ByteBuffer.readBytes(length: Int): ByteArray = ByteArray(length) { get() }

internal tailrec fun ByteBuffer.readVarInt(
    current: Int = get().toInt() and 0xff,
    result: Int = 0
): Int {
    val nextResult = (result shl 7) or (current and 0x7f)
    if (current shr 7 == 0) return nextResult
    return readVarInt(result = nextResult)
}

internal fun ByteBuffer.zlibDecompress(compressedLength: Int): ByteBuffer {
    val inflater = Inflater()
    inflater.setInput(this)
    val decompressed = ByteArray(compressedLength * 10) // initial size guess
    val length = inflater.inflate(decompressed)
    inflater.end()
    return ByteBuffer.wrap(decompressed.copyOf(length))
}

internal fun ByteBuffer.readStringNullTerminated(): String = String(readChars(untilZero())).also {
    discard(1)
}

internal fun ByteBuffer.discard(n: Int) {
    position(position() + n)
}

private fun ByteBuffer.readChars(n: Int): CharArray = CharArray(n) { get().toInt().toChar() }

private tailrec fun ByteBuffer.untilZero(length: Int = 0): Int {
    if (this[position() + length].toInt() == 0) return length
    return untilZero(length + 1)
}