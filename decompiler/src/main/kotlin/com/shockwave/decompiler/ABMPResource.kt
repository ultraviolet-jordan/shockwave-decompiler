package com.shockwave.decompiler

/**
 * @author Jordan Abraham
 */
data class ABMPResource(
    val id: Int,
    val position: Int,
    val compressedLength: Int,
    val decompressedLength: Int,
    val compressionType: Int,
    val tag: Int
)