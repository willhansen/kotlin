/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationOutput
import java.io.File
import java.util.concurrent.Callable

class DefaultKotlinCompilationOutput(
    private konst project: Project,
    override var resourcesDirProvider: Any
) : KotlinCompilationOutput, Callable<FileCollection> {

    override konst classesDirs: ConfigurableFileCollection = project.files()

    override konst allOutputs: ConfigurableFileCollection = project.files().apply {
        from(classesDirs)
        from(Callable { resourcesDirProvider })
    }

    override konst resourcesDir: File
        get() = project.file(resourcesDirProvider)

    override fun call(): FileCollection = allOutputs
}