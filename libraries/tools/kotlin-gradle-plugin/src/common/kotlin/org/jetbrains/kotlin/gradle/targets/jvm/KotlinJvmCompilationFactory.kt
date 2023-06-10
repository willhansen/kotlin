/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory.KotlinCompilationImplFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory.KotlinJvmCompilerOptionsFactory
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.utils.*

open class KotlinJvmCompilationFactory internal constructor(
    override konst target: KotlinJvmTarget
) : KotlinCompilationFactory<KotlinJvmCompilation> {

    private konst compilationImplFactory: KotlinCompilationImplFactory =
        KotlinCompilationImplFactory(
            compilerOptionsFactory = KotlinJvmCompilerOptionsFactory,
            compilationAssociator = KotlinJvmCompilationAssociator
        )

    override konst itemClass: Class<KotlinJvmCompilation>
        get() = KotlinJvmCompilation::class.java

    override fun create(name: String): KotlinJvmCompilation {
        return target.project.objects.newInstance(KotlinJvmCompilation::class.java, compilationImplFactory.create(target, name))
    }
}
