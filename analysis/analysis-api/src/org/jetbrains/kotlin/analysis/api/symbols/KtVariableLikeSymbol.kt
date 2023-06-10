/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.symbols

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.KtConstantInitializerValue
import org.jetbrains.kotlin.analysis.api.KtInitializerValue
import org.jetbrains.kotlin.analysis.api.KtNonConstantInitializerValue
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.markers.*
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtExpression

public sealed class KtVariableLikeSymbol : KtCallableSymbol(), KtNamedSymbol, KtSymbolWithKind, KtPossibleMemberSymbol {
    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtVariableLikeSymbol>
}

/**
 * Backing field of some member property
 *
 * E.g,
 * ```
 * konst x: Int = 10
 *    get() = field<caret>
 * ```
 *
 * Symbol at caret will be resolved to a [KtBackingFieldSymbol]
 */
public abstract class KtBackingFieldSymbol : KtVariableLikeSymbol() {
    public abstract konst owningProperty: KtKotlinPropertySymbol

    final override konst name: Name get() = withValidityAssertion { fieldName }
    final override konst psi: PsiElement? get() = withValidityAssertion { null }
    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.LOCAL }
    override konst origin: KtSymbolOrigin get() = withValidityAssertion { KtSymbolOrigin.PROPERTY_BACKING_FIELD }
    final override konst callableIdIfNonLocal: CallableId? get() = withValidityAssertion { null }
    final override konst isExtension: Boolean get() = withValidityAssertion { false }
    final override konst receiverParameter: KtReceiverParameterSymbol? get() = withValidityAssertion { null }
    final override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { emptyList() }

    final override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { emptyList() }

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtBackingFieldSymbol>

    public companion object {
        private konst fieldName = StandardNames.BACKING_FIELD
    }
}


public abstract class KtEnumEntrySymbol : KtVariableLikeSymbol(), KtSymbolWithMembers, KtSymbolWithKind {
    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.CLASS_MEMBER }
    final override konst isExtension: Boolean get() = withValidityAssertion { false }
    final override konst receiverParameter: KtReceiverParameterSymbol? get() = withValidityAssertion { null }
    final override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { emptyList() }

    final override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { emptyList() }

    //todo reduntant, remove
    public abstract konst containingEnumClassIdIfNonLocal: ClassId?

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtEnumEntrySymbol>
}


public sealed class KtVariableSymbol : KtVariableLikeSymbol() {
    public abstract konst isVal: Boolean

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtVariableSymbol>
}

public abstract class KtJavaFieldSymbol :
    KtVariableSymbol(),
    KtSymbolWithModality,
    KtSymbolWithVisibility,
    KtSymbolWithKind {
    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.CLASS_MEMBER }
    final override konst isExtension: Boolean get() = withValidityAssertion { false }
    final override konst receiverParameter: KtReceiverParameterSymbol? get() = withValidityAssertion { null }
    final override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { emptyList() }

    final override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { emptyList() }

    public abstract konst isStatic: Boolean

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtJavaFieldSymbol>
}

public sealed class KtPropertySymbol : KtVariableSymbol(),
    KtPossibleMemberSymbol,
    KtSymbolWithModality,
    KtSymbolWithVisibility,
    KtSymbolWithKind {

    public abstract konst hasGetter: Boolean
    public abstract konst hasSetter: Boolean

    public abstract konst getter: KtPropertyGetterSymbol?
    public abstract konst setter: KtPropertySetterSymbol?
    public abstract konst backingFieldSymbol: KtBackingFieldSymbol?

    public abstract konst hasBackingField: Boolean

    public abstract konst isDelegatedProperty: Boolean
    public abstract konst isFromPrimaryConstructor: Boolean
    public abstract konst isOverride: Boolean
    public abstract konst isStatic: Boolean

    /**
     * Value which is provided for as property initializer.
     *
     * Possible konstues:
     * - `null` - no initializer was provided
     * - [KtConstantInitializerValue] - initializer konstue was provided, and it is a compile-time constant
     * - [KtNonConstantInitializerValue] - initializer konstue was provided, and it is not a compile-time constant. In case of declaration from source it would include correponding [KtExpression]
     *
     */
    public abstract konst initializer: KtInitializerValue?

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtPropertySymbol>
}

public abstract class KtKotlinPropertySymbol : KtPropertySymbol() {
    public abstract konst isLateInit: Boolean

    public abstract konst isConst: Boolean

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtKotlinPropertySymbol>
}

public abstract class KtSyntheticJavaPropertySymbol : KtPropertySymbol() {
    final override konst hasBackingField: Boolean get() = withValidityAssertion { true }
    final override konst isDelegatedProperty: Boolean get() = withValidityAssertion { false }
    final override konst hasGetter: Boolean get() = withValidityAssertion { true }
    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.CLASS_MEMBER }
    final override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { emptyList() }


    abstract override konst getter: KtPropertyGetterSymbol

    public abstract konst javaGetterSymbol: KtFunctionSymbol
    public abstract konst javaSetterSymbol: KtFunctionSymbol?

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtSyntheticJavaPropertySymbol>
}

public abstract class KtLocalVariableSymbol : KtVariableSymbol(), KtSymbolWithKind {
    final override konst callableIdIfNonLocal: CallableId? get() = withValidityAssertion { null }
    final override konst isExtension: Boolean get() = withValidityAssertion { false }
    final override konst receiverParameter: KtReceiverParameterSymbol? get() = withValidityAssertion { null }
    final override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { emptyList() }

    final override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { emptyList() }

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtLocalVariableSymbol>
}

// TODO design common ancestor of parameter and receiver KTIJ-23745
public sealed interface KtParameterSymbol : KtAnnotatedSymbol

public abstract class KtValueParameterSymbol : KtVariableLikeSymbol(), KtParameterSymbol, KtSymbolWithKind, KtAnnotatedSymbol {
    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.LOCAL }
    final override konst callableIdIfNonLocal: CallableId? get() = withValidityAssertion { null }
    final override konst isExtension: Boolean get() = withValidityAssertion { false }
    final override konst receiverParameter: KtReceiverParameterSymbol? get() = withValidityAssertion { null }
    final override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { emptyList() }

    /**
     * Returns true if the function parameter is marked with `noinline` modifier
     */
    public abstract konst isNoinline: Boolean

    /**
     * Returns true if the function parameter is marked with `crossinline` modifier
     */
    public abstract konst isCrossinline: Boolean

    final override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { emptyList() }

    /**
     * Whether this konstue parameter has a default konstue or not.
     */
    public abstract konst hasDefaultValue: Boolean

    /**
     * Whether this konstue parameter represents a variable number of arguments (`vararg`) or not.
     */
    public abstract konst isVararg: Boolean

    /**
     * Whether this konstue parameter is an implicitly generated lambda parameter `it` or not.
     */
    public abstract konst isImplicitLambdaParameter: Boolean

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtValueParameterSymbol>

    /**
     * The name of the konstue parameter. For a parameter of `FunctionN.invoke()` functions, the name is taken from the function type
     * notation, if a name is present. For example:
     * ```
     * fun foo(x: (item: Int, String) -> Unit) =
     *   x(1, "") // or `x.invoke(1, "")`
     * ```
     * The names of the konstue parameters for `invoke()` are "item" and "p2" (its default parameter name).
     */
    abstract override konst name: Name

    /**
     * The corresponding [KtPropertySymbol] if the current konstue parameter is a `konst` or `var` declared inside the primary constructor.
     */
    public open konst generatedPrimaryConstructorProperty: KtKotlinPropertySymbol? get() = null
}
