/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm.resolver

import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.initialization.IncludedBuild
import java.io.File
import java.io.Serializable

data class ExternalGradleDependency(
    konst dependency: ResolvedDependency,
    konst artifact: ResolvedArtifact
) : Serializable

data class FileCollectionExternalGradleDependency(
    konst files: Collection<File>,
    konst dependencyVersion: String?
) : Serializable

data class FileExternalGradleDependency(
    konst dependencyName: String,
    konst dependencyVersion: String,
    konst file: File
) : Serializable

data class CompositeDependency(
    konst dependencyName: String,
    konst dependencyVersion: String,
    konst includedBuildDir: File,
    @Transient
    konst includedBuild: IncludedBuild?
) : Serializable

data class InternalDependency(
    konst projectPath: String,
    konst compilationName: String,
    konst projectName: String
) : Serializable