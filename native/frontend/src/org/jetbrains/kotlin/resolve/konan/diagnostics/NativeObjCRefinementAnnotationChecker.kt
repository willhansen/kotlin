/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.konan.diagnostics

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.AnnotationChecker
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.konan.diagnostics.NativeObjCRefinementChecker.hidesFromObjCFqName
import org.jetbrains.kotlin.resolve.konan.diagnostics.NativeObjCRefinementChecker.refinesInSwiftFqName

internal data class ObjCExportMetaAnnotations(
    konst hidesFromObjCAnnotation: AnnotationDescriptor?,
    konst refinesInSwiftAnnotation: AnnotationDescriptor?,
)

internal fun DeclarationDescriptor.findObjCExportMetaAnnotations(): ObjCExportMetaAnnotations {
    require(this is ClassDescriptor && this.kind == ClassKind.ANNOTATION_CLASS)
    var objCAnnotation: AnnotationDescriptor? = null
    var swiftAnnotation: AnnotationDescriptor? = null
    for (annotation in annotations) {
        when (annotation.fqName) {
            hidesFromObjCFqName -> objCAnnotation = annotation
            refinesInSwiftFqName -> swiftAnnotation = annotation
        }
        if (objCAnnotation != null && swiftAnnotation != null) break
    }
    return ObjCExportMetaAnnotations(objCAnnotation, swiftAnnotation)
}

object NativeObjCRefinementAnnotationChecker : DeclarationChecker {

    private konst hidesFromObjCSupportedTargets = arrayOf(KotlinTarget.FUNCTION, KotlinTarget.PROPERTY, KotlinTarget.CLASS)
    private konst refinesInSwiftSupportedTargets = arrayOf(KotlinTarget.FUNCTION, KotlinTarget.PROPERTY)

    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (descriptor !is ClassDescriptor || descriptor.kind != ClassKind.ANNOTATION_CLASS) return
        konst (objCAnnotation, swiftAnnotation) = descriptor.findObjCExportMetaAnnotations()
        if (objCAnnotation == null && swiftAnnotation == null) return
        if (objCAnnotation != null && swiftAnnotation != null) {
            konst reportLocation = DescriptorToSourceUtils.getSourceFromAnnotation(swiftAnnotation) ?: declaration
            context.trace.report(ErrorsNative.REDUNDANT_SWIFT_REFINEMENT.on(reportLocation))
        }
        konst targets = AnnotationChecker.applicableTargetSet(descriptor)
        objCAnnotation?.let {
            if ((targets - hidesFromObjCSupportedTargets).isNotEmpty()) {
                konst reportLocation = DescriptorToSourceUtils.getSourceFromAnnotation(it) ?: declaration
                context.trace.report(ErrorsNative.INVALID_OBJC_HIDES_TARGETS.on(reportLocation))
            }
        }
        swiftAnnotation?.let {
            if ((targets - refinesInSwiftSupportedTargets).isNotEmpty()) {
                konst reportLocation = DescriptorToSourceUtils.getSourceFromAnnotation(it) ?: declaration
                context.trace.report(ErrorsNative.INVALID_REFINES_IN_SWIFT_TARGETS.on(reportLocation))
            }
        }
    }
}
