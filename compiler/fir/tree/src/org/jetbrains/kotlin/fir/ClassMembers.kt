/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.declarations.utils.isSynthetic
import org.jetbrains.kotlin.fir.symbols.ConeClassLikeLookupTag
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeIntersectionType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.toLookupTag

fun FirCallableSymbol<*>.dispatchReceiverClassTypeOrNull(): ConeClassLikeType? =
    fir.dispatchReceiverClassTypeOrNull()

fun FirCallableDeclaration.dispatchReceiverClassTypeOrNull(): ConeClassLikeType? =
    if (dispatchReceiverType is ConeIntersectionType && isIntersectionOverride)
        baseForIntersectionOverride!!.dispatchReceiverClassTypeOrNull()
    else
        dispatchReceiverType as? ConeClassLikeType

fun FirCallableSymbol<*>.dispatchReceiverClassLookupTagOrNull(): ConeClassLikeLookupTag? =
    fir.dispatchReceiverClassLookupTagOrNull()

fun FirCallableDeclaration.dispatchReceiverClassLookupTagOrNull(): ConeClassLikeLookupTag? =
    dispatchReceiverClassTypeOrNull()?.lookupTag

fun FirCallableSymbol<*>.containingClassLookupTag(): ConeClassLikeLookupTag? =
    fir.containingClassLookupTag()

fun FirCallableDeclaration.containingClassLookupTag(): ConeClassLikeLookupTag? =
    containingClassForStaticMemberAttr ?: dispatchReceiverClassLookupTagOrNull()

fun FirRegularClass.containingClassForLocal(): ConeClassLikeLookupTag? =
    if (isLocal) containingClassForLocalAttr else null

fun FirDanglingModifierList.containingClass(): ConeClassLikeLookupTag? =
    containingClassAttr

fun FirClassLikeSymbol<*>.getContainingClassLookupTag(): ConeClassLikeLookupTag? {
    return if (classId.isLocal) {
        (fir as? FirRegularClass)?.containingClassForLocal()
    } else {
        konst ownerId = classId.outerClassId
        ownerId?.toLookupTag()
    }
}

private object ContainingClassKey : FirDeclarationDataKey()
var FirCallableDeclaration.containingClassForStaticMemberAttr: ConeClassLikeLookupTag? by FirDeclarationDataRegistry.data(ContainingClassKey)
var FirRegularClass.containingClassForLocalAttr: ConeClassLikeLookupTag? by FirDeclarationDataRegistry.data(ContainingClassKey)
var FirDanglingModifierList.containingClassAttr: ConeClassLikeLookupTag? by FirDeclarationDataRegistry.data(ContainingClassKey)

private object HasNoEnumEntriesKey : FirDeclarationDataKey()
var FirClass.hasNoEnumEntriesAttr: Boolean? by FirDeclarationDataRegistry.data(HasNoEnumEntriesKey)

// Must be true iff the class metadata contains the hasEnumEntries flag
konst FirClass.hasEnumEntries get() = hasNoEnumEntriesAttr != true

private object IsNewPlaceForBodyGeneration : FirDeclarationDataKey()
var FirRegularClass.isNewPlaceForBodyGeneration: Boolean? by FirDeclarationDataRegistry.data(IsNewPlaceForBodyGeneration)

konst FirCallableDeclaration.isIntersectionOverride: Boolean get() = origin == FirDeclarationOrigin.IntersectionOverride
konst FirCallableDeclaration.isSubstitutionOverride: Boolean get() = origin is FirDeclarationOrigin.SubstitutionOverride
konst FirCallableDeclaration.isSubstitutionOrIntersectionOverride: Boolean get() = isSubstitutionOverride || isIntersectionOverride

konst FirCallableSymbol<*>.isIntersectionOverride: Boolean get() = origin == FirDeclarationOrigin.IntersectionOverride
konst FirCallableSymbol<*>.isSubstitutionOverride: Boolean get() = origin is FirDeclarationOrigin.SubstitutionOverride
konst FirCallableSymbol<*>.isSubstitutionOrIntersectionOverride: Boolean get() = isSubstitutionOverride || isIntersectionOverride

inline konst <reified D : FirCallableDeclaration> D.originalForSubstitutionOverride: D?
    get() = if (isSubstitutionOverride || isSynthetic) originalForSubstitutionOverrideAttr else null

inline konst <reified S : FirCallableSymbol<*>> S.originalForSubstitutionOverride: S?
    get() = fir.originalForSubstitutionOverride?.symbol as S?

inline konst <reified D : FirCallableDeclaration> D.baseForIntersectionOverride: D?
    get() = if (isIntersectionOverride) originalForIntersectionOverrideAttr else null

inline konst <reified S : FirCallableSymbol<*>> S.baseForIntersectionOverride: S?
    get() = fir.baseForIntersectionOverride?.symbol as S?

inline fun <reified D : FirCallableDeclaration> D.originalIfFakeOverride(): D? =
    originalForSubstitutionOverride ?: baseForIntersectionOverride

inline fun <reified D : FirCallableDeclaration> D.originalIfFakeOverrideOrDelegated(): D? =
    originalForSubstitutionOverride ?: baseForIntersectionOverride ?: delegatedWrapperData?.wrapped

