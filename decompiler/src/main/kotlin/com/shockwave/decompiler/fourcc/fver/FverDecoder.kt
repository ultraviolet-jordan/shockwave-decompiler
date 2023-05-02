package com.shockwave.decompiler.fourcc.fver

import com.shockwave.decompiler.fourcc.FourCCDecoder
import com.shockwave.decompiler.fourcc.pack
import com.shockwave.decompiler.fourcc.unpack
import com.shockwave.decompiler.readBytes
import com.shockwave.decompiler.readUByte
import com.shockwave.decompiler.readVarInt
import java.nio.ByteBuffer

/**
 * @author Jordan Abraham
 */
object FverDecoder : FourCCDecoder<Fver>(
    code = charArrayOf('F', 'v', 'e', 'r')
) {
    override fun decode(buffer: ByteBuffer): Fver {
        println("Start decoding Fver.")

        // This should match the 'F','v','e','r'.
        val fourCC = buffer.int
        if (fourCC != code.pack()) {
            error("Expected Fver but was ${fourCC.unpack()}.")
        }

        val length = buffer.readVarInt()
        val startPosition = buffer.position()
        val version = buffer.readVarInt()
        val iMapVersion = buffer.readVarInt()
        val directorVersion = buffer.readVarInt()
        val versionString = buffer.readBytes(buffer.readUByte()).decodeToString()
        val endPosition = buffer.position()

        if (endPosition - startPosition != length) {
            error("Fver length mismatch. Was ${endPosition - startPosition}, expected $length")
        }

        return Fver(version, iMapVersion, directorVersion, versionString)
    }
}