/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.name.Name

interface FirTypeArgumentList {
    konst source: KtSourceElement?
    konst typeArguments: List<FirTypeProjection>
}

interface FirQualifierPart {
    konst source: KtSourceElement?
    konst name: Name
    konst typeArgumentList: FirTypeArgumentList
}
