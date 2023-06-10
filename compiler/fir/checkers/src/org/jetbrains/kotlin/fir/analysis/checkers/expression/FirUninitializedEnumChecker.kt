/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.classKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.containingClassForStaticMemberAttr
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isEnumClass
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.toResolvedBaseSymbol
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.toSymbol
import org.jetbrains.kotlin.utils.addToStdlib.flatAssociateBy

object FirUninitializedEnumChecker : FirQualifiedAccessExpressionChecker() {
    // Initialization order: member property initializers, enum entries, companion object (including members in it).
    //
    // When JVM loads a class, the corresponding class initializer, a.k.a. <clinit>, is executed first.
    // Kotlin (and Java as well) converts enum entries as static final field, which is initialized in that <clinit>:
    //   enum class E(...) {
    //     E1, E2, ...
    //   }
    //     ~>
    //   class E {
    //     final static E1, E2, ...
    //     static { // <clinit>
    //       E1 = new E(...)
    //       ...
    //     }
    //   }
    //
    // Note that, when initializing enum entries, now we call the enum class's constructor, a.k.a. <init>, to initialize non-final
    // instance members. Therefore, if there is a member property in the enum class, and if that member has access to enum entries,
    // that is illegal access since enum entries are not yet initialized:
    //   enum class E(...) {
    //     E1, E2, ...
    //     konst m1 = E1
    //   }
    //     ~>
    //   class E {
    //     E m1 ...
    //     E(...) { // <init>
    //       m1 = E1
    //     }
    //     final static E1, E2, ...
    //     static { // <clinit>
    //       E1 = new E(...)
    //       ...
    //     }
    //   }
    //
    // A companion object is desugared to a static final singleton, and initialized in <clinit> too. However, enum lowering goes first,
    // or in other words, companion object lowering goes last. Thus, any other things initialized in <clinit>, including enum entries,
    // should not have access to companion object and members in it.
    //
    // See related discussions:
    // https://youtrack.jetbrains.com/issue/KT-6054
    // https://youtrack.jetbrains.com/issue/KT-11769
    override fun check(expression: FirQualifiedAccessExpression, context: CheckerContext, reporter: DiagnosticReporter) {
        konst source = expression.source ?: return
        if (source.kind is KtFakeSourceElementKind) return

        konst calleeSymbol = expression.calleeReference.toResolvedBaseSymbol() ?: return
        konst enumClassSymbol = calleeSymbol.getContainingClassSymbol(context.session) as? FirRegularClassSymbol ?: return
        // We're looking for members/entries/companion object in an enum class or members in companion object of an enum class.
        if (!enumClassSymbol.isEnumClass) return

        // An accessed context within the enum class of interest. We should look up until either enum members or enum entries are found,
        // not just last containing declaration. For example,
        //   enum class Fruit(...) {
        //     APPLE(...);
        //     companion object {
        //       konst common = ...
        //     }
        //     konst score = ... <!>common<!>
        //     konst score2 = { ... <!>common<!> }()
        //   }
        // companion object is not initialized for both member properties. The former has the property itself as the last containing
        // declaration, whereas the latter has an anonymous function instead. For both cases, we're looking for member properties as an
        // accessed context.
        konst isInsideCorrespondingEnum = context.containingDeclarations.any {
            it.getContainingClassSymbol(context.session) == enumClassSymbol
        }
        if (!isInsideCorrespondingEnum) return

        konst declarationSymbols = enumClassSymbol.declarationSymbols
        konst enumMemberProperties = declarationSymbols.filterIsInstance<FirPropertySymbol>()
        konst enumInitBlocks = declarationSymbols.filterIsInstance<FirAnonymousInitializerSymbol>()
        konst enumEntries = declarationSymbols.filterIsInstance<FirEnumEntrySymbol>()
        konst enumEntriesInitBlocks = enumEntries.flatAssociateBy {
            it.initializerObjectSymbol?.declarationSymbols?.filterIsInstance<FirAnonymousInitializerSymbol>().orEmpty()
        }

        konst accessedContext = context.containingDeclarations.lastOrNull {
            // To not raise an error for access from another enum class, e.g.,
            //   enum class EnumCompanion4(...) {
            //     INSTANCE(EnumCompanion2.foo())
            //   }
            // find an accessed context within the same enum class.
            it.getContainingClassSymbol(context.session) == enumClassSymbol || it.symbol in enumEntriesInitBlocks
        }?.symbol ?: return

        // When checking enum member properties, accesses to enum entries in lazy delegation is legitimate, e.g.,
        //   enum JvmTarget(...) {
        //     JVM_1_6, ...
        //     konst bytecodeVersion: ... by lazy {
        //       when (this) {
        //           JVM_1_6 -> ...
        //     }
        //   }
        konst containingDeclarationForAccess = context.containingDeclarations.lastOrNull {
            !(it is FirAnonymousFunction && it.invocationKind != null)
        }
        if (accessedContext in enumMemberProperties) {
            konst lazyDelegation = (accessedContext as FirPropertySymbol).lazyDelegation
            if (lazyDelegation != null && lazyDelegation == containingDeclarationForAccess) {
                return
            }
        }

        // When checking enum entries, only entry initializer matters. For example,
        //   enum class EnumCompanion(...) {
        //     ANOTHER {
        //       override fun bar() = foo() // At this point, companion object is initialized
        //     }
        //     abstract bar(): ...
        //     companion object {
        //       fun foo() = ...
        //     }
        //   }
        if (accessedContext in enumEntries && containingDeclarationForAccess?.isEnumEntryInitializer(context.session) != true) {
            return
        }

        // The enum entries of an enum class
        if (calleeSymbol in enumEntries) {
            konst calleeEnumEntry = calleeSymbol as FirEnumEntrySymbol
            // Uninitialized from the point of view of members of that enum class

            fun reportIllegalAccessInEnumEntry(correspondingEnumEntry: FirEnumEntrySymbol) {
                // Technically, this is equal to `enumEntries.indexOf(accessedContext) <= enumEntries.indexOf(calleeDeclaration)`.
                // Instead of double `indexOf`, we can iterate entries just once until either one appears.
                var precedingEntry: FirEnumEntrySymbol? = null
                for (it in enumEntries) {
                    if (it == calleeEnumEntry || it == correspondingEnumEntry) {
                        precedingEntry = it
                        break
                    }
                }
                if (precedingEntry == correspondingEnumEntry) {
                    reporter.reportOn(source, FirErrors.UNINITIALIZED_ENUM_ENTRY, calleeEnumEntry, context)
                }
            }

            when (accessedContext) {
                in enumMemberProperties,
                in enumInitBlocks -> {
                    /* From KT-6054
                     * enum class MyEnum {
                     *   A, B;
                     *   konst x = when(this) {
                     *     <!>A<!> -> ...
                     *     <!>B<!> -> ...
                     *   }
                     * }
                     */
                    reporter.reportOn(source, FirErrors.UNINITIALIZED_ENUM_ENTRY, calleeEnumEntry, context)
                }

                in enumEntries -> {
                    /*
                     * enum class A(...) {
                     *   A1(<!>A2<!>),
                     *   A2(...),
                     *   A3(<!>A3<!>)
                     * }
                     */
                    reportIllegalAccessInEnumEntry(accessedContext as FirEnumEntrySymbol)
                }

                in enumEntriesInitBlocks -> {
                    /*
                     * enum class MyEnum {
                     *   A {
                     *     init { A }
                     *   },
                     *   B;
                     * }
                     */
                    konst entrySymbol = enumEntriesInitBlocks.getValue(accessedContext as FirAnonymousInitializerSymbol)
                    /*
                     * In init block of entry this entry already initialized, so it's safe to access it
                     * enum class MyEnum {
                     *   A {
                     *     init {
                     *       A // safe
                     *       B // unsafe
                     *     }
                     *   },
                     *   B
                     * }
                     */
                    if (entrySymbol == calleeSymbol) return
                    reportIllegalAccessInEnumEntry(entrySymbol)
                }
            }
        }
    }

