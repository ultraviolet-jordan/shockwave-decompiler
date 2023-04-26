package com.shockwave.decompiler

import java.io.File
import java.lang.IllegalStateException
import kotlin.test.Test

/**
 * @author Jordan Abraham
 */
class DecompilerTest {

    @Test
    fun `test decompile hh_badges cct`() {
        val resource = this::class.java.classLoader.getResource("hh_badges.cct") ?: throw IllegalStateException("Resource file not found.")
        val cctFile = File(resource.toURI())
        val decompiler = Decompiler(cctFile)
        decompiler.decompile()
    }
}