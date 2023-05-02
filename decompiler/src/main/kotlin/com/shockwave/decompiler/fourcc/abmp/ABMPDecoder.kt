package com.shockwave.decompiler.fourcc.abmp

import com.shockwave.decompiler.ABMPResource
import com.shockwave.decompiler.fourcc.FourCCDecoder
import com.shockwave.decompiler.fourcc.pack
import com.shockwave.decompiler.fourcc.unpack
import com.shockwave.decompiler.readVarInt
import com.shockwave.decompiler.zlibDecompress
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Jordan Abraham
 */
object ABMPDecoder : FourCCDecoder<ABMP>(
    code = charArrayOf('A', 'B', 'M', 'P')
) {
    override fun decode(buffer: ByteBuffer): ABMP {
        println("Start decoding ABMP.")

        // This should match the 'A','B','M','P'.
        val fourCC = buffer.int
        if (fourCC != code.pack()) {
            error("Expected ABMP but was ${fourCC.unpack()}.")
        }

        val length = buffer.readVarInt()
        val endPosition = buffer.position() + length
        buffer.readVarInt() // compressionType
        val decompressedLength = buffer.readVarInt()
        val decompressed = buffer.zlibDecompress(endPosition - buffer.position()).order(ByteOrder.LITTLE_ENDIAN)
        val decompressedLength2 = decompressed.capacity()

        if (decompressedLength != decompressedLength2) {
            error("ABMP Decompressed length mismatch. Was = $decompressedLength2, Expected = $decompressedLength")
        }

        decompressed.readVarInt()
        decompressed.readVarInt()
        val resourceLength = decompressed.readVarInt()

        if (resourceLength == 0) {
            return ABMP(emptyMap())
        }

        val resources = buildMap {
            repeat(resourceLength) {
                val abmpResourceId = decompressed.readVarInt()
                this[abmpResourceId] = ABMPResource(
                    id = abmpResourceId,
                    position = decompressed.readVarInt(),
                    compressedLength = decompressed.readVarInt(),
                    decompressedLength = decompressed.readVarInt(),
                    compressionType = decompressed.readVarInt(),
                    tag = decompressed.int
                )
            }

            if (decompressed.remaining() != 0) {
                error("ABMP Buffer has remaining bytes.")
            }

            if (this[2] == values.last()) {
                error("ABMP Map has no entry for ILS.")
            }
        }

        return ABMP(resources)
    }
}