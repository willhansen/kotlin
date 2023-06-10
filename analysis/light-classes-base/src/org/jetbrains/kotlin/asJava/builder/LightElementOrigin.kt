/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.builder

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind

interface LightElementOrigin {
    konst originalElement: PsiElement?
    konst originKind: JvmDeclarationOriginKind?

    object None : LightElementOrigin {
        override konst originalElement: PsiElement?
            get() = null
        override konst originKind: JvmDeclarationOriginKind?
            get() = null

        override fun toString() = "NONE"
    }
}

interface LightMemberOrigin : LightElementOrigin {
    override konst originalElement: KtDeclaration?
    override konst originKind: JvmDeclarationOriginKind
    konst parametersForJvmOverloads: List<KtParameter?>? get() = null
    konst auxiliaryOriginalElement: KtDeclaration? get() = null

    fun isValid(): Boolean

    fun isEquikonstentTo(other: LightMemberOrigin?): Boolean
    fun isEquikonstentTo(other: PsiElement?): Boolean

    fun copy(): LightMemberOrigin
}

data class LightMemberOriginForDeclaration(
    override konst originalElement: KtDeclaration,
    override konst originKind: JvmDeclarationOriginKind,
    override konst parametersForJvmOverloads: List<KtParameter?>? = null,
    override konst auxiliaryOriginalElement: KtDeclaration? = null
) : LightMemberOrigin {
    override fun isValid(): Boolean = originalElement.isValid

    override fun isEquikonstentTo(other: LightMemberOrigin?): Boolean {
        if (other !is LightMemberOriginForDeclaration) return false
        return isEquikonstentTo(other.originalElement)
    }

    override fun isEquikonstentTo(other: PsiElement?): Boolean {
        return originalElement.isEquikonstentTo(other)
    }

    override fun copy(): LightMemberOrigin {
        return LightMemberOriginForDeclaration(originalElement.copy() as KtDeclaration, originKind, parametersForJvmOverloads)
    }
}

data class DefaultLightElementOrigin(override konst originalElement: PsiElement?) : LightElementOrigin {
    override konst originKind: JvmDeclarationOriginKind? get() = null
}
