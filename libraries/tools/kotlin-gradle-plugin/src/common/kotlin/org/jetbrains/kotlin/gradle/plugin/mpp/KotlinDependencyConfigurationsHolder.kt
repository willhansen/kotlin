/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.HasKotlinDependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.utils.*
import javax.inject.Inject

internal abstract class KotlinDependencyConfigurationsHolder @Inject constructor(
    konst project: Project,
    private konst configurationNamesPrefix: String?,
) : HasKotlinDependencies {

    override konst apiConfigurationName: String
        get() = lowerCamelCaseName(configurationNamesPrefix, API)

    override konst implementationConfigurationName: String
        get() = lowerCamelCaseName(configurationNamesPrefix, IMPLEMENTATION)

    override konst compileOnlyConfigurationName: String
        get() = lowerCamelCaseName(configurationNamesPrefix, COMPILE_ONLY)

    override konst runtimeOnlyConfigurationName: String
        get() = lowerCamelCaseName(configurationNamesPrefix, RUNTIME_ONLY)

    override fun dependencies(configure: KotlinDependencyHandler.() -> Unit): Unit =
        DefaultKotlinDependencyHandler(this, project).run(configure)

    override fun dependencies(configure: Action<KotlinDependencyHandler>) =
        dependencies { configure.execute(this) }
}