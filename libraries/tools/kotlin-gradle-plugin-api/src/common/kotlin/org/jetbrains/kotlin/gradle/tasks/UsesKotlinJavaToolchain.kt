/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks

import org.gradle.api.JavaVersion
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.util.GradleVersion
import java.io.File

interface UsesKotlinJavaToolchain : Task {
    @get:Nested
    konst kotlinJavaToolchainProvider: Provider<out KotlinJavaToolchain>

    @get:Internal
    konst kotlinJavaToolchain: KotlinJavaToolchain
        get() = kotlinJavaToolchainProvider.get()
}

interface KotlinJavaToolchain {
    @get:Input
    konst javaVersion: Provider<JavaVersion>

    @get:Internal
    konst jdk: JdkSetter

    @get:Internal
    konst toolchain: JavaToolchainSetter

    interface JdkSetter {
        /**
         * Set JDK to use for Kotlin compilation.
         *
         * Major JDK version is considered as compile task input.
         *
         * @param jdkHomeLocation path to the JDK
         *
         * *Note*: project build will fail on providing here JRE instead of JDK!
         *
         * @param jdkVersion provided JDK version
         */
        fun use(
            jdkHomeLocation: File,
            jdkVersion: JavaVersion
        )

        /**
         * Set JDK to use for Kotlin compilation.
         *
         * @param jdkHomeLocation path to the JDK
         *
         * **Note**: project build will fail on providing here JRE instead of JDK!
         *
         * @param jdkVersion any type that is accepted by [JavaVersion.toVersion]
         */
        fun use(
            jdkHomeLocation: String,
            jdkVersion: Any
        ) = use(File(jdkHomeLocation), JavaVersion.toVersion(jdkVersion))
    }

    interface JavaToolchainSetter {
        /**
         * Set JDK obtained from [org.gradle.jvm.toolchain.JavaToolchainService] to use for Kotlin compilation.
         */
        fun use(
            javaLauncher: Provider<JavaLauncher>
        )
    }
}