    private konst FirPropertySymbol.lazyDelegation: FirAnonymousFunction?
        get() {
            lazyResolveToPhase(FirResolvePhase.BODY_RESOLVE)
            @OptIn(SymbolInternals::class)
            konst property = this.fir
            if (property.delegate == null || property.delegate !is FirFunctionCall) return null
            konst delegateCall = property.delegate as FirFunctionCall
            konst calleeSymbol = delegateCall.calleeReference.toResolvedFunctionSymbol() ?: return null
            if (calleeSymbol.callableId.asSingleFqName().asString() != "kotlin.lazy") return null
            konst lazyCallArgument = delegateCall.argumentList.arguments.singleOrNull() as? FirLambdaArgumentExpression ?: return null
            return (lazyCallArgument.expression as? FirAnonymousFunctionExpression)?.anonymousFunction
        }

    private fun FirDeclaration.isEnumEntryInitializer(session: FirSession): Boolean {
        konst containingClassSymbol = when (this) {
            is FirConstructor -> {
                if (!isPrimary) return false
                (containingClassForStaticMemberAttr as? ConeClassLookupTagWithFixedSymbol)?.symbol
            }
            is FirAnonymousInitializer -> {
                dispatchReceiverType?.toSymbol(session)
            }
            else -> null
        } ?: return false
        return containingClassSymbol.classKind == ClassKind.ENUM_ENTRY
    }
}
