/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.model

import org.jetbrains.kotlin.types.model.KotlinTypeMarker

interface PostponedResolvedAtomMarker {
    konst inputTypes: Collection<KotlinTypeMarker>
    konst outputType: KotlinTypeMarker?
    konst expectedType: KotlinTypeMarker?
    konst analyzed: Boolean
}

interface PostponedAtomWithRevisableExpectedType : PostponedResolvedAtomMarker {
    konst revisedExpectedType: KotlinTypeMarker?

    fun reviseExpectedType(expectedType: KotlinTypeMarker)
}

interface PostponedCallableReferenceMarker : PostponedAtomWithRevisableExpectedType

interface LambdaWithTypeVariableAsExpectedTypeMarker : PostponedAtomWithRevisableExpectedType {
    konst parameterTypesFromDeclaration: List<KotlinTypeMarker?>?

    fun updateParameterTypesFromDeclaration(types: List<KotlinTypeMarker?>?)
}