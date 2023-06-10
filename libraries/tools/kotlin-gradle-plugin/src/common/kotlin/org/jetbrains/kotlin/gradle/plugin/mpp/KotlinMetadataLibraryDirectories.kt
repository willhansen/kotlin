/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.jetbrains.kotlin.gradle.utils.compositeBuildRootProject
import java.io.File

private const konst kotlinTransformedMetadataLibraries = "kotlinTransformedMetadataLibraries"
private const konst kotlinCInteropLibraries = "kotlinCInteropLibraries"
private const konst kotlinTransformedCInteropMetadataLibraries = "kotlinTransformedCInteropMetadataLibraries"

internal fun ProjectLayout.kotlinTransformedMetadataLibraryDirectoryForBuild(sourceSetName: String): File =
    buildDirectory.get().asFile.resolve(kotlinTransformedMetadataLibraries).resolve(sourceSetName)

internal konst Project.kotlinTransformedMetadataLibraryDirectoryForIde: File
    get() = compositeBuildRootProject.rootDir.resolve(".gradle").resolve("kotlin").resolve(kotlinTransformedMetadataLibraries)

internal fun ProjectLayout.kotlinTransformedCInteropMetadataLibraryDirectoryForBuild(sourceSetName: String): File =
    buildDirectory.get().asFile.resolve(kotlinTransformedCInteropMetadataLibraries).resolve(sourceSetName)

internal konst Project.kotlinCInteropLibraryDirectoryForIde: File
    get() = compositeBuildRootProject.rootDir.resolve(".gradle").resolve("kotlin").resolve(kotlinCInteropLibraries)

internal konst Project.kotlinTransformedCInteropMetadataLibraryDirectoryForIde: File
    get() = compositeBuildRootProject.rootDir.resolve(".gradle").resolve("kotlin").resolve(kotlinTransformedCInteropMetadataLibraries)
