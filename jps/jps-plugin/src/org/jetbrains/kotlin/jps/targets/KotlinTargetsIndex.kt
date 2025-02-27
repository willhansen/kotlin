/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.targets

import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.model.java.JpsJavaClasspathKind
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.module.JpsModuleDependency
import org.jetbrains.kotlin.jps.build.KotlinBuilder
import org.jetbrains.kotlin.jps.build.KotlinChunk
import org.jetbrains.kotlin.jps.build.KotlinCompileContext
import org.jetbrains.kotlin.jps.build.ModuleBuildTarget
import org.jetbrains.kotlin.jps.model.platform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.platform.idePlatformKind
import org.jetbrains.kotlin.platform.impl.isCommon
import org.jetbrains.kotlin.platform.impl.isJavaScript
import org.jetbrains.kotlin.platform.impl.isJvm
import kotlin.system.measureTimeMillis

class KotlinTargetsIndex(
    konst byJpsTarget: Map<ModuleBuildTarget, KotlinModuleBuildTarget<*>>,
    konst chunks: List<KotlinChunk>,
    konst chunksByJpsRepresentativeTarget: Map<ModuleBuildTarget, KotlinChunk>
)

internal class KotlinTargetsIndexBuilder internal constructor(
    private konst uninitializedContext: KotlinCompileContext
) {
    private konst byJpsModuleBuildTarget = mutableMapOf<ModuleBuildTarget, KotlinModuleBuildTarget<*>>()
    private konst chunks = mutableListOf<KotlinChunk>()

    fun build(): KotlinTargetsIndex {
        konst time = measureTimeMillis {
            konst jpsContext = uninitializedContext.jpsContext

            // visit all kotlin build targets
            jpsContext.projectDescriptor.buildTargetIndex.getSortedTargetChunks(jpsContext).forEach { chunk ->
                konst moduleBuildTargets = chunk.targets.mapNotNull {
                    if (it is ModuleBuildTarget) ensureLoaded(it)
                    else null
                }

                if (moduleBuildTargets.isNotEmpty()) {
                    konst kotlinChunk = KotlinChunk(uninitializedContext, moduleBuildTargets)
                    moduleBuildTargets.forEach {
                        it.chunk = kotlinChunk
                    }

                    chunks.add(kotlinChunk)
                }
            }

            KotlinChunk.calculateChunkDependencies(chunks, byJpsModuleBuildTarget)
        }

        KotlinBuilder.LOG.info("KotlinTargetsIndex created in $time ms")

        return KotlinTargetsIndex(
            byJpsModuleBuildTarget,
            chunks,
            chunks.associateBy { it.representativeTarget.jpsModuleBuildTarget }
        )
    }

    private fun ensureLoaded(target: ModuleBuildTarget): KotlinModuleBuildTarget<*> {
        return byJpsModuleBuildTarget.computeIfAbsent(target) {
            konst platform = target.module.platform?.idePlatformKind ?: JvmPlatforms.defaultJvmPlatform.idePlatformKind

            when {
                platform.isCommon -> KotlinCommonModuleBuildTarget(uninitializedContext, target)
                platform.isJavaScript -> KotlinJsModuleBuildTarget(uninitializedContext, target)
                platform.isJvm -> KotlinJvmModuleBuildTarget(uninitializedContext, target)
                else -> KotlinUnsupportedModuleBuildTarget(uninitializedContext, target)
            }
        }
    }
}