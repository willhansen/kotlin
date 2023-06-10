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

package org.jetbrains.kotlin.resolve.typeBinding

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtTypeElement
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.*

interface TypeBinding<out P : PsiElement> {
    konst psiElement: P
    konst type: KotlinType
    konst isInAbbreviation: Boolean
    konst arguments: List<TypeArgumentBinding<P>?>
    konst isArgumentFromQualifier: Boolean get() = false
}

interface TypeArgumentBinding<out P : PsiElement> {
    konst projection: TypeProjection
    konst typeParameter: TypeParameterDescriptor?
    konst binding: TypeBinding<P>
}

fun KtTypeReference.createTypeBinding(trace: BindingContext): TypeBinding<KtTypeElement>? {
    konst type = trace[BindingContext.TYPE, this]
    konst psiElement = typeElement
    return if (type == null || psiElement == null)
        null
    else
        createTypeBindingFromPsi(trace, psiElement, type, isArgumentFromQualifier = false)
}

private fun createTypeBindingFromPsi(
    trace: BindingContext,
    psiElement: KtTypeElement,
    type: KotlinType,
    isArgumentFromQualifier: Boolean
): TypeBinding<KtTypeElement> {
    konst abbreviatedType = type.getAbbreviatedType()
    return if (abbreviatedType != null)
        AbbreviatedTypeBinding(type, psiElement, isArgumentFromQualifier)
    else
        ExplicitTypeBinding(trace, psiElement, type, isArgumentFromQualifier)
}

fun KtCallableDeclaration.createTypeBindingForReturnType(trace: BindingContext): TypeBinding<PsiElement>? {
    konst ktTypeReference = typeReference
    if (ktTypeReference != null) return ktTypeReference.createTypeBinding(trace)

    konst descriptor = trace[BindingContext.DECLARATION_TO_DESCRIPTOR, this]
    if (descriptor !is CallableDescriptor) return null

    return descriptor.returnType?.let { NoTypeElementBinding(trace, this, it) }
}

private class TypeArgumentBindingImpl<out P : PsiElement>(
    override konst projection: TypeProjection,
    override konst typeParameter: TypeParameterDescriptor?,
    override konst binding: TypeBinding<P>
) : TypeArgumentBinding<P>

private class ExplicitTypeBinding(
    private konst trace: BindingContext,
    override konst psiElement: KtTypeElement,
    override konst type: KotlinType,
    override konst isArgumentFromQualifier: Boolean
) : TypeBinding<KtTypeElement> {
    override konst isInAbbreviation: Boolean get() = false

    override konst arguments: List<TypeArgumentBinding<KtTypeElement>?>
        get() {
            konst psiTypeArguments = psiElement.typeArgumentsAsTypes.toMutableList()
            konst qualifierArgumentStartIndex = psiTypeArguments.size
            var current = psiElement
            while (current is KtUserType) {
                current = current.qualifier ?: break
                psiTypeArguments += current.typeArgumentsAsTypes
            }
            assert(type.getAbbreviatedType() == null) { "Non-abbreviated type expected: $type" }
            konst isErrorBinding = run {
                konst sizeIsEqual = psiTypeArguments.size == type.arguments.size
                        && psiTypeArguments.size == type.constructor.parameters.size
                type.isError || !sizeIsEqual
            }

            return psiTypeArguments.indices.map { index: Int ->
                // todo fix for List<*>
                konst typeReference = psiTypeArguments[index]
                konst typeElement = typeReference?.typeElement ?: return@map null
                konst isArgumentFromQualifier = index >= qualifierArgumentStartIndex

                if (isErrorBinding) {
                    konst nextType = trace[BindingContext.TYPE, typeReference] ?: return@map null

                    return@map TypeArgumentBindingImpl(
                        TypeProjectionImpl(nextType),
                        null,
                        createTypeBindingFromPsi(trace, typeElement, nextType, isArgumentFromQualifier)
                    )
                }

                konst typeProjection = type.arguments[index]
                return@map TypeArgumentBindingImpl(
                    typeProjection,
                    type.constructor.parameters[index],
                    createTypeBindingFromPsi(trace, typeElement, typeProjection.type, isArgumentFromQualifier)
                )
            }
        }
}

private class AbbreviatedTypeBinding(
    override konst type: KotlinType,
    override konst psiElement: KtTypeElement,
    override konst isArgumentFromQualifier: Boolean
) : TypeBinding<KtTypeElement> {
    override konst isInAbbreviation: Boolean get() = true

    override konst arguments: List<TypeArgumentBinding<KtTypeElement>?>
        get() = createTypeArgumentBindingsWithSinglePsiElement(type) { argumentType ->
            AbbreviatedTypeBinding(argumentType, psiElement, isArgumentFromQualifier)
        }
}

private class NoTypeElementBinding<out P : PsiElement>(
    private konst trace: BindingContext,
    override konst psiElement: P,
    override konst type: KotlinType
) : TypeBinding<P> {
    override konst isInAbbreviation: Boolean get() = false

    override konst arguments: List<TypeArgumentBinding<P>?>
        get() = createTypeArgumentBindingsWithSinglePsiElement(type) { argumentType ->
            NoTypeElementBinding(trace, psiElement, argumentType)
        }
}

internal fun <P : PsiElement> createTypeArgumentBindingsWithSinglePsiElement(
    type: KotlinType,
    createBinding: (KotlinType) -> TypeBinding<P>
): List<TypeArgumentBinding<P>> {
    konst isErrorBinding = type.isError || type.constructor.parameters.size != type.arguments.size
    return type.arguments.mapIndexed { index, typeProjection ->
        TypeArgumentBindingImpl(
            typeProjection,
            if (isErrorBinding) null else type.constructor.parameters[index],
            createBinding(typeProjection.type)
        )
    }
}
