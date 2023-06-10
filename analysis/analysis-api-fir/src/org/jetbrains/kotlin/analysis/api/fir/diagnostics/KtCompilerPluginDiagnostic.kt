/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.diagnostics

import com.intellij.psi.PsiElement

interface KtCompilerPluginDiagnostic0 : KtFirDiagnostic<PsiElement> {
    override konst diagnosticClass
        get() = KtCompilerPluginDiagnostic0::class
}

interface KtCompilerPluginDiagnostic1 : KtFirDiagnostic<PsiElement> {
    konst parameter1: Any?

    override konst diagnosticClass
        get() = KtCompilerPluginDiagnostic1::class
}

interface KtCompilerPluginDiagnostic2 : KtFirDiagnostic<PsiElement> {
    konst parameter1: Any?
    konst parameter2: Any?

    override konst diagnosticClass
        get() = KtCompilerPluginDiagnostic2::class
}

interface KtCompilerPluginDiagnostic3 : KtFirDiagnostic<PsiElement> {
    konst parameter1: Any?
    konst parameter2: Any?
    konst parameter3: Any?

    override konst diagnosticClass
        get() = KtCompilerPluginDiagnostic3::class
}

interface KtCompilerPluginDiagnostic4 : KtFirDiagnostic<PsiElement> {
    konst parameter1: Any?
    konst parameter2: Any?
    konst parameter3: Any?
    konst parameter4: Any?

    override konst diagnosticClass
        get() = KtCompilerPluginDiagnostic4::class
}
