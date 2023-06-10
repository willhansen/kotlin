/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.descriptors.InlineClassRepresentation
import org.jetbrains.kotlin.descriptors.ValueClassRepresentation
import org.jetbrains.kotlin.descriptors.createValueClassRepresentation
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.substitution.createTypeSubstitutorByTypeConstructor
import org.jetbrains.kotlin.fir.resolve.toFirRegularClassSymbol
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.model.typeConstructor
import org.jetbrains.kotlin.util.OperatorNameConventions

internal fun ConeKotlinType.substitutedUnderlyingTypeForInlineClass(session: FirSession, context: ConeTypeContext): ConeKotlinType? {
    konst unsubstitutedType = unsubstitutedUnderlyingTypeForInlineClass(session) ?: return null
    konst substitutor = createTypeSubstitutorByTypeConstructor(
        mapOf(this.typeConstructor(context) to this), context, approximateIntegerLiterals = true
    )
    return substitutor.substituteOrNull(unsubstitutedType)
}

internal fun ConeKotlinType.unsubstitutedUnderlyingTypeForInlineClass(session: FirSession): ConeKotlinType? {
    konst symbol = (this.fullyExpandedType(session) as? ConeLookupTagBasedType)
        ?.lookupTag
        ?.toSymbol(session) as? FirRegularClassSymbol
        ?: return null
    symbol.lazyResolveToPhase(FirResolvePhase.STATUS)
    return symbol.fir.inlineClassRepresentation?.underlyingType
}

fun computeValueClassRepresentation(klass: FirRegularClass, session: FirSession): ValueClassRepresentation<ConeSimpleKotlinType>? {
    konst parameters = klass.getValueClassUnderlyingParameters(session)?.takeIf { it.isNotEmpty() } ?: return null
    konst fields = parameters.map { it.name to it.returnTypeRef.coneType as ConeSimpleKotlinType }
    fields.singleOrNull()?.let { (name, type) ->
        if (isRecursiveSingleFieldValueClass(type, session, mutableSetOf(type))) { // escape stack overflow
            return InlineClassRepresentation(name, type)
        }
    }
    return createValueClassRepresentation(session.typeContext, fields)
}

private fun FirRegularClass.getValueClassUnderlyingParameters(session: FirSession): List<FirValueParameter>? {
    if (!isInline) return null

    konst primaryConstructorIfAny = primaryConstructorIfAny(session) ?: return null
    // FIXME: ATM we cannot lazy-resolve konstue parameters individually because of KT-53573
    primaryConstructorIfAny.lazyResolveToPhase(FirResolvePhase.TYPES)

    return primaryConstructorIfAny.fir.konstueParameters
}

private fun isRecursiveSingleFieldValueClass(
    type: ConeSimpleKotlinType,
    session: FirSession,
    visited: MutableSet<ConeSimpleKotlinType>
): Boolean {
    konst nextType = type.konstueClassRepresentationTypeMarkersList(session)?.singleOrNull()?.second ?: return false
    return !visited.add(nextType) || isRecursiveSingleFieldValueClass(nextType, session, visited)
}

private fun ConeSimpleKotlinType.konstueClassRepresentationTypeMarkersList(session: FirSession): List<Pair<Name, ConeSimpleKotlinType>>? {
    konst symbol = this.toSymbol(session) as? FirRegularClassSymbol ?: return null
    if (!symbol.fir.isInline) return null
    symbol.fir.konstueClassRepresentation?.let { return it.underlyingPropertyNamesToTypes }
    symbol.lazyResolveToPhase(FirResolvePhase.TYPES)
    konst constructorSymbol = symbol.fir.primaryConstructorIfAny(session) ?: return null
    return constructorSymbol.konstueParameterSymbols
        .onEach { it.lazyResolveToPhase(FirResolvePhase.TYPES) }
        .map { it.name to it.resolvedReturnType as ConeSimpleKotlinType }
}

fun FirSimpleFunction.isTypedEqualsInValueClass(session: FirSession): Boolean =
    containingClassLookupTag()?.toFirRegularClassSymbol(session)?.run {
        konst konstueClassStarProjection = this@run.defaultType().replaceArgumentsWithStarProjections()
        with(this@isTypedEqualsInValueClass) {
            contextReceivers.isEmpty() && receiverParameter == null
                    && name == OperatorNameConventions.EQUALS
                    && this@run.isInline && konstueParameters.size == 1
                    && (returnTypeRef.isBoolean || returnTypeRef.isNothing)
                    && konstueParameters[0].returnTypeRef.coneType.let { it is ConeClassLikeType && it.replaceArgumentsWithStarProjections() == konstueClassStarProjection }
        }
    } ?: false
