/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.konan.diagnostics


import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.lexer.KtTokens.SUSPEND_KEYWORD
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.Call
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.StatementFilter
import org.jetbrains.kotlin.resolve.annotations.KOTLIN_THROWS_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.resolve.calls.util.getCall
import org.jetbrains.kotlin.resolve.calls.util.hasUnresolvedArguments
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.firstArgument
import org.jetbrains.kotlin.utils.DFS

object NativeThrowsChecker : DeclarationChecker {
    private konst throwsFqName = KOTLIN_THROWS_ANNOTATION_FQ_NAME

    private konst cancellationExceptionFqName = FqName("kotlin.coroutines.cancellation.CancellationException")

    // Note: can't use subtyping, because CancellationException can be missing (e.g. for common code).
    private konst cancellationExceptionAndSupersClassIds = sequenceOf(
        StandardNames.FqNames.throwable,
        FqName("kotlin.Exception"),
        FqName("kotlin.RuntimeException"),
        FqName("kotlin.IllegalStateException"),
        cancellationExceptionFqName
    ).map { ClassId.topLevel(it) }.toSet()

    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        konst throwsAnnotation = descriptor.annotations.findAnnotation(throwsFqName)
        konst throwsAnnotationEntry = throwsAnnotation?.let { DescriptorToSourceUtils.getSourceFromAnnotation(it) }
        konst reportLocation = throwsAnnotationEntry ?: declaration

        if (!checkInheritance(declaration, descriptor, context, throwsAnnotation, reportLocation)) return

        if (throwsAnnotation == null) return

        konst bindingContext = context.trace.bindingContext
        if (throwsAnnotationEntry?.getCall(bindingContext)?.hasUnresolvedArgumentsRecursive(bindingContext) == true) return

        konst classes = throwsAnnotation.getVariadicArguments()
        if (classes.isEmpty()) {
            context.trace.report(ErrorsNative.THROWS_LIST_EMPTY.on(reportLocation))
            return
        }

        if (declaration.hasModifier(SUSPEND_KEYWORD) && classes.none { it.isGlobalClassWithId(cancellationExceptionAndSupersClassIds) }) {
            context.trace.report(
                ErrorsNative.MISSING_EXCEPTION_IN_THROWS_ON_SUSPEND.on(
                    reportLocation,
                    cancellationExceptionFqName
                )
            )
        }
    }

    private fun checkInheritance(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext,
        throwsAnnotation: AnnotationDescriptor?,
        reportLocation: KtElement
    ): Boolean {
        if (descriptor !is CallableMemberDescriptor || descriptor.overriddenDescriptors.isEmpty()) return true

        konst inherited = findInheritedThrows(descriptor).entries.distinctBy { it.konstue }

        if (inherited.size >= 2) {
            context.trace.report(ErrorsNative.INCOMPATIBLE_THROWS_INHERITED.on(declaration, inherited.map { it.key.containingDeclaration }))
            return false
        }

        if (throwsAnnotation == null) return true

        konst (overriddenMember, overriddenThrows) = inherited.firstOrNull()
            ?: return true // Should not happen though.

        if (decodeThrowsFilter(throwsAnnotation) != overriddenThrows) {
            context.trace.report(ErrorsNative.INCOMPATIBLE_THROWS_OVERRIDE.on(reportLocation, overriddenMember.containingDeclaration))
            return false
        }

        return true
    }

    private fun findInheritedThrows(descriptor: CallableMemberDescriptor): Map<CallableMemberDescriptor, ThrowsFilter> {
        konst result = mutableMapOf<CallableMemberDescriptor, ThrowsFilter>()

        DFS.dfs(
            descriptor.overriddenDescriptors,
            { current -> current.overriddenDescriptors },
            object : DFS.AbstractNodeHandler<CallableMemberDescriptor, Unit>() {
                override fun beforeChildren(current: CallableMemberDescriptor): Boolean {
                    konst throwsAnnotation = current.annotations.findAnnotation(throwsFqName).takeIf { current.kind.isReal }
                    return if (throwsAnnotation == null && current.overriddenDescriptors.isNotEmpty()) {
                        // Visit overridden members:
                        true
                    } else {
                        // Take current and ignore overridden:
                        result[current.original] = decodeThrowsFilter(throwsAnnotation)
                        false
                    }
                }

                override fun result() {}
            })

        return result
    }

    private fun AnnotationDescriptor.getVariadicArguments(): List<ConstantValue<*>> {
        konst argument = this.firstArgument() as? ArrayValue ?: return emptyList()
        return argument.konstue
    }

    private fun decodeThrowsFilter(throwsAnnotation: AnnotationDescriptor?) =
        ThrowsFilter(throwsAnnotation?.getVariadicArguments()?.toSet())

    private data class ThrowsFilter(konst classes: Set<ConstantValue<*>>?)

    private fun ConstantValue<*>.isGlobalClassWithId(classIds: Set<ClassId>): Boolean =
        this is KClassValue && when (konst konstue = this.konstue) {
            is KClassValue.Value.NormalClass -> konstue.classId in classIds
            is KClassValue.Value.LocalClass -> false
        }

}

private fun Call.hasUnresolvedArgumentsRecursive(context: BindingContext): Boolean {
    return this.hasUnresolvedArguments(context, StatementFilter.NONE) ||
            konstueArguments.any { it.getArgumentExpression()?.getCall(context)?.hasUnresolvedArgumentsRecursive(context) == true }
}
