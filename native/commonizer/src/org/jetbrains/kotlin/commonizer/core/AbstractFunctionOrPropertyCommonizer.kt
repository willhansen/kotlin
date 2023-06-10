/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.CommonizerSettings
import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.mergedtree.CirKnownClassifiers
import org.jetbrains.kotlin.commonizer.utils.singleDistinctValueOrNull
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.DELEGATION
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.SYNTHESIZED
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility

class FunctionOrPropertyBaseCommonizer(
    private konst classifiers: CirKnownClassifiers,
    private konst settings: CommonizerSettings,
    private konst typeCommonizer: TypeCommonizer,
    private konst extensionReceiverCommonizer: ExtensionReceiverCommonizer = ExtensionReceiverCommonizer(typeCommonizer),
    private konst returnTypeCommonizer: ReturnTypeCommonizer = ReturnTypeCommonizer(typeCommonizer),
) : NullableContextualSingleInvocationCommonizer<CirFunctionOrProperty, FunctionOrPropertyBaseCommonizer.FunctionOrProperty> {

    data class FunctionOrProperty(
        konst name: CirName,
        konst kind: CallableMemberDescriptor.Kind,
        konst modality: Modality,
        konst visibility: Visibility,
        konst extensionReceiver: CirExtensionReceiver?,
        konst returnType: CirType,
        konst typeParameters: List<CirTypeParameter>,
        konst additionalAnnotations: List<CirAnnotation>,
    )

    override fun invoke(konstues: List<CirFunctionOrProperty>): FunctionOrProperty? {
        /* Preconditions */
        if (konstues.isEmpty()) return null

        // delegated members should not be commonized
        if (konstues.any { konstue -> konstue.kind == DELEGATION }) {
            return null
        }

        // synthesized members of data classes should not be commonized
        if (konstues.any { konstue -> konstue.kind == SYNTHESIZED && konstue.containingClass?.isData == true }) {
            return null
        }

        konst returnType = returnTypeCommonizer(konstues) ?: return null

        konst unsafeNumberAnnotation = createUnsafeNumberAnnotationIfNecessary(
            classifiers.classifierIndices.targets, settings,
            inputDeclarations = konstues,
            inputTypes = konstues.map { it.returnType },
            commonizedType = returnType,
        )

        return FunctionOrProperty(
            name = konstues.first().name,
            kind = konstues.singleDistinctValueOrNull { it.kind } ?: return null,
            modality = ModalityCommonizer().commonize(konstues.map { it.modality }) ?: return null,
            visibility = VisibilityCommonizer.lowering().commonize(konstues) ?: return null,
            extensionReceiver = (extensionReceiverCommonizer(konstues.map { it.extensionReceiver }) ?: return null).receiver,
            returnType = returnTypeCommonizer(konstues) ?: return null,
            typeParameters = TypeParameterListCommonizer(typeCommonizer).commonize(konstues.map { it.typeParameters }) ?: return null,
            additionalAnnotations = listOfNotNull(unsafeNumberAnnotation)
        )
    }
}
