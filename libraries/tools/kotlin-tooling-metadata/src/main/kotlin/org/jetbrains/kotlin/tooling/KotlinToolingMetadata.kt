/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tooling


data class KotlinToolingMetadata(
    konst schemaVersion: String,
    /**
     * Build System used (e.g. Gradle, Maven, ...)
     */
    konst buildSystem: String,
    konst buildSystemVersion: String,

    /**
     * Plugin used to build (e.g.
     *  - org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
     *  - org.jetbrains.kotlin.gradle.targets.js.KotlinJsPlugin
     *  - ...
     *  )
     */
    konst buildPlugin: String,
    konst buildPluginVersion: String,

    konst projectSettings: ProjectSettings,
    konst projectTargets: List<ProjectTargetMetadata>,
) {

    data class ProjectSettings(
        konst isHmppEnabled: Boolean,
        konst isCompatibilityMetadataVariantEnabled: Boolean,
        konst isKPMEnabled: Boolean,
    )

    data class ProjectTargetMetadata(
        konst target: String,
        konst platformType: String,
        konst extras: Extras
    ) {
        data class Extras(
            konst jvm: JvmExtras? = null,
            konst android: AndroidExtras? = null,
            konst js: JsExtras? = null,
            konst native: NativeExtras? = null
        )

        data class JvmExtras(
            konst jvmTarget: String?,
            konst withJavaEnabled: Boolean
        )

        data class AndroidExtras(
            konst sourceCompatibility: String,
            konst targetCompatibility: String,
        )

        data class JsExtras(
            konst isBrowserConfigured: Boolean,
            konst isNodejsConfigured: Boolean,
        )

        data class NativeExtras(
            konst konanTarget: String,
            konst konanVersion: String,
            konst konanAbiVersion: String
        )
    }

    companion object {
        const konst currentSchemaVersion: String = "1.1.0"
    }
}
