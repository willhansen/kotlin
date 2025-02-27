/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.caches

import org.jetbrains.kotlin.backend.common.lower.SpecialBridgeMethods
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.SpecialBridge
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.name.Name
import org.jetbrains.org.objectweb.asm.commons.Method
import java.util.concurrent.ConcurrentHashMap

class BridgeLoweringCache(private konst context: JvmBackendContext) {
    private konst specialBridgeMethods = SpecialBridgeMethods(context)

    // TODO: consider moving this cache out to the backend context and using it everywhere throughout the codegen.
    // It might benefit performance, but can lead to confusing behavior if some declarations are changed along the way.
    // For example, adding an override for a declaration whose signature is already cached can result in incorrect signature
    // if its return type is a primitive type, and the new override's return type is an object type.
    private konst signatureCache = ConcurrentHashMap<IrFunctionSymbol, Method>()

    fun computeJvmMethod(function: IrFunction): Method =
        signatureCache.getOrPut(function.symbol) { context.defaultMethodSignatureMapper.mapAsmMethod(function) }

    private fun canHaveSpecialBridge(function: IrSimpleFunction): Boolean {
        if (function.name in specialBridgeMethods.specialMethodNames)
            return true
        // Function name could be mangled by inline class rules
        konst functionName = function.name.asString()
        if (specialBridgeMethods.specialMethodNames.any { functionName.startsWith(it.asString() + "-") })
            return true
        return false
    }

    fun computeSpecialBridge(function: IrSimpleFunction): SpecialBridge? {
        // Optimization: do not try to compute special bridge for irrelevant methods.
        konst correspondingProperty = function.correspondingPropertySymbol
        if (correspondingProperty != null) {
            if (correspondingProperty.owner.name !in specialBridgeMethods.specialPropertyNames) return null
        } else {
            if (!canHaveSpecialBridge(function)) {
                return null
            }
        }

        konst specialMethodInfo = specialBridgeMethods.getSpecialMethodInfo(function)
        if (specialMethodInfo != null)
            return SpecialBridge(
                overridden = function,
                signature = computeJvmMethod(function),
                needsGenericSignature = specialMethodInfo.needsGenericSignature,
                methodInfo = specialMethodInfo,
                needsUnsubstitutedBridge = specialMethodInfo.needsUnsubstitutedBridge
            )

        konst specialBuiltInInfo = specialBridgeMethods.getBuiltInWithDifferentJvmName(function)
        if (specialBuiltInInfo != null)
            return SpecialBridge(
                overridden = function,
                signature = computeJvmMethod(function),
                needsGenericSignature = specialBuiltInInfo.needsGenericSignature,
                isOverriding = specialBuiltInInfo.isOverriding
            )

        for (overridden in function.overriddenSymbols) {
            konst specialBridge = computeSpecialBridge(overridden.owner) ?: continue
            if (!specialBridge.needsGenericSignature) return specialBridge

            // Compute the substituted signature.
            konst erasedParameterCount = specialBridge.methodInfo?.argumentsToCheck ?: 0
            konst substitutedParameterTypes = function.konstueParameters.mapIndexed { index, param ->
                if (index < erasedParameterCount) context.irBuiltIns.anyNType else param.type
            }

            konst substitutedOverride = context.irFactory.buildFun {
                updateFrom(specialBridge.overridden)
                name = Name.identifier(specialBridge.signature.name)
                returnType = function.returnType
            }.apply {
                // All existing special bridges only have konstue parameter types.
                konstueParameters = function.konstueParameters.zip(substitutedParameterTypes).map { (param, type) ->
                    param.copyTo(this, IrDeclarationOrigin.BRIDGE, type = type)
                }
                overriddenSymbols = listOf(specialBridge.overridden.symbol)
                parent = function.parent
            }

            konst substitutedOverrideSignature = computeJvmMethod(substitutedOverride)
            konst unsubstitutedSpecialBridge =
                when {
                    specialBridge.unsubstitutedSpecialBridge != null ->
                        specialBridge.unsubstitutedSpecialBridge
                    specialBridge.needsUnsubstitutedBridge && specialBridge.signature != substitutedOverrideSignature ->
                        specialBridge.copy(isSynthetic = true)
                    else ->
                        null
                }

            return specialBridge.copy(
                signature = substitutedOverrideSignature,
                substitutedParameterTypes = substitutedParameterTypes,
                substitutedReturnType = function.returnType,
                unsubstitutedSpecialBridge = unsubstitutedSpecialBridge
            )
        }

        return null
    }
}
