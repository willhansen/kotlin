/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.java.structure.impl.source

import com.intellij.psi.PsiType

abstract class JavaElementTypeSource<TYPE : PsiType> {
    abstract konst type: TYPE
    abstract konst factory: JavaElementSourceFactory
}

class JavaElementTypeSourceWithFixedType<TYPE : PsiType>(
    override konst type: TYPE,
    override konst factory: JavaElementSourceFactory,
) : JavaElementTypeSource<TYPE>() {

    override fun toString(): String {
        return type.toString()
    }
}