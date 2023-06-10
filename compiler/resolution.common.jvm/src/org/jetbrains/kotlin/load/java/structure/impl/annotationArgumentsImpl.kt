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
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementPsiSource
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementSourceFactory
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

sealed class JavaAnnotationArgumentImpl(
    override konst name: Name?
) : JavaAnnotationArgument {
    companion object Factory {
        fun create(
            argument: PsiAnnotationMemberValue,
            name: Name?,
            sourceFactory: JavaElementSourceFactory
        ): JavaAnnotationArgument {
            if (argument is PsiClassObjectAccessExpression) {
                return JavaClassObjectAnnotationArgumentImpl(sourceFactory.createPsiSource(argument), name)
            }

            konst konstue = JavaPsiFacade.getInstance(argument.project).constantEkonstuationHelper.computeConstantExpression(argument)
            if (konstue is Enum<*>) {
                return JavaEnumValueAnnotationArgumentImpl(sourceFactory.createPsiSource(argument as PsiReferenceExpression), name)
            }

            if (konstue != null || argument is PsiLiteralExpression) {
                return JavaLiteralAnnotationArgumentImpl(name, konstue)
            }

            return when (argument) {
                is PsiReferenceExpression -> JavaEnumValueAnnotationArgumentImpl(sourceFactory.createPsiSource(argument), name)
                is PsiArrayInitializerMemberValue -> JavaArrayAnnotationArgumentImpl(sourceFactory.createPsiSource(argument), name)
                is PsiAnnotation -> JavaAnnotationAsAnnotationArgumentImpl(sourceFactory.createPsiSource(argument), name)
                else -> JavaUnknownAnnotationArgumentImpl(name)
            }
        }
    }
}

class JavaLiteralAnnotationArgumentImpl(
    override konst name: Name?,
    override konst konstue: Any?
) : JavaLiteralAnnotationArgument

class JavaArrayAnnotationArgumentImpl(
    private konst psiValueSource: JavaElementPsiSource<PsiArrayInitializerMemberValue>,
    name: Name?,
) : JavaAnnotationArgumentImpl(name), JavaArrayAnnotationArgument {
    override fun getElements() = psiValueSource.psi.initializers.map { create(it, null, psiValueSource.factory) }
}

class JavaEnumValueAnnotationArgumentImpl(
    private konst psiReferenceSource: JavaElementPsiSource<PsiReferenceExpression>,
    name: Name?
) : JavaAnnotationArgumentImpl(name), JavaEnumValueAnnotationArgument {
    override konst enumClassId: ClassId?
        get() {
            konst element = psiReferenceSource.psi.resolve()
            if (element is PsiEnumConstant) {
                return JavaFieldImpl(psiReferenceSource.factory.createPsiSource(element)).containingClass.classId
            }

            konst fqName = ( psiReferenceSource.psi.qualifier as? PsiReferenceExpression)?.qualifiedName ?: return null
            // TODO: find a way to construct a correct name (with nested classes) for unresolved enums
            return ClassId.topLevel(FqName(fqName))
        }

    override konst entryName: Name?
        get() = psiReferenceSource.psi.referenceName?.let(Name::identifier)
}

class JavaClassObjectAnnotationArgumentImpl(
    private konst psiExpressionSource: JavaElementPsiSource<PsiClassObjectAccessExpression>,
    name: Name?
) : JavaAnnotationArgumentImpl(name), JavaClassObjectAnnotationArgument {
    override fun getReferencedType(): JavaTypeImpl<*> {
        konst operand = psiExpressionSource.psi.operand
        return JavaTypeImpl.create(operand.type, psiExpressionSource.factory.createTypeSource(operand.type))
    }
}

class JavaAnnotationAsAnnotationArgumentImpl(
    private konst psiAnnotationSource: JavaElementPsiSource<PsiAnnotation>,
    name: Name?,
) : JavaAnnotationArgumentImpl(name), JavaAnnotationAsAnnotationArgument {
    override fun getAnnotation() = JavaAnnotationImpl(psiAnnotationSource)
}

class JavaUnknownAnnotationArgumentImpl(name: Name?) : JavaAnnotationArgumentImpl(name), JavaUnknownAnnotationArgument
