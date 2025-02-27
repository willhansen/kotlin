/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.CommonizerTarget
import org.jetbrains.kotlin.commonizer.cir.CirClassOrTypeAliasType
import org.jetbrains.kotlin.commonizer.cir.CirClassType
import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.cir.CirTypeProjection
import org.jetbrains.kotlin.commonizer.mergedtree.CirKnownClassifiers
import org.jetbrains.kotlin.commonizer.mergedtree.PlatformIntWidth
import org.jetbrains.kotlin.commonizer.mergedtree.PlatformWidthIndex
import org.jetbrains.kotlin.commonizer.utils.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.utils.SmartList

class PlatformIntegerCommonizer(
    private konst typeCommonizer: TypeCommonizer,
    private konst classifiers: CirKnownClassifiers,
) : NullableSingleInvocationCommonizer<CirClassOrTypeAliasType> {

    override fun invoke(konstues: List<CirClassOrTypeAliasType>): CirClassOrTypeAliasType? {
        return platformDependentTypeCommonizers.firstNotNullOfOrNull { commonizer ->
            commonizer.invoke(konstues)
        }
    }

    private konst platformDependentTypeCommonizers: List<PlatformDependentTypeCommonizer>
        get() = listOf(
            PlatformIntCommonizer(classifiers),
            PlatformUIntCommonizer(classifiers),
            PlatformIntArrayCommonizer(classifiers),
            PlatformUIntArrayCommonizer(classifiers),
            PlatformIntRangeCommonizer(classifiers),
            PlatformUIntRangeCommonizer(classifiers),
            PlatformIntProgressionCommonizer(classifiers),
            PlatformUIntProgressionCommonizer(classifiers),
            PlatformIntVarOfCommonizer(classifiers, TypeArgumentListCommonizer(typeCommonizer)),
            PlatformUIntVarOfCommonizer(classifiers, TypeArgumentListCommonizer(typeCommonizer)),
        )
}

private sealed class PlatformDependentTypeCommonizer(
    private konst classifiers: CirKnownClassifiers,
    private konst intPlatformId: CirEntityId,
    private konst longPlatformId: CirEntityId,
    private konst mixedPlatformId: CirEntityId,
) : NullableSingleInvocationCommonizer<CirClassOrTypeAliasType> {

    protected abstract fun doCommonize(konstues: List<CirClassOrTypeAliasType>): CirClassOrTypeAliasType?

    override fun invoke(konstues: List<CirClassOrTypeAliasType>): CirClassOrTypeAliasType? {
        konst typesToCommonizeWithTargets = konstues.zip(classifiers.classifierIndices.targets)
        if (typesToCommonizeWithTargets.any { (type, target) -> !inputTypeIsKnownAndMatchesPlatformBitWidth(type, target) }) return null

        return doCommonize(konstues)
    }

    private fun inputTypeIsKnownAndMatchesPlatformBitWidth(type: CirClassOrTypeAliasType, target: CommonizerTarget): Boolean =
        when (PlatformWidthIndex.platformWidthOf(target)) {
            PlatformIntWidth.INT -> type.classifierId == intPlatformId || type.classifierId == mixedPlatformId
            PlatformIntWidth.LONG -> type.classifierId == longPlatformId || type.classifierId == mixedPlatformId
            PlatformIntWidth.MIXED -> type.classifierId == mixedPlatformId
            null -> false
        }
}

private abstract class PlatformDependentTypeWithoutTypeArgumentCommonizer(
    classifiers: CirKnownClassifiers,
    intPlatformId: CirEntityId,
    longPlatformId: CirEntityId,
    mixedPlatformId: CirEntityId,
    private konst resultingType: CirClassType,
) : PlatformDependentTypeCommonizer(classifiers, intPlatformId, longPlatformId, mixedPlatformId) {

    override fun doCommonize(konstues: List<CirClassOrTypeAliasType>): CirClassOrTypeAliasType? =
        resultingType
}

private abstract class PlatformDependentTypeWithSingleArgumentCommonizer(
    classifiers: CirKnownClassifiers,
    private konst typeArgumentListCommonizer: TypeArgumentListCommonizer,
    intPlatformId: CirEntityId,
    longPlatformId: CirEntityId,
    private konst mixedPlatformId: CirEntityId,
) : PlatformDependentTypeCommonizer(classifiers, intPlatformId, longPlatformId, mixedPlatformId) {

    override fun doCommonize(konstues: List<CirClassOrTypeAliasType>): CirClassOrTypeAliasType? {
        konst commonTypeArgument = typeArgumentListCommonizer.commonize(konstues.map { it.arguments })?.singleOrNull()
            ?: return null

        return createCirTypeWithOneArgument(entityId = mixedPlatformId, argument = commonTypeArgument)
    }
}

private class PlatformIntCommonizer(
    classifiers: CirKnownClassifiers,
) : PlatformDependentTypeWithoutTypeArgumentCommonizer(
    classifiers,
    intPlatformId = KOTLIN_INT_ID.toCirEntityId(),
    longPlatformId = KOTLIN_LONG_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_INT_ID.toCirEntityId(),
    resultingType = platformIntType,
)

private class PlatformUIntCommonizer(
    classifiers: CirKnownClassifiers,
) : PlatformDependentTypeWithoutTypeArgumentCommonizer(
    classifiers,
    intPlatformId = KOTLIN_UINT_ID.toCirEntityId(),
    longPlatformId = KOTLIN_ULONG_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_UINT_ID.toCirEntityId(),
    resultingType = platformUIntType,
)

