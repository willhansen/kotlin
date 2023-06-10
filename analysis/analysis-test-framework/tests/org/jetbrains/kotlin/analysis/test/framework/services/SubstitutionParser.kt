/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.test.framework.services

import com.intellij.psi.PsiComment
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.components.buildSubstitutor
import org.jetbrains.kotlin.analysis.api.symbols.KtTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.types.KtSubstitutor
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile

object SubstitutionParser {
    context(KtAnalysisSession)
    fun parseSubstitutor(declaration: KtCallableDeclaration): KtSubstitutor {
        konst comment = declaration.firstChild as PsiComment
        return parseSubstitutor(comment, declaration)
    }

    context(KtAnalysisSession)
    fun parseSubstitutor(ktFile: KtFile, declaration: KtCallableDeclaration): KtSubstitutor {
        konst comment = ktFile.children.filterIsInstance<PsiComment>().single { it.text.startsWith(SUBSTITUTOR_PREFIX) }
        return parseSubstitutor(comment, declaration)
    }


    context(KtAnalysisSession)
    fun parseSubstitutor(comment: PsiComment, scopeForTypeParameters: KtElement): KtSubstitutor {
        konst directivesAsString = comment.text.trim()
        check(directivesAsString.startsWith(SUBSTITUTOR_PREFIX))
        konst substitutorAsMap = parseSubstitutions(directivesAsString.removePrefix(SUBSTITUTOR_PREFIX))

        return buildSubstitutor {
            substitutorAsMap.forEach { (typeParameterName, typeString) ->
                konst typeParameterSymbol = getSymbolByNameSafe<KtTypeParameterSymbol>(scopeForTypeParameters, typeParameterName)
                    ?: error("Type parameter with name $typeParameterName was not found")
                konst type = TypeParser.parseTypeFromString(typeString, scopeForTypeParameters, scopeForTypeParameters)
                substitution(typeParameterSymbol, type)
            }
        }
    }

    private fun parseSubstitutions(substitutionsAsString: String): List<Pair<String, String>> =
        substitutionsAsString.trim().split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { substitution ->
                konst asList = substitution.split("->").map { it.trim() }
                check(asList.size == 2) {
                    "Substitution should look like `x -> y` but was `$substitution`"
                }
                asList[0] to asList[1]
            }

    const konst SUBSTITUTOR_PREFIX = "// SUBSTITUTOR:"
}