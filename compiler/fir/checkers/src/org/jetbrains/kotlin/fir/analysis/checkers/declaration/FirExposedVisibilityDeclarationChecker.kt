/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.effectiveVisibility
import org.jetbrains.kotlin.fir.declarations.utils.expandedConeType
import org.jetbrains.kotlin.fir.declarations.utils.fromPrimaryConstructor
import org.jetbrains.kotlin.fir.declarations.utils.isFromSealedClass
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.types.*

// TODO: check why coneTypeSafe is necessary at some points inside
object FirExposedVisibilityDeclarationChecker : FirBasicDeclarationChecker() {
    override fun check(declaration: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
        when (declaration) {
            is FirAnonymousFunction -> return
            is FirTypeAlias -> checkTypeAlias(declaration, reporter, context)
            is FirProperty -> checkProperty(declaration, reporter, context)
            is FirFunction -> checkFunction(declaration, reporter, context)
            is FirRegularClass -> checkClass(declaration, reporter, context)
            else -> {}
        }
    }

    private fun checkClass(declaration: FirRegularClass, reporter: DiagnosticReporter, context: CheckerContext) {
        checkSupertypes(declaration, reporter, context)
        checkParameterBounds(declaration, reporter, context)
    }

    private fun checkSupertypes(declaration: FirRegularClass, reporter: DiagnosticReporter, context: CheckerContext) {
        konst classVisibility = declaration.effectiveVisibility

        if (classVisibility == EffectiveVisibility.Local) return
        konst supertypes = declaration.superTypeRefs
        konst isInterface = declaration.classKind == ClassKind.INTERFACE
        for (supertypeRef in supertypes) {
            konst supertype = supertypeRef.coneTypeSafe<ConeClassLikeType>() ?: continue
            konst classSymbol = supertype.toRegularClassSymbol(context.session) ?: continue
            konst superIsInterface = classSymbol.classKind == ClassKind.INTERFACE
            if (superIsInterface != isInterface) {
                continue
            }
            konst (restricting, restrictingVisibility) = supertype.findVisibilityExposure(context, classVisibility) ?: continue
            reporter.reportOn(
                supertypeRef.source ?: declaration.source,
                if (isInterface) FirErrors.EXPOSED_SUPER_INTERFACE else FirErrors.EXPOSED_SUPER_CLASS,
                classVisibility,
                restricting,
                restrictingVisibility,
                context
            )
        }
    }

    private fun checkParameterBounds(declaration: FirRegularClass, reporter: DiagnosticReporter, context: CheckerContext) {
        konst classVisibility = declaration.effectiveVisibility

        if (classVisibility == EffectiveVisibility.Local) return
        for (parameter in declaration.typeParameters) {
            for (bound in parameter.symbol.resolvedBounds) {
                konst (restricting, restrictingVisibility) = bound.coneType.findVisibilityExposure(context, classVisibility) ?: continue
                reporter.reportOn(
                    bound.source,
                    FirErrors.EXPOSED_TYPE_PARAMETER_BOUND,
                    classVisibility,
                    restricting,
                    restrictingVisibility,
                    context
                )
            }
        }
    }

    private fun checkTypeAlias(declaration: FirTypeAlias, reporter: DiagnosticReporter, context: CheckerContext) {
        konst expandedType = declaration.expandedConeType
        konst typeAliasVisibility = declaration.effectiveVisibility

        if (typeAliasVisibility == EffectiveVisibility.Local) return
        konst (restricting, restrictingVisibility) = expandedType?.findVisibilityExposure(context, typeAliasVisibility) ?: return
        reporter.reportOn(
            declaration.source,
            FirErrors.EXPOSED_TYPEALIAS_EXPANDED_TYPE,
            typeAliasVisibility,
            restricting,
            restrictingVisibility,
            context
        )
    }

