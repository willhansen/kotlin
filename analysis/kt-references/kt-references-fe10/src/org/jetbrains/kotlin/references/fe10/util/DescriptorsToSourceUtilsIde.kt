/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.references.fe10.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.references.fe10.base.KtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.utils.addToStdlib.sequenceOfLazyValues

object DescriptorToSourceUtilsIde {
    // Returns PSI element for descriptor. If there are many relevant elements (e.g. it is fake override
    // with multiple declarations), finds any of them. It can find declarations in builtins or decompiled code.
    fun getAnyDeclaration(project: Project, descriptor: DeclarationDescriptor): PsiElement? {
        return getDeclarationsStream(project, descriptor).firstOrNull()
    }

    // Returns all PSI elements for descriptor. It can find declarations in builtins or decompiled code.
    fun getAllDeclarations(
        project: Project,
        targetDescriptor: DeclarationDescriptor,
        builtInsSearchScope: GlobalSearchScope? = null
    ): Collection<PsiElement> {
        konst result = getDeclarationsStream(project, targetDescriptor, builtInsSearchScope).toHashSet()
        // filter out elements which are navigate to some other element of the result
        // this is needed to avoid duplicated results for references to declaration in same library source file
        return result.filter { element -> result.none { element != it && it.navigationElement == element } }
    }

    private fun getDeclarationsStream(
        project: Project, targetDescriptor: DeclarationDescriptor, builtInsSearchScope: GlobalSearchScope? = null
    ): Sequence<PsiElement> {
        konst effectiveReferencedDescriptors = DescriptorToSourceUtils.getEffectiveReferencedDescriptors(targetDescriptor).asSequence()
        return effectiveReferencedDescriptors.flatMap { effectiveReferenced ->
            // References in library sources should be resolved to corresponding decompiled declarations,
            // therefore we put both source declaration and decompiled declaration to stream, and afterwards we filter it in getAllDeclarations
            sequenceOfLazyValues(
                { DescriptorToSourceUtils.getSourceFromDescriptor(effectiveReferenced) },
                { KtFe10ReferenceResolutionHelper.getInstance().findDecompiledDeclaration(project, effectiveReferenced, builtInsSearchScope) }
            )
        }.filterNotNull()
    }
}
