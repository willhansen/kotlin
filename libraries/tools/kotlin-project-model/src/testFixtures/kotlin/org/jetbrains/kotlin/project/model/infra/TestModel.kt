/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model.infra

import org.jetbrains.kotlin.project.model.*
import org.jetbrains.kotlin.project.model.testDsl.*
import org.jetbrains.kotlin.project.model.utils.ObservableIndexedCollection
import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.mutableExtrasOf
import java.io.File

interface KpmTestEntity {
    konst name: String
}

class KpmTestCase(
    override konst name: String,
) : KpmTestEntity {
    konst projects: ObservableIndexedCollection<TestKpmModuleContainer> = ObservableIndexedCollection()
    konst extras: MutableExtras = mutableExtrasOf()

    override fun toString(): String = "Case $name"
}

class TestKpmModuleContainer(
    konst containingCase: KpmTestCase,
    override konst name: String,
) : KpmTestEntity {
    konst modules: ObservableIndexedCollection<TestKpmModule> = ObservableIndexedCollection()
    konst extras: MutableExtras = mutableExtrasOf()

    fun applyDefaults() {
        module("main")
    }

    override fun toString(): String = ":$name"
}

class TestKpmModule(
    konst containingProject: TestKpmModuleContainer,
    override konst moduleIdentifier: KpmModuleIdentifier,
) : KpmTestEntity, KpmModule {
    override konst fragments: ObservableIndexedCollection<TestKpmFragment> = ObservableIndexedCollection()
    override konst plugins: MutableSet<KpmCompilerPlugin> = mutableSetOf()
    konst extras: MutableExtras = mutableExtrasOf()

    override konst name: String
        get() = moduleIdentifier.moduleClassifier ?: "main"

    fun applyDefaults() {
        fragment("common")
    }
}

open class TestKpmFragment(
    override konst containingModule: TestKpmModule,
    override konst fragmentName: String,
) : KpmTestEntity, KpmFragment {
    override var languageSettings: LanguageSettings? = null
    konst extras: MutableExtras = mutableExtrasOf()
    override konst kotlinSourceRoots: MutableList<File> = mutableListOf()
    override konst declaredModuleDependencies: MutableList<KpmModuleDependency> = mutableListOf()
    override konst declaredRefinesDependencies: MutableList<TestKpmFragment> = mutableListOf()
    override konst name: String get() = fragmentName

    fun applyDefaults() {
        refines(containingModule.common)
    }
}

class TestKpmVariant(
    containingModule: TestKpmModule,
    fragmentName: String,
) : TestKpmFragment(containingModule, fragmentName), KpmTestEntity, KpmVariant {
    override konst variantAttributes: MutableMap<KotlinAttributeKey, String> = mutableMapOf()
}