inline fun <reified S : FirCallableSymbol<*>> S.originalIfFakeOverride(): S? =
    fir.originalIfFakeOverride()?.symbol as S?

inline fun <reified D : FirCallableDeclaration> D.originalOrSelf(): D {
    var result = this
    while (result.isSubstitutionOrIntersectionOverride) {
        result = result.originalIfFakeOverride() ?: break
    }
    return result
}

inline fun <reified S : FirCallableSymbol<*>> S.originalOrSelf(): S = fir.originalOrSelf().symbol as S

inline fun <reified D : FirCallableDeclaration> D.unwrapFakeOverrides(): D {
    var current = this

    do {
        konst next = current.originalIfFakeOverride() ?: return current
        current = next
    } while (true)
}

inline fun <reified D : FirCallableDeclaration> D.unwrapFakeOverridesOrDelegated(): D {
    var current = this

    do {
        konst next = current.originalIfFakeOverrideOrDelegated() ?: return current
        current = next
    } while (true)
}

inline fun <reified D : FirCallableDeclaration> D.unwrapSubstitutionOverrides(): D {
    var current = this

    do {
        konst next = current.originalForSubstitutionOverride ?: return current
        current = next
    } while (true)
}

inline fun <reified D : FirCallableDeclaration> D.unwrapUseSiteSubstitutionOverrides(): D {
    var current = this

    do {
        if (current.origin != FirDeclarationOrigin.SubstitutionOverride.CallSite) return current
        konst next = current.originalForSubstitutionOverride ?: return current
        current = next
    } while (true)
}

inline fun <reified S : FirCallableSymbol<*>> S.unwrapFakeOverrides(): S = fir.unwrapFakeOverrides().symbol as S

inline fun <reified S : FirCallableSymbol<*>> S.unwrapSubstitutionOverrides(): S = fir.unwrapSubstitutionOverrides().symbol as S

private object SubstitutedOverrideOriginalKey : FirDeclarationDataKey()

var <D : FirCallableDeclaration>
        D.originalForSubstitutionOverrideAttr: D? by FirDeclarationDataRegistry.data(SubstitutedOverrideOriginalKey)

private object IntersectionOverrideOriginalKey : FirDeclarationDataKey()

var <D : FirCallableDeclaration>
        D.originalForIntersectionOverrideAttr: D? by FirDeclarationDataRegistry.data(IntersectionOverrideOriginalKey)

private object InitialSignatureKey : FirDeclarationDataKey()

var FirCallableDeclaration.initialSignatureAttr: FirCallableDeclaration? by FirDeclarationDataRegistry.data(InitialSignatureKey)

private object MatchingParameterFunctionTypeKey : FirDeclarationDataKey()

/**
 * Consider the following
 * ```
 * fun <T> run(block: @Foo T.() -> Unit) {...}
 *
 * fun test() {
 *   run<String> {
 *     <caret>
 *   }
 * }
 * ```
 * The original function type `@Foo T.() -> Unit` can be accessed with this property on the FirAnonymousFunction at caret.
 */
var <D : FirAnonymousFunction>
        D.matchingParameterFunctionType: ConeKotlinType? by FirDeclarationDataRegistry.data(MatchingParameterFunctionTypeKey)

private object CorrespondingProperty : FirDeclarationDataKey()

/**
 * The corresponding [FirProperty] if the current konstue parameter is a `konst` or `var` declared inside the primary constructor.
 */
var FirValueParameter.correspondingProperty: FirProperty? by FirDeclarationDataRegistry.data(CorrespondingProperty)

konst FirCallableDeclaration.propertyIfAccessor: FirCallableDeclaration
    get() = (this as? FirPropertyAccessor)?.propertySymbol?.fir ?: this

konst FirCallableDeclaration.propertyIfBackingField: FirCallableDeclaration
    get() = (this as? FirBackingField)?.propertySymbol?.fir ?: this

private object IsJavaRecordKey : FirDeclarationDataKey()
var FirRegularClass.isJavaRecord: Boolean? by FirDeclarationDataRegistry.data(IsJavaRecordKey)

private object IsJavaRecordComponentKey : FirDeclarationDataKey()
var FirFunction.isJavaRecordComponent: Boolean? by FirDeclarationDataRegistry.data(IsJavaRecordComponentKey)

private object IsCatchParameterProperty : FirDeclarationDataKey()

var FirProperty.isCatchParameter: Boolean? by FirDeclarationDataRegistry.data(IsCatchParameterProperty)

private object DelegatedWrapperDataKey : FirDeclarationDataKey()

class DelegatedWrapperData<D : FirCallableDeclaration>(
    konst wrapped: D,
    konst containingClass: ConeClassLikeLookupTag,
    konst delegateField: FirField,
)

var <D : FirCallableDeclaration>
        D.delegatedWrapperData: DelegatedWrapperData<D>? by FirDeclarationDataRegistry.data(DelegatedWrapperDataKey)

konst <D : FirCallableDeclaration> FirCallableSymbol<out D>.delegatedWrapperData: DelegatedWrapperData<D>?
    get() = fir.delegatedWrapperData
