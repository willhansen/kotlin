/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.java.source

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementPsiSource
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementSourceFactory

internal class JavaElementPsiSourceWithSmartPointer<PSI : PsiElement>(
    konst pointer: SmartPsiElementPointer<PSI>,
    override konst factory: JavaElementSourceFactory,
) : JavaElementPsiSource<PSI>() {

    override konst psi: PSI
        get() {
            return pointer.element
                ?: error("Cannot restore a PsiElement from $pointer")
        }
}

