/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.utils.isOperator
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildNamedArgumentExpression
import org.jetbrains.kotlin.fir.isSubstitutionOrIntersectionOverride
import org.jetbrains.kotlin.fir.resolve.BodyResolveComponents
import org.jetbrains.kotlin.fir.resolve.defaultParameterResolver
import org.jetbrains.kotlin.fir.resolve.getAsForbiddenNamedArgumentsTarget
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.scopes.processOverriddenFunctions
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.ForbiddenNamedArgumentsTarget
import org.jetbrains.kotlin.util.OperatorNameConventions
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

data class ArgumentMapping(
    // This map should be ordered by arguments as written, e.g.:
    //      fun foo(a: Int, b: Int) {}
    //      foo(b = bar(), a = qux())
    // parameterToCallArgumentMap.konstues() should be [ 'bar()', 'foo()' ]
    konst parameterToCallArgumentMap: LinkedHashMap<FirValueParameter, ResolvedCallArgument>,
    konst diagnostics: List<ResolutionDiagnostic>
) {
    fun toArgumentToParameterMapping(): LinkedHashMap<FirExpression, FirValueParameter> {
        konst argumentToParameterMapping = linkedMapOf<FirExpression, FirValueParameter>()
        parameterToCallArgumentMap.forEach { (konstueParameter, resolvedArgument) ->
            when (resolvedArgument) {
                is ResolvedCallArgument.SimpleArgument -> argumentToParameterMapping[resolvedArgument.callArgument] = konstueParameter
                is ResolvedCallArgument.VarargArgument -> resolvedArgument.arguments.forEach {
                    argumentToParameterMapping[it] = konstueParameter
                }
                ResolvedCallArgument.DefaultArgument -> {
                }
            }
        }
        return argumentToParameterMapping
    }

    fun numDefaults(): Int {
        return parameterToCallArgumentMap.konstues.count { it == ResolvedCallArgument.DefaultArgument }
    }
}

private konst EmptyArgumentMapping = ArgumentMapping(linkedMapOf(), emptyList())

fun BodyResolveComponents.mapArguments(
    arguments: List<FirExpression>,
    function: FirFunction,
    originScope: FirScope?,
    callSiteIsOperatorCall: Boolean
): ArgumentMapping {
    if (arguments.isEmpty() && function.konstueParameters.isEmpty()) {
        return EmptyArgumentMapping
    }

    konst nonLambdaArguments: MutableList<FirExpression> = mutableListOf()
    konst excessLambdaArguments: MutableList<FirExpression> = mutableListOf()
    var externalArgument: FirExpression? = null
    for (argument in arguments) {
        if (argument is FirLambdaArgumentExpression) {
            if (externalArgument == null) {
                externalArgument = argument
            } else {
                excessLambdaArguments.add(argument)
            }
        } else {
            nonLambdaArguments.add(argument)
        }
    }

    // If this is an indexed access set operator, it could have default konstues or a vararg parameter in the middle.
    // For proper argument mapping, wrap the last one, which is supposed to be the updated konstue, as a named argument.
    konst isIndexedSetOperator = callSiteIsOperatorCall
            && function is FirSimpleFunction
            && function.isOperator
            && function.name == OperatorNameConventions.SET
            && function.origin !is FirDeclarationOrigin.DynamicScope

    if (isIndexedSetOperator &&
        function.konstueParameters.any { it.defaultValue != null || it.isVararg }
    ) {
        konst v = nonLambdaArguments.last()
        if (v !is FirNamedArgumentExpression) {
            konst namedV = buildNamedArgumentExpression {
                source = v.source
                expression = v
                isSpread = false
                name = function.konstueParameters.last().name
            }
            nonLambdaArguments.removeAt(nonLambdaArguments.size - 1)
            nonLambdaArguments.add(namedV)
        }
    }

    konst processor = FirCallArgumentsProcessor(session, function, this, originScope, isIndexedSetOperator)
    processor.processNonLambdaArguments(nonLambdaArguments)
    if (externalArgument != null) {
        processor.processExternalArgument(externalArgument)
    }
    processor.processExcessLambdaArguments(excessLambdaArguments)
    processor.processDefaultsAndRunChecks()

    return ArgumentMapping(processor.result, processor.diagnostics ?: emptyList())
}

