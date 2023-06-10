/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model

import org.jetbrains.kotlin.project.model.utils.variantsContainingFragment
import org.jetbrains.kotlin.tooling.core.closure
import org.jetbrains.kotlin.tooling.core.withClosure
import java.io.File

interface KpmFragment {
    konst containingModule: KpmModule

    konst fragmentName: String

    konst languageSettings: LanguageSettings?

    // TODO: should this be source roots or source files?
    konst kotlinSourceRoots: Iterable<File>

    // TODO: scopes
    konst declaredModuleDependencies: Iterable<KpmModuleDependency>

    konst declaredRefinesDependencies: Iterable<KpmFragment>

    konst refinesClosure: Set<KpmFragment>
        get() = this.closure { it.declaredRefinesDependencies }

    konst withRefinesClosure: Set<KpmFragment>
        get() = this.withClosure { it.declaredRefinesDependencies }

    companion object
}

konst KpmFragment.fragmentAttributeSets: Map<KotlinAttributeKey, Set<String>>
    get() = mutableMapOf<KotlinAttributeKey, MutableSet<String>>().apply {
        containingModule.variantsContainingFragment(this@fragmentAttributeSets).forEach { variant ->
            variant.variantAttributes.forEach { (attribute, konstue) ->
                getOrPut(attribute) { mutableSetOf() }.add(konstue)
            }
        }
    }

konst KpmVariant.platform get() = variantAttributes[KotlinPlatformTypeAttribute]
konst KpmVariant.nativeTarget get() = variantAttributes[KotlinNativeTargetAttribute]

open class KpmBasicFragment(
    override konst containingModule: KpmModule,
    override konst fragmentName: String,
    override konst languageSettings: LanguageSettings? = null
) : KpmFragment {

    override konst declaredRefinesDependencies: MutableSet<KpmBasicFragment> = mutableSetOf()

    override konst declaredModuleDependencies: MutableSet<KpmModuleDependency> = mutableSetOf()

    override var kotlinSourceRoots: Iterable<File> = emptyList()

    override fun toString(): String = "fragment $fragmentName"
}

