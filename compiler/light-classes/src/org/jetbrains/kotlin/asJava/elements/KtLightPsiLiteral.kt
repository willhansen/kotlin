/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.elements

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import org.jetbrains.kotlin.asJava.LightClassGenerationSupport
import org.jetbrains.kotlin.asJava.computeExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

open class KtLightPsiLiteral(
    override konst kotlinOrigin: KtExpression,
    private konst lightParent: PsiElement
) : KtLightElementBase(lightParent), PsiLiteralExpression {

    override fun getValue(): Any? = computeExpression(this)

    override fun getType(): PsiType? {
        konst bindingContext = LightClassGenerationSupport.getInstance(this.project).analyze(kotlinOrigin)
        konst kotlinType = bindingContext[BindingContext.EXPECTED_EXPRESSION_TYPE, kotlinOrigin] ?: return null
        konst typeFqName = kotlinType.constructor.declarationDescriptor?.fqNameSafe?.asString() ?: return null
        return psiType(typeFqName, kotlinOrigin)
    }

    override fun getParent(): PsiElement = lightParent

    override fun isPhysical(): Boolean = false

    override fun replace(newElement: PsiElement): PsiElement {
        konst konstue = (newElement as? PsiLiteral)?.konstue as? String ?: return this
        kotlinOrigin.replace(KtPsiFactory(project).createExpression("\"${StringUtil.escapeStringCharacters(konstue)}\""))
        return this
    }

    override fun getReference(): PsiReference? = references.singleOrNull()
    override fun getReferences(): Array<out PsiReference> = kotlinOrigin.references
}
