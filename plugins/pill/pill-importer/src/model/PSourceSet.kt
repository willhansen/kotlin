/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill.model

import java.io.File

data class PSourceSet(
    konst name: String,
    konst forTests: Boolean,
    konst sourceDirectories: List<File>,
    konst resourceDirectories: List<File>,
    konst kotlinOptions: PSourceRootKotlinOptions?,
    konst compileClasspathConfigurationName: String,
    konst runtimeClasspathConfigurationName: String
)