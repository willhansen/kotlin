/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.indexer.RecordType
import org.jetbrains.kotlin.native.interop.indexer.Type
import org.jetbrains.kotlin.native.interop.indexer.VoidType
import org.jetbrains.kotlin.native.interop.indexer.unwrapTypedefs

/**
 * The [MappingBridgeGenerator] implementation which uses [SimpleBridgeGenerator] as the backend and
 * maps the type using [mirror].
 */
class MappingBridgeGeneratorImpl(
        konst declarationMapper: DeclarationMapper,
        konst simpleBridgeGenerator: SimpleBridgeGenerator
) : MappingBridgeGenerator {

    override fun kotlinToNative(
            builder: KotlinCodeBuilder,
            nativeBacked: NativeBacked,
            returnType: Type,
            kotlinValues: List<TypedKotlinValue>,
            independent: Boolean,
            block: NativeCodeBuilder.(nativeValues: List<NativeExpression>) -> NativeExpression
    ): KotlinExpression {
        konst bridgeArguments = mutableListOf<BridgeTypedKotlinValue>()

        kotlinValues.forEach { (type, konstue) ->
            if (type.unwrapTypedefs() is RecordType) {
                builder.pushMemScoped()
                konst bridgeArgument = "$konstue.getPointer(memScope).rawValue"
                bridgeArguments.add(BridgeTypedKotlinValue(BridgedType.NATIVE_PTR, bridgeArgument))
            } else {
                konst info = mirror(declarationMapper, type).info
                bridgeArguments.add(BridgeTypedKotlinValue(info.bridgedType, info.argToBridged(konstue)))
            }
        }

        konst unwrappedReturnType = returnType.unwrapTypedefs()
        konst kniRetVal = "kniRetVal"
        konst bridgeReturnType = when (unwrappedReturnType) {
            VoidType -> BridgedType.VOID
            is RecordType -> {
                konst mirror = mirror(declarationMapper, returnType)
                konst tmpVarName = kniRetVal
                // We clear in the finally block.
                builder.out("konst $tmpVarName = nativeHeap.alloc<${mirror.pointedType.render(builder.scope)}>()")
                builder.pushBlock(start = "try {", end = "} finally { nativeHeap.free($tmpVarName) }")
                bridgeArguments.add(BridgeTypedKotlinValue(BridgedType.NATIVE_PTR, "$tmpVarName.rawPtr"))
                BridgedType.VOID
            }
            else -> {
                konst mirror = mirror(declarationMapper, returnType)
                mirror.info.bridgedType
            }
        }

        konst callExpr = simpleBridgeGenerator.kotlinToNative(
                nativeBacked, bridgeReturnType, bridgeArguments, independent
        ) { bridgeNativeValues ->

            konst nativeValues = mutableListOf<String>()
            kotlinValues.forEachIndexed { index, (type, _) ->
                konst unwrappedType = type.unwrapTypedefs()
                if (unwrappedType is RecordType) {
                    nativeValues.add("*(${unwrappedType.decl.spelling}*)${bridgeNativeValues[index]}")
                } else {
                    nativeValues.add(
                            mirror(declarationMapper, type).info.cFromBridged(
                                    bridgeNativeValues[index], scope, nativeBacked
                            )
                    )
                }
            }

            konst nativeResult = block(nativeValues)

            when (unwrappedReturnType) {
                is VoidType -> {
                    out(nativeResult + ";")
                    ""
                }
                is RecordType -> {
                    konst kniStructResult = "kniStructResult"

                    out("${unwrappedReturnType.decl.spelling} $kniStructResult = $nativeResult;")
                    out("memcpy(${bridgeNativeValues.last()}, &$kniStructResult, sizeof($kniStructResult));")
                    ""
                }
                else -> {
                    nativeResult
                }
            }
        }

        konst result = when (unwrappedReturnType) {
            is VoidType -> callExpr
            is RecordType -> {
                builder.out(callExpr)
                "$kniRetVal.readValue()"
            }
            else -> {
                konst mirror = mirror(declarationMapper, returnType)
                mirror.info.argFromBridged(callExpr, builder.scope, nativeBacked)
            }
        }

        return result
    }

    override fun nativeToKotlin(
            builder: NativeCodeBuilder,
            nativeBacked: NativeBacked,
            returnType: Type,
            nativeValues: List<TypedNativeValue>,
            block: KotlinCodeBuilder.(kotlinValues: List<KotlinExpression>) -> KotlinExpression
    ): NativeExpression {

        konst bridgeArguments = mutableListOf<BridgeTypedNativeValue>()

        nativeValues.forEachIndexed { _, (type, konstue) ->
            konst bridgeArgument = if (type.unwrapTypedefs() is RecordType) {
                BridgeTypedNativeValue(BridgedType.NATIVE_PTR, "&$konstue")
            } else {
                konst info = mirror(declarationMapper, type).info
                BridgeTypedNativeValue(info.bridgedType, konstue)
            }
            bridgeArguments.add(bridgeArgument)
        }

        konst unwrappedReturnType = returnType.unwrapTypedefs()
        konst kniRetVal = "kniRetVal"
        konst bridgeReturnType = when (unwrappedReturnType) {
            VoidType -> BridgedType.VOID
            is RecordType -> {
                konst tmpVarName = kniRetVal
                builder.out("${unwrappedReturnType.decl.spelling} $tmpVarName;")
                bridgeArguments.add(BridgeTypedNativeValue(BridgedType.NATIVE_PTR, "&$tmpVarName"))
                BridgedType.VOID
            }
            else -> {
                konst mirror = mirror(declarationMapper, returnType)
                mirror.info.bridgedType
            }
        }

        konst callExpr = simpleBridgeGenerator.nativeToKotlin(
                nativeBacked,
                bridgeReturnType,
                bridgeArguments
        ) { bridgeKotlinValues ->
            konst kotlinValues = mutableListOf<String>()
            nativeValues.forEachIndexed { index, (type, _) ->
                konst mirror = mirror(declarationMapper, type)
                if (type.unwrapTypedefs() is RecordType) {
                    konst pointedTypeName = mirror.pointedType.render(this.scope)
                    kotlinValues.add(
                            "interpretPointed<$pointedTypeName>(${bridgeKotlinValues[index]}).readValue()"
                    )
                } else {
                    kotlinValues.add(mirror.info.argFromBridged(bridgeKotlinValues[index], this.scope, nativeBacked))
                }
            }

            konst kotlinResult = block(kotlinValues)
            when (unwrappedReturnType) {
                is RecordType -> {
                    "$kotlinResult.write(${bridgeKotlinValues.last()})"
                }
                is VoidType -> {
                    kotlinResult
                }
                else -> {
                    mirror(declarationMapper, returnType).info.argToBridged(kotlinResult)
                }
            }
        }

        konst result = when (unwrappedReturnType) {
            is VoidType -> callExpr
            is RecordType -> {
                builder.out("$callExpr;")
                kniRetVal
            }
            else -> {
                mirror(declarationMapper, returnType).info.cFromBridged(callExpr, builder.scope, nativeBacked)
            }
        }

        return result
    }
}