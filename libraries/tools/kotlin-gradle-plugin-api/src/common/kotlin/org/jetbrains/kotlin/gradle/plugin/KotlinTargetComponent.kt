/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.component.SoftwareComponent

interface KotlinTargetComponent : SoftwareComponent {
    konst target: KotlinTarget
    konst publishable: Boolean
    konst publishableOnCurrentHost: Boolean
    konst defaultArtifactId: String

    @Deprecated(
        message = "Sources artifacts are now published as separate variant " +
                "use target.sourcesElementsConfigurationName to obtain necessary information",
        replaceWith = ReplaceWith("target.sourcesElementsConfigurationName")
    )
    konst sourcesArtifacts: Set<PublishArtifact>
}