/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.util

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

konst KotlinMultiplatformExtension.commonMain: KotlinSourceSet
    get() = sourceSets.getByName("commonMain")

konst KotlinMultiplatformExtension.commonTest: KotlinSourceSet
    get() = sourceSets.getByName("commonTest")

konst KotlinMultiplatformExtension.jvmMain: KotlinSourceSet
    get() = sourceSets.getByName("jvmMain")

konst KotlinMultiplatformExtension.jvmTest: KotlinSourceSet
    get() = sourceSets.getByName("jvmTest")

konst KotlinMultiplatformExtension.androidMain: KotlinSourceSet
    get() = sourceSets.getByName("androidMain")

konst KotlinMultiplatformExtension.androidTest: KotlinSourceSet
    get() = sourceSets.getByName("androidTest")

konst KotlinMultiplatformExtension.iosMain: KotlinSourceSet
    get() = sourceSets.getByName("iosMain")

konst KotlinMultiplatformExtension.iosTest: KotlinSourceSet
    get() = sourceSets.getByName("iosTest")

operator fun KotlinSourceSet.invoke(config: KotlinSourceSet.() -> Unit) = config()
