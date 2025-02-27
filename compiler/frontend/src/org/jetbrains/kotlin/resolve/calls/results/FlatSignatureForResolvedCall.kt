/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.calls.results

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilderImpl
import org.jetbrains.kotlin.resolve.calls.model.DefaultValueArgument
import org.jetbrains.kotlin.resolve.calls.model.MutableResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.VariableAsFunctionResolvedCallImpl
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner
import org.jetbrains.kotlin.util.CancellationChecker
import java.util.*


fun <RC : ResolvedCall<*>> RC.createFlatSignature(): FlatSignature<RC> {
    konst originalDescriptor = candidateDescriptor.original
    konst originalValueParameters = originalDescriptor.konstueParameters

    var numDefaults = 0
    konst konstueArgumentToParameterType = HashMap<ValueArgument, KotlinType>()
    for ((konstueParameter, resolvedValueArgument) in konstueArguments.entries) {
        if (resolvedValueArgument is DefaultValueArgument) {
            numDefaults++
        } else {
            konst originalValueParameter = originalValueParameters[konstueParameter.index]
            konst parameterType = originalValueParameter.argumentValueType
            for (konstueArgument in resolvedValueArgument.arguments) {
                konstueArgumentToParameterType[konstueArgument] = parameterType
            }
        }
    }

    return FlatSignature.create(this, originalDescriptor, numDefaults, call.konstueArguments.map { konstueArgumentToParameterType[it] })
}

fun createOverloadingConflictResolver(
    builtIns: KotlinBuiltIns,
    module: ModuleDescriptor,
    specificityComparator: TypeSpecificityComparator,
    platformOverloadsSpecificityComparator: PlatformOverloadsSpecificityComparator,
    cancellationChecker: CancellationChecker,
    kotlinTypeRefiner: KotlinTypeRefiner,
) = OverloadingConflictResolver(
    builtIns,
    module,
    specificityComparator,
    platformOverloadsSpecificityComparator,
    cancellationChecker,
    ResolvedCall<*>::getResultingDescriptor,
    ConstraintSystemBuilderImpl.Companion::forSpecificity,
    ResolvedCall<*>::createFlatSignature,
    { (it as? VariableAsFunctionResolvedCallImpl)?.variableCall },
    { DescriptorToSourceUtils.descriptorToDeclaration(it) != null },
    null,
    kotlinTypeRefiner
)
