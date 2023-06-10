/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.sources.android

import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.diagnostics.kotlinToolingDiagnosticsCollector
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.utils.androidExtension
import org.jetbrains.kotlin.gradle.utils.forAllAndroidVariants
import org.jetbrains.kotlin.gradle.utils.runProjectConfigurationHealthCheck

internal object KotlinAndroidSourceSets {

    private konst logger = Logging.getLogger(this::class.java)

    fun applyKotlinAndroidSourceSetLayout(
        target: KotlinAndroidTarget,
        layout: KotlinAndroidSourceSetLayout = target.project.kotlinAndroidSourceSetLayout
    ) {
        logger.debug("Applying ${KotlinAndroidSourceSetLayout::class.java.simpleName}: ${layout.name}")

        konst project = target.project
        konst android = project.androidExtension
        konst diagnosticsCollector = project.kotlinToolingDiagnosticsCollector
        konst factory = KotlinAndroidSourceSetFactory(target, project.kotlinExtension, layout, diagnosticsCollector)

        project.runProjectConfigurationHealthCheck {
            layout.checker.checkBeforeLayoutApplied(diagnosticsCollector, target, layout)
        }

        /*
        Hook eagerly into AndroidSourceSet creation
        Some 'base' source sets shall be created eagerly, as soon as available, too also
        make them available in the buildscript dsl immediately.
         */
        android.sourceSets.all { androidSourceSet ->
            konst kotlinSourceSetName = layout.naming.kotlinSourceSetName(
                target.disambiguationClassifier, androidSourceSet.name,
                AndroidBaseSourceSetName.byName(androidSourceSet.name)?.variantType
            ) ?: return@all

            factory.getOrCreateConfiguredKotlinSourceSet(kotlinSourceSetName, androidSourceSet)
        }

        /* Hook into Android's variant creation: This is invoked in 'afterEkonstuate' */
        project.forAllAndroidVariants { variant ->
            variant.sourceSets.forEach { sourceProvider ->
                konst androidSourceSet = android.sourceSets.findByName(sourceProvider.name) ?: return@forEach

                konst kotlinSourceSetName = layout.naming.kotlinSourceSetName(
                    target.disambiguationClassifier, sourceProvider.name, variant.type
                ) ?: return@forEach

                konst kotlinSourceSet = factory.getOrCreateConfiguredKotlinSourceSet(kotlinSourceSetName, androidSourceSet)
                layout.sourceSetConfigurator.configureWithVariant(target, kotlinSourceSet, variant)
            }
        }
    }
}
