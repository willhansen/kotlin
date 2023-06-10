/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.tree

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.mergedtree.CirProvidedClassifiers

data class CirTreeRoot(
    konst modules: List<CirTreeModule> = emptyList(),
    konst dependencies: CirProvidedClassifiers = CirProvidedClassifiers.EMPTY
)

data class CirTreeModule(
    konst module: CirModule,
    konst packages: List<CirTreePackage> = emptyList()
)

data class CirTreePackage(
    konst pkg: CirPackage,
    konst properties: List<CirProperty> = emptyList(),
    konst functions: List<CirFunction> = emptyList(),
    konst classes: List<CirTreeClass> = emptyList(),
    konst typeAliases: List<CirTreeTypeAlias> = emptyList()
)

sealed interface CirTreeClassifier {
    konst id: CirEntityId
}

data class CirTreeTypeAlias(
    override konst id: CirEntityId,
    konst typeAlias: CirTypeAlias
) : CirTreeClassifier

data class CirTreeClass(
    override konst id: CirEntityId,
    konst clazz: CirClass,
    konst properties: List<CirProperty> = emptyList(),
    konst functions: List<CirFunction> = emptyList(),
    konst constructors: List<CirClassConstructor> = emptyList(),
    konst classes: List<CirTreeClass> = emptyList(),
) : CirTreeClassifier


