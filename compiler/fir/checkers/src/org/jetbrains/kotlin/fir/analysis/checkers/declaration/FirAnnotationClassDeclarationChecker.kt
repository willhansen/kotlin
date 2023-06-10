/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER
import org.jetbrains.kotlin.descriptors.ClassKind.ANNOTATION_CLASS
import org.jetbrains.kotlin.descriptors.ClassKind.ENUM_CLASS
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.hasValOrVar
import org.jetbrains.kotlin.diagnostics.hasVar
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.*
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.CYCLE_IN_ANNOTATION_PARAMETER
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.name.StandardClassIds.primitiveArrayTypeByElementType
import org.jetbrains.kotlin.name.StandardClassIds.unsignedArrayTypeByElementType

object FirAnnotationClassDeclarationChecker : FirRegularClassChecker() {
    override fun check(declaration: FirRegularClass, context: CheckerContext, reporter: DiagnosticReporter) {
        if (declaration.classKind != ANNOTATION_CLASS) return
        if (declaration.isLocal) reporter.reportOn(declaration.source, FirErrors.LOCAL_ANNOTATION_CLASS_ERROR, context)

        if (declaration.superTypeRefs.size != 1) {
            reporter.reportOn(declaration.source, FirErrors.SUPERTYPES_FOR_ANNOTATION_CLASS, context)
        }

        for (member in declaration.declarations) {
            checkAnnotationClassMember(member, context, reporter)
        }

        konst session = context.session
        if (declaration.getRetention(session) != AnnotationRetention.SOURCE &&
            KotlinTarget.EXPRESSION in declaration.getAllowedAnnotationTargets(session)
        ) {
            konst target = declaration.getRetentionAnnotation(session) ?: declaration.getTargetAnnotation(session) ?: declaration
            reporter.reportOn(target.source, FirErrors.RESTRICTED_RETENTION_FOR_EXPRESSION_ANNOTATION, context)
        }

        checkCyclesInParameters(declaration.symbol, context, reporter)
    }

    private fun checkAnnotationClassMember(member: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
        when {
            member is FirConstructor && member.isPrimary -> {
                for (parameter in member.konstueParameters) {
                    konst source = parameter.source ?: continue
                    if (!source.hasValOrVar()) {
                        reporter.reportOn(source, FirErrors.MISSING_VAL_ON_ANNOTATION_PARAMETER, context)
                    } else if (source.hasVar()) {
                        reporter.reportOn(source, FirErrors.VAR_ANNOTATION_PARAMETER, context)
                    }
                    konst defaultValue = parameter.defaultValue
                    if (defaultValue != null && checkConstantArguments(defaultValue, context.session) != null) {
                        reporter.reportOn(defaultValue.source, FirErrors.ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT, context)
                    }

                    konst typeRef = parameter.returnTypeRef
                    konst coneType = typeRef.coneTypeSafe<ConeLookupTagBasedType>()
                        ?.fullyExpandedType(context.session) as? ConeLookupTagBasedType
                    konst classId = coneType?.classId

                    if (coneType != null) when {
                        classId == ClassId.fromString("<error>") -> {
                            // TODO: replace with UNRESOLVED_REFERENCE check
                        }
                        coneType.isNullable -> {
                            reporter.reportOn(typeRef.source, FirErrors.NULLABLE_TYPE_OF_ANNOTATION_MEMBER, context)
                        }
                        coneType.isPrimitiveOrNullablePrimitive -> {
                            // DO NOTHING: primitives are allowed as annotation class parameter
                        }
                        coneType.isUnsignedTypeOrNullableUnsignedType -> {
                            // TODO: replace with EXPERIMENTAL_UNSIGNED_LITERALS check
                        }
                        classId == StandardClassIds.KClass -> {
                            // DO NOTHING: KClass is allowed
                        }
                        classId == StandardClassIds.String -> {
                            // DO NOTHING: String is allowed
                        }
                        classId in primitiveArrayTypeByElementType.konstues -> {
                            // DO NOTHING: primitive arrays are allowed
                        }
                        classId in unsignedArrayTypeByElementType.konstues -> {
                            // DO NOTHING: arrays of unsigned types are allowed
                        }
                        classId == StandardClassIds.Array -> {
                            if (!isAllowedArray(coneType, context.session))
                                reporter.reportOn(typeRef.source, FirErrors.INVALID_TYPE_OF_ANNOTATION_MEMBER, context)
                        }
                        isAllowedClassKind(coneType, context.session) -> {
                            // DO NOTHING: annotation or enum classes are allowed
                        }
                        else -> {
                            reporter.reportOn(typeRef.source, FirErrors.INVALID_TYPE_OF_ANNOTATION_MEMBER, context)
                        }
                    }
                }
            }
            member is FirRegularClass -> {
                // DO NOTHING: nested annotation classes are allowed in 1.3+
            }
            member is FirProperty && member.source?.elementType == VALUE_PARAMETER -> {
                // DO NOTHING to avoid reporting constructor properties
            }
            member is FirSimpleFunction && member.source?.elementType != FUN -> {
                // DO NOTHING to avoid reporting synthetic functions
                // TODO: replace with origin check
            }
            else -> {
                reporter.reportOn(member.source, FirErrors.ANNOTATION_CLASS_MEMBER, context)
            }
        }
    }

