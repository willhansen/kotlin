/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator

import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.annotation
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.block
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.controlFlowGraphReference
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.declaration
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.declarationStatus
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.expression
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.reference
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.typeParameter
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.typeParameterRef
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.typeProjection
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.typeRef
import org.jetbrains.kotlin.fir.tree.generator.FirTreeBuilder.konstueParameter
import org.jetbrains.kotlin.fir.tree.generator.context.type
import org.jetbrains.kotlin.fir.tree.generator.model.*

object FieldSets {
    konst calleeReference by lazy { field("calleeReference", reference, withReplace = true) }

    konst receivers by lazy {
        fieldSet(
            field("explicitReceiver", expression, nullable = true, withReplace = true).withTransform(),
            field("dispatchReceiver", expression, withReplace = true),
            field("extensionReceiver", expression, withReplace = true)
        )
    }

    konst typeArguments by lazy { fieldList("typeArguments", typeProjection, useMutableOrEmpty = true, withReplace = true) }

    konst arguments by lazy { fieldList("arguments", expression) }

    konst declarations by lazy { fieldList(declaration.withArgs("E" to "*")) }

    konst annotations by lazy {
        fieldList(
            "annotations",
            annotation,
            withReplace = true,
            useMutableOrEmpty = true
        ).withTransform(needTransformInOtherChildren = true)
    }

    fun symbolWithPackage(packageName: String?, symbolClassName: String, argument: String? = null): Field {
        return field("symbol", type(packageName, symbolClassName), argument)
    }

    fun symbol(symbolClassName: String, argument: String? = null): Field =
        symbolWithPackage("fir.symbols.impl", symbolClassName, argument)

    fun body(nullable: Boolean = false, withReplace: Boolean = false) =
        field("body", block, nullable, withReplace = withReplace)

    konst returnTypeRef =field("returnTypeRef", typeRef)

    konst typeRefField = field(typeRef, withReplace = true)

    konst konstueParameters by lazy { fieldList(konstueParameter) }

    konst typeParameters by lazy { fieldList("typeParameters", typeParameter) }

    konst typeParameterRefs by lazy { fieldList("typeParameters", typeParameterRef) }

    konst name by lazy { field(nameType) }

    konst initializer by lazy { field("initializer", expression, nullable = true) }

    fun superTypeRefs(withReplace: Boolean = false) = fieldList("superTypeRefs", typeRef, withReplace)

    konst classKind by lazy { field(classKindType) }

    konst status by lazy { field("status", declarationStatus, withReplace = true) }

    konst controlFlowGraphReferenceField by lazy { field("controlFlowGraphReference", controlFlowGraphReference, withReplace = true, nullable = true) }

    konst visibility by lazy { field(visibilityType) }

    konst effectiveVisibility by lazy { field("effectiveVisibility", effectiveVisibilityType) }

    konst modality by lazy { field(modalityType, nullable = true) }

    konst scopeProvider by lazy { field("scopeProvider", firScopeProviderType) }

    konst smartcastStability by lazy { field(smartcastStabilityType) }
}
