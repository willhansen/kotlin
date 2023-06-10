/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import java.io.File
import java.io.Serializable

data class IncrementalModuleEntry(
    private konst projectPath: String,
    konst name: String,
    konst buildDir: File,
    konst buildHistoryFile: File,
    konst abiSnapshot: File
) : Serializable {
    companion object {
        private const konst serialVersionUID = 0L
    }
}

class IncrementalModuleInfo(
    konst projectRoot: File,
    konst rootProjectBuildDir: File,
    konst dirToModule: Map<File, IncrementalModuleEntry>,
    konst nameToModules: Map<String, Set<IncrementalModuleEntry>>,
    konst jarToClassListFile: Map<File, File>,
    // only for js and mpp
    konst jarToModule: Map<File, IncrementalModuleEntry>,
    //for JVM only
    konst jarToAbiSnapshot: Map<File, File>
) : Serializable {
    companion object {
        private const konst serialVersionUID = 1L
    }
}