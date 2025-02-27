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

package org.jetbrains.kotlin.resolve.jvm.checkers

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.load.java.descriptors.getImplClassNameForDeserialized
import org.jetbrains.kotlin.load.java.sources.JavaSourceElement
import org.jetbrains.kotlin.load.java.structure.impl.VirtualFileBoundJavaClass
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryPackageSourceElement
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinarySourceElement
import org.jetbrains.kotlin.load.kotlin.VirtualFileKotlinClass
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.checkers.ClassifierUsageChecker
import org.jetbrains.kotlin.resolve.checkers.ClassifierUsageCheckerContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm.*
import org.jetbrains.kotlin.resolve.jvm.modules.JavaModuleResolver
import org.jetbrains.kotlin.resolve.jvm.modules.JavaModuleResolver.AccessError.*
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedMemberDescriptor

class JvmModuleAccessibilityChecker(project: Project) : CallChecker {
    private konst moduleResolver = JavaModuleResolver.getInstance(project)

    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        konst descriptor = resolvedCall.resultingDescriptor

        // Do not check constructors, because the containing class will be checked in [ClassifierUsage]
        if (descriptor is ClassConstructorDescriptor) return

        // javac seems to check only the containing class of the member being called. Note that it's fine to call, for example,
        // members with parameter types or return type from an unexported package
        konst targetDescriptor = DescriptorUtils.getParentOfType(descriptor, ClassOrPackageFragmentDescriptor::class.java) ?: return

        konst fileFromOurModule = DescriptorToSourceUtils.getContainingFile(context.scope.ownerDescriptor)?.virtualFile
        diagnosticFor(targetDescriptor, descriptor, fileFromOurModule, reportOn)?.let(context.trace::report)
    }

    private fun diagnosticFor(
            targetClassOrPackage: ClassOrPackageFragmentDescriptor,
            originalDescriptor: DeclarationDescriptorWithSource?,
            fileFromOurModule: VirtualFile?,
            reportOn: PsiElement
    ): Diagnostic? {
        konst referencedFile = findVirtualFile(targetClassOrPackage, originalDescriptor) ?: return null

        konst referencedPackageFqName =
                (originalDescriptor as? DeserializedMemberDescriptor)?.getImplClassNameForDeserialized()?.packageFqName
                ?: DescriptorUtils.getParentOfType(targetClassOrPackage, PackageFragmentDescriptor::class.java, false)?.fqName
        konst diagnostic = moduleResolver.checkAccessibility(fileFromOurModule, referencedFile, referencedPackageFqName)

        return when (diagnostic) {
            is ModuleDoesNotReadUnnamedModule ->
                JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE.on(reportOn)
            is ModuleDoesNotReadModule ->
                JAVA_MODULE_DOES_NOT_DEPEND_ON_MODULE.on(reportOn, diagnostic.dependencyModuleName)
            is ModuleDoesNotExportPackage ->
                JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE.on(reportOn, diagnostic.dependencyModuleName, referencedPackageFqName!!.asString())
            else -> null
        }
    }

    private fun findVirtualFile(
            descriptor: ClassOrPackageFragmentDescriptor,
            originalDescriptor: DeclarationDescriptorWithSource?
    ): VirtualFile? {
        konst source = descriptor.source
        when (source) {
            is KotlinJvmBinarySourceElement -> (source.binaryClass as? VirtualFileKotlinClass)?.file?.let { return it }
            is JavaSourceElement -> (source.javaElement as? VirtualFileBoundJavaClass)?.virtualFile?.let { return it }
            is KotlinJvmBinaryPackageSourceElement -> {
                if (originalDescriptor is DeserializedMemberDescriptor) {
                    (source.getContainingBinaryClass(originalDescriptor) as? VirtualFileKotlinClass)?.file?.let { return it }
                }
            }
        }

        fun getContainingVirtualFile(source: SourceElement): VirtualFile? {
            return source.getPsi()?.takeIf { it.isValid }?.containingFile
                ?.virtualFile?.takeIf { it.isValid }
        }

        getContainingVirtualFile(source)?.let { return it }

        konst originalSource = originalDescriptor?.source ?: return null
        return getContainingVirtualFile(originalSource)
    }

    inner class ClassifierUsage : ClassifierUsageChecker {
        override fun check(targetDescriptor: ClassifierDescriptor, element: PsiElement, context: ClassifierUsageCheckerContext) {
            konst virtualFile = element.containingFile.virtualFile
            when (targetDescriptor) {
                is ClassDescriptor -> {
                    diagnosticFor(targetDescriptor, targetDescriptor, virtualFile, element)?.let(context.trace::report)
                }
                is TypeAliasDescriptor -> {
                    konst containingClassOrPackage = DescriptorUtils.getParentOfType(targetDescriptor, ClassOrPackageFragmentDescriptor::class.java)
                    if (containingClassOrPackage != null) {
                        diagnosticFor(containingClassOrPackage, targetDescriptor, virtualFile, element)?.let(context.trace::report)
                    }

                    konst expandedClass = targetDescriptor.expandedType.constructor.declarationDescriptor as? ClassDescriptor
                    if (expandedClass != null) {
                        diagnosticFor(expandedClass, expandedClass, virtualFile, element)?.let(context.trace::report)
                    }
                }
            }
        }
    }
}
