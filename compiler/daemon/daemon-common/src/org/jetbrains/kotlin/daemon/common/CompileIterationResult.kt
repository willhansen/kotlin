/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.daemon.common

import java.io.File
import java.io.Serializable

class CompileIterationResult(
    @Suppress("unused") // used in Gradle
        konst sourceFiles: Iterable<File>,
    @Suppress("unused") // used in Gradle
        konst exitCode: String
) : Serializable {
    companion object {
        const konst serialVersionUID: Long = 0
    }
}