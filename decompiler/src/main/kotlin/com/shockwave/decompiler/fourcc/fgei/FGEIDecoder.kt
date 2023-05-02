package com.shockwave.decompiler.fourcc.fgei

import com.shockwave.decompiler.ILSResource
import com.shockwave.decompiler.fourcc.FourCCDecoder
import com.shockwave.decompiler.fourcc.abmp.ABMP
import com.shockwave.decompiler.fourcc.pack
import com.shockwave.decompiler.fourcc.unpack
import com.shockwave.decompiler.readVarInt
import com.shockwave.decompiler.zlibDecompress
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Jordan Abraham
 */
class FGEIDecoder(
    val abmp: ABMP
) : FourCCDecoder<FGEI>(
    code = charArrayOf('F', 'G', 'E', 'I')
) {
    override fun decode(buffer: ByteBuffer): FGEI {
        println("Start decoding FGEI.")

        // This should match the 'F','G','E','I'.
        val fourCC = buffer.int
        if (fourCC != code.pack()) {
            error("Expected Fcdr but was ${fourCC.unpack()}.")
        }

        val ilsInfo = abmp.resources[2] ?: throw IllegalStateException("ILS Info not found.")
        buffer.readVarInt()

        val ilsBuffer = buffer.zlibDecompress(ilsInfo.compressedLength).order(ByteOrder.LITTLE_ENDIAN)
        val ilsDecompressedLength = ilsBuffer.capacity()

        if (ilsDecompressedLength != ilsInfo.decompressedLength) {
            throw IllegalStateException("ILS Decompressed Length mismatch. Was = $ilsDecompressedLength, Expected = ${ilsInfo.decompressedLength}")
        }

        val resources = buildMap {
            while (ilsBuffer.hasRemaining()) {
                val ilsResourceId = ilsBuffer.readVarInt()
                val resource = abmp.resources[ilsResourceId] ?: throw IllegalStateException("ILS ABMP resource not found.")
                val bytes = ByteArray(resource.compressedLength) { ilsBuffer.get() }
                val ilsResource = ILSResource(bytes)
                this[ilsResourceId] = ilsResource
                println("ILS Resource = ID = $ilsResourceId, Tag = ${resource.tag.unpack()}, Length = ${resource.compressedLength}, Bytes Length = ${bytes.size}, $ilsResource")
            }
        }

        if (ilsBuffer.remaining() != 0) {
            throw IllegalStateException("ILS Buffer has remaining bytes.")
        }

        return FGEI(resources)
    }
}