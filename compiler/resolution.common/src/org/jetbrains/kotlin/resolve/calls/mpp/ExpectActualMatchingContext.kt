/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.mpp

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.mpp.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.TypeSubstitutorMarker
import org.jetbrains.kotlin.types.model.TypeSystemInferenceExtensionContext

interface ExpectActualMatchingContext<T : DeclarationSymbolMarker> : TypeSystemInferenceExtensionContext {
    konst shouldCheckReturnTypesOfCallables: Boolean

    konst RegularClassSymbolMarker.classId: ClassId
    konst TypeAliasSymbolMarker.classId: ClassId
    konst CallableSymbolMarker.callableId: CallableId
    konst TypeParameterSymbolMarker.parameterName: Name
    konst ValueParameterSymbolMarker.parameterName: Name

    fun TypeAliasSymbolMarker.expandToRegularClass(): RegularClassSymbolMarker?

    konst RegularClassSymbolMarker.classKind: ClassKind

    konst RegularClassSymbolMarker.isCompanion: Boolean
    konst RegularClassSymbolMarker.isInner: Boolean
    konst RegularClassSymbolMarker.isInline: Boolean
    konst RegularClassSymbolMarker.isValue: Boolean
    konst RegularClassSymbolMarker.isFun: Boolean
    konst ClassLikeSymbolMarker.typeParameters: List<TypeParameterSymbolMarker>

    konst ClassLikeSymbolMarker.modality: Modality?
    konst ClassLikeSymbolMarker.visibility: Visibility

    konst CallableSymbolMarker.modality: Modality?
    konst CallableSymbolMarker.visibility: Visibility

    konst RegularClassSymbolMarker.superTypes: List<KotlinTypeMarker>

    konst CallableSymbolMarker.isExpect: Boolean
    konst CallableSymbolMarker.isInline: Boolean
    konst CallableSymbolMarker.isSuspend: Boolean
    konst CallableSymbolMarker.isExternal: Boolean
    konst CallableSymbolMarker.isInfix: Boolean
    konst CallableSymbolMarker.isOperator: Boolean
    konst CallableSymbolMarker.isTailrec: Boolean

    konst PropertySymbolMarker.isVar: Boolean
    konst PropertySymbolMarker.isLateinit: Boolean
    konst PropertySymbolMarker.isConst: Boolean

    konst PropertySymbolMarker.setter: FunctionSymbolMarker?

    fun createExpectActualTypeParameterSubstitutor(
        expectTypeParameters: List<TypeParameterSymbolMarker>,
        actualTypeParameters: List<TypeParameterSymbolMarker>,
        parentSubstitutor: TypeSubstitutorMarker?
    ): TypeSubstitutorMarker

    fun RegularClassSymbolMarker.collectAllMembers(isActualDeclaration: Boolean): List<DeclarationSymbolMarker>
    fun RegularClassSymbolMarker.getMembersForExpectClass(name: Name): List<DeclarationSymbolMarker>

    fun RegularClassSymbolMarker.collectEnumEntryNames(): List<Name>

    konst CallableSymbolMarker.dispatchReceiverType: KotlinTypeMarker?
    konst CallableSymbolMarker.extensionReceiverType: KotlinTypeMarker?
    konst CallableSymbolMarker.returnType: KotlinTypeMarker
    konst CallableSymbolMarker.typeParameters: List<TypeParameterSymbolMarker>
    konst FunctionSymbolMarker.konstueParameters: List<ValueParameterSymbolMarker>

    konst CallableSymbolMarker.konstueParameters: List<ValueParameterSymbolMarker>
        get() = (this as? FunctionSymbolMarker)?.konstueParameters ?: emptyList()

    konst ValueParameterSymbolMarker.isVararg: Boolean
    konst ValueParameterSymbolMarker.isNoinline: Boolean
    konst ValueParameterSymbolMarker.isCrossinline: Boolean
    konst ValueParameterSymbolMarker.hasDefaultValue: Boolean

    fun CallableSymbolMarker.isAnnotationConstructor(): Boolean

    konst TypeParameterSymbolMarker.bounds: List<KotlinTypeMarker>
    konst TypeParameterSymbolMarker.variance: Variance
    konst TypeParameterSymbolMarker.isReified: Boolean

    fun areCompatibleExpectActualTypes(
        expectType: KotlinTypeMarker?,
        actualType: KotlinTypeMarker?,
    ): Boolean

    fun RegularClassSymbolMarker.isNotSamInterface(): Boolean

    /*
     * Determines should some declaration from expect class scope be checked
     *  - FE 1.0: skip fake overrides
     *  - FIR: skip nothing
     *  - IR: skip nothing
     */
    fun CallableSymbolMarker.shouldSkipMatching(containingExpectClass: RegularClassSymbolMarker): Boolean

    konst CallableSymbolMarker.hasStableParameterNames: Boolean
}
