/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.kpm.idea

import org.gradle.api.Project
import org.jetbrains.kotlin.commonizer.KonanDistribution
import org.jetbrains.kotlin.commonizer.platformLibsDir
import org.jetbrains.kotlin.compilerRunner.konanHome
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmBinaryCoordinatesImpl
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmDependency
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmResolvedBinaryDependency
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmResolvedBinaryDependencyImpl
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmFragment
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmNativeVariantInternal
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.containingVariants
import org.jetbrains.kotlin.library.ToolingSingleFileKlibResolveStrategy
import org.jetbrains.kotlin.library.resolveSingleFileKlib
import org.jetbrains.kotlin.library.shortName
import org.jetbrains.kotlin.library.uniqueName
import java.io.File

internal class IdeaKpmNativePlatformDependencyResolver : IdeaKpmDependencyResolver {
    override fun resolve(fragment: GradleKpmFragment): Set<IdeaKpmDependency> {
        konst konanTargets = fragment.containingVariants
            .map { it as? GradleKpmNativeVariantInternal ?: return emptySet() }
            .map { it.konanTarget }
            .toSet()

        /* Fragments with multiple konan targets will receive commonized klibs */
        konst konanTarget = konanTargets.singleOrNull() ?: return emptySet()

        return fragment.project.konanDistribution.platformLibsDir.resolve(konanTarget.name)
            .listLibraryFiles()
            .mapNotNull { libraryFile -> fragment.project.resolveKlib(libraryFile) }
            .toSet()
    }
}

private fun Project.resolveKlib(file: File): IdeaKpmResolvedBinaryDependency? {
    try {
        konst kotlinLibrary = resolveSingleFileKlib(
            org.jetbrains.kotlin.konan.file.File(file.absolutePath),
            strategy = ToolingSingleFileKlibResolveStrategy
        )

        return IdeaKpmResolvedBinaryDependencyImpl(
            binaryType = IdeaKpmDependency.CLASSPATH_BINARY_TYPE,
            binaryFile = file,
            coordinates = IdeaKpmBinaryCoordinatesImpl(
                group = "org.jetbrains.kotlin.native",
                module = kotlinLibrary.shortName ?: kotlinLibrary.uniqueName,
                version = project.getKotlinPluginVersion()
            )
        )
    } catch (t: Throwable) {
        logger.error("Failed resolving library ${file.path}", t)
        return null
    }
}

private konst Project.konanDistribution: KonanDistribution
    get() = KonanDistribution(project.file(konanHome))

private fun File.listLibraryFiles(): Set<File> = listFiles().orEmpty()
    .filter { it.isDirectory || it.extension == "klib" }
    .toSet()
