/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import org.jetbrains.kotlin.types.Variance

data class CirTypeParameter(
    override konst annotations: List<CirAnnotation>,
    override konst name: CirName,
    konst isReified: Boolean,
    konst variance: Variance,
    konst upperBounds: List<CirType>
) : CirHasAnnotations, CirHasName
