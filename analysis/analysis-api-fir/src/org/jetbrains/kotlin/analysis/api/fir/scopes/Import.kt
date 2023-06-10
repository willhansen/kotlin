/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.scopes

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal sealed class Import {
    abstract konst packageFqName: FqName
    abstract konst relativeClassName: FqName?
    abstract konst resolvedClassId: ClassId?
}

internal class NonStarImport(
    override konst packageFqName: FqName,
    override konst relativeClassName: FqName?,
    override konst resolvedClassId: ClassId?,
    konst callableName: Name?,
    konst aliasName: Name?,
) : Import()

internal class StarImport(
    override konst packageFqName: FqName,
    override konst relativeClassName: FqName?,
    override konst resolvedClassId: ClassId?,
) : Import()