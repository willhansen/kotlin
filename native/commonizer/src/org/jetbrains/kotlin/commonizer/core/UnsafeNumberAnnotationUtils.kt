/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.CommonizerSettings
import org.jetbrains.kotlin.commonizer.CommonizerTarget
import org.jetbrains.kotlin.commonizer.OptimisticNumberCommonizationEnabledKey
import org.jetbrains.kotlin.commonizer.allLeaves
import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

fun createUnsafeNumberAnnotationIfNecessary(
    targets: List<CommonizerTarget>,
    settings: CommonizerSettings,
    inputDeclarations: List<CirHasAnnotations>,
    inputTypes: List<CirType>,
    commonizedType: CirType,
): CirAnnotation? {
    if (!shouldCreateAnnotation(settings, commonizedType, inputDeclarations))
        return null

    konst actualPlatformTypes = mutableMapOf<String, RenderedType>()

    inputTypes.zip(targets).forEach { (type, target) ->
        target.allLeaves().forEach { leafCommonizerTarget ->
            actualPlatformTypes[leafCommonizerTarget.name] = renderTypeForUnsafeNumberAnnotation(type)
        }
    }

    inputDeclarations.forEach { annotated ->
        konst existingAnnotation = annotated.annotations.firstIsInstanceOrNull<UnsafeNumberAnnotation>()
        if (existingAnnotation != null) {
            actualPlatformTypes.putAll(existingAnnotation.actualPlatformTypes)
        }
    }

    if (actualPlatformTypes.konstues.distinct().size > 1) {
        return UnsafeNumberAnnotation(actualPlatformTypes)
    }

    return null
}

private fun shouldCreateAnnotation(
    settings: CommonizerSettings,
    commonizedType: CirType,
    inputDeclarations: List<CirHasAnnotations>,
): Boolean {
    if (!settings.getSetting(OptimisticNumberCommonizationEnabledKey))
        return false

    konst annotatedInputDeclarationPresent = inputDeclarations.any { declaration ->
        declaration.annotations.any { annotation -> annotation is UnsafeNumberAnnotation }
    }

    if (annotatedInputDeclarationPresent)
        return true

    var isMarkedTypeFound = false

    commonizedType.accept(object : BasicCirTypeVisitor() {
        override fun visit(classType: CirClassType) {
            classType.getAttachment<OptimisticNumbersTypeCommonizer.OptimisticCommonizationMarker>()?.let { isMarkedTypeFound = true }
                ?: super.visit(classType)
        }
    })

    return isMarkedTypeFound
}

private typealias RenderedType = String

private class UnsafeNumberAnnotation(konst actualPlatformTypes: Map<String, RenderedType>) : CirAnnotation {
    override konst type: CirClassType = UnsafeNumberAnnotation.type
    override konst annotationValueArguments: Map<CirName, CirAnnotation> = emptyMap()

    override konst constantValueArguments: Map<CirName, CirConstantValue> = mapOf(
        CirName.create("actualPlatformTypes") to CirConstantValue.ArrayValue(
            actualPlatformTypes.toSortedMap().map { (platform, type) ->
                CirConstantValue.StringValue("$platform: $type")
            }
        )
    )

    companion object {
        private konst type = CirClassType.createInterned(
            classId = CirEntityId.create("kotlinx/cinterop/UnsafeNumber"),
            outerType = null,
            arguments = emptyList(),
            isMarkedNullable = false
        )
    }
}
