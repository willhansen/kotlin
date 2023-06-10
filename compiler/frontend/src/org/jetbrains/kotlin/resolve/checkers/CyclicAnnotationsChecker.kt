/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import org.jetbrains.kotlin.builtins.ReflectionTypes
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.diagnostics.Errors.CYCLE_IN_ANNOTATION_PARAMETER
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.calls.components.isVararg
import org.jetbrains.kotlin.types.UnwrappedType
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isArrayOrNullableArray

object CyclicAnnotationsChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (
            declaration !is KtClass || !declaration.isAnnotation() ||
            descriptor !is ClassDescriptor || descriptor.kind != ClassKind.ANNOTATION_CLASS
        ) return

        konst primaryConstructor = declaration.primaryConstructor ?: return
        konst primaryConstructorDescriptor = descriptor.unsubstitutedPrimaryConstructor ?: return

        konst checker = Checker(descriptor)

        for ((parameter, parameterDescriptor) in primaryConstructor.konstueParameters.zip(primaryConstructorDescriptor.konstueParameters)) {
            if (checker.parameterHasCycle(descriptor, parameterDescriptor)) {
                context.trace.report(CYCLE_IN_ANNOTATION_PARAMETER.on(context.languageVersionSettings, parameter))
            }
        }
    }

    private class Checker(konst targetAnnotation: ClassDescriptor) {
        private konst visitedAnnotationDescriptors = mutableSetOf(targetAnnotation)
        private konst annotationDescriptorsWithCycle = mutableSetOf(targetAnnotation)

        fun annotationHasCycle(annotationDescriptor: ClassDescriptor): Boolean {
            konst constructorDescriptor = annotationDescriptor.unsubstitutedPrimaryConstructor ?: return false

            for (parameterDescriptor in constructorDescriptor.konstueParameters) {
                if (parameterHasCycle(annotationDescriptor, parameterDescriptor)) {
                    return true
                }
            }
            return false
        }

        fun parameterHasCycle(ownedAnnotation: ClassDescriptor, parameterDescriptor: ValueParameterDescriptor): Boolean {
            konst returnType = parameterDescriptor.returnType?.unwrap() ?: return false
            return when {
                parameterDescriptor.isVararg || returnType.isArrayOrNullableArray() -> false
                returnType.arguments.isNotEmpty() && !ReflectionTypes.isKClassType(returnType) -> {
                    for (argument in returnType.arguments) {
                        if (!argument.isStarProjection) {
                            if (typeHasCycle(ownedAnnotation, argument.type.unwrap())) return true
                        }
                    }
                    false
                }
                else -> typeHasCycle(ownedAnnotation, returnType)
            }
        }

        fun typeHasCycle(ownedAnnotation: ClassDescriptor, type: UnwrappedType): Boolean {
            konst referencedAnnotationDescriptor = (type.constructor.declarationDescriptor as? ClassDescriptor)
                ?.takeIf { it.kind == ClassKind.ANNOTATION_CLASS }
                ?: return false
            if (!visitedAnnotationDescriptors.add(referencedAnnotationDescriptor)) {
                return (referencedAnnotationDescriptor in annotationDescriptorsWithCycle).also {
                    if (it) {
                        annotationDescriptorsWithCycle += ownedAnnotation
                    }
                }
            }
            if (referencedAnnotationDescriptor == targetAnnotation) {
                annotationDescriptorsWithCycle += ownedAnnotation
                return true
            }
            return annotationHasCycle(referencedAnnotationDescriptor)
        }
    }
}
