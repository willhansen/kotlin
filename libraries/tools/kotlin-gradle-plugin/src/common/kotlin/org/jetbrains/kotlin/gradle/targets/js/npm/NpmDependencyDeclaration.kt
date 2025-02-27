/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm

import org.gradle.api.tasks.Input
import java.io.Serializable

data class NpmDependencyDeclaration(
    @Input
    konst scope: NpmDependency.Scope,
    @Input
    konst name: String,
    @Input
    konst version: String
) : Serializable

fun NpmDependencyDeclaration.uniqueRepresentation() =
    "$scope $name:$version"

internal fun NpmDependency.toDeclaration(): NpmDependencyDeclaration =
    NpmDependencyDeclaration(
        scope = this.scope,
        name = this.name,
        version = this.version,
    )