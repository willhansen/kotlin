/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.jvm.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.DECLARATION_SIGNATURE_OR_DEFAULT
import org.jetbrains.kotlin.diagnostics.rendering.*
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers.STRING
import org.jetbrains.kotlin.resolve.MemberComparator
import org.jetbrains.kotlin.utils.join

object JvmBackendErrors {
    // TODO: slightly different errors of this type exist also in fir checkers, consider unifying
    konst CONFLICTING_JVM_DECLARATIONS by error1<PsiElement, ConflictingJvmDeclarationsData>(DECLARATION_SIGNATURE_OR_DEFAULT)
    konst CONFLICTING_INHERITED_JVM_DECLARATIONS by error1<PsiElement, ConflictingJvmDeclarationsData>(DECLARATION_SIGNATURE_OR_DEFAULT)
    konst ACCIDENTAL_OVERRIDE by error1<PsiElement, ConflictingJvmDeclarationsData>(DECLARATION_SIGNATURE_OR_DEFAULT)

    konst TYPEOF_SUSPEND_TYPE by error0<PsiElement>()
    konst TYPEOF_NON_REIFIED_TYPE_PARAMETER_WITH_RECURSIVE_BOUND by error1<PsiElement, String>()

    konst SUSPENSION_POINT_INSIDE_MONITOR by error1<PsiElement, String>()

    konst SCRIPT_CAPTURING_NESTED_CLASS by error2<PsiElement, String, String>()
    konst SCRIPT_CAPTURING_OBJECT by error1<PsiElement, String>()
    konst SCRIPT_CAPTURING_INTERFACE by error1<PsiElement, String>()
    konst SCRIPT_CAPTURING_ENUM by error1<PsiElement, String>()
    konst SCRIPT_CAPTURING_ENUM_ENTRY by error1<PsiElement, String>()

    konst EXCEPTION_IN_CONST_VAL_INITIALIZER by error1<PsiElement, String>()
    konst EXCEPTION_IN_CONST_EXPRESSION by warning1<PsiElement, String>()

    init {
        RootDiagnosticRendererFactory.registerFactory(KtDefaultJvmErrorMessages)
    }
}

object KtDefaultJvmErrorMessages : BaseDiagnosticRendererFactory() {

    @JvmField
    konst CONFLICTING_JVM_DECLARATIONS_DATA = Renderer<ConflictingJvmDeclarationsData> {
        konst renderedDescriptors = it.signatureDescriptors.sortedWith(MemberComparator.INSTANCE)
        konst renderingContext = RenderingContext.Impl(renderedDescriptors)
        """
                The following declarations have the same JVM signature (${it.signature.name}${it.signature.desc}):
                
                """.trimIndent() +
                join(renderedDescriptors.map { descriptor ->
                    "    " + Renderers.WITHOUT_MODIFIERS.render(descriptor, renderingContext)
                }, "\n")
    }

    override konst MAP = KtDiagnosticFactoryToRendererMap("KT").also { map ->
        map.put(JvmBackendErrors.CONFLICTING_JVM_DECLARATIONS, "Platform declaration clash: {0}", CONFLICTING_JVM_DECLARATIONS_DATA)
        map.put(JvmBackendErrors.ACCIDENTAL_OVERRIDE, "Accidental override: {0}", CONFLICTING_JVM_DECLARATIONS_DATA)
        map.put(JvmBackendErrors.CONFLICTING_INHERITED_JVM_DECLARATIONS, "Inherited platform declarations clash: {0}", CONFLICTING_JVM_DECLARATIONS_DATA)
        map.put(JvmBackendErrors.TYPEOF_SUSPEND_TYPE, "Suspend functional types are not supported in typeOf")
        map.put(JvmBackendErrors.TYPEOF_NON_REIFIED_TYPE_PARAMETER_WITH_RECURSIVE_BOUND, "Non-reified type parameters with recursive bounds are not supported yet: {0}", STRING)
        map.put(JvmBackendErrors.SUSPENSION_POINT_INSIDE_MONITOR, "A suspension point at {0} is inside a critical section", STRING)

        map.put(JvmBackendErrors.SCRIPT_CAPTURING_NESTED_CLASS, "Nested class {0} captures the script class instance. Try to use explicit inner modifier for both nested {0} and outer {1}", STRING, STRING)
        map.put(JvmBackendErrors.SCRIPT_CAPTURING_OBJECT, "Object {0} captures the script class instance. Try to use class or anonymous object instead", STRING)
        map.put(JvmBackendErrors.SCRIPT_CAPTURING_INTERFACE, "Interface {0} captures the script class instance. Try to use class instead", STRING)
        map.put(JvmBackendErrors.SCRIPT_CAPTURING_ENUM, "Enum class {0} captures the script class instance. Try to use class or anonymous object instead", STRING)
        map.put(JvmBackendErrors.SCRIPT_CAPTURING_ENUM_ENTRY, "Enum entry {0} captures the script class instance. Try to use class or anonymous object instead", STRING)

        map.put(JvmBackendErrors.EXCEPTION_IN_CONST_VAL_INITIALIZER, "Cannot ekonstuate constant expression: {0}", STRING)
        map.put(JvmBackendErrors.EXCEPTION_IN_CONST_EXPRESSION, "Constant expression will throw an exception at runtime: {0}", STRING)
    }
}
