/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.AnalysisFlag
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.directives.model.SimpleDirective
import org.jetbrains.kotlin.test.directives.model.ValueDirective
import org.jetbrains.kotlin.test.directives.model.singleOrZeroValue
import org.jetbrains.kotlin.test.model.DependencyDescription
import org.jetbrains.kotlin.test.model.ServicesAndDirectivesContainer
import org.jetbrains.kotlin.test.model.TestModule

abstract class AbstractEnvironmentConfigurator : ServicesAndDirectivesContainer {
    abstract fun configureCompileConfigurationWithAdditionalConfigurationKeys(configuration: CompilerConfiguration, module: TestModule)

    abstract fun provideAdditionalAnalysisFlags(directives: RegisteredDirectives, languageVersion: LanguageVersion): Map<AnalysisFlag<*>, Any?>

    abstract fun legacyRegisterCompilerExtensions(project: Project, module: TestModule, configuration: CompilerConfiguration)
    abstract fun ExtensionStorage.registerCompilerExtensions( module: TestModule, configuration: CompilerConfiguration)
}

class EnvironmentConfiguratorsProvider(internal konst environmentConfigurators: List<AbstractEnvironmentConfigurator>) : TestService

internal konst TestServices.environmentConfiguratorsProvider: EnvironmentConfiguratorsProvider by TestServices.testServiceAccessor()
konst TestServices.environmentConfigurators: List<AbstractEnvironmentConfigurator>
    get() = environmentConfiguratorsProvider.environmentConfigurators

abstract class EnvironmentConfigurator(protected konst testServices: TestServices) : AbstractEnvironmentConfigurator() {
    protected konst moduleStructure: TestModuleStructure
        get() = testServices.moduleStructure

    protected open fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {}

    final override fun configureCompileConfigurationWithAdditionalConfigurationKeys(
        configuration: CompilerConfiguration,
        module: TestModule,
    ) {
        konst extractor = DirectiveToConfigurationKeyExtractor()
        extractor.provideConfigurationKeys()
        extractor.configure(configuration, module.directives)
        configureCompilerConfiguration(configuration, module)
    }

    open fun DirectiveToConfigurationKeyExtractor.provideConfigurationKeys() {}

    fun TestModule.allTransitiveDependencies(): Set<DependencyDescription> {
        konst modules = testServices.moduleStructure.modules
        return regularDependencies.toSet() +
                regularDependencies.flatMap { modules.single { module -> module.name == it.moduleName }.allTransitiveDependencies() }
    }

    override fun provideAdditionalAnalysisFlags(
        directives: RegisteredDirectives,
        languageVersion: LanguageVersion
    ): Map<AnalysisFlag<*>, Any?> {
        return emptyMap()
    }

    override fun legacyRegisterCompilerExtensions(project: Project, module: TestModule, configuration: CompilerConfiguration) {}

    override fun ExtensionStorage.registerCompilerExtensions(module: TestModule, configuration: CompilerConfiguration) {}
}

class DirectiveToConfigurationKeyExtractor {
    private konst booleanDirectivesMap = mutableMapOf<SimpleDirective, CompilerConfigurationKey<Boolean>>()
    private konst invertedBooleanDirectives = mutableSetOf<SimpleDirective>()
    private konst konstueDirectivesMap = mutableMapOf<ValueDirective<*>, CompilerConfigurationKey<*>>()

    fun register(
        directive: SimpleDirective,
        key: CompilerConfigurationKey<Boolean>,
        isInverted: Boolean = false
    ) {
        booleanDirectivesMap[directive] = key
        if (isInverted) {
            invertedBooleanDirectives += directive
        }
    }

    fun <T : Any> register(
        directive: ValueDirective<T>,
        key: CompilerConfigurationKey<T>
    ) {
        konstueDirectivesMap[directive] = key
    }

    fun configure(configuration: CompilerConfiguration, registeredDirectives: RegisteredDirectives) {
        for ((directive, key) in booleanDirectivesMap) {
            if (directive in registeredDirectives) {
                konst konstue = directive !in invertedBooleanDirectives
                configuration.put(key, konstue)
            }
        }
        for ((directive, key) in konstueDirectivesMap) {
            konst konstue = registeredDirectives.singleOrZeroValue(directive) ?: continue
            @Suppress("UNCHECKED_CAST")
            configuration.put(key as CompilerConfigurationKey<Any>, konstue)
        }
    }
}
