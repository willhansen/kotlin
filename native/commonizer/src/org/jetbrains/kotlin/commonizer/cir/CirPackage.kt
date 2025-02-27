/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

interface CirPackage : CirDeclaration {
    konst packageName: CirPackageName

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun create(packageName: CirPackageName): CirPackage = CirPackageImpl(packageName)
    }
}

data class CirPackageImpl(
    override konst packageName: CirPackageName
) : CirPackage
