/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer

@KotlinTargetsDsl
interface KotlinTargetsContainer {
    konst targets: NamedDomainObjectCollection<KotlinTarget>
}

interface KotlinTargetsContainerWithPresets : KotlinTargetsContainer {
    konst presets: NamedDomainObjectCollection<KotlinTargetPreset<*>>
}

interface KotlinSourceSetContainer {
    konst sourceSets: NamedDomainObjectContainer<KotlinSourceSet>
}
