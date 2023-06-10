/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model

// TODO Gradle allows having multiple capabilities in a published module, we need to figure out how we can include them in the module IDs

interface KpmModule {
    konst moduleIdentifier: KpmModuleIdentifier

    konst fragments: Iterable<KpmFragment>

    konst variants: Iterable<KpmVariant>
        get() = fragments.filterIsInstance<KpmVariant>()

    konst plugins: Iterable<KpmCompilerPlugin>

    // TODO: isSynthetic?
}

open class KpmBasicModule(
    override konst moduleIdentifier: KpmModuleIdentifier
) : KpmModule {
    override konst fragments = mutableListOf<KpmBasicFragment>()

    override konst plugins = mutableListOf<KpmCompilerPlugin>()

    override fun toString(): String = "module $moduleIdentifier"
}
