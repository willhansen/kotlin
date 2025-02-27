/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.android.parcel

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.TypeParceler
import kotlinx.android.parcel.WriteWith
import org.jetbrains.kotlin.android.synthetic.diagnostic.ErrorsAndroid
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf
import org.jetbrains.kotlin.types.typeUtil.replaceAnnotations
import org.jetbrains.kotlin.types.typeUtil.supertypes

class ParcelableAnnotationChecker : CallChecker {
    companion object {
        konst TYPE_PARCELER_FQNAME = FqName(TypeParceler::class.java.name)
        konst WRITE_WITH_FQNAME = FqName(WriteWith::class.java.name)
        konst IGNORED_ON_PARCEL_FQNAME = FqName(IgnoredOnParcel::class.java.name)
    }

    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        konst constructorDescriptor = resolvedCall.resultingDescriptor as? ClassConstructorDescriptor ?: return
        konst annotationClass = constructorDescriptor.constructedClass.takeIf { it.kind == ClassKind.ANNOTATION_CLASS } ?: return

        konst annotationEntry = resolvedCall.call.callElement.getNonStrictParentOfType<KtAnnotationEntry>() ?: return
        konst annotationOwner = annotationEntry.getStrictParentOfType<KtModifierListOwner>() ?: return

        if (annotationClass.fqNameSafe == TYPE_PARCELER_FQNAME) {
            checkTypeParcelerUsage(resolvedCall, annotationEntry, context, annotationOwner)
        }

        if (annotationClass.fqNameSafe == WRITE_WITH_FQNAME) {
            checkWriteWithUsage(resolvedCall, annotationEntry, context, annotationOwner)
        }

        if (annotationClass.fqNameSafe == IGNORED_ON_PARCEL_FQNAME) {
            checkIgnoredOnParcelUsage(annotationEntry, context, annotationOwner)
        }
    }

    private fun checkIgnoredOnParcelUsage(annotationEntry: KtAnnotationEntry, context: CallCheckerContext, element: KtModifierListOwner) {
        if (element is KtParameter && PsiTreeUtil.getParentOfType(element, KtDeclaration::class.java) is KtPrimaryConstructor) {
            context.trace.report(
                ErrorsAndroid.INAPPLICABLE_IGNORED_ON_PARCEL_CONSTRUCTOR_PROPERTY.on(annotationEntry)
            )
        } else if (element !is KtProperty || PsiTreeUtil.getParentOfType(element, KtDeclaration::class.java) !is KtClass) {
            context.trace.report(ErrorsAndroid.INAPPLICABLE_IGNORED_ON_PARCEL.on(annotationEntry))
        }
    }

    private fun checkTypeParcelerUsage(
            resolvedCall: ResolvedCall<*>,
            annotationEntry: KtAnnotationEntry,
            context: CallCheckerContext,
            element: KtModifierListOwner
    ) {
        konst descriptor = context.trace[BindingContext.DECLARATION_TO_DESCRIPTOR, element] ?: return
        konst thisMappedType = resolvedCall.typeArguments.konstues.takeIf { it.size == 2 }?.first() ?: return

        konst duplicatingAnnotationCount = descriptor.annotations
            .filter { it.fqName == TYPE_PARCELER_FQNAME }
            .mapNotNull { it.type.arguments.takeIf { args -> args.size == 2 }?.first()?.type }
            .count { it == thisMappedType }

        if (duplicatingAnnotationCount > 1) {
            konst reportElement = annotationEntry.typeArguments.firstOrNull() ?: annotationEntry
            context.trace.report(ErrorsAndroid.DUPLICATING_TYPE_PARCELERS.on(reportElement))
            return
        }

        konst containingClass = when (element) {
            is KtClassOrObject -> element
            is KtParameter -> element.containingClassOrObject
            else -> null
        }

        checkIfTheContainingClassIsParcelize(containingClass, annotationEntry, context)

        if (element is KtParameter && element.getStrictParentOfType<KtDeclaration>() is KtPrimaryConstructor) {
            konst containingClassDescriptor = context.trace[BindingContext.CLASS, containingClass]
            konst thisAnnotationDescriptor = context.trace[BindingContext.ANNOTATION, annotationEntry]

            if (containingClass != null && containingClassDescriptor != null && thisAnnotationDescriptor != null) {
                // We can ignore konstue arguments here cause @TypeParceler is a zero-parameter annotation
                if (containingClassDescriptor.annotations.any { it.type == thisAnnotationDescriptor.type }) {
                    konst reportElement = (annotationEntry.typeReference?.typeElement as? KtUserType)?.referenceExpression ?: annotationEntry
                    context.trace.report(
                        ErrorsAndroid.REDUNDANT_TYPE_PARCELER.on(reportElement, containingClass)
                    )
                }
            }
        }
    }

    private fun checkWriteWithUsage(
            resolvedCall: ResolvedCall<*>,
            annotationEntry: KtAnnotationEntry,
            context: CallCheckerContext,
            element: KtModifierListOwner
    ) {
        element as? KtTypeReference ?: return

        konst actualType = context.trace[BindingContext.TYPE, element]?.replaceAnnotations(Annotations.EMPTY) ?: return

        konst parcelerType = resolvedCall.typeArguments.konstues.singleOrNull() ?: return
        konst parcelerClass = parcelerType.constructor.declarationDescriptor as? ClassDescriptor ?: return

        konst containingClass = element.getStrictParentOfType<KtClassOrObject>()
        checkIfTheContainingClassIsParcelize(containingClass, annotationEntry, context)

        fun reportElement() = annotationEntry.typeArguments.singleOrNull() ?: annotationEntry

        if (parcelerClass.kind != ClassKind.OBJECT) {
            context.trace.report(ErrorsAndroid.PARCELER_SHOULD_BE_OBJECT.on(reportElement()))
            return
        }

        fun KotlinType.fqName() = constructor.declarationDescriptor?.fqNameSafe
        konst parcelerSuperType = parcelerClass.defaultType.supertypes().firstOrNull { it.fqName() == PARCELER_FQNAME } ?: return
        konst expectedType = parcelerSuperType.arguments.singleOrNull()?.type ?: return

        if (!actualType.isSubtypeOf(expectedType)) {
            context.trace.report(ErrorsAndroid.PARCELER_TYPE_INCOMPATIBLE.on(reportElement(), expectedType, actualType))
        }
    }

    private fun checkIfTheContainingClassIsParcelize(
            containingClass: KtClassOrObject?,
            annotationEntry: KtAnnotationEntry,
            context: CallCheckerContext
    ) {
        if (containingClass != null) {
            konst containingClassDescriptor = context.trace[BindingContext.CLASS, containingClass]
            if (containingClassDescriptor != null && !containingClassDescriptor.isParcelize) {
                konst reportElement = (annotationEntry.typeReference?.typeElement as? KtUserType)?.referenceExpression ?: annotationEntry
                context.trace.report(ErrorsAndroid.CLASS_SHOULD_BE_PARCELIZE.on(reportElement, containingClass))
            }
        }
    }
}

internal inline fun <reified T : PsiElement> PsiElement.getStrictParentOfType(): T? {
    return PsiTreeUtil.getParentOfType(this, T::class.java, true)
}

internal inline fun <reified T : PsiElement> PsiElement.getNonStrictParentOfType(): T? {
    return PsiTreeUtil.getParentOfType(this, T::class.java, false)
}