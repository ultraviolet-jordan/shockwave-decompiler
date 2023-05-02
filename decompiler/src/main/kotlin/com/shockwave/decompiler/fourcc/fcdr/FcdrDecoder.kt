package com.shockwave.decompiler.fourcc.fcdr

import com.shockwave.decompiler.FCDRResource
import com.shockwave.decompiler.fourcc.FourCCDecoder
import com.shockwave.decompiler.fourcc.fver.FverDecoder
import com.shockwave.decompiler.fourcc.pack
import com.shockwave.decompiler.fourcc.unpack
import com.shockwave.decompiler.readStringNullTerminated
import com.shockwave.decompiler.readUByte
import com.shockwave.decompiler.readUShort
import com.shockwave.decompiler.readVarInt
import com.shockwave.decompiler.zlibDecompress
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Jordan Abraham
 */
object FcdrDecoder : FourCCDecoder<Fcdr>(
    code = charArrayOf('F', 'c', 'd', 'r')
) {
    override fun decode(buffer: ByteBuffer): Fcdr {
        println("Start decoding Fcdr.")

        // This should match the 'F','c','d','r'.
        val fourCC = buffer.int
        if (fourCC != code.pack()) {
            error("Expected Fcdr but was ${fourCC.unpack()}.")
        }

        val decompressed = buffer.zlibDecompress(buffer.readVarInt()).order(ByteOrder.LITTLE_ENDIAN)
        val decompressedLength = decompressed.capacity()
        val compressedResources = decompressed.readUShort()

        val resources = Array(compressedResources) {
            FCDRResource(
                moaId1 = decompressed.int,
                moaId2 = decompressed.readUShort(),
                moaId3 = decompressed.readUShort(),
                moaId4 = IntArray(8) { decompressed.readUByte() }
            )
        }

        resources.forEach {
            println("Fcdr Resource = $it")
        }

        val compressionDescriptions = Array(compressedResources) { decompressed.readStringNullTerminated() }
        compressionDescriptions.forEach {
            println("Compression description = $it")
        }

        // If fcdr remaining == 0
        if (decompressed.position() != decompressedLength) {
            error("Fcdr decompressed buffer mismatch. Was = ${decompressed.position()}, Expected = $decompressedLength")
        }

        return Fcdr(resources)
    }
}