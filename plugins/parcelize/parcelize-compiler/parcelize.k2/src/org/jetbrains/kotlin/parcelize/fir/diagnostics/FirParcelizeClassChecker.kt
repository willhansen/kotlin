/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.parcelize.fir.diagnostics

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.parcelize.ParcelizeNames.CREATOR_NAME
import org.jetbrains.kotlin.parcelize.ParcelizeNames.OLD_PARCELER_ID
import org.jetbrains.kotlin.parcelize.ParcelizeNames.PARCELABLE_ID
import org.jetbrains.kotlin.parcelize.ParcelizeNames.PARCELER_CLASS_IDS
import org.jetbrains.kotlin.parcelize.ParcelizeNames.PARCELIZE_CLASS_CLASS_IDS
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object FirParcelizeClassChecker : FirClassChecker() {
    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        checkParcelableClass(declaration, context, reporter)
        checkParcelerClass(declaration, context, reporter)
    }

    private fun checkParcelableClass(klass: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        konst symbol = klass.symbol
        if (!symbol.isParcelize(context.session)) return
        konst source = klass.source ?: return
        konst classKind = klass.classKind

        if (klass is FirRegularClass) {
            if (classKind == ClassKind.ANNOTATION_CLASS || classKind == ClassKind.INTERFACE && !klass.isSealed) {
                reporter.reportOn(source, KtErrorsParcelize.PARCELABLE_SHOULD_BE_CLASS, context)
                return
            }

            klass.companionObjectSymbol?.let { companionSymbol ->
                if (companionSymbol.classId.shortClassName == CREATOR_NAME) {
                    reporter.reportOn(companionSymbol.source, KtErrorsParcelize.CREATOR_DEFINITION_IS_NOT_ALLOWED, context)
                }
            }

            if (klass.isInner) {
                reporter.reportOn(source, KtErrorsParcelize.PARCELABLE_CANT_BE_INNER_CLASS, context)
            }

            if (klass.isLocal) {
                reporter.reportOn(source, KtErrorsParcelize.PARCELABLE_CANT_BE_LOCAL_CLASS, context)
            }
        } else if (classKind != ClassKind.ENUM_ENTRY) {
            reporter.reportOn(source, KtErrorsParcelize.PARCELABLE_SHOULD_BE_CLASS, context)
            return
        }

        if (classKind == ClassKind.CLASS && klass.isAbstract) {
            reporter.reportOn(source, KtErrorsParcelize.PARCELABLE_SHOULD_BE_INSTANTIABLE, context)
        }

        konst supertypes = lookupSuperTypes(klass, lookupInterfaces = true, deep = true, context.session, substituteTypes = false)
        if (supertypes.none { it.classId == PARCELABLE_ID }) {
            reporter.reportOn(source, KtErrorsParcelize.NO_PARCELABLE_SUPERTYPE, context)
        }

        klass.delegateFieldsMap?.forEach { (index, _) ->
            konst superTypeRef = klass.superTypeRefs[index]
            konst superType = superTypeRef.coneType
            konst parcelableType = ConeClassLikeTypeImpl(
                PARCELABLE_ID.toLookupTag(),
                emptyArray(),
                isNullable = false
            )
            if (superType.isSubtypeOf(parcelableType, context.session)) {
                reporter.reportOn(superTypeRef.source, KtErrorsParcelize.PARCELABLE_DELEGATE_IS_NOT_ALLOWED, context)
            }
        }

        konst constructorSymbols = klass.constructors(context.session)
        konst primaryConstructorSymbol = constructorSymbols.find { it.isPrimary }
        konst secondaryConstructorSymbols = constructorSymbols.filterNot { it.isPrimary }
        if (primaryConstructorSymbol == null && secondaryConstructorSymbols.isNotEmpty()) {
            reporter.reportOn(source, KtErrorsParcelize.PARCELABLE_SHOULD_HAVE_PRIMARY_CONSTRUCTOR, context)
        }
    }

    private fun checkParcelerClass(klass: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        if (klass !is FirRegularClass || klass.isCompanion) return
        for (superTypeRef in klass.superTypeRefs) {
            if (superTypeRef.coneType.classId == OLD_PARCELER_ID) {
                konst strategy = if (klass.name == SpecialNames.NO_NAME_PROVIDED) {
                    SourceElementPositioningStrategies.OBJECT_KEYWORD
                } else {
                    SourceElementPositioningStrategies.NAME_IDENTIFIER
                }
                reporter.reportOn(klass.source, KtErrorsParcelize.DEPRECATED_PARCELER, context, positioningStrategy = strategy)
            }
        }
    }
}

@OptIn(ExperimentalContracts::class)
fun FirClassSymbol<*>?.isParcelize(session: FirSession): Boolean {
    contract {
        returns(true) implies (this@isParcelize != null)
    }

    if (this == null) return false
    if (this.annotations.any { it.toAnnotationClassId(session) in PARCELIZE_CLASS_CLASS_IDS }) return true
    return resolvedSuperTypeRefs.any { superTypeRef ->
        konst symbol = superTypeRef.type.fullyExpandedType(session).toRegularClassSymbol(session) ?: return@any false
        symbol.annotations.any { it.toAnnotationClassId(session) in PARCELIZE_CLASS_CLASS_IDS }
    }
}

fun FirRegularClass.hasCustomParceler(session: FirSession): Boolean {
    konst companion = companionObjectSymbol ?: return false
    return lookupSuperTypes(companion, lookupInterfaces = true, deep = true, useSiteSession = session).any {
        it.classId in PARCELER_CLASS_IDS
    }
}
