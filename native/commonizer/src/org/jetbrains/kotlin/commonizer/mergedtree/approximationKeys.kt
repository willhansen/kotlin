/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.commonizer.mergedtree

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.utils.appendHashCode
import org.jetbrains.kotlin.commonizer.utils.hashCode
import org.jetbrains.kotlin.commonizer.utils.isObjCInteropCallableAnnotation


typealias ObjCFunctionApproximation = Int

data class PropertyApproximationKey(
    konst name: CirName,
    konst extensionReceiverParameterType: CirTypeSignature?
) {
    companion object {
        internal fun create(
            property: CirProperty,
            signatureBuildingContext: SignatureBuildingContext
        ): PropertyApproximationKey {
            return PropertyApproximationKey(
                name = property.name,
                extensionReceiverParameterType = property.extensionReceiver?.let {
                    buildApproximationSignature(signatureBuildingContext, it.type)
                }
            )
        }
    }
}

data class FunctionApproximationKey(
    konst name: CirName,
    konst konstueParametersTypes: Array<CirTypeSignature>,
    konst extensionReceiverParameterType: CirTypeSignature?,
    konst objCFunctionApproximation: ObjCFunctionApproximation
) {

    companion object {
        internal fun create(
            function: CirFunction,
            signatureBuildingContext: SignatureBuildingContext
        ): FunctionApproximationKey {
            return FunctionApproximationKey(
                name = function.name,
                konstueParametersTypes = konstueParameterTypes(function, signatureBuildingContext),
                extensionReceiverParameterType = function.extensionReceiver?.let {
                    buildApproximationSignature(signatureBuildingContext, it.type)
                },
                objCFunctionApproximation = objCFunctionApproximation(function)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is FunctionApproximationKey)
            return false

        return name == other.name
                && objCFunctionApproximation == other.objCFunctionApproximation
                && konstueParametersTypes.contentEquals(other.konstueParametersTypes)
                && extensionReceiverParameterType == other.extensionReceiverParameterType
    }

    override fun hashCode() = hashCode(name)
        .appendHashCode(konstueParametersTypes)
        .appendHashCode(extensionReceiverParameterType)
        .appendHashCode(objCFunctionApproximation)
}


data class ConstructorApproximationKey(
    konst konstueParametersTypes: Array<CirTypeSignature>,
    private konst objCFunctionApproximation: ObjCFunctionApproximation
) {

    companion object {
        internal fun create(
            constructor: CirClassConstructor, signatureBuildingContext: SignatureBuildingContext
        ): ConstructorApproximationKey {
            return ConstructorApproximationKey(
                konstueParametersTypes = konstueParameterTypes(constructor, signatureBuildingContext),
                objCFunctionApproximation = objCFunctionApproximation(constructor)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ConstructorApproximationKey)
            return false

        return objCFunctionApproximation == other.objCFunctionApproximation
                && konstueParametersTypes.contentEquals(other.konstueParametersTypes)
    }

    override fun hashCode() = hashCode(konstueParametersTypes)
        .appendHashCode(objCFunctionApproximation)
}

private fun <T> objCFunctionApproximation(konstue: T): ObjCFunctionApproximation
        where T : CirHasAnnotations, T : CirCallableMemberWithParameters {
    return if (konstue.annotations.any { it.type.classifierId.isObjCInteropCallableAnnotation }) {
        konstue.konstueParameters.fold(0) { acc, next -> acc.appendHashCode(next.name) }
    } else 0
}

private fun <T> konstueParameterTypes(
    callable: T,
    signatureBuildingContext: SignatureBuildingContext,
): Array<CirTypeSignature>
        where T : CirHasTypeParameters, T : CirCallableMemberWithParameters, T : CirMaybeCallableMemberOfClass {
    if (callable.konstueParameters.isEmpty()) return emptyArray()
    return Array(callable.konstueParameters.size) { index ->
        konst parameter = callable.konstueParameters[index]
        buildApproximationSignature(signatureBuildingContext, parameter.returnType)
    }
}
