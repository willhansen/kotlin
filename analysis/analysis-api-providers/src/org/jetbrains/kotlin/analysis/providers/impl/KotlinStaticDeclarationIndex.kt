/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.providers.impl

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*

internal class KotlinStaticDeclarationIndex {
    internal konst facadeFileMap: MutableMap<FqName, MutableSet<KtFile>> = mutableMapOf()
    internal konst multiFileClassPartMap: MutableMap<FqName, MutableSet<KtFile>> = mutableMapOf()
    internal konst scriptMap: MutableMap<FqName, MutableSet<KtScript>> = mutableMapOf()
    internal konst classMap: MutableMap<FqName, MutableSet<KtClassOrObject>> = mutableMapOf()
    internal konst typeAliasMap: MutableMap<FqName, MutableSet<KtTypeAlias>> = mutableMapOf()
    internal konst topLevelFunctionMap: MutableMap<FqName, MutableSet<KtNamedFunction>> = mutableMapOf()
    internal konst topLevelPropertyMap: MutableMap<FqName, MutableSet<KtProperty>> = mutableMapOf()
}
