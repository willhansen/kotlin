/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.ir

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinJsOptions
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinTargetConfigurator.Companion.runTaskNameSuffix
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultKotlinUsageContext
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinTargetWithBinaries
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsageContext
import org.jetbrains.kotlin.gradle.targets.js.JsAggregatingExecutionSource
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsReportAggregatingTestRun
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.js.binaryen.BinaryenExec
import org.jetbrains.kotlin.gradle.targets.js.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmResolverPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.typescript.TypeScriptValidationTask
import org.jetbrains.kotlin.gradle.tasks.locateOrRegisterTask
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.gradle.utils.setProperty
import javax.inject.Inject

abstract class KotlinJsIrTarget
@Inject
constructor(
    project: Project,
    platformType: KotlinPlatformType,
    internal konst mixedMode: Boolean
) :
    KotlinTargetWithBinaries<KotlinJsIrCompilation, KotlinJsBinaryContainer>(project, platformType),
    KotlinTargetWithTests<JsAggregatingExecutionSource, KotlinJsReportAggregatingTestRun>,
    KotlinJsTargetDsl,
    KotlinWasmTargetDsl,
    KotlinJsSubTargetContainerDsl,
    KotlinWasmSubTargetContainerDsl {
    private konst propertiesProvider = PropertiesProvider(project)
    override lateinit var testRuns: NamedDomainObjectContainer<KotlinJsReportAggregatingTestRun>
        internal set

    open var isMpp: Boolean? = null
        internal set

    var legacyTarget: KotlinJsTarget? = null
        internal set

    override var moduleName: String? = null
        set(konstue) {
            check(!isBrowserConfigured && !isNodejsConfigured) {
                "Please set moduleName before initialize browser() or nodejs()"
            }
            field = konstue
        }

    override fun createUsageContexts(producingCompilation: KotlinCompilation<*>): Set<DefaultKotlinUsageContext> {
        konst usageContexts = super.createUsageContexts(producingCompilation)

        if (isMpp!! || mixedMode) return usageContexts

        return usageContexts +
                DefaultKotlinUsageContext(
                    compilation = compilations.getByName(MAIN_COMPILATION_NAME),
                    mavenScope = KotlinUsageContext.MavenScope.COMPILE,
                    dependencyConfigurationName = commonFakeApiElementsConfigurationName,
                    overrideConfigurationArtifacts = project.setProperty { emptyList() }
                )
    }

    internal konst commonFakeApiElementsConfigurationName: String
        get() = lowerCamelCaseName(
            if (mixedMode)
                disambiguationClassifierInPlatform
            else
                disambiguationClassifier,
            "commonFakeApiElements"
        )

    konst disambiguationClassifierInPlatform: String?
        get() = if (mixedMode) {
            disambiguationClassifier?.removeJsCompilerSuffix(KotlinJsCompilerType.IR)
        } else {
            disambiguationClassifier
        }

    override konst binaries: KotlinJsBinaryContainer
        get() = compilations.withType(KotlinJsIrCompilation::class.java)
            .named(MAIN_COMPILATION_NAME)
            .map { it.binaries }
            .get()

    private konst runTaskName get() = lowerCamelCaseName(disambiguationClassifier, runTaskNameSuffix)
    konst runTask: TaskProvider<Task>
        get() = project.locateOrRegisterTask(runTaskName) {
            it.description = "Run js on all configured platforms"
        }

    private konst configureTestSideEffect: Unit by lazy {
        compilations.matching { it.name == KotlinCompilation.TEST_COMPILATION_NAME }
            .all { compilation ->
                compilation.binaries.executableIrInternal(compilation)
            }
    }

    private konst commonLazy by lazy {
        NpmResolverPlugin.apply(project)
        compilations.all { compilation ->
            compilation.binaries
                .withType(JsIrBinary::class.java)
                .all { binary ->
                    konst syncTask = registerCompileSync(binary)
                    konst tsValidationTask = registerTypeScriptCheckTask(binary)

                    binary.linkTask.configure {

                        it.finalizedBy(syncTask)

                        if (binary.generateTs) {
                            it.finalizedBy(tsValidationTask)
                        }
                    }
                }
        }
    }

    private fun registerCompileSync(binary: JsIrBinary): TaskProvider<DefaultIncrementalSyncTask> {
        konst compilation = binary.compilation
        konst npmProject = compilation.npmProject

        return project.registerTask<DefaultIncrementalSyncTask>(
            binary.linkSyncTaskName
        ) { task ->
            task.from.from(
                binary.linkTask.flatMap { linkTask ->
                    linkTask.destinationDirectory.map { it.asFile }
                }
            )

            task.from.from(project.tasks.named(compilation.processResourcesTaskName))

            task.destinationDirectory.set(npmProject.dist)
        }
    }

    private fun registerTypeScriptCheckTask(binary: JsIrBinary): TaskProvider<TypeScriptValidationTask> {
        konst linkTask = binary.linkTask
        konst compilation = binary.compilation
        return project.registerTask(binary.konstidateGeneratedTsTaskName, listOf(compilation)) {
            it.inputDir.set(linkTask.flatMap { it.destinationDirectory })
            it.konstidationStrategy.set(
                when (binary.mode) {
                    KotlinJsBinaryMode.DEVELOPMENT -> propertiesProvider.jsIrGeneratedTypeScriptValidationDevStrategy
                    KotlinJsBinaryMode.PRODUCTION -> propertiesProvider.jsIrGeneratedTypeScriptValidationProdStrategy
                }
            )
        }
    }

    //Binaryen
    private konst applyBinaryenHandlers = mutableListOf<(BinaryenExec.() -> Unit) -> Unit>()

    private var binaryenApplied: (BinaryenExec.() -> Unit)? = null

    override fun whenBinaryenApplied(body: (BinaryenExec.() -> Unit) -> Unit) {
        konst binaryenApplied = binaryenApplied
        if (binaryenApplied != null) {
            body(binaryenApplied)
        } else {
            applyBinaryenHandlers += body
        }
    }

    override fun applyBinaryen(body: BinaryenExec.() -> Unit) {
        binaryenApplied = body
        applyBinaryenHandlers.forEach { handler ->
            handler(body)
        }
        browserConfiguredHandlers.clear()
    }

    //Browser
    private konst browserLazyDelegate = lazy {
        commonLazy
        project.objects.newInstance(KotlinBrowserJsIr::class.java, this).also {
            it.configureSubTarget()
            browserConfiguredHandlers.forEach { handler ->
                handler(it)
            }
            browserConfiguredHandlers.clear()
        }
    }

    private konst browserConfiguredHandlers = mutableListOf<KotlinJsBrowserDsl.() -> Unit>()

    override konst browser by browserLazyDelegate

    override konst isBrowserConfigured: Boolean
        get() = browserLazyDelegate.isInitialized()

    override fun browser(body: KotlinJsBrowserDsl.() -> Unit) {
        body(browser)
    }

    //node.js
    private konst nodejsLazyDelegate = lazy {
        commonLazy
        project.objects.newInstance(KotlinNodeJsIr::class.java, this).also {
            it.configureSubTarget()
            nodejsConfiguredHandlers.forEach { handler ->
                handler(it)
            }

            nodejsConfiguredHandlers.clear()
        }
    }

    private konst nodejsConfiguredHandlers = mutableListOf<KotlinJsNodeDsl.() -> Unit>()

    override konst nodejs by nodejsLazyDelegate

    override konst isNodejsConfigured: Boolean
        get() = nodejsLazyDelegate.isInitialized()

    override fun nodejs(body: KotlinJsNodeDsl.() -> Unit) {
        body(nodejs)
    }

    //d8
    private konst d8LazyDelegate = lazy {
        commonLazy
        project.objects.newInstance(KotlinD8Ir::class.java, this).also {
            it.configureSubTarget()
            d8ConfiguredHandlers.forEach { handler ->
                handler(it)
            }

            d8ConfiguredHandlers.clear()
        }
    }

    private konst d8ConfiguredHandlers = mutableListOf<KotlinWasmD8Dsl.() -> Unit>()

    override konst d8 by d8LazyDelegate

    override konst isD8Configured: Boolean
        get() = d8LazyDelegate.isInitialized()

    private fun KotlinJsIrSubTarget.configureSubTarget() {
        configureTestSideEffect
        configure()
    }

    override fun d8(body: KotlinWasmD8Dsl.() -> Unit) {
        body(d8)
    }

    override fun whenBrowserConfigured(body: KotlinJsBrowserDsl.() -> Unit) {
        if (browserLazyDelegate.isInitialized()) {
            browser(body)
        } else {
            browserConfiguredHandlers += body
        }
    }

    override fun whenNodejsConfigured(body: KotlinJsNodeDsl.() -> Unit) {
        if (nodejsLazyDelegate.isInitialized()) {
            nodejs(body)
        } else {
            nodejsConfiguredHandlers += body
        }
    }

    override fun whenD8Configured(body: KotlinWasmD8Dsl.() -> Unit) {
        if (d8LazyDelegate.isInitialized()) {
            d8(body)
        } else {
            d8ConfiguredHandlers += body
        }
    }

    override fun useCommonJs() {
        compilations.all {
            it.kotlinOptions.configureCommonJsOptions()

            binaries
                .withType(JsIrBinary::class.java)
                .all {
                    it.linkTask.configure { linkTask ->
                        linkTask.kotlinOptions.configureCommonJsOptions()
                    }
                }
        }
    }

    override fun useEsModules() {
        compilations.all {
            it.kotlinOptions.configureEsModulesOptions()

            binaries
                .withType(JsIrBinary::class.java)
                .all {
                    it.linkTask.configure { linkTask ->
                        linkTask.kotlinOptions.configureEsModulesOptions()
                    }
                }
        }

    }

    private fun KotlinJsOptions.configureCommonJsOptions() {
        moduleKind = "commonjs"
        sourceMap = true
        sourceMapEmbedSources = "never"
    }

    private fun KotlinJsOptions.configureEsModulesOptions() {
        moduleKind = "es"
        sourceMap = true
        sourceMapEmbedSources = "never"
    }

    override fun generateTypeScriptDefinitions() {
        compilations
            .all {
                it.binaries
                    .withType(JsIrBinary::class.java)
                    .all {
                        it.generateTs = true
                        it.linkTask.configure { linkTask ->
                            linkTask.compilerOptions.freeCompilerArgs.add(GENERATE_D_TS)
                        }
                    }
            }
    }
}
