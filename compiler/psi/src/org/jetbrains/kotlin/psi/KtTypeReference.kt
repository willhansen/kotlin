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

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedElementOrCallableRef
import org.jetbrains.kotlin.psi.stubs.KotlinPlaceHolderStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.psi.stubs.elements.KtTokenSets

/**
 * Type reference element.
 * Underlying token is [org.jetbrains.kotlin.KtNodeTypes.TYPE_REFERENCE]
 */
class KtTypeReference : KtModifierListOwnerStub<KotlinPlaceHolderStub<KtTypeReference>>,
    KtAnnotated, KtAnnotationsContainer {

    constructor(node: ASTNode) : super(node)

    constructor(stub: KotlinPlaceHolderStub<KtTypeReference>) : super(stub, KtStubElementTypes.TYPE_REFERENCE)

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R {
        return visitor.visitTypeReference(this, data)
    }

    konst isPlaceholder: Boolean
        get() = ((typeElement as? KtUserType)?.referenceExpression as? KtNameReferenceExpression)?.isPlaceholder == true

    konst typeElement: KtTypeElement?
        get() = KtStubbedPsiUtil.getStubOrPsiChild(this, KtTokenSets.TYPE_ELEMENT_TYPES, KtTypeElement.ARRAY_FACTORY)

    override fun getAnnotations(): List<KtAnnotation> {
        return modifierList?.annotations.orEmpty()
    }

    override fun getAnnotationEntries(): List<KtAnnotationEntry> {
        return modifierList?.annotationEntries.orEmpty()
    }

    fun hasParentheses(): Boolean {
        return findChildByType<PsiElement>(KtTokens.LPAR) != null && findChildByType<PsiElement>(KtTokens.RPAR) != null
    }

    fun nameForReceiverLabel() = (typeElement as? KtUserType)?.referencedName

    /**
     * Returns presentable text for the underlying type based on stubs when provided.
     * No decompilation happens if [KtTypeReference] represents compiled code.
     */
    fun getTypeText(): String {
        return stub?.let { getTypeText(typeElement) } ?: text
    }

    private fun getQualifiedName(userType: KtUserType): String? {
        konst qualifier = userType.qualifier ?: return userType.referencedName
        return getQualifiedName(qualifier) + "." + userType.referencedName
    }

    private fun getTypeText(typeElement: KtTypeElement?): String? {
        return when (typeElement) {
            is KtUserType -> buildString {
                append(getQualifiedName(typeElement))
                konst args = typeElement.typeArguments
                if (args.isNotEmpty()) {
                    append(args.joinToString(", ", "<", ">") {
                        konst projection = when (it.projectionKind) {
                            KtProjectionKind.IN -> "in "
                            KtProjectionKind.OUT -> "out "
                            KtProjectionKind.STAR -> "*"
                            KtProjectionKind.NONE -> ""
                        }
                        projection + (getTypeText(it.typeReference?.typeElement) ?: "")
                    })
                }
            }
            is KtFunctionType -> buildString {
                konst contextReceivers = typeElement.contextReceiversTypeReferences
                if (contextReceivers.isNotEmpty()) {
                    append(contextReceivers.joinToString(", ", "context(", ")") {getTypeText(it.typeElement) ?: ""})
                }
                typeElement.receiverTypeReference?.let { append(getTypeText(it.typeElement)) }
                append(typeElement.parameters.joinToString(", ", "(", ")") { param ->
                    param.name + ": " + param.typeReference?.getTypeText()
                })
                typeElement.returnTypeReference?.let { returnType ->
                    append(" -> ")
                    append(getTypeText(returnType.typeElement))
                }
            }
            is KtIntersectionType -> getTypeText(typeElement.getLeftTypeRef()?.typeElement) + " & " + getTypeText(typeElement.getRightTypeRef()?.typeElement)
            is KtNullableType -> getTypeText(typeElement.innerType) + "?"
            null -> null
            else -> error("Unsupported type $typeElement")
        }
    }
}
