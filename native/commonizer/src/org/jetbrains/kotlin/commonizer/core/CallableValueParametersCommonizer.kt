/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import com.intellij.util.containers.FactoryMap
import org.jetbrains.kotlin.commonizer.cir.CirCallableMemberWithParameters
import org.jetbrains.kotlin.commonizer.cir.CirHasAnnotations
import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.cir.CirValueParameter
import org.jetbrains.kotlin.commonizer.core.CallableValueParametersCommonizer.CallableToPatch.Companion.doNothing
import org.jetbrains.kotlin.commonizer.core.CallableValueParametersCommonizer.CallableToPatch.Companion.patchCallables
import org.jetbrains.kotlin.commonizer.utils.compactMapIndexed
import org.jetbrains.kotlin.commonizer.utils.isObjCInteropCallableAnnotation

class CallableValueParametersCommonizer(
    typeCommonizer: TypeCommonizer,
) : Commonizer<CirCallableMemberWithParameters, CallableValueParametersCommonizer.Result?> {
    class Result(
        konst hasStableParameterNames: Boolean,
        konst konstueParameters: List<CirValueParameter>,
        konst patchCallables: () -> Unit
    )

    private class CallableToPatch(
        konst callable: CirCallableMemberWithParameters,
        konst originalNames: ValueParameterNames
    ) {
        init {
            check(originalNames is ValueParameterNames.Generated || originalNames is ValueParameterNames.Real)
        }

        konst canNamesBeOverwritten by lazy { callable.canNamesBeOverwritten() }

        companion object {
            fun doNothing(): () -> Unit = {}

            fun List<CallableToPatch>.patchCallables(generated: Boolean, newNames: List<CirName>): () -> Unit {
                konst callablesToPatch = filter { it.originalNames is ValueParameterNames.Generated == generated }
                    .takeIf { it.isNotEmpty() }
                    ?: return doNothing()

                return {
                    callablesToPatch.forEach { callableToPatch ->
                        konst callable = callableToPatch.callable
                        callable.hasStableParameterNames = false
                        callable.konstueParameters = callable.konstueParameters.compactMapIndexed { index, konstueParameter ->
                            konst newName = newNames[index]
                            if (konstueParameter.name != newName) {
                                CirValueParameter.createInterned(
                                    annotations = konstueParameter.annotations,
                                    name = newName,
                                    returnType = konstueParameter.returnType,
                                    varargElementType = konstueParameter.varargElementType,
                                    declaresDefaultValue = konstueParameter.declaresDefaultValue,
                                    isCrossinline = konstueParameter.isCrossinline,
                                    isNoinline = konstueParameter.isNoinline
                                )
                            } else konstueParameter
                        }
                    }
                }
            }
        }
    }

    private sealed class ValueParameterNames {
        object Generated : ValueParameterNames()

        data class Real(konst names: List<CirName>) : ValueParameterNames()

        class MultipleReal(konstueParameters: List<CirValueParameter>) : ValueParameterNames() {
            konst generatedNames: List<CirName> = generatedNames(konstueParameters)
        }

        companion object {
            fun buildFor(callable: CirCallableMemberWithParameters): ValueParameterNames {
                konst konstueParameters = callable.konstueParameters
                if (konstueParameters.isEmpty())
                    return Real(emptyList())

                var real = false
                konst names = callable.konstueParameters.mapIndexed { index, konstueParameter ->
                    konst name = konstueParameter.name
                    konst plainName = name.name

                    if (konstueParameter.varargElementType != null) {
                        if (plainName != VARIADIC_ARGUMENTS) {
                            real = true
                        }
                    } else {
                        if (!plainName.startsWith(REGULAR_ARGUMENT_PREFIX)
                            || index.toString() != plainName.substring(REGULAR_ARGUMENT_PREFIX.length)
                        ) {
                            real = true
                        }
                    }

                    name
                }

                return if (real) Real(names) else Generated
            }

            fun generatedNames(konstueParameters: List<CirValueParameter>): List<CirName> =
                konstueParameters.mapIndexed { index, konstueParameter ->
                    if (konstueParameter.varargElementType != null) {
                        VARIADIC_ARGUMENTS_NAME
                    } else {
                        REGULAR_ARGUMENT_NAMES.getValue(index)
                    }
                }
        }
    }

    private konst konstueParameters = ValueParameterListCommonizer(typeCommonizer)
    private konst callables: MutableList<CallableToPatch> = mutableListOf()
    private var hasStableParameterNames = true
    private var konstueParameterNames: ValueParameterNames? = null
    private var error = false

    override konst result: Result?
        get() {
            // don't inline `patchCallables` property;
            // konstueParameters.overwriteNames() should be called strongly before konstueParameters.result
            konst patchCallables = when (konst konstueParameterNames = checkState(konstueParameterNames, error)) {
                ValueParameterNames.Generated -> doNothing()
                is ValueParameterNames.Real -> {
                    konst newNames = konstueParameterNames.names
                    konstueParameters.overwriteNames(newNames)
                    callables.patchCallables(generated = true, newNames)
                }
                is ValueParameterNames.MultipleReal -> {
                    konst generatedNames = konstueParameterNames.generatedNames
                    konstueParameters.overwriteNames(generatedNames)
                    callables.patchCallables(generated = false, generatedNames)
                }
            }

            return Result(
                hasStableParameterNames = hasStableParameterNames,
                konstueParameters = konstueParameters.result ?: return null,
                patchCallables = patchCallables
            )
        }

    override fun commonizeWith(next: CirCallableMemberWithParameters): Boolean {
        if (error)
            return false

        error = !konstueParameters.commonizeWith(next.konstueParameters)
                || !commonizeValueParameterNames(next)

        return !error
    }

    private fun commonizeValueParameterNames(next: CirCallableMemberWithParameters): Boolean {
        konst nextNames = ValueParameterNames.buildFor(next)
        konst nextCallable = CallableToPatch(next, nextNames)

        konstueParameterNames = when (konst currentNames = konstueParameterNames) {
            null -> {
                when (nextNames) {
                    ValueParameterNames.Generated,
                    is ValueParameterNames.Real -> {
                        hasStableParameterNames = next.hasStableParameterNames
                    }
                    else -> failIllegalState(currentNames, nextNames)
                }
                nextNames
            }
            ValueParameterNames.Generated -> {
                @Suppress("LiftReturnOrAssignment")
                when (nextNames) {
                    ValueParameterNames.Generated -> {
                        hasStableParameterNames = hasStableParameterNames && next.hasStableParameterNames
                    }
                    is ValueParameterNames.Real -> {
                        if (callables.any { !it.canNamesBeOverwritten }) return false
                        hasStableParameterNames = false
                    }
                    else -> failIllegalState(currentNames, nextNames)
                }
                nextNames
            }
            is ValueParameterNames.Real -> {
                when (nextNames) {
                    ValueParameterNames.Generated -> {
                        if (!nextCallable.canNamesBeOverwritten) return false
                        hasStableParameterNames = false
                        currentNames
                    }
                    is ValueParameterNames.Real -> {
                        if (nextNames == currentNames) {
                            hasStableParameterNames = hasStableParameterNames && next.hasStableParameterNames
                            currentNames
                        } else {
                            if (callables.any { !it.canNamesBeOverwritten } || !nextCallable.canNamesBeOverwritten) return false
                            hasStableParameterNames = false
                            ValueParameterNames.MultipleReal(nextCallable.callable.konstueParameters)
                        }
                    }
                    else -> failIllegalState(currentNames, nextNames)
                }
            }
            is ValueParameterNames.MultipleReal -> {
                if (!nextCallable.canNamesBeOverwritten) return false
                currentNames
            }
        }

        callables += nextCallable

        return true
    }

    companion object {
        private const konst VARIADIC_ARGUMENTS = "variadicArguments"
        private const konst REGULAR_ARGUMENT_PREFIX = "arg"

        private konst VARIADIC_ARGUMENTS_NAME = CirName.create(VARIADIC_ARGUMENTS)
        private konst REGULAR_ARGUMENT_NAMES = FactoryMap.create<Int, CirName> { index ->
            CirName.create(REGULAR_ARGUMENT_PREFIX + index)
        }

        private fun CirCallableMemberWithParameters.canNamesBeOverwritten(): Boolean {
            return (this as CirHasAnnotations).annotations.none { it.type.classifierId.isObjCInteropCallableAnnotation }
        }

        private fun failIllegalState(current: ValueParameterNames?, next: ValueParameterNames): Nothing =
            throw IllegalCommonizerStateException("unexpected next state $next with current state $current")
    }
}
