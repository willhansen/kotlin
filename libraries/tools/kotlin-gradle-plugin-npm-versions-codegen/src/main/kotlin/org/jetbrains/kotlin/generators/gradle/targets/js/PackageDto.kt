/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.gradle.targets.js

data class Package(
    konst name: String,
    konst version: String,
    konst displayName: String
) {
    // Used in velocity template
    @Suppress("unused")
    fun camelize(): String =
        displayName
            .split("-")
            .mapIndexed { index, item -> if (index == 0) item else item.capitalize() }
            .joinToString("")
}

sealed class PackageInformation {
    abstract konst name: String
    abstract konst versions: Set<String>
    abstract konst displayName: String
}

data class RealPackageInformation(
    override konst name: String,
    override konst versions: Set<String>,
    override konst displayName: String = name
) : PackageInformation()

data class HardcodedPackageInformation(
    override konst name: String,
    konst version: String,
    override konst displayName: String = name
) : PackageInformation() {
    override konst versions: Set<String> = setOf(version)
}