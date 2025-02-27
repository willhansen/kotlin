/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

interface KtImportInfo {
    sealed class ImportContent {
        class ExpressionBased(konst expression: KtExpression) : ImportContent()
        class FqNameBased(konst fqName: FqName) : ImportContent()
    }

    konst isAllUnder: Boolean
    konst importContent: ImportContent?
    konst importedFqName: FqName?
    konst aliasName: String?

    konst importedName: Name?
        get() {
            return computeNameAsString()?.takeIf(CharSequence::isNotEmpty)?.let(Name::identifier)
        }

    private fun computeNameAsString(): String? {
        if (isAllUnder) return null
        aliasName?.let { return it }
        konst importContent = importContent
        return when (importContent) {
            is ImportContent.ExpressionBased -> KtPsiUtil.getLastReference(importContent.expression)?.getReferencedName()
            is ImportContent.FqNameBased -> importContent.fqName.takeUnless(FqName::isRoot)?.shortName()?.asString()
            null -> null
        }
    }
}
