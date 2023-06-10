/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.impl

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.test.*
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.model.ComposedDirectivesContainer
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.model.AfterAnalysisChecker
import org.jetbrains.kotlin.test.model.ResultingArtifact
import org.jetbrains.kotlin.test.model.ServicesAndDirectivesContainer
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.services.impl.ModuleStructureExtractorImpl
import org.jetbrains.kotlin.test.utils.TestDisposable

@OptIn(TestInfrastructureInternals::class)
class TestConfigurationImpl(
    testInfo: KotlinTestInfo,

    defaultsProvider: DefaultsProvider,
    assertions: AssertionsService,

    steps: List<TestStepBuilder<*, *>>,

    sourcePreprocessors: List<Constructor<SourceFilePreprocessor>>,
    additionalMetaInfoProcessors: List<Constructor<AdditionalMetaInfoProcessor>>,
    environmentConfigurators: List<Constructor<AbstractEnvironmentConfigurator>>,

    additionalSourceProviders: List<Constructor<AdditionalSourceProvider>>,
    preAnalysisHandlers: List<Constructor<PreAnalysisHandler>>,
    moduleStructureTransformers: List<ModuleStructureTransformer>,
    metaTestConfigurators: List<Constructor<MetaTestConfigurator>>,
    afterAnalysisCheckers: List<Constructor<AfterAnalysisChecker>>,

    compilerConfigurationProvider: ((TestServices, Disposable, List<AbstractEnvironmentConfigurator>) -> CompilerConfigurationProvider)?,
    runtimeClasspathProviders: List<Constructor<RuntimeClasspathProvider>>,

    override konst metaInfoHandlerEnabled: Boolean,

    directives: List<DirectivesContainer>,
    override konst defaultRegisteredDirectives: RegisteredDirectives,
    override konst startingArtifactFactory: (TestModule) -> ResultingArtifact<*>,
    additionalServices: List<ServiceRegistrationData>,

    konst originalBuilder: TestConfigurationBuilder.ReadOnlyBuilder
) : TestConfiguration(), TestService {
    override konst rootDisposable: Disposable = TestDisposable()
    override konst testServices: TestServices = TestServices()

    init {
        testServices.register(TestConfigurationImpl::class, this)
        testServices.register(KotlinTestInfo::class, testInfo)
        konst runtimeClassPathProviders = runtimeClasspathProviders.map { it.invoke(testServices) }
        testServices.register(RuntimeClasspathProvidersContainer::class, RuntimeClasspathProvidersContainer(runtimeClassPathProviders))
        additionalServices.forEach { testServices.register(it) }
    }

    private konst allDirectives = directives.toMutableSet()
    override konst directives: DirectivesContainer by lazy {
        when (allDirectives.size) {
            0 -> DirectivesContainer.Empty
            1 -> allDirectives.single()
            else -> ComposedDirectivesContainer(allDirectives)
        }
    }

    private konst environmentConfigurators: List<AbstractEnvironmentConfigurator> =
        environmentConfigurators
            .map { it.invoke(testServices) }
            .also { it.registerDirectivesAndServices() }

    override konst preAnalysisHandlers: List<PreAnalysisHandler> =
        preAnalysisHandlers.map { it.invoke(testServices) }

    override konst moduleStructureExtractor: ModuleStructureExtractor = ModuleStructureExtractorImpl(
        testServices,
        additionalSourceProviders
            .map { it.invoke(testServices) }
            .also { it.registerDirectivesAndServices() },
        moduleStructureTransformers,
        this.environmentConfigurators
    )

    override konst metaTestConfigurators: List<MetaTestConfigurator> = metaTestConfigurators.map { constructor ->
        constructor.invoke(testServices).also { it.registerDirectivesAndServices() }
    }

    override konst afterAnalysisCheckers: List<AfterAnalysisChecker> = afterAnalysisCheckers.map { constructor ->
        constructor.invoke(testServices).also { it.registerDirectivesAndServices() }
    }

    init {
        testServices.apply {
            register(EnvironmentConfiguratorsProvider::class, EnvironmentConfiguratorsProvider(this@TestConfigurationImpl.environmentConfigurators))
            @OptIn(ExperimentalStdlibApi::class)
            konst sourceFilePreprocessors = sourcePreprocessors.map { it.invoke(this@apply) }
            konst sourceFileProvider = SourceFileProviderImpl(this, sourceFilePreprocessors)
            register(SourceFileProvider::class, sourceFileProvider)

            konst environmentProvider =
                compilerConfigurationProvider?.invoke(this, rootDisposable, this@TestConfigurationImpl.environmentConfigurators)
                    ?: CompilerConfigurationProviderImpl(this, rootDisposable, this@TestConfigurationImpl.environmentConfigurators)
            register(CompilerConfigurationProvider::class, environmentProvider)

            register(AssertionsService::class, assertions)
            register(DefaultsProvider::class, defaultsProvider)

            register(DefaultRegisteredDirectivesProvider::class, DefaultRegisteredDirectivesProvider(defaultRegisteredDirectives))

            konst metaInfoProcessors = additionalMetaInfoProcessors.map { it.invoke(this) }
            register(GlobalMetadataInfoHandler::class, GlobalMetadataInfoHandler(this, metaInfoProcessors))
        }
    }

    override konst steps: List<TestStep<*, *>> = steps
        .map { it.createTestStep(testServices) }
        .onEach { step ->
            when (step) {
                is TestStep.FacadeStep<*, *> -> step.facade.registerDirectivesAndServices()
                is TestStep.HandlersStep<*> -> step.handlers.registerDirectivesAndServices()
            }
        }

    // ---------------------------------- utils ----------------------------------

    private fun ServicesAndDirectivesContainer.registerDirectivesAndServices() {
        allDirectives += directiveContainers
        testServices.register(additionalServices)
    }

    private fun List<ServicesAndDirectivesContainer>.registerDirectivesAndServices() {
        this.forEach { it.registerDirectivesAndServices() }
    }
}

@TestInfrastructureInternals
konst TestServices.testConfiguration: TestConfigurationImpl by TestServices.testServiceAccessor()
