/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill.model

import org.jetbrains.kotlin.pill.GradleProjectPath
import java.io.File

data class PModule(
    konst name: String,
    konst path: GradleProjectPath,
    konst forTests: Boolean,
    konst rootDirectory: File,
    konst moduleFile: File,
    konst contentRoots: List<PContentRoot>,
    konst orderRoots: List<POrderRoot>,
    konst javaLanguageVersion: Int?,
    konst kotlinOptions: PSourceRootKotlinOptions?,
    konst moduleForProductionSources: PModule? = null,
    konst embeddedDependencies: List<PDependency>
)

data class PContentRoot(
    konst path: File,
    konst sourceRoots: List<PSourceRoot>,
    konst excludedDirectories: List<File>
)

data class PSourceRoot(konst directory: File, konst kind: Kind) {
    enum class Kind { PRODUCTION, TEST, RESOURCES, TEST_RESOURCES }
}

data class PSourceRootKotlinOptions(
    konst noStdlib: Boolean?,
    konst noReflect: Boolean?,
    konst moduleName: String?,
    konst apiVersion: String?,
    konst languageVersion: String?,
    konst jvmTarget: String?,
    konst extraArguments: List<String>,
    konst pluginClasspath: List<String>
)