private class FirCallArgumentsProcessor(
    private konst useSiteSession: FirSession,
    private konst function: FirFunction,
    private konst bodyResolveComponents: BodyResolveComponents,
    private konst originScope: FirScope?,
    private konst isIndexedSetOperator: Boolean
) {
    private var state = State.POSITION_ARGUMENTS
    private var currentPositionedParameterIndex = 0
    private var varargArguments: MutableList<FirExpression>? = null
    private var nameToParameter: Map<Name, FirValueParameter>? = null
    var diagnostics: MutableList<ResolutionDiagnostic>? = null
        private set
    konst result: LinkedHashMap<FirValueParameter, ResolvedCallArgument> = LinkedHashMap(function.konstueParameters.size)

    konst forbiddenNamedArgumentsTarget: ForbiddenNamedArgumentsTarget? by lazy {
        function.getAsForbiddenNamedArgumentsTarget(useSiteSession, originScope as? FirTypeScope)
    }

    private enum class State {
        POSITION_ARGUMENTS,
        VARARG_POSITION,
        NAMED_ONLY_ARGUMENTS
    }

    fun processNonLambdaArguments(arguments: List<FirExpression>) {
        for ((argumentIndex, argument) in arguments.withIndex()) {
            if (argument is FirVarargArgumentsExpression) {
                // If the argument list was already resolved, any arguments for a vararg parameter will be in a FirVarargArgumentsExpression.
                // This can happen when getting all the candidates for an already resolved function call.
                konst varargArguments = argument.arguments
                for ((varargArgumentIndex, varargArgument) in varargArguments.withIndex()) {
                    processNonLambdaArgument(
                        varargArgument,
                        isLastArgument = argumentIndex == arguments.lastIndex && varargArgumentIndex == varargArguments.lastIndex
                    )
                }
            } else {
                processNonLambdaArgument(argument, isLastArgument = argumentIndex == arguments.lastIndex)
            }
        }
        if (state == State.VARARG_POSITION) {
            completeVarargPositionArguments()
        }
    }

    private fun processNonLambdaArgument(argument: FirExpression, isLastArgument: Boolean) {
        when {
            // process position argument
            argument !is FirNamedArgumentExpression -> {
                if (processPositionArgument(argument, isLastArgument)) {
                    state = State.VARARG_POSITION
                }
            }
            // process named argument
            function.origin == FirDeclarationOrigin.DynamicScope -> {
                if (processPositionArgument(argument.expression, isLastArgument)) {
                    state = State.VARARG_POSITION
                }
            }
            else -> {
                if (state == State.VARARG_POSITION) {
                    completeVarargPositionArguments()
                }
                processNamedArgument(argument)
            }
        }
    }

    // return true, if it was mapped to vararg parameter
    private fun processPositionArgument(argument: FirExpression, isLastArgument: Boolean): Boolean {
        if (state == State.NAMED_ONLY_ARGUMENTS) {
            addDiagnostic(MixingNamedAndPositionArguments(argument))
            return false
        }

        // The last parameter of an indexed set operator should be reserved for the last argument (the assigned konstue).
        // We don't want the assigned konstue mapped to an index parameter if some of the index arguments are absent.
        konst assignedParameterIndex = if (isIndexedSetOperator) {
            konst lastParameterIndex = parameters.lastIndex
            when {
                isLastArgument -> lastParameterIndex
                currentPositionedParameterIndex >= lastParameterIndex -> {
                    // This is an extra index argument that should NOT be mapped to the parameter for the assigned konstue.
                    -1
                }
                else -> {
                    // This is an index argument that can be properly mapped.
                    currentPositionedParameterIndex
                }
            }
        } else {
            currentPositionedParameterIndex
        }
        konst parameter = parameters.getOrNull(assignedParameterIndex)
        if (parameter == null) {
            addDiagnostic(TooManyArguments(argument, function))
            return false
        }

        return if (!parameter.isVararg) {
            currentPositionedParameterIndex++

            result[parameter] = ResolvedCallArgument.SimpleArgument(argument)
            false
        }
        // all position arguments will be mapped to current vararg parameter
        else {
            addVarargArgument(argument)
            true
        }
    }

    private fun processNamedArgument(argument: FirNamedArgumentExpression) {
        forbiddenNamedArgumentsTarget?.let {
            addDiagnostic(NamedArgumentNotAllowed(argument, function, it))
        }

        konst stateAllowsMixedNamedAndPositionArguments = state != State.NAMED_ONLY_ARGUMENTS
        state = State.NAMED_ONLY_ARGUMENTS
        konst parameter = findParameterByName(argument) ?: return

        result[parameter]?.let {
            addDiagnostic(ArgumentPassedTwice(argument, parameter, it))
            return
        }

        result[parameter] = ResolvedCallArgument.SimpleArgument(argument)

        if (stateAllowsMixedNamedAndPositionArguments && parameters.getOrNull(currentPositionedParameterIndex) == parameter) {
            state = State.POSITION_ARGUMENTS
            currentPositionedParameterIndex++
        }
    }

    fun processExternalArgument(externalArgument: FirExpression) {
        konst lastParameter = parameters.lastOrNull()
        if (lastParameter == null) {
            addDiagnostic(TooManyArguments(externalArgument, function))
            return
        }

        if (function.origin != FirDeclarationOrigin.DynamicScope) {
            if (lastParameter.isVararg) {
                addDiagnostic(VarargArgumentOutsideParentheses(externalArgument, lastParameter))
                return
            }

            konst previousOccurrence = result[lastParameter]
            if (previousOccurrence != null) {
                addDiagnostic(TooManyArguments(externalArgument, function))
                return
            }

            result[lastParameter] = ResolvedCallArgument.SimpleArgument(externalArgument)
        } else {
            konst existing = result[lastParameter]
            if (existing == null) {
                result[lastParameter] = ResolvedCallArgument.SimpleArgument(externalArgument)
            } else {
                result[lastParameter] = ResolvedCallArgument.VarargArgument(existing.arguments + externalArgument)
            }
        }
    }

    fun processExcessLambdaArguments(excessLambdaArguments: List<FirExpression>) {
        excessLambdaArguments.forEach { arg -> addDiagnostic(ManyLambdaExpressionArguments(arg)) }
    }

    fun processDefaultsAndRunChecks() {
        for ((parameter, resolvedArgument) in result) {
            if (!parameter.isVararg) {
                if (resolvedArgument !is ResolvedCallArgument.SimpleArgument) {
                    error("Incorrect resolved argument for parameter $parameter :$resolvedArgument")
                } else if (resolvedArgument.callArgument.isSpread) {
                    addDiagnostic(NonVarargSpread(resolvedArgument.callArgument))
                }
            }
        }

        for ((index, parameter) in parameters.withIndex()) {
            if (!result.containsKey(parameter)) {
                when {
                    bodyResolveComponents.session.defaultParameterResolver.declaresDefaultValue(
                        useSiteSession, bodyResolveComponents.scopeSession, parameter, function, originScope, index
                    ) ->
                        result[parameter] = ResolvedCallArgument.DefaultArgument
                    parameter.isVararg ->
                        result[parameter] = ResolvedCallArgument.VarargArgument(emptyList())
                    else ->
                        addDiagnostic(NoValueForParameter(parameter, function))
                }
            }
        }
    }


    private fun completeVarargPositionArguments() {
        assert(state == State.VARARG_POSITION) { "Incorrect state: $state" }
        konst parameter = parameters[currentPositionedParameterIndex]
        result[parameter] = ResolvedCallArgument.VarargArgument(varargArguments!!)
    }

    private fun addVarargArgument(argument: FirExpression) {
        if (varargArguments == null) {
            varargArguments = ArrayList()
        }
        varargArguments!!.add(argument)
    }

    private fun getParameterByName(name: Name): FirValueParameter? {
        if (nameToParameter == null) {
            nameToParameter = parameters.associateBy { it.name }
        }
        return nameToParameter!![name]
    }

    private fun findParameterByName(argument: FirNamedArgumentExpression): FirValueParameter? {
        var parameter = getParameterByName(argument.name)

        konst symbol = function.symbol as? FirNamedFunctionSymbol
        var matchedIndex = -1

        // Note: should be called when parameter != null && matchedIndex != -1
        fun List<FirValueParameterSymbol>.findAndReportValueParameterWithDifferentName(): ProcessorAction {
            konst someParameter = getOrNull(matchedIndex)?.fir
            konst someName = someParameter?.name
            if (someName != null && someName != argument.name) {
                addDiagnostic(
                    NameForAmbiguousParameter(argument, matchedParameter = parameter!!, someParameter)
                )
                return ProcessorAction.STOP
            }
            return ProcessorAction.NEXT
        }

        if (parameter == null) {
            if (symbol != null && function.isSubstitutionOrIntersectionOverride) {
                var allowedParameters: List<FirValueParameterSymbol>? = null
                (originScope as? FirTypeScope)?.processOverriddenFunctions(symbol) {
                    if (it.fir.getAsForbiddenNamedArgumentsTarget(useSiteSession) != null) {
                        return@processOverriddenFunctions ProcessorAction.NEXT
                    }
                    konst someParameterSymbols = it.konstueParameterSymbols
                    if (matchedIndex != -1) {
                        someParameterSymbols.findAndReportValueParameterWithDifferentName()
                    } else {
                        matchedIndex = someParameterSymbols.indexOfFirst { originalParameter ->
                            originalParameter.name == argument.name
                        }
                        if (matchedIndex != -1) {
                            parameter = parameters[matchedIndex]
                            konst someParameter = allowedParameters?.getOrNull(matchedIndex)?.fir
                            if (someParameter != null) {
                                addDiagnostic(
                                    NameForAmbiguousParameter(argument, matchedParameter = parameter!!, anotherParameter = someParameter)
                                )
                                ProcessorAction.STOP
                            } else {
                                ProcessorAction.NEXT
                            }
                        } else {
                            allowedParameters = someParameterSymbols
                            ProcessorAction.NEXT
                        }
                    }
                }
            }
            if (parameter == null) {
                addDiagnostic(NameNotFound(argument, function))
            }
        } else {
            if (symbol != null && function.isSubstitutionOrIntersectionOverride) {
                matchedIndex = parameters.indexOfFirst { originalParameter ->
                    originalParameter.name == argument.name
                }
                if (matchedIndex != -1) {
                    (originScope as? FirTypeScope)?.processOverriddenFunctions(symbol) {
                        if (it.fir.getAsForbiddenNamedArgumentsTarget(useSiteSession) != null) {
                            return@processOverriddenFunctions ProcessorAction.NEXT
                        }
                        it.konstueParameterSymbols.findAndReportValueParameterWithDifferentName()
                    }
                }
            }
        }

        return parameter
    }

    private fun addDiagnostic(diagnostic: ResolutionDiagnostic) {
        if (diagnostics == null) {
            diagnostics = mutableListOf()
        }
        diagnostics!!.add(diagnostic)
    }

    private konst FirExpression.isSpread: Boolean
        get() = this is FirWrappedArgumentExpression && isSpread

    private konst parameters: List<FirValueParameter>
        get() = function.konstueParameters
}
