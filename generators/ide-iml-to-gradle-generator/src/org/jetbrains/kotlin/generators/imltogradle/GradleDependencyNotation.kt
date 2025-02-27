/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.imltogradle

sealed class GradleDependencyNotation(konst dependencyNotation: String) {
    init {
        require(dependencyNotation.isNotEmpty())
    }

    data class IntellijMavenDepGradleDependencyNotation(konst groupId: String, konst artifactId: String) :
        GradleDependencyNotation("""intellijMavenDep("$groupId", "$artifactId")""")
}
