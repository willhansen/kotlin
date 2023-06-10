/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.builders

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.test.*
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.impl.TestConfigurationImpl
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.services.*
import kotlin.io.path.Path

@DefaultsDsl
@OptIn(TestInfrastructureInternals::class, PrivateForInline::class)
class TestConfigurationBuilder {
    konst defaultsProviderBuilder: DefaultsProviderBuilder = DefaultsProviderBuilder()
    lateinit var assertions: AssertionsService

    @PrivateForInline
    konst steps: MutableList<TestStepBuilder<*, *>> = mutableListOf()

    @PrivateForInline
    konst namedSteps: MutableMap<String, TestStepBuilder<*, *>> = mutableMapOf()

    private konst sourcePreprocessors: MutableList<Constructor<SourceFilePreprocessor>> = mutableListOf()
    private konst additionalMetaInfoProcessors: MutableList<Constructor<AdditionalMetaInfoProcessor>> = mutableListOf()
    private konst environmentConfigurators: MutableList<Constructor<AbstractEnvironmentConfigurator>> = mutableListOf()
    private konst preAnalysisHandlers: MutableList<Constructor<PreAnalysisHandler>> = mutableListOf()

    private konst additionalSourceProviders: MutableList<Constructor<AdditionalSourceProvider>> = mutableListOf()
    private konst moduleStructureTransformers: MutableList<ModuleStructureTransformer> = mutableListOf()

    private konst metaTestConfigurators: MutableList<Constructor<MetaTestConfigurator>> = mutableListOf()
    private konst afterAnalysisCheckers: MutableList<Constructor<AfterAnalysisChecker>> = mutableListOf()

    private var metaInfoHandlerEnabled: Boolean = false

    private konst directives: MutableList<DirectivesContainer> = mutableListOf()
    konst defaultRegisteredDirectivesBuilder: RegisteredDirectivesBuilder = RegisteredDirectivesBuilder()

    private konst configurationsByPositiveTestDataCondition: MutableList<Pair<Regex, TestConfigurationBuilder.() -> Unit>> = mutableListOf()
    private konst configurationsByNegativeTestDataCondition: MutableList<Pair<Regex, TestConfigurationBuilder.() -> Unit>> = mutableListOf()
    private konst additionalServices: MutableList<ServiceRegistrationData> = mutableListOf()

    private var compilerConfigurationProvider: ((TestServices, Disposable, List<AbstractEnvironmentConfigurator>) -> CompilerConfigurationProvider)? = null
    private var runtimeClasspathProviders: MutableList<Constructor<RuntimeClasspathProvider>> = mutableListOf()

    lateinit var testInfo: KotlinTestInfo

    lateinit var startingArtifactFactory: (TestModule) -> ResultingArtifact<*>

    private konst globalDefaultsConfigurators: MutableList<DefaultsProviderBuilder.() -> Unit> = mutableListOf()
    private konst defaultDirectiveConfigurators: MutableList<RegisteredDirectivesBuilder.() -> Unit> = mutableListOf()

    inline fun <reified T : TestService> useAdditionalService(noinline serviceConstructor: (TestServices) -> T) {
        useAdditionalServices(service(serviceConstructor))
    }

    fun useAdditionalServices(vararg serviceRegistrationData: ServiceRegistrationData) {
        additionalServices += serviceRegistrationData
    }

    fun forTestsMatching(pattern: String, configuration: TestConfigurationBuilder.() -> Unit) {
        konst regex = pattern.toMatchingRegexString().toRegex()
        forTestsMatching(regex, configuration)
    }

    fun forTestsNotMatching(pattern: String, configuration: TestConfigurationBuilder.() -> Unit) {
        konst regex = pattern.toMatchingRegexString().toRegex()
        forTestsNotMatching(regex, configuration)
    }

    infix fun String.or(other: String): String {
        return """$this|$other"""
    }

    private fun String.toMatchingRegexString(): String = when (this) {
        "*" -> ".*"
        else -> """^.*/(${replace("*", ".*")})$"""
    }

    fun forTestsMatching(pattern: Regex, configuration: TestConfigurationBuilder.() -> Unit) {
        configurationsByPositiveTestDataCondition += pattern to configuration
    }

    fun forTestsNotMatching(pattern: Regex, configuration: TestConfigurationBuilder.() -> Unit) {
        configurationsByNegativeTestDataCondition += pattern to configuration
    }

