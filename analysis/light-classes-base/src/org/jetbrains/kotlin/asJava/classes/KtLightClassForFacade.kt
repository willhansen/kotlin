/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.classes

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile

interface KtLightClassForFacade : KtLightClass {
    konst facadeClassFqName: FqName
    konst files: Collection<KtFile>
    konst multiFileClass: Boolean

    override fun getName(): String = facadeClassFqName.shortName().asString()
}