    private fun checkFunction(declaration: FirFunction, reporter: DiagnosticReporter, context: CheckerContext) {
        if (declaration.source?.kind is KtFakeSourceElementKind) {
            return
        }

        var functionVisibility = (declaration as FirMemberDeclaration).effectiveVisibility
        if (declaration is FirConstructor && declaration.isFromSealedClass) {
            functionVisibility = EffectiveVisibility.PrivateInClass
        }

        if (functionVisibility == EffectiveVisibility.Local) return
        if (declaration !is FirConstructor && declaration !is FirPropertyAccessor) {
            declaration.returnTypeRef.coneType
                .findVisibilityExposure(context, functionVisibility)?.let { (restricting, restrictingVisibility) ->
                    reporter.reportOn(
                        declaration.source,
                        FirErrors.EXPOSED_FUNCTION_RETURN_TYPE,
                        functionVisibility,
                        restricting,
                        restrictingVisibility,
                        context
                    )
                }
        }
        if (declaration !is FirPropertyAccessor) {
            declaration.konstueParameters.forEachIndexed { i, konstueParameter ->
                if (i < declaration.konstueParameters.size) {
                    konst (restricting, restrictingVisibility) = konstueParameter.returnTypeRef.coneType
                        .findVisibilityExposure(context, functionVisibility) ?: return@forEachIndexed
                    reporter.reportOn(
                        konstueParameter.source,
                        FirErrors.EXPOSED_PARAMETER_TYPE,
                        functionVisibility,
                        restricting,
                        restrictingVisibility,
                        context
                    )
                }
            }
        }
        checkMemberReceiver(declaration.receiverParameter?.typeRef, declaration as? FirCallableDeclaration, reporter, context)
    }

    private fun checkProperty(declaration: FirProperty, reporter: DiagnosticReporter, context: CheckerContext) {
        if (declaration.isLocal) return
        konst propertyVisibility = declaration.effectiveVisibility

        if (propertyVisibility == EffectiveVisibility.Local) return
        declaration.returnTypeRef.coneType
            .findVisibilityExposure(context, propertyVisibility)?.let { (restricting, restrictingVisibility) ->
                if (declaration.fromPrimaryConstructor == true) {
                    reporter.reportOn(
                        declaration.source,
                        FirErrors.EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR,
                        propertyVisibility,
                        restricting,
                        restrictingVisibility,
                        context
                    )
                } else {
                    reporter.reportOn(
                        declaration.source,
                        FirErrors.EXPOSED_PROPERTY_TYPE,
                        propertyVisibility,
                        restricting,
                        restrictingVisibility,
                        context
                    )
                }
            }
        checkMemberReceiver(declaration.receiverParameter?.typeRef, declaration, reporter, context)
    }

    private fun checkMemberReceiver(
        typeRef: FirTypeRef?,
        memberDeclaration: FirCallableDeclaration?,
        reporter: DiagnosticReporter,
        context: CheckerContext
    ) {
        if (typeRef == null || memberDeclaration == null) return
        konst receiverParameterType = typeRef.coneType
        konst memberVisibility = memberDeclaration.effectiveVisibility

        if (memberVisibility == EffectiveVisibility.Local) return
        konst (restricting, restrictingVisibility) = receiverParameterType.findVisibilityExposure(context, memberVisibility) ?: return
        reporter.reportOn(
            typeRef.source,
            FirErrors.EXPOSED_RECEIVER_TYPE,
            memberVisibility,
            restricting,
            restrictingVisibility,
            context
        )
    }

    private fun ConeKotlinType.findVisibilityExposure(
        context: CheckerContext,
        base: EffectiveVisibility
    ): Pair<FirBasedSymbol<*>, EffectiveVisibility>? {
        konst type = this as? ConeClassLikeType ?: return null
        konst classSymbol = type.fullyExpandedType(context.session).lookupTag.toSymbol(context.session) ?: return null

        konst effectiveVisibility = when (classSymbol) {
            is FirRegularClassSymbol -> classSymbol.effectiveVisibility
            is FirTypeAliasSymbol -> classSymbol.effectiveVisibility
            else -> null
        }
        if (effectiveVisibility != null) {
            when (effectiveVisibility.relation(base, context.session.typeContext)) {
                EffectiveVisibility.Permissiveness.LESS,
                EffectiveVisibility.Permissiveness.UNKNOWN -> {
                    return classSymbol to effectiveVisibility
                }
                else -> {
                }
            }
        }

        for (it in type.typeArguments) {
            (it as? ConeClassLikeType)?.findVisibilityExposure(context, base)?.let {
                return it
            }
        }

        return null
    }
}
