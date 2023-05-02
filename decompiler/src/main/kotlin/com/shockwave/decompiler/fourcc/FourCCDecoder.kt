package com.shockwave.decompiler.fourcc

import com.shockwave.decompiler.FourCCType
import java.nio.ByteBuffer

/**
 * @author Jordan Abraham
 */
abstract class FourCCDecoder<T : FourCCType>(
    val code: CharArray
) {
    abstract fun decode(buffer: ByteBuffer): T
}