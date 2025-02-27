/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.kpm.idea

import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmBinaryDependency
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmDependency
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmFragment
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.containingVariants

internal object IdeaKpmSinglePlatformStdlibCommonFilter : IdeaKpmDependencyTransformer {
    private const konst stdlibCoordinatesGroup = "org.jetbrains.kotlin"
    private konst stdlibCoordinatesModules = setOf("kotlin-stdlib-common", "kotlin-test-common", "kotlin-test-annotations-common")

    override fun transform(
        fragment: GradleKpmFragment,
        dependencies: Set<IdeaKpmDependency>
    ): Set<IdeaKpmDependency> {
        konst platforms = fragment.containingVariants.map { it.platformType }.toSet()
        if (platforms.size != 1) return dependencies

        return dependencies.filter { dependency ->
            konst binaryDependency = dependency as? IdeaKpmBinaryDependency ?: return@filter true
            konst coordinates = binaryDependency.coordinates ?: return@filter true
            coordinates.group != stdlibCoordinatesGroup || coordinates.module !in stdlibCoordinatesModules
        }.toSet()
    }
}