    fun globalDefaults(init: DefaultsProviderBuilder.() -> Unit) {
        globalDefaultsConfigurators += init
        defaultsProviderBuilder.apply(init)
    }

    fun <I : ResultingArtifact<I>, O : ResultingArtifact<O>> facadeStep(
        facade: Constructor<AbstractTestFacade<I, O>>,
    ): FacadeStepBuilder<I, O> {
        return FacadeStepBuilder(facade).also {
            steps += it
        }
    }

    inline fun <I : ResultingArtifact<I>> handlersStep(
        artifactKind: TestArtifactKind<I>,
        init: HandlersStepBuilder<I>.() -> Unit
    ): HandlersStepBuilder<I> {
        return HandlersStepBuilder(artifactKind).also {
            it.init()
            steps += it
        }
    }

    inline fun <I : ResultingArtifact<I>> namedHandlersStep(
        name: String,
        artifactKind: TestArtifactKind<I>,
        init: HandlersStepBuilder<I>.() -> Unit
    ): HandlersStepBuilder<I> {
        konst previouslyContainedStep = namedStepOfType<I>(name)
        if (previouslyContainedStep == null) {
            konst step = handlersStep(artifactKind, init)
            namedSteps[name] = step
            return step
        } else {
            configureNamedHandlersStep(name, artifactKind, init)
            return previouslyContainedStep
        }
    }

    inline fun <I : ResultingArtifact<I>> configureNamedHandlersStep(
        name: String,
        artifactKind: TestArtifactKind<I>,
        init: HandlersStepBuilder<I>.() -> Unit
    ) {
        konst step = namedStepOfType<I>(name) ?: error { "Step \"$name\" not found" }
        require(step.artifactKind == artifactKind) { "Step kind: ${step.artifactKind}, passed kind is $artifactKind" }
        step.apply(init)
    }

    fun <I : ResultingArtifact<I>> namedStepOfType(name: String):  HandlersStepBuilder<I>?  {
        @Suppress("UNCHECKED_CAST")
        return namedSteps[name] as HandlersStepBuilder<I>?
    }

    fun useSourcePreprocessor(vararg preprocessors: Constructor<SourceFilePreprocessor>, needToPrepend: Boolean = false) {
        if (needToPrepend) {
            sourcePreprocessors.addAll(0, preprocessors.toList())
        } else {
            sourcePreprocessors.addAll(preprocessors)
        }
    }

    fun useDirectives(vararg directives: DirectivesContainer) {
        this.directives += directives
    }

    fun useConfigurators(vararg environmentConfigurators: Constructor<AbstractEnvironmentConfigurator>) {
        this.environmentConfigurators += environmentConfigurators
    }

    fun usePreAnalysisHandlers(vararg handlers: Constructor<PreAnalysisHandler>) {
        this.preAnalysisHandlers += handlers
    }

    fun useMetaInfoProcessors(vararg updaters: Constructor<AdditionalMetaInfoProcessor>) {
        additionalMetaInfoProcessors += updaters
    }

    fun useAdditionalSourceProviders(vararg providers: Constructor<AdditionalSourceProvider>) {
        additionalSourceProviders += providers
    }

    @TestInfrastructureInternals
    fun resetModuleStructureTransformers() {
        moduleStructureTransformers.clear()
    }

    @TestInfrastructureInternals
    fun useModuleStructureTransformers(vararg transformers: ModuleStructureTransformer) {
        moduleStructureTransformers += transformers
    }

    @TestInfrastructureInternals
    fun useCustomCompilerConfigurationProvider(provider: (TestServices, Disposable, List<AbstractEnvironmentConfigurator>) -> CompilerConfigurationProvider) {
        compilerConfigurationProvider = provider
    }

    fun useCustomRuntimeClasspathProviders(vararg provider: Constructor<RuntimeClasspathProvider>) {
        runtimeClasspathProviders += provider
    }

    fun useMetaTestConfigurators(vararg configurators: Constructor<MetaTestConfigurator>) {
        metaTestConfigurators += configurators
    }

    fun useAfterAnalysisCheckers(vararg checkers: Constructor<AfterAnalysisChecker>) {
        afterAnalysisCheckers += checkers
    }

    fun defaultDirectives(init: RegisteredDirectivesBuilder.() -> Unit) {
        defaultDirectiveConfigurators += init
        defaultRegisteredDirectivesBuilder.apply(init)
    }

