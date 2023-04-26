package com.shockwave.decompiler

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Jordan Abraham
 */
class Decompiler(
    private val resource: File
) {
    private val abmpResources = mutableMapOf<Int, ABMPResource>()
    private val ilsResources = mutableMapOf<Int, ILSResource>()

    fun decompile() {
        // Allows read of file resource up to 2GB.
        val buffer = ByteBuffer.wrap(resource.readBytes())

        // Read metadata.
        val metadataCC = buffer.int
        println("MetadataCC = $metadataCC")
        if (metadataCC == "XFIR".packString()) {
            println("Metadata: Set as little endian.")
            buffer.order(ByteOrder.LITTLE_ENDIAN)
        }

        val metadataLength = buffer.int
        println("Metadata Length = $metadataLength")
        val metadataCodec = buffer.int
        println("Metadata Codec = $metadataCodec")

        when (metadataCodec) {
            // Read memory map.
            "MV93".packString(), "MC95".packString() -> {
                println("Metadata Codec Read Memory Map")
            }

            // Read afterburner.
            "FGDM".packString(), "FGDC".packString() -> {
                println("Metadata Codec Read Afterburner")
                val fileVersion = buffer.int
                println("Metadata Codec Read Afterburner File Version = $fileVersion")
                if (fileVersion != "Fver".packString()) {
                    throw IllegalStateException("Metadata File Version mismatched. Found=$fileVersion")
                }
                val fverLength = buffer.getVarInt()
                val startPosition = buffer.position()
                println("Metadata FVER Length = $fverLength")
                val fverVersion = buffer.getVarInt()
                println("Metadata FVER Version = $fverVersion")

                if (fverVersion >= 1025) {
                    val iMapVersion = buffer.getVarInt()
                    println("Metadata iMapVersion = $iMapVersion")
                    val directorVersion = buffer.getVarInt()
                    println("Metadata Director Version = $directorVersion")
                }

                if (fverVersion >= 1281) {
                    val versionStringLength = buffer.get().toInt() and 0xff
                    println("Metadata Version String Length = $versionStringLength")
                    val fverVersionString = String(ByteArray(versionStringLength) { buffer.get() })
                    println("Metadata FVER Version String = $fverVersionString")
                }
                val endPosition = buffer.position()

                if (endPosition - startPosition != fverLength) {
                    throw IllegalStateException("Metadata FVER length mismatch. Was = ${endPosition - startPosition}, Expected = $fverLength")
                }

                val fcdr = buffer.int
                println("Metadata FCDR = $fcdr")
                if (fcdr != "Fcdr".packString()) {
                    throw IllegalStateException("Metadata FCDR mismatch. Was = $fcdr")
                }

                val fcdrLength = buffer.getVarInt()
                println("Metadata FCDR Length = $fcdrLength")

                val fcdrBuffer = buffer.zlibDecompress(fcdrLength)
                val fcdrDecompressedLength = fcdrBuffer.capacity()
                fcdrBuffer.order(ByteOrder.LITTLE_ENDIAN)

                val compressionTypeCount = fcdrBuffer.short.toInt() and 0xffff
                println("Metadata FCDR Compression Type Count = $compressionTypeCount")

                val fcdrResources = Array(compressionTypeCount) {
                    FCDRResource(
                        moaId1 = fcdrBuffer.int,
                        moaId2 = fcdrBuffer.short.toInt() and 0xffff,
                        moaId3 = fcdrBuffer.short.toInt() and 0xffff,
                        moaId4 = IntArray(8) { fcdrBuffer.get().toInt() and 0xff }
                    )
                }

                fcdrResources.forEach {
                    println("FCDR Resource = $it")
                }

                val compressionDescriptions = Array(compressionTypeCount) { fcdrBuffer.getCString() }

                // If fcdr remaining == 0
                if (fcdrBuffer.position() != fcdrDecompressedLength) {
                    throw IllegalStateException("Metadata FCDR decompressed buffer mismatch. Was = ${fcdrBuffer.position()}, Expected = $fcdrDecompressedLength")
                }

                compressionDescriptions.forEach {
                    println("Compression description = $it")
                }

                val abmp = buffer.int
                println("ABMP = $abmp")
                if (abmp != "ABMP".packString()) {
                    throw IllegalStateException("ABMP expected. Found = $abmp")
                }

                val abmpLength = buffer.getVarInt()
                println("ABMP Length = $abmpLength")
                val abmpEnd = buffer.position() + abmpLength
                println("ABMP End = $abmpEnd")
                val abmpCompressionType = buffer.getVarInt()
                println("ABMP Compression Type = $abmpCompressionType")
                val abmpDecompressedLength = buffer.getVarInt()
                println("ABMP Decompressed Length = $abmpDecompressedLength")
                val abmpBuffer = buffer.zlibDecompress(abmpEnd - buffer.position())
                val abmpDecompressedLength2 = abmpBuffer.capacity()

                if (abmpDecompressedLength != abmpDecompressedLength2) {
                    throw IllegalStateException("ABMP Decompressed length mismatch. Was = $abmpDecompressedLength2, Expected = $abmpDecompressedLength")
                }

                abmpBuffer.order(ByteOrder.LITTLE_ENDIAN)

                val abmp1 = abmpBuffer.getVarInt()
                println("ABMP1 = $abmp1")
                val abmp2 = abmpBuffer.getVarInt()
                println("ABMP2 = $abmp2")
                val abmpResourceLength = abmpBuffer.getVarInt()
                println("ABMP Resource Length = $abmpResourceLength")

                repeat(abmpResourceLength) {
                    val abmpResourceId = abmpBuffer.getVarInt()
                    abmpResources[abmpResourceId] = ABMPResource(
                        id = abmpResourceId,
                        position = abmpBuffer.getVarInt(),
                        compressedLength = abmpBuffer.getVarInt(),
                        decompressedLength = abmpBuffer.getVarInt(),
                        compressionType = abmpBuffer.getVarInt(),
                        tag = abmpBuffer.int
                    )
                }

                if (abmpBuffer.remaining() != 0) {
                    throw IllegalStateException("ABMP Buffer has remaining bytes.")
                }

                if (abmpResources[2] == abmpResources.values.last()) {
                    throw IllegalStateException("ABMP Map has no entry for ILS.")
                }

                val fgei = buffer.int
                if (fgei != "FGEI".packString()) {
                    throw IllegalStateException("FGEI expected. Found = $fgei")
                }

                val ilsInfo = abmpResources[2] ?: throw IllegalStateException("ILS Info not found.")
                val ils1 = buffer.getVarInt()
                println("ILS Length = ${ilsInfo.compressedLength}, ILS1 = $ils1")

                val ilsBuffer = buffer.zlibDecompress(ilsInfo.compressedLength)
                ilsBuffer.order(ByteOrder.LITTLE_ENDIAN)
                val ilsDecompressedLength = ilsBuffer.capacity()

                if (ilsDecompressedLength != ilsInfo.decompressedLength) {
                    throw IllegalStateException("ILS Decompressed Length mismatch. Was = $ilsDecompressedLength, Expected = ${ilsInfo.decompressedLength}")
                }

                while (ilsBuffer.hasRemaining()) {
                    val ilsResourceId = ilsBuffer.getVarInt()
                    val resource = abmpResources[ilsResourceId] ?: throw IllegalStateException("ILS ABMP resource not found.")
                    val bytes = ByteArray(resource.compressedLength) { ilsBuffer.get() }
                    val ilsResource = ILSResource(bytes)
                    ilsResources[ilsResourceId] = ilsResource
                    println("ILS Resource = ID = $ilsResourceId, Tag = ${resource.tag.unpackInt()}, Length = ${resource.compressedLength}, Bytes Length = ${bytes.size}, $ilsResource")
                }

                if (ilsBuffer.remaining() != 0) {
                    throw IllegalStateException("ILS Buffer has remaining bytes.")
                }

                println("Finish Afterburner.")
            }
            else -> throw IllegalStateException("Codec not found for $metadataCodec.")
        }
    }

    private fun String.packString(): Int {
        assert(length == 4)
        return get(0).code or (get(1).code shl 8) or (get(2).code shl 16) or (get(3).code shl 24)
    }

    private fun Int.unpackInt(): String {
        val bytes = ByteArray(4)
        bytes[0] = (this shr 24).toByte()
        bytes[1] = (this shr 16).toByte()
        bytes[2] = (this shr 8).toByte()
        bytes[3] = toByte()
        return String(bytes)
    }
}