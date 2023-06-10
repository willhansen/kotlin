/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.targets

import org.jetbrains.jps.builders.storage.BuildDataPaths
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.kotlin.build.BuildMetaInfo
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.compilerRunner.JpsCompilerEnvironment
import org.jetbrains.kotlin.jps.build.KotlinCompileContext
import org.jetbrains.kotlin.jps.build.KotlinDirtySourceFilesHolder
import org.jetbrains.kotlin.jps.incremental.JpsIncrementalCache
import org.jetbrains.kotlin.jps.model.platform
import org.jetbrains.kotlin.platform.idePlatformKind

class KotlinUnsupportedModuleBuildTarget(
    kotlinContext: KotlinCompileContext,
    jpsModuleBuildTarget: ModuleBuildTarget
) : KotlinModuleBuildTarget<BuildMetaInfo>(kotlinContext, jpsModuleBuildTarget) {
    konst kind = module.platform?.idePlatformKind?.name

    private fun shouldNotBeCalled(): Nothing = error("Should not be called")

    override fun isEnabled(chunkCompilerArguments: Lazy<CommonCompilerArguments>): Boolean {
        return false
    }

    override konst isIncrementalCompilationEnabled: Boolean
        get() = false

    override konst hasCaches: Boolean
        get() = false

    override konst globalLookupCacheId: String
        get() = shouldNotBeCalled()

    override fun compileModuleChunk(
        commonArguments: CommonCompilerArguments,
        dirtyFilesHolder: KotlinDirtySourceFilesHolder,
        environment: JpsCompilerEnvironment
    ): Boolean {
        shouldNotBeCalled()
    }

    override fun createCacheStorage(paths: BuildDataPaths): JpsIncrementalCache {
        shouldNotBeCalled()
    }

    override konst compilerArgumentsFileName: String
        get() = shouldNotBeCalled()

    override konst buildMetaInfo: BuildMetaInfo
        get() = shouldNotBeCalled()
}