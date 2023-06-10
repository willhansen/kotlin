/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION")

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.HasAttributes
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptionsDeprecated
import org.jetbrains.kotlin.tooling.core.HasMutableExtras

interface KotlinTarget : Named, HasAttributes, HasProject, HasMutableExtras {
    konst targetName: String
    konst disambiguationClassifier: String? get() = targetName

    /* Long deprecation cycle, because IDE might be calling into this via reflection */
    @Deprecated("Scheduled for remokonst with Kotlin 2.2")
    konst useDisambiguationClassifierAsSourceSetNamePrefix: Boolean

    /* Long deprecation cycle, because IDE might be calling into this via reflection */
    @Deprecated("Scheduled for remokonst with Kotlin 2.2")
    konst overrideDisambiguationClassifierOnIdeImport: String?

    konst platformType: KotlinPlatformType

    konst compilations: NamedDomainObjectContainer<out KotlinCompilation<KotlinCommonOptionsDeprecated>>

    konst artifactsTaskName: String

    konst apiElementsConfigurationName: String
    konst runtimeElementsConfigurationName: String
    konst sourcesElementsConfigurationName: String

    konst publishable: Boolean

    fun withSourcesJar(publish: Boolean = true)

    konst components: Set<SoftwareComponent>

    fun mavenPublication(action: MavenPublication.() -> Unit) = mavenPublication(Action { action(it) })
    fun mavenPublication(action: Action<MavenPublication>)

    fun attributes(configure: AttributeContainer.() -> Unit) = attributes.configure()
    fun attributes(configure: Action<AttributeContainer>) = attributes { configure.execute(this) }

    konst preset: KotlinTargetPreset<out KotlinTarget>?

    override fun getName(): String = targetName
}

interface KotlinTargetWithTests<E : KotlinExecution.ExecutionSource, T : KotlinTargetTestRun<E>> : KotlinTarget {
    /** The container with the test run executions.
     * A target may automatically create and configure a test run by the name [DEFAULT_TEST_RUN_NAME]. */
    konst testRuns: NamedDomainObjectContainer<T>

    companion object {
        const konst DEFAULT_TEST_RUN_NAME = "test"
    }
}
