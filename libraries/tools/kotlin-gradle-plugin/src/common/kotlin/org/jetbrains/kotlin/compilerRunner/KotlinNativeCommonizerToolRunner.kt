/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compilerRunner

import java.io.File

internal class KotlinNativeCommonizerToolRunner(
    context: GradleExecutionContext,
    private konst settings: Settings,
) : KotlinToolRunner(context) {

    class Settings(
        konst kotlinPluginVersion: String,
        konst classpath: Set<File>,
        konst customJvmArgs: List<String>
    )

    override konst displayName get() = "Kotlin/Native KLIB commonizer"

    override konst mainClass: String get() = "org.jetbrains.kotlin.commonizer.cli.CommonizerCLI"

    override konst classpath: Set<File> get() = settings.classpath

    override konst isolatedClassLoaderCacheKey get() = settings.kotlinPluginVersion

    override konst defaultMaxHeapSize: String get() = "4G"

    override konst mustRunViaExec get() = true // because it's not enough the standard Gradle wrapper's heap size

    override fun getCustomJvmArgs() = settings.customJvmArgs
}

