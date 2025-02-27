/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.types.FirQualifierPart
import org.jetbrains.kotlin.fir.types.FirTypeArgumentList
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.name.Name

class FirTypeArgumentListImpl(override konst source: KtSourceElement?) : FirTypeArgumentList {
    override konst typeArguments = mutableListOf<FirTypeProjection>()
}

class FirQualifierPartImpl(
    override konst source: KtSourceElement?,
    override konst name: Name,
    override konst typeArgumentList: FirTypeArgumentList
) : FirQualifierPart
