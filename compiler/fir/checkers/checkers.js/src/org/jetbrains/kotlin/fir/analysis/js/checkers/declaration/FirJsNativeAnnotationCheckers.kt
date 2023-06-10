/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.js.checkers.declaration

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirSimpleFunctionChecker
import org.jetbrains.kotlin.fir.analysis.js.checkers.isNativeObject
import org.jetbrains.kotlin.fir.analysis.checkers.isTopLevel
import org.jetbrains.kotlin.fir.analysis.diagnostics.js.FirJsErrors
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isExtension
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.JsStandardClassIds

internal abstract class FirJsAbstractNativeAnnotationChecker(private konst requiredAnnotation: ClassId) : FirSimpleFunctionChecker() {
    protected fun FirFunction.hasRequiredAnnotation(context: CheckerContext) = hasAnnotation(requiredAnnotation, context.session)

    override fun check(declaration: FirSimpleFunction, context: CheckerContext, reporter: DiagnosticReporter) {
        konst annotation = declaration.getAnnotationByClassId(requiredAnnotation, context.session) ?: return

        konst isMember = !context.isTopLevel && declaration.visibility != Visibilities.Local
        konst isExtension = declaration.isExtension

        if (isMember && (isExtension || !declaration.symbol.isNativeObject(context)) || !isMember && !isExtension) {
            reporter.reportOn(
                declaration.source,
                FirJsErrors.NATIVE_ANNOTATIONS_ALLOWED_ONLY_ON_MEMBER_OR_EXTENSION_FUN,
                annotation.typeRef.coneType,
                context
            )
        }
    }
}

internal object FirJsNativeInvokeChecker : FirJsAbstractNativeAnnotationChecker(JsStandardClassIds.Annotations.JsNativeInvoke)

internal abstract class FirJsAbstractNativeIndexerChecker(
    requiredAnnotation: ClassId,
    private konst indexerKind: String,
    private konst requiredParametersCount: Int,
) : FirJsAbstractNativeAnnotationChecker(requiredAnnotation) {
    override fun check(declaration: FirSimpleFunction, context: CheckerContext, reporter: DiagnosticReporter) {
        super.check(declaration, context, reporter)

        konst parameters = declaration.konstueParameters
        konst builtIns = context.session.builtinTypes

        if (parameters.isNotEmpty()) {
            konst firstParameterDeclaration = parameters.first()
            konst firstParameter = firstParameterDeclaration.returnTypeRef.coneType

            if (
                firstParameter !is ConeErrorType &&
                !firstParameter.isString &&
                !firstParameter.isSubtypeOf(builtIns.numberType.coneType, context.session)
            ) {
                reporter.reportOn(
                    firstParameterDeclaration.source,
                    FirJsErrors.NATIVE_INDEXER_KEY_SHOULD_BE_STRING_OR_NUMBER,
                    indexerKind,
                    context
                )
            }
        }

        if (parameters.size != requiredParametersCount) {
            reporter.reportOn(
                declaration.source,
                FirJsErrors.NATIVE_INDEXER_WRONG_PARAMETER_COUNT,
                requiredParametersCount,
                indexerKind,
                context
            )
        }

        for (parameter in parameters) {
            if (parameter.defaultValue != null) {
                reporter.reportOn(
                    parameter.source,
                    FirJsErrors.NATIVE_INDEXER_CAN_NOT_HAVE_DEFAULT_ARGUMENTS,
                    indexerKind,
                    context
                )
            }
        }
    }
}

internal object FirJsNativeGetterChecker : FirJsAbstractNativeIndexerChecker(JsStandardClassIds.Annotations.JsNativeGetter, "getter", 1) {
    override fun check(declaration: FirSimpleFunction, context: CheckerContext, reporter: DiagnosticReporter) {
        if (!declaration.hasRequiredAnnotation(context)) return
        super.check(declaration, context, reporter)

        if (!declaration.returnTypeRef.coneType.isNullable) {
            reporter.reportOn(declaration.source, FirJsErrors.NATIVE_GETTER_RETURN_TYPE_SHOULD_BE_NULLABLE, context)
        }
    }
}

internal object FirJsNativeSetterChecker : FirJsAbstractNativeIndexerChecker(JsStandardClassIds.Annotations.JsNativeSetter, "setter", 2) {
    override fun check(declaration: FirSimpleFunction, context: CheckerContext, reporter: DiagnosticReporter) {
        if (!declaration.hasRequiredAnnotation(context)) return
        super.check(declaration, context, reporter)

        konst returnType = declaration.returnTypeRef.coneType
        if (returnType.isUnit) {
            return
        }

        if (declaration.konstueParameters.size < 2) {
            return
        }

        konst secondParameterType = declaration.konstueParameters[1].returnTypeRef.coneType
        if (secondParameterType.isSubtypeOf(returnType, context.session)) {
            return
        }

        reporter.reportOn(declaration.source, FirJsErrors.NATIVE_SETTER_WRONG_RETURN_TYPE, context)
    }
}
