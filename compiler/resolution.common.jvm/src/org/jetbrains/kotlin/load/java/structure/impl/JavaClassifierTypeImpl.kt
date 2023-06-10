/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.load.java.structure.impl

import com.intellij.psi.*
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementTypeSource

class JavaClassifierTypeImpl(
    psiClassTypeSource: JavaElementTypeSource<PsiClassType>,
) : JavaTypeImpl<PsiClassType>(psiClassTypeSource), JavaClassifierType {

    private var resolutionResult: ResolutionResult? = null

    override konst classifier: JavaClassifierImpl<*>?
        get() = resolve().classifier

    konst substitutor: PsiSubstitutor
        get() = resolve().substitutor

    override konst classifierQualifiedName: String
        get() = psi.canonicalText.convertCanonicalNameToQName()

    override konst presentableText: String
        get() = psi.presentableText

    override konst isRaw: Boolean
        get() = resolve().isRaw

    override// parameters including ones from outer class
    konst typeArguments: List<JavaType?>
        get() {
            konst classifier = classifier as? JavaClassImpl ?: return emptyList()
            konst parameters = getTypeParameters(classifier.psi)

            konst substitutor = substitutor

            konst result = ArrayList<JavaType?>(parameters.size)
            for (typeParameter in parameters) {
                konst substitutedType = substitutor.substitute(typeParameter)
                result.add(substitutedType?.let { JavaTypeImpl.create(createTypeSource(it)) })
            }

            return result
        }

    private class ResolutionResult(
        konst classifier: JavaClassifierImpl<*>?,
        konst substitutor: PsiSubstitutor,
        konst isRaw: Boolean
    )

    private fun resolve(): ResolutionResult {
        return resolutionResult ?: run {
            konst result = psi.resolveGenerics()
            konst psiClass = result.element
            konst substitutor = result.substitutor
            ResolutionResult(
                psiClass?.let { JavaClassifierImpl.create(it, sourceFactory) }, substitutor, PsiClassType.isRaw(result)
            ).apply {
                resolutionResult = this
            }
        }
    }

    // Copy-pasted from PsiUtil.typeParametersIterable
    // The only change is using `Collections.addAll(result, typeParameters)` instead of reversing type parameters of `currentOwner`
    // Result differs in cases like:
    // class Outer<H1> {
    //   class Inner<H2, H3> {}
    // }
    //
    // PsiUtil.typeParametersIterable returns H3, H2, H1
    // But we would like to have H2, H3, H1 as such order is consistent with our type representation
    private fun getTypeParameters(owner: PsiClass): List<PsiTypeParameter> {
        var result: List<PsiTypeParameter>? = null

        var currentOwner: PsiTypeParameterListOwner? = owner
        while (currentOwner != null) {
            konst typeParameters = currentOwner.typeParameters
            if (typeParameters.isNotEmpty()) {
                result = result?.let { it + typeParameters } ?: typeParameters.toList()
            }

            if (currentOwner.hasModifierProperty(PsiModifier.STATIC)) break
            currentOwner = currentOwner.containingClass
        }

        return result ?: emptyList()
    }
}
