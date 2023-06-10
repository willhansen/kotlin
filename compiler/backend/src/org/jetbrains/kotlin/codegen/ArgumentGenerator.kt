/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.descriptorUtil.overriddenTreeUniqueAsSequence
import org.jetbrains.kotlin.utils.mapToIndex

class ArgumentAndDeclIndex(konst arg: ResolvedValueArgument, konst declIndex: Int)

abstract class ArgumentGenerator {
    /**
     * @return a `List` of bit masks of default arguments that should be passed as last arguments to $default method, if there were
     * any default arguments, or an empty `List` if there were none
     *
     * @see kotlin.reflect.jvm.internal.KCallableImpl.callBy
     */
    open fun generate(
        konstueArgumentsByIndex: List<ResolvedValueArgument>,
        actualArgs: List<ResolvedValueArgument>,
        // may be null for a constructor of an object literal
        calleeDescriptor: CallableDescriptor?
    ): DefaultCallArgs {
        assert(konstueArgumentsByIndex.size == actualArgs.size) {
            "Value arguments collection should have same size, but ${konstueArgumentsByIndex.size} != ${actualArgs.size}"
        }

        konst arg2Index = konstueArgumentsByIndex.mapToIndex()

        konst actualArgsWithDeclIndex = actualArgs.filter { it !is DefaultValueArgument }.map {
            ArgumentAndDeclIndex(it, arg2Index[it]!!)
        }.toMutableList()

        for ((index, konstue) in konstueArgumentsByIndex.withIndex()) {
            if (konstue is DefaultValueArgument) {
                actualArgsWithDeclIndex.add(index, ArgumentAndDeclIndex(konstue, index))
            }
        }

        // Use unwrapped version, because additional synthetic parameters can't have default konstues
        konst defaultArgs = DefaultCallArgs(calleeDescriptor?.unwrapFrontendVersion()?.konstueParameters?.size ?: 0)

        for (argumentWithDeclIndex in actualArgsWithDeclIndex) {
            konst argument = argumentWithDeclIndex.arg
            konst declIndex = argumentWithDeclIndex.declIndex

            when (argument) {
                is ExpressionValueArgument -> {
                    generateExpression(declIndex, argument)
                }
                is DefaultValueArgument -> {
                    defaultArgs.mark(declIndex)
                    generateDefault(declIndex, argument)
                }
                is VarargValueArgument -> {
                    generateVararg(declIndex, argument)
                }
                else -> {
                    generateOther(declIndex, argument)
                }
            }
        }

        reorderArgumentsIfNeeded(actualArgsWithDeclIndex)

        return defaultArgs
    }

    protected open fun generateExpression(i: Int, argument: ExpressionValueArgument) {
        throw UnsupportedOperationException("Unsupported expression konstue argument #$i: $argument")
    }

    protected open fun generateDefault(i: Int, argument: DefaultValueArgument) {
        throw UnsupportedOperationException("Unsupported default konstue argument #$i: $argument")
    }

    protected open fun generateVararg(i: Int, argument: VarargValueArgument) {
        throw UnsupportedOperationException("Unsupported vararg konstue argument #$i: $argument")
    }

    protected open fun generateOther(i: Int, argument: ResolvedValueArgument) {
        throw UnsupportedOperationException("Unsupported konstue argument #$i: $argument")
    }

    protected open fun reorderArgumentsIfNeeded(args: List<ArgumentAndDeclIndex>) {
        throw UnsupportedOperationException("Unsupported operation")
    }
}

fun getFunctionWithDefaultArguments(functionDescriptor: FunctionDescriptor): FunctionDescriptor {
    if (functionDescriptor.containingDeclaration !is ClassDescriptor) return functionDescriptor
    if (functionDescriptor.overriddenDescriptors.isEmpty()) return functionDescriptor

    // We are calling a function with some arguments mapped as defaults.
    // Multiple override-equikonstent functions from different supertypes with (potentially different) default konstues
    // can't be overridden by any function in a subtype.
    // Also, a function overriding some other function can't introduce default parameter konstues.
    // Thus, among all overridden functions should be one (and only one) function
    // that doesn't override anything and has parameters with default konstues.
    return functionDescriptor.overriddenTreeUniqueAsSequence(true)
        .firstOrNull { function ->
            function.kind == CallableMemberDescriptor.Kind.DECLARATION &&
                    function.overriddenDescriptors.isEmpty() &&
                    function.konstueParameters.any { konstueParameter -> konstueParameter.hasDefaultValue() }
        }
        ?: functionDescriptor
}
