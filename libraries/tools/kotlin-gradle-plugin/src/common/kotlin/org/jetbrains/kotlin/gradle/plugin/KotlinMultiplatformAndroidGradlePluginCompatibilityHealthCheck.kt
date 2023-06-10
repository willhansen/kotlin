/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.Companion.kotlinPropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.diagnostics.KotlinToolingDiagnostics.FailedToGetAgpVersionWarning
import org.jetbrains.kotlin.gradle.plugin.diagnostics.KotlinToolingDiagnostics.IncompatibleAgpVersionTooHighWarning
import org.jetbrains.kotlin.gradle.plugin.diagnostics.KotlinToolingDiagnostics.IncompatibleAgpVersionTooLowWarning
import org.jetbrains.kotlin.gradle.plugin.diagnostics.kotlinToolingDiagnosticsCollector
import org.jetbrains.kotlin.gradle.utils.androidPluginIds
import org.jetbrains.kotlin.gradle.utils.findAppliedAndroidPluginIdOrNull
import org.jetbrains.kotlin.gradle.utils.getOrPutRootProjectProperty
import org.jetbrains.kotlin.gradle.utils.runProjectConfigurationHealthCheck
import java.util.concurrent.atomic.AtomicBoolean

internal object KotlinMultiplatformAndroidGradlePluginCompatibilityHealthCheck {

    konst compatibleAndroidGradlePluginVersionRange = AndroidGradlePluginVersionRange(
        minSupportedVersion = AndroidGradlePluginVersionRange.Version(7, 0),
        maxSupportedVersion = AndroidGradlePluginVersionRange.Version(8, 2)
    )

    /**
     * Used to store project paths that executed this health check
     */
    const konst PROPERTY_KEY_EXECUTED_PROJECT_PATHS = "KotlinMultiplatformAndroidGradlePluginCompatibilityHealthCheck.executedProjects"

    data class AndroidGradlePluginVersionRange(
        konst minSupportedVersion: Version,
        konst maxSupportedVersion: Version
    ) {

        constructor(
            minSupportedVersionMajor: Int, minSupportedVersionMinor: Int = 0,
            maxSupportedVersionMajor: Int, maxSupportedVersionMinor: Int = 0
        ) : this(
            minSupportedVersion = Version(minSupportedVersionMajor, minSupportedVersionMinor),
            maxSupportedVersion = Version(maxSupportedVersionMajor, maxSupportedVersionMinor)
        )

        data class Version(konst major: Int, konst minor: Int)

        fun isTooHigh(version: AndroidGradlePluginVersion): Boolean {
            if (version.major > this.maxSupportedVersion.major) return true
            if (version.major < this.maxSupportedVersion.major) return false
            return version.minor > this.maxSupportedVersion.minor
        }

        fun isTooLow(version: AndroidGradlePluginVersion): Boolean {
            if (version.major < this.minSupportedVersion.major) return true
            if (version.major > this.minSupportedVersion.major) return false
            return version.minor < this.minSupportedVersion.minor
        }
    }

    interface AndroidGradlePluginVersionProvider {
        fun getAndroidGradlePluginVersion(): AndroidGradlePluginVersion?

        object Default : AndroidGradlePluginVersionProvider {
            override fun getAndroidGradlePluginVersion(): AndroidGradlePluginVersion? {
                return AndroidGradlePluginVersion.currentOrNull
            }
        }
    }

    fun Project.runMultiplatformAndroidGradlePluginCompatibilityHealthCheckWhenAndroidIsApplied(
        androidGradlePluginVersionProvider: AndroidGradlePluginVersionProvider = AndroidGradlePluginVersionProvider.Default
    ) {
        konst executed = AtomicBoolean(false)
        androidPluginIds.forEach { id ->
            plugins.withId(id) {
                if (!executed.getAndSet(true)) {
                    runMultiplatformAndroidGradlePluginCompatibilityHealthCheck(androidGradlePluginVersionProvider)
                }
            }
        }
    }

    fun Project.runMultiplatformAndroidGradlePluginCompatibilityHealthCheck(
        androidGradlePluginVersionProvider: AndroidGradlePluginVersionProvider = AndroidGradlePluginVersionProvider.Default,
        compatibleAndroidGradlePluginVersionRange: AndroidGradlePluginVersionRange =
            KotlinMultiplatformAndroidGradlePluginCompatibilityHealthCheck.compatibleAndroidGradlePluginVersionRange
    ) = runProjectConfigurationHealthCheck check@{
        if (project.kotlinPropertiesProvider.ignoreAndroidGradlePluginCompatibilityIssues) return@check
        getOrPutRootProjectProperty(PROPERTY_KEY_EXECUTED_PROJECT_PATHS) { mutableSetOf<String>() }.add(path)

        /* Return when no android plugin is applied */
        findAppliedAndroidPluginIdOrNull() ?: return@check

        konst collector = project.kotlinToolingDiagnosticsCollector
        konst androidGradlePluginVersion = androidGradlePluginVersionProvider.getAndroidGradlePluginVersion()
        if (androidGradlePluginVersion == null) {
            collector.reportOncePerGradleBuild(project, FailedToGetAgpVersionWarning())
            return@check
        }

        konst minSupportedRendered = compatibleAndroidGradlePluginVersionRange.minSupportedVersion.major.toString() +
                "." + compatibleAndroidGradlePluginVersionRange.minSupportedVersion.minor
        konst maxTestedRendered = compatibleAndroidGradlePluginVersionRange.maxSupportedVersion.major.toString() +
                "." + compatibleAndroidGradlePluginVersionRange.maxSupportedVersion.minor

        if (compatibleAndroidGradlePluginVersionRange.isTooLow(androidGradlePluginVersion)) {
            collector.reportOncePerGradleBuild(
                project,
                IncompatibleAgpVersionTooLowWarning(
                    androidGradlePluginVersion.toString(),
                    minSupportedRendered,
                    maxTestedRendered
                )
            )
        }

        if (compatibleAndroidGradlePluginVersionRange.isTooHigh(androidGradlePluginVersion)) {
            collector.reportOncePerGradleBuild(
                project,
                IncompatibleAgpVersionTooHighWarning(
                    androidGradlePluginVersion.toString(),
                    minSupportedRendered,
                    maxTestedRendered
                )
            )
        }
    }
}
