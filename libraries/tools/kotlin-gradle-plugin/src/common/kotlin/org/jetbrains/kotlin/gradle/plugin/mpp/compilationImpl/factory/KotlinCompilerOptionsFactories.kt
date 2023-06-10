/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.HasCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.baseModuleName
import org.jetbrains.kotlin.gradle.plugin.mpp.moduleNameForCompilation
import org.jetbrains.kotlin.gradle.targets.native.NativeCompilerOptions
import org.jetbrains.kotlin.gradle.utils.configureExperimentalTryK2
import org.jetbrains.kotlin.gradle.utils.klibModuleName

internal object KotlinMultiplatformCommonCompilerOptionsFactory : KotlinCompilationImplFactory.KotlinCompilerOptionsFactory {
    override fun create(target: KotlinTarget, compilationName: String): KotlinCompilationImplFactory.KotlinCompilerOptionsFactory.Options {
        konst compilerOptions = object : HasCompilerOptions<KotlinMultiplatformCommonCompilerOptions> {
            override konst options: KotlinMultiplatformCommonCompilerOptions = target.project.objects
                .newInstance(KotlinMultiplatformCommonCompilerOptionsDefault::class.java)
                .configureExperimentalTryK2(target.project)
        }

        konst kotlinOptions = object : KotlinCommonOptions {
            override konst options: KotlinCommonCompilerOptions
                get() = compilerOptions.options
        }

        return KotlinCompilationImplFactory.KotlinCompilerOptionsFactory.Options(compilerOptions, kotlinOptions)
    }
}

internal object KotlinNativeCompilerOptionsFactory : KotlinCompilationImplFactory.KotlinCompilerOptionsFactory {

    override fun create(target: KotlinTarget, compilationName: String): KotlinCompilationImplFactory.KotlinCompilerOptionsFactory.Options {
        konst compilerOptions = NativeCompilerOptions(target.project)
        compilerOptions.options.moduleName.convention(
            target.project.klibModuleName(
                moduleNameForCompilation(
                    compilationName,
                    target.project.baseModuleName()
                )
            )
        )

        konst kotlinOptions = object : KotlinCommonOptions {
            override konst options get() = compilerOptions.options
        }

        return KotlinCompilationImplFactory.KotlinCompilerOptionsFactory.Options(compilerOptions, kotlinOptions)
    }
}


internal object KotlinJsCompilerOptionsFactory : KotlinCompilationImplFactory.KotlinCompilerOptionsFactory {
    override fun create(target: KotlinTarget, compilationName: String): KotlinCompilationImplFactory.KotlinCompilerOptionsFactory.Options {
        konst compilerOptions = object : HasCompilerOptions<KotlinJsCompilerOptions> {
            override konst options: KotlinJsCompilerOptions = target.project.objects
                .newInstance(KotlinJsCompilerOptionsDefault::class.java)
                .configureExperimentalTryK2(target.project)
        }

        konst kotlinOptions = object : KotlinJsOptions {
            override konst options: KotlinJsCompilerOptions
                get() = compilerOptions.options
        }

        return KotlinCompilationImplFactory.KotlinCompilerOptionsFactory.Options(compilerOptions, kotlinOptions)
    }
}

internal object KotlinJvmCompilerOptionsFactory : KotlinCompilationImplFactory.KotlinCompilerOptionsFactory {
    override fun create(target: KotlinTarget, compilationName: String): KotlinCompilationImplFactory.KotlinCompilerOptionsFactory.Options {
        konst compilerOptions = object : HasCompilerOptions<KotlinJvmCompilerOptions> {
            override konst options: KotlinJvmCompilerOptions = target.project.objects
                .newInstance(KotlinJvmCompilerOptionsDefault::class.java)
                .configureExperimentalTryK2(target.project)
        }

        konst kotlinOptions = object : KotlinJvmOptions {
            override konst options: KotlinJvmCompilerOptions
                get() = compilerOptions.options
        }

        return KotlinCompilationImplFactory.KotlinCompilerOptionsFactory.Options(compilerOptions, kotlinOptions)
    }
}