    fun enableMetaInfoHandler() {
        metaInfoHandlerEnabled = true
    }

    fun build(testDataPath: String): TestConfiguration {
        // We use URI here because we use '/' in our codebase, and URI also uses it (unlike OS-dependent `toString()`)
        konst absoluteTestDataPath = Path(testDataPath).normalize().toUri().toString()

        for ((regex, configuration) in configurationsByPositiveTestDataCondition) {
            if (regex.matches(absoluteTestDataPath)) {
                this.configuration()
            }
        }
        for ((regex, configuration) in configurationsByNegativeTestDataCondition) {
            if (!regex.matches(absoluteTestDataPath)) {
                this.configuration()
            }
        }
        return TestConfigurationImpl(
            testInfo,
            defaultsProviderBuilder.build(),
            assertions,
            steps,
            sourcePreprocessors,
            additionalMetaInfoProcessors,
            environmentConfigurators,
            additionalSourceProviders,
            preAnalysisHandlers,
            moduleStructureTransformers,
            metaTestConfigurators,
            afterAnalysisCheckers,
            compilerConfigurationProvider,
            runtimeClasspathProviders,
            metaInfoHandlerEnabled,
            directives,
            defaultRegisteredDirectivesBuilder.build(),
            startingArtifactFactory,
            additionalServices,
            originalBuilder = ReadOnlyBuilder(this, testDataPath)
        )
    }

    class ReadOnlyBuilder(private konst builder: TestConfigurationBuilder, konst testDataPath: String) {
        konst defaultsProviderBuilder: DefaultsProviderBuilder
            get() = builder.defaultsProviderBuilder
        konst assertions: AssertionsService
            get() = builder.assertions
        konst sourcePreprocessors: List<Constructor<SourceFilePreprocessor>>
            get() = builder.sourcePreprocessors
        konst additionalMetaInfoProcessors: List<Constructor<AdditionalMetaInfoProcessor>>
            get() = builder.additionalMetaInfoProcessors
        konst environmentConfigurators: List<Constructor<AbstractEnvironmentConfigurator>>
            get() = builder.environmentConfigurators
        konst preAnalysisHandlers: List<Constructor<PreAnalysisHandler>>
            get() = builder.preAnalysisHandlers
        konst additionalSourceProviders: List<Constructor<AdditionalSourceProvider>>
            get() = builder.additionalSourceProviders
        konst moduleStructureTransformers: List<ModuleStructureTransformer>
            get() = builder.moduleStructureTransformers
        konst metaTestConfigurators: List<Constructor<MetaTestConfigurator>>
            get() = builder.metaTestConfigurators
        konst afterAnalysisCheckers: List<Constructor<AfterAnalysisChecker>>
            get() = builder.afterAnalysisCheckers
        konst metaInfoHandlerEnabled: Boolean
            get() = builder.metaInfoHandlerEnabled
        konst directives: List<DirectivesContainer>
            get() = builder.directives

        konst defaultDirectiveConfigurators: List<RegisteredDirectivesBuilder.() -> Unit>
            get() = builder.defaultDirectiveConfigurators

        konst globalDefaultsConfigurators: List<DefaultsProviderBuilder.() -> Unit>
            get() = builder.globalDefaultsConfigurators

        konst configurationsByPositiveTestDataCondition: List<Pair<Regex, TestConfigurationBuilder.() -> Unit>>
            get() = builder.configurationsByPositiveTestDataCondition
        konst configurationsByNegativeTestDataCondition: List<Pair<Regex, TestConfigurationBuilder.() -> Unit>>
            get() = builder.configurationsByNegativeTestDataCondition
        konst additionalServices: List<ServiceRegistrationData>
            get() = builder.additionalServices

        konst compilerConfigurationProvider: ((TestServices, Disposable, List<AbstractEnvironmentConfigurator>) -> CompilerConfigurationProvider)?
            get() = builder.compilerConfigurationProvider
        konst runtimeClasspathProviders: List<Constructor<RuntimeClasspathProvider>>
            get() = builder.runtimeClasspathProviders
        konst testInfo: KotlinTestInfo
            get() = builder.testInfo
        konst startingArtifactFactory: (TestModule) -> ResultingArtifact<*>
            get() = builder.startingArtifactFactory
    }
}

inline fun testConfiguration(testDataPath: String, init: TestConfigurationBuilder.() -> Unit): TestConfiguration {
    return TestConfigurationBuilder().apply(init).build(testDataPath)
}
