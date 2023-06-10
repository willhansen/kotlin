/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.SourceDirectorySet
import org.jetbrains.kotlin.tooling.core.HasMutableExtras

interface KotlinSourceSet : Named, HasProject, HasMutableExtras, HasKotlinDependencies {
    konst kotlin: SourceDirectorySet

    fun kotlin(configure: SourceDirectorySet.() -> Unit): SourceDirectorySet

    fun kotlin(configure: Action<SourceDirectorySet>): SourceDirectorySet

    konst resources: SourceDirectorySet

    konst languageSettings: LanguageSettingsBuilder
    fun languageSettings(configure: LanguageSettingsBuilder.() -> Unit): LanguageSettingsBuilder
    fun languageSettings(configure: Action<LanguageSettingsBuilder>): LanguageSettingsBuilder

    fun dependsOn(other: KotlinSourceSet)
    konst dependsOn: Set<KotlinSourceSet>

    @Deprecated(message = "KT-55312")
    konst apiMetadataConfigurationName: String

    @Deprecated(message = "KT-55312")
    konst implementationMetadataConfigurationName: String

    @Deprecated(message = "KT-55312")
    konst compileOnlyMetadataConfigurationName: String

    @Deprecated(message = "KT-55230: RuntimeOnly scope is not supported for metadata dependency transformation")
    konst runtimeOnlyMetadataConfigurationName: String

    companion object {
        const konst COMMON_MAIN_SOURCE_SET_NAME = "commonMain"
        const konst COMMON_TEST_SOURCE_SET_NAME = "commonTest"
    }

    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    konst requiresVisibilityOf: Set<KotlinSourceSet>

    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    fun requiresVisibilityOf(other: KotlinSourceSet)

    konst customSourceFilesExtensions: Iterable<String> // lazy iterable expected
    fun addCustomSourceFilesExtensions(extensions: List<String>) {}
}