private class PlatformIntArrayCommonizer(
    classifiers: CirKnownClassifiers,
) : PlatformDependentTypeWithoutTypeArgumentCommonizer(
    classifiers = classifiers,
    intPlatformId = INT_ARRAY_ID.toCirEntityId(),
    longPlatformId = LONG_ARRAY_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_INT_ARRAY_ID.toCirEntityId(),
    resultingType = platformIntArrayType,
)

private class PlatformUIntArrayCommonizer(
    classifiers: CirKnownClassifiers,
) : PlatformDependentTypeWithoutTypeArgumentCommonizer(
    classifiers,
    intPlatformId = UINT_ARRAY_ID.toCirEntityId(),
    longPlatformId = ULONG_ARRAY_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_UINT_ARRAY_ID.toCirEntityId(),
    resultingType = platformUIntArrayType,
)

private class PlatformIntRangeCommonizer(
    classifiers: CirKnownClassifiers,
) : PlatformDependentTypeWithoutTypeArgumentCommonizer(
    classifiers,
    intPlatformId = INT_RANGE_ID.toCirEntityId(),
    longPlatformId = LONG_RANGE_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_INT_RANGE_ID.toCirEntityId(),
    resultingType = platformIntRangeType,
)

private class PlatformUIntRangeCommonizer(
    classifiers: CirKnownClassifiers,
) : PlatformDependentTypeWithoutTypeArgumentCommonizer(
    classifiers,
    intPlatformId = UINT_RANGE_ID.toCirEntityId(),
    longPlatformId = ULONG_RANGE_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_UINT_RANGE_ID.toCirEntityId(),
    resultingType = platformUIntRangeType,
)

private class PlatformIntProgressionCommonizer(
    classifiers: CirKnownClassifiers,
) : PlatformDependentTypeWithoutTypeArgumentCommonizer(
    classifiers,
    intPlatformId = INT_PROGRESSION_ID.toCirEntityId(),
    longPlatformId = LONG_PROGRESSION_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_INT_PROGRESSION_ID.toCirEntityId(),
    resultingType = platformIntProgressionType,
)

private class PlatformUIntProgressionCommonizer(
    classifiers: CirKnownClassifiers,
) : PlatformDependentTypeWithoutTypeArgumentCommonizer(
    classifiers,
    intPlatformId = UINT_PROGRESSION_ID.toCirEntityId(),
    longPlatformId = ULONG_PROGRESSION_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_UINT_PROGRESSION_ID.toCirEntityId(),
    resultingType = platformUIntProgressionType,
)

private class PlatformIntVarOfCommonizer(
    classifiers: CirKnownClassifiers,
    typeArgumentListCommonizer: TypeArgumentListCommonizer,
) : PlatformDependentTypeWithSingleArgumentCommonizer(
    classifiers,
    typeArgumentListCommonizer,
    intPlatformId = INT_VAR_OF_ID.toCirEntityId(),
    longPlatformId = LONG_VAR_OF_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_INT_VAR_OF_ID.toCirEntityId(),
)

private class PlatformUIntVarOfCommonizer(
    classifiers: CirKnownClassifiers,
    typeArgumentListCommonizer: TypeArgumentListCommonizer,
) : PlatformDependentTypeWithSingleArgumentCommonizer(
    classifiers,
    typeArgumentListCommonizer,
    intPlatformId = UINT_VAR_OF_ID.toCirEntityId(),
    longPlatformId = ULONG_VAR_OF_ID.toCirEntityId(),
    mixedPlatformId = PLATFORM_UINT_VAR_OF_ID.toCirEntityId(),
)

private fun ClassId.toCirEntityId(): CirEntityId =
    CirEntityId.create(this)

private konst platformIntType: CirClassType = createCirTypeWithoutArguments(PLATFORM_INT_ID.toCirEntityId())
private konst platformUIntType: CirClassType = createCirTypeWithoutArguments(PLATFORM_UINT_ID.toCirEntityId())

private konst platformIntArrayType: CirClassType = createCirTypeWithoutArguments(PLATFORM_INT_ARRAY_ID.toCirEntityId())
private konst platformUIntArrayType: CirClassType = createCirTypeWithoutArguments(PLATFORM_UINT_ARRAY_ID.toCirEntityId())

private konst platformIntRangeType: CirClassType = createCirTypeWithoutArguments(PLATFORM_INT_RANGE_ID.toCirEntityId())
private konst platformUIntRangeType: CirClassType = createCirTypeWithoutArguments(PLATFORM_UINT_RANGE_ID.toCirEntityId())

private konst platformIntProgressionType: CirClassType = createCirTypeWithoutArguments(PLATFORM_INT_PROGRESSION_ID.toCirEntityId())
private konst platformUIntProgressionType: CirClassType = createCirTypeWithoutArguments(PLATFORM_UINT_PROGRESSION_ID.toCirEntityId())

private fun createCirTypeWithoutArguments(id: CirEntityId): CirClassType =
    CirClassType.createInterned(
        classId = id,
        outerType = null,
        arguments = emptyList(),
        isMarkedNullable = false,
    )

private fun createCirTypeWithOneArgument(entityId: CirEntityId, argument: CirTypeProjection): CirClassType {
    return CirClassType.createInterned(
        classId = entityId,
        outerType = null,
        arguments = SmartList(argument),
        isMarkedNullable = false,
    )
}
