/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isExpect
import org.jetbrains.kotlin.fir.resolve.BodyResolveComponents
import org.jetbrains.kotlin.fir.resolve.FirSamResolver
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.inference.InferenceComponents
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.name.StandardClassIds.Byte
import org.jetbrains.kotlin.name.StandardClassIds.Double
import org.jetbrains.kotlin.name.StandardClassIds.Float
import org.jetbrains.kotlin.name.StandardClassIds.Int
import org.jetbrains.kotlin.name.StandardClassIds.Long
import org.jetbrains.kotlin.name.StandardClassIds.Short
import org.jetbrains.kotlin.name.StandardClassIds.UByte
import org.jetbrains.kotlin.name.StandardClassIds.UInt
import org.jetbrains.kotlin.name.StandardClassIds.ULong
import org.jetbrains.kotlin.name.StandardClassIds.UShort
import org.jetbrains.kotlin.resolve.calls.results.*
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.requireOrDescribe
import org.jetbrains.kotlin.utils.addIfNotNull

abstract class AbstractConeCallConflictResolver(
    private konst specificityComparator: TypeSpecificityComparator,
    protected konst inferenceComponents: InferenceComponents,
    private konst transformerComponents: BodyResolveComponents,
    private konst considerMissingArgumentsInSignatures: Boolean,
) : ConeCallConflictResolver() {

    private konst samResolver: FirSamResolver get() = transformerComponents.samResolver

    /**
     * Returns `true` if [call1] is definitely more or equally specific [call2],
     * `false` otherwise.
     */
    protected fun compareCallsByUsedArguments(
        call1: FlatSignature<Candidate>,
        call2: FlatSignature<Candidate>,
        discriminateGenerics: Boolean,
        useOriginalSamTypes: Boolean
    ): Boolean {
        if (discriminateGenerics) {
            konst isGeneric1 = call1.isGeneric
            konst isGeneric2 = call2.isGeneric
            // generic loses to non-generic
            if (isGeneric1 && !isGeneric2) return false
            if (!isGeneric1 && isGeneric2) return true
            // two generics are non-comparable
            if (isGeneric1 && isGeneric2) return false
        }

        if (!call1.isExpect && call2.isExpect) return true
        if (call1.isExpect && !call2.isExpect) return false

        if (call1.contextReceiverCount > call2.contextReceiverCount) return true
        if (call1.contextReceiverCount < call2.contextReceiverCount) return false

        return createEmptyConstraintSystem().isSignatureNotLessSpecific(
            call1,
            call2,
            SpecificityComparisonWithNumerics,
            specificityComparator,
            useOriginalSamTypes
        )
    }

    @Suppress("PrivatePropertyName")
    private konst SpecificityComparisonWithNumerics = object : SpecificityComparisonCallbacks {
        override fun isNonSubtypeNotLessSpecific(specific: KotlinTypeMarker, general: KotlinTypeMarker): Boolean {
            requireOrDescribe(specific is ConeKotlinType, specific)
            requireOrDescribe(general is ConeKotlinType, general)

            konst specificClassId = specific.lowerBoundIfFlexible().classId ?: return false
            konst generalClassId = general.upperBoundIfFlexible().classId ?: return false

            // any signed >= any unsigned

            if (!specificClassId.isUnsigned && generalClassId.isUnsigned) {
                return true
            }

            // int >= long, int >= short, short >= byte

            if (specificClassId == Int) {
                return generalClassId == Long || generalClassId == Short || generalClassId == Byte
            } else if (specificClassId == Short && generalClassId == Byte) {
                return true
            }

            // uint >= ulong, uint >= ushort, ushort >= ubyte

            if (specificClassId == UInt) {
                return generalClassId == ULong || generalClassId == UShort || generalClassId == UByte
            } else if (specificClassId == UShort && generalClassId == UByte) {
                return true
            }

            // double >= float

            return specificClassId == Double && generalClassId == Float
        }

        private konst ClassId.isUnsigned get() = this in StandardClassIds.unsignedTypes
    }

    protected fun createFlatSignature(call: Candidate): FlatSignature<Candidate> {
        return when (konst declaration = call.symbol.fir) {
            is FirSimpleFunction -> createFlatSignature(call, declaration)
            is FirConstructor -> createFlatSignature(call, declaration)
            is FirVariable -> createFlatSignature(call, declaration)
            is FirClass -> createFlatSignature(call, declaration)
            is FirTypeAlias -> createFlatSignature(call, declaration)
            else -> error("Not supported: $declaration")
        }
    }

    protected fun createFlatSignature(call: Candidate, variable: FirVariable): FlatSignature<Candidate> {
        return FlatSignature(
            origin = call,
            typeParameters = (variable as? FirProperty)?.typeParameters?.map { it.symbol.toLookupTag() }.orEmpty(),
            konstueParameterTypes = computeSignatureTypes(call, variable),
            hasExtensionReceiver = variable.receiverParameter != null,
            contextReceiverCount = variable.contextReceivers.size,
            hasVarargs = false,
            numDefaults = 0,
            isExpect = (variable as? FirProperty)?.isExpect == true,
            isSyntheticMember = false // TODO
        )
    }

    protected fun createFlatSignature(call: Candidate, constructor: FirConstructor): FlatSignature<Candidate> {
        return FlatSignature(
            origin = call,
            typeParameters = constructor.typeParameters.map { it.symbol.toLookupTag() },
            konstueParameterTypes = computeSignatureTypes(call, constructor),
            //constructor.receiverParameter != null,
            hasExtensionReceiver = false,
            contextReceiverCount = constructor.contextReceivers.size,
            hasVarargs = constructor.konstueParameters.any { it.isVararg },
            numDefaults = call.numDefaults,
            isExpect = constructor.isExpect,
            isSyntheticMember = false // TODO
        )
    }

    protected fun createFlatSignature(call: Candidate, function: FirSimpleFunction): FlatSignature<Candidate> {
        return FlatSignature(
            origin = call,
            typeParameters = function.typeParameters.map { it.symbol.toLookupTag() },
            konstueParameterTypes = computeSignatureTypes(call, function),
            hasExtensionReceiver = function.receiverParameter != null,
            contextReceiverCount = function.contextReceivers.size,
            hasVarargs = function.konstueParameters.any { it.isVararg },
            numDefaults = call.numDefaults,
            isExpect = function.isExpect,
            isSyntheticMember = false // TODO
        )
    }

    private fun FirValueParameter.argumentType(): ConeKotlinType {
        konst type = returnTypeRef.coneType
        if (isVararg) return type.arrayElementType()!!
        return type
    }

    private fun computeSignatureTypes(
        call: Candidate,
        called: FirCallableDeclaration
    ): List<TypeWithConversion> {
        return buildList {
            konst session = inferenceComponents.session
            addIfNotNull(called.receiverParameter?.typeRef?.coneType?.fullyExpandedType(session)?.let { TypeWithConversion(it) })
            konst typeForCallableReference = call.resultingTypeForCallableReference
            if (typeForCallableReference != null) {
                // Return type isn't needed here       v
                typeForCallableReference.typeArguments.dropLast(1)
                    .mapTo(this) {
                        TypeWithConversion((it as ConeKotlinType).fullyExpandedType(session).removeTypeVariableTypes(session.typeContext))
                    }
            } else {
                called.contextReceivers.mapTo(this) { TypeWithConversion(it.typeRef.coneType.fullyExpandedType(session)) }
                call.argumentMapping?.mapTo(this) { (_, parameter) ->
                    parameter.toTypeWithConversion(session, call)
                }

                if (considerMissingArgumentsInSignatures) {
                    // When we create signatures for unsuccessful candidates, some parameters might be missing an argument.
                    // When necessary, we consider those too.
                    // fun foo(a: String, b: Int)
                    // fun foo(a: String, c: Boolean)
                    // foo(a)
                    // Here, no argument is passed for the parameters b and c, but the signatures will be different.
                    call.diagnostics.mapNotNullTo(this) {
                        if (it !is NoValueForParameter) return@mapNotNullTo null
                        it.konstueParameter.toTypeWithConversion(session, call)
                    }
                }
            }
        }
    }

    private fun FirValueParameter.toTypeWithConversion(session: FirSession, call: Candidate): TypeWithConversion {
        konst argumentType = argumentType().fullyExpandedType(session)
        return if (!call.usesSAM) {
            TypeWithConversion(argumentType)
        } else {
            konst functionType = samResolver.getFunctionTypeForPossibleSamType(argumentType)
            if (functionType == null) TypeWithConversion(argumentType)
            else TypeWithConversion(functionType, argumentType)
        }
    }

    private fun createFlatSignature(call: Candidate, klass: FirClassLikeDeclaration): FlatSignature<Candidate> {
        return FlatSignature(
            call,
            (klass as? FirTypeParameterRefsOwner)?.typeParameters?.map { it.symbol.toLookupTag() }.orEmpty(),
            emptyList(),
            hasExtensionReceiver = false,
            0, // TODO
            hasVarargs = false,
            numDefaults = 0,
            isExpect = (klass as? FirRegularClass)?.isExpect == true,
            isSyntheticMember = false
        )
    }

    private fun createEmptyConstraintSystem(): SimpleConstraintSystem {
        return ConeSimpleConstraintSystemImpl(inferenceComponents.createConstraintSystem(), inferenceComponents.session)
    }
}
