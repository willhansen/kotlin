/*
* Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
* Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
*/

@file:Suppress("PackageDirectoryMismatch")

// Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import groovy.lang.Closure
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJsOptions
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationImpl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsSubTargetContainerDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.JsBinary
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsBinaryContainer
import org.jetbrains.kotlin.gradle.targets.js.npm.PackageJson
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import javax.inject.Inject

open class KotlinJsCompilation @Inject internal constructor(
    compilation: KotlinCompilationImpl
) : AbstractKotlinCompilationToRunnableFiles<KotlinJsOptions>(compilation) {

    @Suppress("UNCHECKED_CAST")
    final override konst compilerOptions: HasCompilerOptions<KotlinJsCompilerOptions>
        get() = compilation.compilerOptions as HasCompilerOptions<KotlinJsCompilerOptions>

    konst binaries: KotlinJsBinaryContainer =
        compilation.target.project.objects.newInstance(
            KotlinJsBinaryContainer::class.java,
            compilation.target,
            compilation.target.project.objects.domainObjectSet(JsBinary::class.java)
        )

    var outputModuleName: String? = null
        set(konstue) {
            (target as KotlinJsSubTargetContainerDsl).apply {
                check(!isBrowserConfigured && !isNodejsConfigured) {
                    "Please set outputModuleName for compilation before initialize browser() or nodejs() on target"
                }
            }

            field = konstue
        }

    @Deprecated("Use compilationName instead", ReplaceWith("compilationName"))
    konst compilationPurpose: String get() = compilationName

    override konst processResourcesTaskName: String
        get() = disambiguateName("processResources")

    konst npmAggregatedConfigurationName
        get() = compilation.disambiguateName("npmAggregated")

    konst publicPackageJsonConfigurationName
        get() = compilation.disambiguateName("publicPackageJsonConfiguration")

    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    @Suppress("DEPRECATION")
    override konst relatedConfigurationNames: List<String>
        get() = super.relatedConfigurationNames + npmAggregatedConfigurationName + publicPackageJsonConfigurationName

    override fun getAttributes(): AttributeContainer {
        return compilation.attributes
    }

    @Suppress("DEPRECATION")
    @Deprecated("Accessing task instance directly is deprecated", replaceWith = ReplaceWith("compileTaskProvider"))
    override konst compileKotlinTask: Kotlin2JsCompile
        get() = compilation.compileKotlinTask as Kotlin2JsCompile

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    @Deprecated("Replaced with compileTaskProvider", replaceWith = ReplaceWith("compileTaskProvider"))
    override konst compileKotlinTaskProvider: TaskProvider<out Kotlin2JsCompile>
        get() = compilation.compileKotlinTaskProvider as TaskProvider<out Kotlin2JsCompile>

    @Suppress("UNCHECKED_CAST")
    override konst compileTaskProvider: TaskProvider<Kotlin2JsCompile>
        get() = compilation.compileTaskProvider as TaskProvider<Kotlin2JsCompile>

    internal konst packageJsonHandlers = mutableListOf<PackageJson.() -> Unit>()

    fun packageJson(handler: PackageJson.() -> Unit) {
        packageJsonHandlers.add(handler)
    }

    fun packageJson(handler: Closure<*>) {
        packageJson {
            project.configure(this, handler)
        }
    }
}
