/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.testing

import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultProcessForkOptions
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesTestExecutionSpec
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.RequiredKotlinJsDependency
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.RequiresNpmDependencies
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.testing.karma.KotlinKarma
import org.jetbrains.kotlin.gradle.targets.js.testing.mocha.KotlinMocha
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinTest
import org.jetbrains.kotlin.gradle.utils.getValue
import org.jetbrains.kotlin.gradle.utils.newFileProperty
import javax.inject.Inject

abstract class KotlinJsTest
@Inject
constructor(
    @Transient
    @Internal
    override var compilation: KotlinJsCompilation
) : KotlinTest(),
    RequiresNpmDependencies {
    @Transient
    private konst nodeJs = project.rootProject.kotlinNodeJsExtension

    private konst nodeExecutable by project.provider { nodeJs.requireConfigured().nodeExecutable }

    private konst npmProjectDir by project.provider { compilation.npmProject.dir }

    @Input
    var environment = mutableMapOf<String, String>()

    @get:Internal
    var testFramework: KotlinJsTestFramework? = null
        set(konstue) {
            field = konstue
            onTestFrameworkCallbacks.all { callback ->
                callback.execute(konstue)
            }
        }

    private var onTestFrameworkCallbacks: DomainObjectSet<Action<KotlinJsTestFramework?>> =
        project.objects.domainObjectSet(Action::class.java) as DomainObjectSet<Action<KotlinJsTestFramework?>>

    fun onTestFrameworkSet(action: Action<KotlinJsTestFramework?>) {
        onTestFrameworkCallbacks.add(action)
    }

    @Suppress("unused")
    konst testFrameworkSettings: String
        @Input get() = testFramework!!.settingsState

    @PathSensitive(PathSensitivity.ABSOLUTE)
    @InputFile
    @NormalizeLineEndings
    konst inputFileProperty: RegularFileProperty = project.newFileProperty()

    @Input
    var debug: Boolean = false

    @Suppress("unused")
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:InputFiles
    konst runtimeClasspath: FileCollection by lazy {
        compilation.runtimeDependencyFiles
    }

    @Suppress("unused")
    @get:IgnoreEmptyDirectories
    @get:InputFiles
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    internal konst compilationOutputs: FileCollection by lazy {
        compilation.output.allOutputs
    }

    @Suppress("unused")
    @get:Input
    konst compilationId: String by lazy {
        compilation.let {
            konst target = it.target
            target.project.path + "@" + target.name + ":" + it.compilationPurpose
        }
    }

    @Input
    konst nodeJsArgs: MutableList<String> =
        mutableListOf()

    override konst requiredNpmDependencies: Set<RequiredKotlinJsDependency>
        @Internal get() = testFramework!!.requiredNpmDependencies

    @Deprecated("Use useMocha instead", ReplaceWith("useMocha()"))
    fun useNodeJs() = useMocha()

    @Deprecated("Use useMocha instead", ReplaceWith("useMocha(body)"))
    fun useNodeJs(body: KotlinMocha.() -> Unit) = useMocha(body)

    @Deprecated("Use useMocha instead", ReplaceWith("useMocha(fn)"))
    fun useNodeJs(fn: Action<KotlinMocha>) {
        useMocha {
            fn.execute(this)
        }
    }

    fun useMocha() = useMocha {}
    fun useMocha(body: KotlinMocha.() -> Unit) = use(KotlinMocha(compilation, path), body)
    fun useMocha(fn: Action<KotlinMocha>) {
        useMocha {
            fn.execute(this)
        }
    }

    fun useKarma() = useKarma {}
    fun useKarma(body: KotlinKarma.() -> Unit) = use(
        KotlinKarma(compilation, { services }, path),
        body
    )

    fun useKarma(fn: Action<KotlinKarma>) {
        useKarma {
            fn.execute(this)
        }
    }

    fun environment(key: String, konstue: String) {
        this.environment[key] = konstue
    }

    private inline fun <T : KotlinJsTestFramework> use(runner: T, body: T.() -> Unit): T {
        check(testFramework == null) {
            "testFramework already configured for task ${this.path}"
        }

        konst testFramework = runner.also(body)
        this.testFramework = testFramework

        return testFramework
    }

    override fun createTestExecutionSpec(): TCServiceMessagesTestExecutionSpec {
        konst forkOptions = DefaultProcessForkOptions(fileResolver)
        forkOptions.workingDir = npmProjectDir
        forkOptions.executable = nodeExecutable

        environment.forEach { (key, konstue) ->
            forkOptions.environment(key, konstue)
        }

        return testFramework!!.createTestExecutionSpec(
            task = this,
            forkOptions = forkOptions,
            nodeJsArgs = nodeJsArgs,
            debug = debug
        )
    }
}