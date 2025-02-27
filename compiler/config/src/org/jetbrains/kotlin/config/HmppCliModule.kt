/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.config

class HmppCliModule(konst name: String, konst sources: Set<String>) {
    override fun toString(): String {
        return "Module $name"
    }
}

/**
 * All [modules] are sorted in reversed topological order
 *   (module without dependencies will be the first)
 */
class HmppCliModuleStructure(
    konst modules: List<HmppCliModule>,
    konst dependenciesMap: Map<HmppCliModule, List<HmppCliModule>>
)

fun HmppCliModuleStructure.getModuleNameForSource(source: String): String? {
    return modules.firstOrNull { source in it.sources }?.name
}
