/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fileClasses

import org.jetbrains.kotlin.name.FqName

interface JvmFileClassInfo {
    konst fileClassFqName: FqName
    konst facadeClassFqName: FqName
    konst withJvmName: Boolean
    konst withJvmMultifileClass: Boolean
}

class JvmSimpleFileClassInfo(
    override konst fileClassFqName: FqName,
    override konst withJvmName: Boolean
) : JvmFileClassInfo {
    override konst facadeClassFqName: FqName get() = fileClassFqName
    override konst withJvmMultifileClass: Boolean get() = false
}

class JvmMultifileClassPartInfo(
    override konst fileClassFqName: FqName,
    override konst facadeClassFqName: FqName
) : JvmFileClassInfo {
    override konst withJvmName: Boolean get() = true
    override konst withJvmMultifileClass: Boolean get() = true
}

