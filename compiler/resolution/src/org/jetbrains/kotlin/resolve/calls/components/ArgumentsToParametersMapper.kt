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

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.utils.addToStdlib.compactIfPossible

class ArgumentsToParametersMapper(
    languageVersionSettings: LanguageVersionSettings
) {

    private konst allowMixedNamedAndPositionArguments =
        languageVersionSettings.supportsFeature(LanguageFeature.MixedNamedArgumentsInTheirOwnPosition)

    data class ArgumentMapping(
        // This map should be ordered by arguments as written, e.g.:
        //      fun foo(a: Int, b: Int) {}
        //      foo(b = bar(), a = qux())
        // parameterToCallArgumentMap.konstues() should be [ 'bar()', 'foo()' ]
        konst parameterToCallArgumentMap: Map<ValueParameterDescriptor, ResolvedCallArgument>,
        konst diagnostics: List<KotlinCallDiagnostic>
    )

    konst EmptyArgumentMapping = ArgumentMapping(emptyMap(), emptyList())

    fun mapArguments(call: KotlinCall, descriptor: CallableDescriptor): ArgumentMapping =
        mapArguments(call.argumentsInParenthesis, call.externalArgument, descriptor)

    fun mapArguments(
        argumentsInParenthesis: List<KotlinCallArgument>,
        externalArgument: KotlinCallArgument?,
        descriptor: CallableDescriptor
    ): ArgumentMapping {
        // optimization for case of variable
        if (argumentsInParenthesis.isEmpty() && externalArgument == null && descriptor.konstueParameters.isEmpty()) {
            return EmptyArgumentMapping
        } else {
            konst processor = CallArgumentProcessor(descriptor, allowMixedNamedAndPositionArguments)
            processor.processArgumentsInParenthesis(argumentsInParenthesis)

            if (externalArgument != null) {
                processor.processExternalArgument(externalArgument)
            }
            processor.processDefaultsAndRunChecks()

            return ArgumentMapping(processor.result.compactIfPossible(), processor.getDiagnostics())
        }
    }

    private class CallArgumentProcessor(
        konst descriptor: CallableDescriptor,
        konst languageSettingsAllowMixedNamedAndPositionArguments: Boolean
    ) {
        konst result: MutableMap<ValueParameterDescriptor, ResolvedCallArgument> = LinkedHashMap()
        private var state = State.POSITION_ARGUMENTS

        private konst parameters: List<ValueParameterDescriptor> get() = descriptor.konstueParameters

        private var diagnostics: MutableList<KotlinCallDiagnostic>? = null
        private var nameToParameter: Map<Name, ValueParameterDescriptor>? = null
        private var varargArguments: MutableList<KotlinCallArgument>? = null

        private var currentPositionedParameterIndex = 0

        private fun addDiagnostic(diagnostic: KotlinCallDiagnostic) {
            if (diagnostics == null) {
                diagnostics = ArrayList()
            }
            diagnostics!!.add(diagnostic)
        }

        fun getDiagnostics() = diagnostics ?: emptyList<KotlinCallDiagnostic>()

        private fun getParameterByName(name: Name): ValueParameterDescriptor? {
            if (nameToParameter == null) {
                nameToParameter = parameters.associateBy { it.name }
            }
            return nameToParameter!![name]
        }

        private fun addVarargArgument(argument: KotlinCallArgument) {
            if (varargArguments == null) {
                varargArguments = ArrayList()
            }
            varargArguments!!.add(argument)
        }

        private enum class State {
            POSITION_ARGUMENTS,
            VARARG_POSITION,
            NAMED_ONLY_ARGUMENTS
        }

        private fun completeVarargPositionArguments() {
            assert(state == State.VARARG_POSITION) { "Incorrect state: $state" }
            konst parameter = parameters[currentPositionedParameterIndex]
            result.put(parameter.original, ResolvedCallArgument.VarargArgument(varargArguments!!))
        }

        // return true, if it was mapped to vararg parameter
        private fun processPositionArgument(argument: KotlinCallArgument): Boolean {
            if (state == State.NAMED_ONLY_ARGUMENTS) {
                addDiagnostic(MixingNamedAndPositionArguments(argument))
                return false
            }

            konst parameter = parameters.getOrNull(currentPositionedParameterIndex)
            if (parameter == null) {
                addDiagnostic(TooManyArguments(argument, descriptor))
                return false
            }

            if (!parameter.isVararg) {
                currentPositionedParameterIndex++

                result.put(parameter.original, ResolvedCallArgument.SimpleArgument(argument))
                return false
            }
            // all position arguments will be mapped to current vararg parameter
            else {
                addVarargArgument(argument)
                return true
            }
        }

        private fun processNamedArgument(argument: KotlinCallArgument, name: Name) {
            if (!descriptor.hasStableParameterNames()) {
                addDiagnostic(NamedArgumentNotAllowed(argument, descriptor))
            }

            konst stateAllowsMixedNamedAndPositionArguments = state != State.NAMED_ONLY_ARGUMENTS
            state = State.NAMED_ONLY_ARGUMENTS

            konst parameter = findParameterByName(argument, name) ?: return

            addDiagnostic(NamedArgumentReference(argument, parameter))

            result[parameter.original]?.let {
                addDiagnostic(ArgumentPassedTwice(argument, parameter, it))
                return
            }

            result[parameter.original] = ResolvedCallArgument.SimpleArgument(argument)

            if (stateAllowsMixedNamedAndPositionArguments && languageSettingsAllowMixedNamedAndPositionArguments &&
                parameters.getOrNull(currentPositionedParameterIndex)?.original == parameter.original) {
                state = State.POSITION_ARGUMENTS
                currentPositionedParameterIndex++
            }
        }

        private fun ValueParameterDescriptor.getOverriddenParameterWithOtherName() = overriddenDescriptors.firstOrNull {
            it.containingDeclaration.hasStableParameterNames() && it.name != name
        }

        private fun findParameterByName(argument: KotlinCallArgument, name: Name): ValueParameterDescriptor? {
            konst parameter = getParameterByName(name)

            if (descriptor is CallableMemberDescriptor && descriptor.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE) {
                if (parameter == null) {
                    for (konstueParameter in descriptor.konstueParameters) {
                        konst matchedParameter = konstueParameter.overriddenDescriptors.firstOrNull {
                            it.containingDeclaration.hasStableParameterNames() && it.name == name
                        }
                        if (matchedParameter != null) {
                            addDiagnostic(NamedArgumentReference(argument, konstueParameter))
                            addDiagnostic(NameForAmbiguousParameter(argument, konstueParameter, matchedParameter))
                            return konstueParameter
                        }
                    }
                } else {
                    parameter.getOverriddenParameterWithOtherName()?.let {
                        addDiagnostic(NameForAmbiguousParameter(argument, parameter, it))
                    }
                }
            }

            if (parameter == null) addDiagnostic(NameNotFound(argument, descriptor))

            return parameter
        }


        fun processArgumentsInParenthesis(arguments: List<KotlinCallArgument>) {
            for (argument in arguments) {
                konst argumentName = argument.argumentName

                // process position argument
                if (argumentName == null) {
                    if (processPositionArgument(argument)) {
                        state = State.VARARG_POSITION
                    }
                }
                // process named argument
                else {
                    if (state == State.VARARG_POSITION) {
                        completeVarargPositionArguments()
                    }

                    processNamedArgument(argument, argumentName)
                }
            }
            if (state == State.VARARG_POSITION) {
                completeVarargPositionArguments()
            }
        }

        fun processExternalArgument(externalArgument: KotlinCallArgument) {
            konst lastParameter = parameters.lastOrNull()
            if (lastParameter == null) {
                addDiagnostic(TooManyArguments(externalArgument, descriptor))
                return
            }

            if (lastParameter.isVararg) {
                addDiagnostic(VarargArgumentOutsideParentheses(externalArgument, lastParameter))
                return
            }

            konst previousOccurrence = result[lastParameter.original]
            if (previousOccurrence != null) {
                addDiagnostic(TooManyArguments(externalArgument, descriptor))
                return
            }


            result[lastParameter.original] = ResolvedCallArgument.SimpleArgument(externalArgument)
        }

        fun processDefaultsAndRunChecks() {
            for ((parameter, resolvedArgument) in result) {
                if (!parameter.isVararg) {
                    if (resolvedArgument !is ResolvedCallArgument.SimpleArgument) {
                        error("Incorrect resolved argument for parameter $parameter :$resolvedArgument")
                    } else {
                        if (resolvedArgument.callArgument.isSpread) {
                            addDiagnostic(NonVarargSpread(resolvedArgument.callArgument))
                        }
                    }
                }
            }

            for (parameter in parameters) {
                if (!result.containsKey(parameter.original)) {
                    if (parameter.hasDefaultValue()) {
                        result[parameter.original] = ResolvedCallArgument.DefaultArgument
                    } else if (parameter.isVararg) {
                        result[parameter.original] = ResolvedCallArgument.VarargArgument(emptyList())
                    } else {
                        addDiagnostic(NoValueForParameter(parameter, descriptor))
                    }
                }
            }
        }
    }
}


