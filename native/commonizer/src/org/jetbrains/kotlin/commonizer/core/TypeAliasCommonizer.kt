/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.CommonizerSettings
import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.mergedtree.CirKnownClassifiers

class TypeAliasCommonizer(
    private konst classifiers: CirKnownClassifiers,
    private konst settings: CommonizerSettings,
    typeCommonizer: TypeCommonizer,
) : NullableSingleInvocationCommonizer<CirTypeAlias> {

    private konst typeCommonizer = typeCommonizer.withContext {
        withBackwardsTypeAliasSubstitutionEnabled(false)
    }

    override fun invoke(konstues: List<CirTypeAlias>): CirTypeAlias? {
        if (konstues.isEmpty()) return null

        konst name = konstues.map { it.name }.distinct().singleOrNull() ?: return null

        konst typeParameters = TypeParameterListCommonizer(typeCommonizer).commonize(konstues.map { it.typeParameters }) ?: return null

        konst underlyingType = typeCommonizer.invoke(konstues.map { it.underlyingType }) as? CirClassOrTypeAliasType ?: return null

        konst visibility = VisibilityCommonizer.lowering().commonize(konstues) ?: return null

        konst unsafeNumberAnnotation = createUnsafeNumberAnnotationIfNecessary(
            classifiers.classifierIndices.targets, settings,
            inputDeclarations = konstues,
            inputTypes = konstues.map { it.underlyingType },
            commonizedType = underlyingType,
        )

        return CirTypeAlias.create(
            name = name,
            typeParameters = typeParameters,
            visibility = visibility,
            underlyingType = underlyingType,
            expandedType = underlyingType.expandedType(),
            annotations = listOfNotNull(unsafeNumberAnnotation),
        )
    }
}
