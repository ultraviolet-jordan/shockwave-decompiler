package com.shockwave.decompiler.fourcc.abmp

import com.shockwave.decompiler.ABMPResource
import com.shockwave.decompiler.FourCCType

/**
 * @author Jordan Abraham
 */
data class ABMP(
    val resources: Map<Int, ABMPResource>
) : FourCCType