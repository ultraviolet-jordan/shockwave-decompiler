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
    fun decompile() {
        // Allows read of file resource up to 2GB.
        val buffer = ByteBuffer.wrap(resource.readBytes())

        // Read metadata.
        val metadataCC = buffer.int
        println("MetadataCC = $metadataCC")
        if (metadataCC == bitPackFourCC('X', 'F', 'I', 'R')) {
            println("Metadata: Set as little endian.")
            buffer.order(ByteOrder.LITTLE_ENDIAN)
        }

        val metadataLength = buffer.int
        println("Metadata Length = $metadataLength")
        val metadataCodec = buffer.int
        println("Metadata Codec = $metadataCodec")

        when (metadataCodec) {
            // Read memory map.
            bitPackFourCC('M', 'V', '9', '3'), bitPackFourCC('M', 'C', '9', '5') -> {
                println("Metadata Codec Read Memory Map")
            }

            // Read afterburner.
            bitPackFourCC('F', 'G', 'D', 'M'), bitPackFourCC('F', 'G', 'D', 'C') -> {
                println("Metadata Codec Read Afterburner")
                val fileVersion = buffer.int
                println("Metadata Codec Read Afterburner File Version = $fileVersion")
                if (fileVersion != bitPackFourCC('F', 'v', 'e', 'r')) {
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
                if (fcdr != bitPackFourCC('F', 'c', 'd', 'r')) {
                    throw IllegalStateException("Metadata FCDR mismatch. Was = $fcdr")
                }

                val fcdrLength = buffer.getVarInt()
                println("Metadata FCDR Length = $fcdrLength")

                val fcdrBuf = ByteBuffer.wrap(buffer.zlibDecompress().encodeToByteArray())
                fcdrBuf.order(ByteOrder.LITTLE_ENDIAN)

                val compressionTypeCount = fcdrBuf.short.toInt() and 0xffff
                println("Metadata FCDR Compression Type Count = $compressionTypeCount")
            }
            else -> throw IllegalStateException("Codec not found for $metadataCodec.")
        }
    }

    private fun bitPackFourCC(
        first: Char,
        second: Char,
        third: Char,
        fourth: Char
    ): Int = first.code or (second.code shl 8) or (third.code shl 16) or (fourth.code shl 24)
}