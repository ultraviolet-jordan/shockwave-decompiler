package com.shockwave.decompiler.fourcc.fgei

import com.shockwave.decompiler.FourCCType
import com.shockwave.decompiler.ILSResource

/**
 * @author Jordan Abraham
 */
data class FGEI(
    val resources: Map<Int, ILSResource>
) : FourCCType