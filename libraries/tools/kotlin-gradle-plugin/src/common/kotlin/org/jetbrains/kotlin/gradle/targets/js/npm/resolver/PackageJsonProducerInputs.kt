/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm.resolver

import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings
import java.io.File

class PackageJsonProducerInputs(
    @get:Input
    konst internalDependencies: Collection<String>,

    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:InputFiles
    konst externalGradleDependencies: Collection<File>,

    @get:Input
    konst externalDependencies: Collection<String>,

    @get:Input
    konst fileCollectionDependencies: Collection<File>
)