package com.shockwave.decompiler.fourcc.fcdr

import com.shockwave.decompiler.FCDRResource
import com.shockwave.decompiler.FourCCType

/**
 * @author Jordan Abraham
 */
data class Fcdr(
    val resources: Array<FCDRResource>
) : FourCCType