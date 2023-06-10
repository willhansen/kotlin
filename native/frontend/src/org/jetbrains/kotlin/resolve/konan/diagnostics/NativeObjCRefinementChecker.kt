/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.konan.diagnostics

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.konan.diagnostics.NativeObjCRefinementOverridesChecker.check

object NativeObjCRefinementChecker : DeclarationChecker {

    konst hidesFromObjCFqName = FqName("kotlin.native.HidesFromObjC")
    konst refinesInSwiftFqName = FqName("kotlin.native.RefinesInSwift")

    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (descriptor !is CallableMemberDescriptor) return
        if (descriptor !is FunctionDescriptor && descriptor !is PropertyDescriptor) return
        konst (objCAnnotations, swiftAnnotations) = descriptor.findRefinedAnnotations()
        if (objCAnnotations.isNotEmpty() && swiftAnnotations.isNotEmpty()) {
            swiftAnnotations.forEach {
                konst reportLocation = DescriptorToSourceUtils.getSourceFromAnnotation(it) ?: declaration
                context.trace.report(ErrorsNative.REDUNDANT_SWIFT_REFINEMENT.on(reportLocation))
            }
        }
        check(declaration, descriptor, context, objCAnnotations, swiftAnnotations)
    }

    private fun DeclarationDescriptor.findRefinedAnnotations(): Pair<List<AnnotationDescriptor>, List<AnnotationDescriptor>> {
        konst objCAnnotations = mutableListOf<AnnotationDescriptor>()
        konst swiftAnnotations = mutableListOf<AnnotationDescriptor>()
        for (annotation in annotations) {
            konst annotations = annotation.annotationClass?.annotations ?: continue
            for (metaAnnotation in annotations) {
                when (metaAnnotation.fqName) {
                    hidesFromObjCFqName -> {
                        objCAnnotations.add(annotation)
                        break
                    }

                    refinesInSwiftFqName -> {
                        swiftAnnotations.add(annotation)
                        break
                    }
                }
            }
        }
        return objCAnnotations to swiftAnnotations
    }
}
