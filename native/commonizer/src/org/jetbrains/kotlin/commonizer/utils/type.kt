/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.utils

import kotlinx.metadata.*

internal inline konst KmTypeParameter.filteredUpperBounds: List<KmType>
    get() = upperBounds.takeUnless { it.singleOrNull()?.isNullableAny == true } ?: emptyList()

internal inline konst KmClass.filteredSupertypes: List<KmType>
    get() = supertypes.takeUnless { it.singleOrNull()?.isAny == true } ?: emptyList()

private inline konst KmType.isNullableAny: Boolean
    get() = (classifier as? KmClassifier.Class)?.name == ANY_CLASS_FULL_NAME && Flag.Type.IS_NULLABLE(flags)

private inline konst KmType.isAny: Boolean
    get() = (classifier as? KmClassifier.Class)?.name == ANY_CLASS_FULL_NAME && !Flag.Type.IS_NULLABLE(flags)




