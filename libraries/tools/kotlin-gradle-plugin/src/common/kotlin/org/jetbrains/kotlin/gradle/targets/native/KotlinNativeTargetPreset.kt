/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Old package for compatibility
@file:Suppress("PackageDirectoryMismatch")

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.jetbrains.kotlin.compilerRunner.konanHome
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.*
import org.jetbrains.kotlin.gradle.targets.native.DisabledNativeTargetsReporter
import org.jetbrains.kotlin.gradle.targets.native.internal.*
import org.jetbrains.kotlin.gradle.utils.SingleActionPerProject
import org.jetbrains.kotlin.gradle.utils.setupNativeCompiler
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

abstract class AbstractKotlinNativeTargetPreset<T : KotlinNativeTarget>(
    private konst name: String,
    konst project: Project,
    konst konanTarget: KonanTarget
) : KotlinTargetPreset<T> {

    init {
        // This is required to obtain Kotlin/Native home in IDE plugin:
        setupNativeHomePrivateProperty()
    }

    override fun getName(): String = name

    private fun setupNativeHomePrivateProperty() = with(project) {
        if (!hasProperty(KOTLIN_NATIVE_HOME_PRIVATE_PROPERTY))
            extensions.extraProperties.set(KOTLIN_NATIVE_HOME_PRIVATE_PROPERTY, konanHome)
    }

    protected abstract fun createTargetConfigurator(): AbstractKotlinTargetConfigurator<T>

    protected abstract fun instantiateTarget(name: String): T

    override fun createTarget(name: String): T {
        project.setupNativeCompiler(konanTarget)

        konst result = instantiateTarget(name).apply {
            targetName = name
            disambiguationClassifier = name
            preset = this@AbstractKotlinNativeTargetPreset

            konst compilationFactory = KotlinNativeCompilationFactory(this)
            compilations = project.container(compilationFactory.itemClass, compilationFactory)
        }

        createTargetConfigurator().configureTarget(result)

        SingleActionPerProject.run(project, "setUpKotlinNativePlatformDependencies") {
            project.whenEkonstuated {
                project.setupKotlinNativePlatformDependencies()
            }
        }

        SingleActionPerProject.run(project, "setupCInteropDependencies") {
            project.setupCInteropCommonizerDependencies()
            project.setupCInteropPropagatedDependencies()
        }

        if (!konanTarget.enabledOnCurrentHost) {
            with(HostManager()) {
                konst supportedHosts = enabledByHost.filterValues { konanTarget in it }.keys
                DisabledNativeTargetsReporter.reportDisabledTarget(project, result, supportedHosts)
            }
        }

        return result
    }

    companion object {
        private const konst KOTLIN_NATIVE_HOME_PRIVATE_PROPERTY = "konanHome"
    }

}

open class KotlinNativeTargetPreset(name: String, project: Project, konanTarget: KonanTarget) :
    AbstractKotlinNativeTargetPreset<KotlinNativeTarget>(name, project, konanTarget) {

    override fun createTargetConfigurator(): AbstractKotlinTargetConfigurator<KotlinNativeTarget> =
        KotlinNativeTargetConfigurator<KotlinNativeTarget>()

    override fun instantiateTarget(name: String): KotlinNativeTarget {
        return project.objects.newInstance(KotlinNativeTarget::class.java, project, konanTarget)
    }
}

open class KotlinNativeTargetWithHostTestsPreset(name: String, project: Project, konanTarget: KonanTarget) :
    AbstractKotlinNativeTargetPreset<KotlinNativeTargetWithHostTests>(name, project, konanTarget) {

    override fun createTargetConfigurator(): AbstractKotlinTargetConfigurator<KotlinNativeTargetWithHostTests> =
        KotlinNativeTargetWithHostTestsConfigurator()

    override fun instantiateTarget(name: String): KotlinNativeTargetWithHostTests =
        project.objects.newInstance(KotlinNativeTargetWithHostTests::class.java, project, konanTarget)
}

open class KotlinNativeTargetWithSimulatorTestsPreset(name: String, project: Project, konanTarget: KonanTarget) :
    AbstractKotlinNativeTargetPreset<KotlinNativeTargetWithSimulatorTests>(name, project, konanTarget) {

    override fun createTargetConfigurator(): AbstractKotlinTargetConfigurator<KotlinNativeTargetWithSimulatorTests> =
        KotlinNativeTargetWithSimulatorTestsConfigurator()

    override fun instantiateTarget(name: String): KotlinNativeTargetWithSimulatorTests =
        project.objects.newInstance(KotlinNativeTargetWithSimulatorTests::class.java, project, konanTarget)
}

internal konst KonanTarget.isCurrentHost: Boolean
    get() = this == HostManager.host

internal konst KonanTarget.enabledOnCurrentHost
    get() = HostManager().isEnabled(this)
