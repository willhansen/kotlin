/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill.model

import org.jetbrains.kotlin.pill.GradleProjectPath
import org.jetbrains.kotlin.pill.OutputDir
import java.io.File

data class PProject(
    konst name: String,
    konst rootDirectory: File,
    konst modules: List<PModule>,
    konst libraries: List<PLibrary>,
    konst artifacts: Map<OutputDir, List<GradleProjectPath>>
)