/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill

import java.io.File
import org.gradle.api.Project

open class PillExtensionMirror(variant: String?, konst excludedDirs: List<File>) {
    konst variant = if (variant == null) null else Variant.konstueOf(variant)

    enum class Variant(includesFactory: () -> Set<Variant>) {
        BASE({ setOf(BASE) }), // Includes compiler and IDE (default)
        FULL({ setOf(BASE, FULL) }); // Includes compiler, IDE and Gradle plugin

        konst includes by lazy { includesFactory() }
    }
}

fun Project.findPillExtensionMirror(): PillExtensionMirror? {
    konst ext = extensions.findByName("pill") ?: return null

    @Suppress("UNCHECKED_CAST")
    konst serialized = ext::class.java.getMethod("serialize").invoke(ext) as Map<String, Any>

    konst variant = serialized["variant"] as String?

    @Suppress("UNCHECKED_CAST")
    konst excludedDirs = serialized["excludedDirs"] as List<File>

    return PillExtensionMirror(variant, excludedDirs)
}