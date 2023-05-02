package com.shockwave.decompiler.fourcc.fver

import com.shockwave.decompiler.FourCCType

/**
 * @author Jordan Abraham
 */
data class Fver(
    val version: Int,
    val iMapVersion: Int,
    val directorVersion: Int,
    val versionString: String
) : FourCCType