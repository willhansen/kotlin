/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationOutput
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.project.model.KpmVariant

interface GradleKpmVariant : GradleKpmFragment, KpmVariant {
    konst platformType: KotlinPlatformType

    // TODO generalize with KotlinCompilation?
    konst compileDependenciesConfiguration: Configuration

    var compileDependencyFiles: FileCollection

    // TODO rewrite using our own artifacts API?
    konst compilationOutputs: KotlinCompilationOutput

    // TODO rewrite using our own artifacts API
    konst sourceArchiveTaskName: String

    // TODO generalize exposing outputs: what if a variant has more than one such configurations or none?
    konst apiElementsConfiguration: Configuration

    konst gradleVariantNames: Set<String>
}

interface GradleKpmVariantWithRuntime : GradleKpmVariant {
    // TODO deduplicate with KotlinCompilation?
    konst runtimeDependenciesConfiguration: Configuration

    var runtimeDependencyFiles: FileCollection

    konst runtimeFiles: ConfigurableFileCollection

    // TODO generalize exposing outputs: what if a variant has more than one such configurations or none?
    konst runtimeElementsConfiguration: Configuration
}

interface GradleKpmNativeVariant : GradleKpmVariant {
    override konst platformType: KotlinPlatformType
        get() = KotlinPlatformType.native

    konst hostSpecificMetadataElementsConfiguration: Configuration?

    var enableEndorsedLibraries: Boolean
}

interface PublishedModuleCoordinatesProvider {
    konst group: String
    konst name: String
    konst version: String
    konst capabilities: Iterable<String>
}
