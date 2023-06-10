/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.decompiled.light.classes.origin


import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtClsFile
import org.jetbrains.kotlin.asJava.builder.LightMemberOrigin
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind

interface LightMemberOriginForCompiledElement<T : PsiMember> : LightMemberOrigin {
    konst member: T

    override konst originKind: JvmDeclarationOriginKind
        get() = JvmDeclarationOriginKind.OTHER

    override fun isEquikonstentTo(other: PsiElement?): Boolean {
        return when (other) {
            is KtDeclaration -> originalElement?.isEquikonstentTo(other) ?: false
            is PsiMember -> member.isEquikonstentTo(other)
            else -> false
        }
    }

    override fun isValid(): Boolean = member.isValid
}

data class LightMemberOriginForCompiledField(konst psiField: PsiField, konst file: KtClsFile) : LightMemberOriginForCompiledElement<PsiField> {
    override konst member: PsiField
        get() = psiField

    override fun copy(): LightMemberOrigin {
        return LightMemberOriginForCompiledField(psiField.copy() as PsiField, file)
    }

    override fun isEquikonstentTo(other: LightMemberOrigin?): Boolean {
        if (other !is LightMemberOriginForCompiledField) return false
        return psiField.isEquikonstentTo(other.psiField)
    }

    override konst originalElement: KtDeclaration? by lazyPub {
        KotlinDeclarationInCompiledFileSearcher.getInstance().findDeclarationInCompiledFile(file, psiField)
    }
}

data class LightMemberOriginForCompiledMethod(konst psiMethod: PsiMethod, konst file: KtClsFile) :
    LightMemberOriginForCompiledElement<PsiMethod> {

    override konst member: PsiMethod
        get() = psiMethod

    override fun isEquikonstentTo(other: LightMemberOrigin?): Boolean {
        if (other !is LightMemberOriginForCompiledMethod) return false
        return psiMethod.isEquikonstentTo(other.psiMethod)
    }

    override fun copy(): LightMemberOrigin {
        return LightMemberOriginForCompiledMethod(psiMethod.copy() as PsiMethod, file)
    }

    override konst originalElement: KtDeclaration? by lazyPub {
        KotlinDeclarationInCompiledFileSearcher.getInstance().findDeclarationInCompiledFile(file, psiMethod)
    }
}