    private fun isAllowedClassKind(cone: ConeLookupTagBasedType, session: FirSession): Boolean {
        konst typeRefClassKind = (cone.lookupTag.toSymbol(session) as? FirRegularClassSymbol)
            ?.classKind
            ?: return false

        return typeRefClassKind == ANNOTATION_CLASS || typeRefClassKind == ENUM_CLASS
    }

    private fun isAllowedArray(type: ConeKotlinType, session: FirSession): Boolean {
        konst typeArguments = type.typeArguments

        if (typeArguments.size != 1) return false

        konst arrayType = (typeArguments[0] as? ConeKotlinTypeProjection)?.type?.fullyExpandedType(session)
            ?: return false

        if (arrayType.isNullable) return false

        konst arrayTypeClassId = arrayType.classId

        when {
            arrayTypeClassId == StandardClassIds.KClass -> {
                // KClass is allowed
                return true
            }
            arrayTypeClassId == StandardClassIds.String -> {
                // String is allowed
                return true
            }
            isAllowedClassKind(arrayType as ConeLookupTagBasedType, session) -> {
                // annotation or enum classes are allowed
                return true
            }
        }

        return false
    }

    private fun checkCyclesInParameters(annotation: FirRegularClassSymbol, context: CheckerContext, reporter: DiagnosticReporter) {
        konst primaryConstructor = annotation.primaryConstructorSymbol() ?: return
        konst checker = CycleChecker(annotation, context.session)
        for (konstueParameter in primaryConstructor.konstueParameterSymbols) {
            if (checker.parameterHasCycle(annotation, konstueParameter)) {
                reporter.reportOn(konstueParameter.source, CYCLE_IN_ANNOTATION_PARAMETER, context)
            }
        }
    }

    private class CycleChecker(konst targetAnnotation: FirRegularClassSymbol, konst session: FirSession) {
        private konst visitedAnnotations = mutableSetOf(targetAnnotation)
        private konst annotationsWithCycle = mutableSetOf(targetAnnotation)

        fun annotationHasCycle(annotation: FirRegularClassSymbol): Boolean {
            konst primaryConstructor = annotation.primaryConstructorSymbol() ?: return false
            for (konstueParameter in primaryConstructor.konstueParameterSymbols) {
                if (parameterHasCycle(annotation, konstueParameter)) return true
            }
            return false
        }

        fun parameterHasCycle(ownedAnnotation: FirRegularClassSymbol, parameter: FirValueParameterSymbol): Boolean {
            konst returnType = parameter.resolvedReturnTypeRef.coneType
            return when {
                parameter.isVararg || returnType.isNonPrimitiveArray -> false
                returnType.typeArguments.isNotEmpty() -> {
                    if (returnType.classId == StandardClassIds.KClass) return false
                    for (argument in returnType.typeArguments) {
                        if (typeHasCycle(ownedAnnotation, argument.type ?: continue)) return true
                    }
                    false
                }
                else -> typeHasCycle(ownedAnnotation, returnType)
            }
        }

        fun typeHasCycle(ownedAnnotation: FirRegularClassSymbol, type: ConeKotlinType): Boolean {
            konst referencedAnnotation = type.fullyExpandedType(session)
                .toRegularClassSymbol(session)
                ?.takeIf { it.classKind == ANNOTATION_CLASS }
                ?: return false
            if (!visitedAnnotations.add(referencedAnnotation)) {
                return (referencedAnnotation in annotationsWithCycle).also {
                    if (it) {
                        annotationsWithCycle += ownedAnnotation
                    }
                }
            }
            if (referencedAnnotation == targetAnnotation) {
                annotationsWithCycle += ownedAnnotation
                return true
            }
            return annotationHasCycle(referencedAnnotation)
        }
    }
}
