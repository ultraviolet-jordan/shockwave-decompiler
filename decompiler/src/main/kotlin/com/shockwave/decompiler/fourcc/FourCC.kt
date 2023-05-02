package com.shockwave.decompiler.fourcc

/**
 * @author Jordan Abraham
 */
internal fun String.packFourCC(): Int {
    assert(length == 4)
    return get(0).code or (get(1).code shl 8) or (get(2).code shl 16) or (get(3).code shl 24)
}

internal fun Int.unpack(): String {
    val bytes = ByteArray(4)
    bytes[0] = (this shr 24).toByte()
    bytes[1] = (this shr 16).toByte()
    bytes[2] = (this shr 8).toByte()
    bytes[3] = toByte()
    return bytes.decodeToString()
}

internal fun CharArray.pack(): Int {
    if (size != 4) throw AssertionError("FourCC CharArray is not of length 4.")
    return get(0).code or (get(1).code shl 8) or (get(2).code shl 16) or (get(3).code shl 24)
}