package com.shockwave.decompiler

import com.shockwave.decompiler.fourcc.abmp.ABMPDecoder
import com.shockwave.decompiler.fourcc.fcdr.FcdrDecoder
import com.shockwave.decompiler.fourcc.fgei.FGEIDecoder
import com.shockwave.decompiler.fourcc.fver.FverDecoder
import com.shockwave.decompiler.fourcc.packFourCC
import com.shockwave.decompiler.fourcc.unpack
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Jordan Abraham
 */
class Decompiler(
    private val resource: File
) {
    fun decompile() {
        // Allows read of file resource up to 2GB.
        val buffer = ByteBuffer.wrap(resource.readBytes())

        // Read metadata.
        val metadataCC = buffer.int
        println("MetadataCC = $metadataCC")
        if (metadataCC == "XFIR".packFourCC()) {
            println("Metadata: Set as little endian.")
            buffer.order(ByteOrder.LITTLE_ENDIAN)
        }

        val metadataLength = buffer.int
        println("Metadata Length = $metadataLength")
        val metadataCodec = buffer.int
        println("Metadata Codec = $metadataCodec")

        when (metadataCodec) {
            // Read memory map.
            "MV93".packFourCC(), "MC95".packFourCC() -> {
                println("Metadata Codec Read Memory Map")
            }

            // Read afterburner.
            "FGDM".packFourCC(), "FGDC".packFourCC() -> {
                val fver = FverDecoder.decode(buffer)
                val fcdr = FcdrDecoder.decode(buffer)
                val abmp = ABMPDecoder.decode(buffer)
                val fgei = FGEIDecoder(abmp).decode(buffer)
                println("Finish Afterburner.")
            }
            else -> error("Codec not found for $metadataCodec.")
        }
    }
}