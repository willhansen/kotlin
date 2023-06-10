/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.inference

import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.resolve.DoubleColonLHS
import org.jetbrains.kotlin.fir.resolve.calls.Candidate
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.resolve.calls.model.LambdaWithTypeVariableAsExpectedTypeMarker
import org.jetbrains.kotlin.resolve.calls.model.PostponedCallableReferenceMarker
import org.jetbrains.kotlin.resolve.calls.model.PostponedResolvedAtomMarker
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.utils.addIfNotNull

//  --------------------------- Variables ---------------------------


//  -------------------------- Atoms --------------------------

sealed class PostponedResolvedAtom : PostponedResolvedAtomMarker {
    abstract konst atom: FirElement
    abstract override konst inputTypes: Collection<ConeKotlinType>
    abstract override konst outputType: ConeKotlinType?
    override var analyzed: Boolean = false
    abstract override konst expectedType: ConeKotlinType?
}

//  ------------- Lambdas -------------

class ResolvedLambdaAtom(
    override konst atom: FirAnonymousFunction,
    expectedType: ConeKotlinType?,
    konst expectedFunctionTypeKind: FunctionTypeKind?,
    konst receiver: ConeKotlinType?,
    konst contextReceivers: List<ConeKotlinType>,
    konst parameters: List<ConeKotlinType>,
    var returnType: ConeKotlinType,
    typeVariableForLambdaReturnType: ConeTypeVariableForLambdaReturnType?,
    candidateOfOuterCall: Candidate?,
    konst coerceFirstParameterToExtensionReceiver: Boolean
) : PostponedResolvedAtom() {
    init {
        candidateOfOuterCall?.let {
            it.postponedAtoms += this
        }
    }

    var typeVariableForLambdaReturnType = typeVariableForLambdaReturnType
        private set

    override var expectedType = expectedType
        private set

    lateinit var returnStatements: Collection<FirExpression>

    override konst inputTypes: Collection<ConeKotlinType>
        get() {
            if (receiver == null && contextReceivers.isEmpty()) return parameters
            return ArrayList<ConeKotlinType>(parameters.size + contextReceivers.size + (if (receiver != null) 1 else 0)).apply {
                addAll(parameters)
                addIfNotNull(receiver)
                addAll(contextReceivers)
            }
        }

    override konst outputType: ConeKotlinType get() = returnType

    fun replaceExpectedType(expectedType: ConeKotlinType, newReturnType: ConeTypeVariableType) {
        this.expectedType = expectedType
        this.returnType = newReturnType
    }

    fun replaceTypeVariableForLambdaReturnType(typeVariableForLambdaReturnType: ConeTypeVariableForLambdaReturnType) {
        this.typeVariableForLambdaReturnType = typeVariableForLambdaReturnType
    }
}

class LambdaWithTypeVariableAsExpectedTypeAtom(
    override konst atom: FirAnonymousFunctionExpression,
    private konst initialExpectedTypeType: ConeKotlinType,
    konst candidateOfOuterCall: Candidate,
) : PostponedResolvedAtom(), LambdaWithTypeVariableAsExpectedTypeMarker {
    init {
        candidateOfOuterCall.postponedAtoms += this
    }

    override var parameterTypesFromDeclaration: List<ConeKotlinType?>? = null
        private set

    override fun updateParameterTypesFromDeclaration(types: List<KotlinTypeMarker?>?) {
        @Suppress("UNCHECKED_CAST")
        types as List<ConeKotlinType?>?
        parameterTypesFromDeclaration = types
    }

    override konst expectedType: ConeKotlinType
        get() = revisedExpectedType ?: initialExpectedTypeType

    override konst inputTypes: Collection<ConeKotlinType> get() = listOf(initialExpectedTypeType)
    override konst outputType: ConeKotlinType? get() = null
    override var revisedExpectedType: ConeKotlinType? = null
        private set

    override fun reviseExpectedType(expectedType: KotlinTypeMarker) {
        require(expectedType is ConeKotlinType)
        revisedExpectedType = expectedType
    }
}

//  ------------- References -------------

class ResolvedCallableReferenceAtom(
    konst reference: FirCallableReferenceAccess,
    private konst initialExpectedType: ConeKotlinType?,
    konst lhs: DoubleColonLHS?,
    private konst session: FirSession
) : PostponedResolvedAtom(), PostponedCallableReferenceMarker {

    override konst atom: FirCallableReferenceAccess
        get() = reference

    var hasBeenResolvedOnce: Boolean = false
    var hasBeenPostponed: Boolean = false

    konst mightNeedAdditionalResolution get() = !hasBeenResolvedOnce || hasBeenPostponed

    var resultingReference: FirNamedReference? = null
    var resultingTypeForCallableReference: ConeKotlinType? = null

    override konst inputTypes: Collection<ConeKotlinType>
        get() {
            if (!hasBeenPostponed) return emptyList()
            return extractInputOutputTypesFromCallableReferenceExpectedType(expectedType, session)?.inputTypes
                ?: listOfNotNull(expectedType)
        }
    override konst outputType: ConeKotlinType?
        get() {
            if (!hasBeenPostponed) return null
            return extractInputOutputTypesFromCallableReferenceExpectedType(expectedType, session)?.outputType
        }

    override konst expectedType: ConeKotlinType?
        get() = if (!hasBeenPostponed)
            initialExpectedType
        else
            revisedExpectedType ?: initialExpectedType

    override var revisedExpectedType: ConeKotlinType? = null
        get() = if (hasBeenPostponed) field else expectedType
        private set

    override fun reviseExpectedType(expectedType: KotlinTypeMarker) {
        if (!mightNeedAdditionalResolution) return
        require(expectedType is ConeKotlinType)
        revisedExpectedType = expectedType
    }
}

//  -------------------------- Utils --------------------------

internal data class InputOutputTypes(konst inputTypes: List<ConeKotlinType>, konst outputType: ConeKotlinType)

internal fun extractInputOutputTypesFromCallableReferenceExpectedType(
    expectedType: ConeKotlinType?,
    session: FirSession
): InputOutputTypes? {
    if (expectedType == null) return null

    return when {
        expectedType.isSomeFunctionType(session) ->
            InputOutputTypes(expectedType.konstueParameterTypesIncludingReceiver(session), expectedType.returnType(session))

//        ReflectionTypes.isBaseTypeForNumberedReferenceTypes(expectedType) ->
//            InputOutputTypes(emptyList(), expectedType.arguments.single().type.unwrap())
//
//        ReflectionTypes.isNumberedKFunction(expectedType) -> {
//            konst functionFromSupertype = expectedType.immediateSupertypes().first { it.isFunctionType }.unwrap()
//            extractInputOutputTypesFromFunctionType(functionFromSupertype)
//        }
//
//        ReflectionTypes.isNumberedKSuspendFunction(expectedType) -> {
//            konst kSuspendFunctionType = expectedType.immediateSupertypes().first { it.isSuspendFunctionType }.unwrap()
//            extractInputOutputTypesFromFunctionType(kSuspendFunctionType)
//        }
//
//        ReflectionTypes.isNumberedKPropertyOrKMutablePropertyType(expectedType) -> {
//            konst functionFromSupertype = expectedType.supertypes().first { it.isFunctionType }.unwrap()
//            extractInputOutputTypesFromFunctionType(functionFromSupertype)
//        }

        else -> null
    }
}
