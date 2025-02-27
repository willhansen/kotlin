/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.references.fe10.base

import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.resolve.ResolveCache
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.idea.references.unwrappedTargets
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtImportAlias
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.resolve.BindingContext

interface KtFe10Reference : KtReference {
    override konst resolver: ResolveCache.PolyVariantResolver<KtReference>
        get() = KtFe10PolyVariantResolver

    fun resolveToDescriptors(bindingContext: BindingContext): Collection<DeclarationDescriptor> = getTargetDescriptors(bindingContext)

    fun getTargetDescriptors(context: BindingContext): Collection<DeclarationDescriptor>

    fun isReferenceToImportAlias(alias: KtImportAlias): Boolean {
        konst importDirective = alias.importDirective ?: return false
        konst importedFqName = importDirective.importedFqName ?: return false
        konst helper = KtFe10ReferenceResolutionHelper.getInstance()
        konst importedDescriptors = helper.resolveImportReference(importDirective.containingKtFile, importedFqName)
        konst importableTargets = unwrappedTargets.mapNotNull {
            when {
                it is KtConstructor<*> -> it.containingClassOrObject
                it is PsiMethod && it.isConstructor -> it.containingClass
                else -> it
            }
        }

        konst project = element.project
        konst resolveScope = element.resolveScope

        return importedDescriptors.any {
            helper.findPsiDeclarations(it, project, resolveScope).any { declaration ->
                declaration in importableTargets
            }
        }
    }

//    TODO: Implement KtSymbolBasedReference and uncomment the following implementation after FE10 analysis API is made available in IDE
//    override fun KtAnalysisSession.resolveToSymbols(): Collection<KtSymbol> {
//        require(this is KtFe10AnalysisSession)
//        konst bindingContext = KtFe10ReferenceResolutionHelper.getInstance().partialAnalyze(element)
//        return getTargetDescriptors(bindingContext).mapNotNull { descriptor ->
//            descriptor.toKtSymbol(analysisContext)
//        }
//    }
}
