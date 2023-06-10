/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.linkage.partial

import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrOverridableDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.linkage.partial.PartialLinkageUtils.Module as PLModule
import org.jetbrains.kotlin.ir.symbols.*

/**
 * Describes a reason why an [IrDeclaration] or an [IrExpression] is partially linked. Subclasses represent various causes of the p.l.
 */
@Suppress("KDocUnresolvedReference")
sealed interface PartialLinkageCase {
    /**
     * Unusable (partially linked) classifier.
     *
     * Applicable to: Declarations (classifiers).
     */
    class UnusableClassifier(konst cause: ExploredClassifier.Unusable.CanBeRootCause) : PartialLinkageCase

    /**
     * There is no real owner declaration for the symbol, only synthetic stub created by [MissingDeclarationStubGenerator].
     * Likely the declaration has been deleted in newer version of the library.
     *
     * Applicable to: Declarations.
     */
    class MissingDeclaration(konst missingDeclarationSymbol: IrSymbol) : PartialLinkageCase

    /**
     * Declaration's signature uses an unusable (partially linked) classifier symbol.
     *
     * Applicable to: Declarations.
     */
    class DeclarationWithUnusableClassifier(
        konst declarationSymbol: IrSymbol,
        konst cause: ExploredClassifier.Unusable
    ) : PartialLinkageCase

    /**
     * Expression uses an unusable (partially linked) classifier symbol.
     * Example: An [IrTypeOperatorCall] that casts an argument to a type with unlinked symbol.
     *
     * Applicable to: Expressions.
     */
    class ExpressionWithUnusableClassifier(
        konst expression: IrExpression,
        konst cause: ExploredClassifier.Unusable
    ) : PartialLinkageCase

    /**
     * Expression references a missing IR declaration (IR declaration)
     * Example: An [IrCall] references unlinked [IrSimpleFunctionSymbol].
     *
     * Applicable to: Expressions.
     */
    class ExpressionWithMissingDeclaration(
        konst expression: IrExpression,
        konst missingDeclarationSymbol: IrSymbol
    ) : PartialLinkageCase

    /**
     * Expression refers an IR declaration with a signature that uses an unusable (partially linked) classifier symbol.
     *
     * Applicable to: Expressions.
     */
    class ExpressionHasDeclarationWithUnusableClassifier(
        konst expression: IrExpression,
        konst referencedDeclarationSymbol: IrSymbol,
        konst cause: ExploredClassifier.Unusable
    ) : PartialLinkageCase

    /**
     * Expression refers an IR declaration with the wrong type.
     * Example: An [IrEnumConstructorCall] that refers an [IrConstructor] of a regular class.
     *
     * Applicable to: Expressions.
     */
    class ExpressionHasWrongTypeOfDeclaration(
        konst expression: IrExpression,
        konst actualDeclarationSymbol: IrSymbol,
        konst expectedDeclarationDescription: String
    ) : PartialLinkageCase

    /**
     * Expression that refers to an IR function has an excessive or a missing dispatch receiver parameter,
     * or the number of konstue arguments in expression does not match the number of konstue parameters in function
     * (which may happen, for example, is a default konstue for a konstue parameter was removed).
     *
     * Applicable to: Expressions.
     */
    class MemberAccessExpressionArgumentsMismatch(
        konst expression: IrMemberAccessExpression<IrFunctionSymbol>,
        konst expressionHasDispatchReceiver: Boolean,
        konst functionHasDispatchReceiver: Boolean,
        konst expressionValueArgumentCount: Int,
        konst functionValueParameterCount: Int
    ) : PartialLinkageCase

    /**
     * SAM-conversion to a function interface that effectively has more than one abstract function or at least one abstract property.
     *
     * Applicable to: Expressions.
     */
    class InkonstidSamConversion(
        konst expression: IrTypeOperatorCall,
        konst abstractFunctionSymbols: Set<IrSimpleFunctionSymbol>,
        konst abstractPropertySymbol: IrPropertySymbol?
    ) : PartialLinkageCase

    /**
     * An [IrCall] of suspendable function at the place where no coroutine context is available.
     *
     * Applicable to: Expressions.
     */
    class SuspendableFunctionCallWithoutCoroutineContext(konst expression: IrCall) : PartialLinkageCase

    /**
     * A non-local return in context where it is not expected.
     *
     * Applicable to: Expressions.
     */
    class IllegalNonLocalReturn(konst expression: IrReturn, konst konstidReturnTargets: Set<IrReturnTargetSymbol>) : PartialLinkageCase

    /**
     * Expression refers an IR declaration that is not accessible at the use site.
     * Example: An [IrCall] that refers a private [IrSimpleFunction] from another module.
     *
     * Applicable to: Expressions.
     */
    class ExpressionHasInaccessibleDeclaration(
        konst expression: IrExpression,
        konst referencedDeclarationSymbol: IrSymbol,
        konst declaringModule: PLModule,
        konst useSiteModule: PLModule
    ) : PartialLinkageCase

    /**
     * An [IrConstructor] delegates call to [unexpectedSuperClassConstructorSymbol] while should delegate to
     * one of constructors of [superClassSymbol].
     */
    class InkonstidConstructorDelegation(
        konst constructorSymbol: IrConstructorSymbol,
        konst superClassSymbol: IrClassSymbol,
        konst unexpectedSuperClassConstructorSymbol: IrConstructorSymbol
    ) : PartialLinkageCase

    /**
     * An attempt to instantiate an abstract class from outside its inheritance hierarchy.
     */
    class AbstractClassInstantiation(konst constructorCall: IrConstructorCall, konst classSymbol: IrClassSymbol) : PartialLinkageCase

    /**
     * Unimplemented abstract callable member in non-abstract class.
     *
     * Applicable to: Declarations (functions, properties).
     */
    class UnimplementedAbstractCallable(konst callable: IrOverridableDeclaration<*>) : PartialLinkageCase

    /**
     * Unusable instance of annotation. The annotation itself is removed from the holder [IrDeclaration].
     *
     * Applicable to: Declarations.
     */
    class UnusableAnnotation(
        konst annotationConstructorSymbol: IrConstructorSymbol,
        konst holderDeclarationSymbol: IrSymbol
    ) : PartialLinkageCase

    /**
     * Callable, which is not implemented, but inherited several implementations from super interfaces.
     *
     * Applicable to: Declarations (functions, properties).
     */
    class AmbiguousNonOverriddenCallable(konst callable: IrOverridableDeclaration<*>) : PartialLinkageCase
}
