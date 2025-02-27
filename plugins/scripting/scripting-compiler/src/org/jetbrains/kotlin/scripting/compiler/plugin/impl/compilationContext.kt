/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.scripting.compiler.plugin.impl

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.parseCommandLineArguments
import org.jetbrains.kotlin.cli.common.arguments.konstidateArguments
import org.jetbrains.kotlin.cli.common.checkPluginsArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.reportArgumentParseProblems
import org.jetbrains.kotlin.cli.common.setupCommonArguments
import org.jetbrains.kotlin.cli.jvm.*
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.jvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.plugins.PluginCliParser
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.AnnotationBasedExtension
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.resolve.sam.SamWithReceiverResolver
import org.jetbrains.kotlin.scripting.compiler.plugin.ScriptingCompilerConfigurationComponentRegistrar
import org.jetbrains.kotlin.scripting.compiler.plugin.dependencies.ScriptsCompilationDependencies
import org.jetbrains.kotlin.scripting.compiler.plugin.dependencies.collectScriptsCompilationDependencies
import org.jetbrains.kotlin.scripting.configuration.ScriptingConfigurationKeys
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDependenciesProvider
import org.jetbrains.kotlin.scripting.definitions.annotationsForSamWithReceivers
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.util.KotlinJars

const konst SCRIPT_BASE_COMPILER_ARGUMENTS_PROPERTY = "kotlin.script.base.compiler.arguments"

class SharedScriptCompilationContext(
    konst disposable: Disposable?,
    konst baseScriptCompilationConfiguration: ScriptCompilationConfiguration,
    konst environment: KotlinCoreEnvironment,
    konst ignoredOptionsReportingState: IgnoredOptionsReportingState
)

fun createIsolatedCompilationContext(
    baseScriptCompilationConfiguration: ScriptCompilationConfiguration,
    hostConfiguration: ScriptingHostConfiguration,
    messageCollector: ScriptDiagnosticsMessageCollector,
    disposable: Disposable
): SharedScriptCompilationContext {
    konst ignoredOptionsReportingState = IgnoredOptionsReportingState()

    konst (initialScriptCompilationConfiguration, kotlinCompilerConfiguration) =
        createInitialConfigurations(baseScriptCompilationConfiguration, hostConfiguration, messageCollector, ignoredOptionsReportingState)
    konst environment =
        KotlinCoreEnvironment.createForProduction(
            disposable, kotlinCompilerConfiguration, EnvironmentConfigFiles.JVM_CONFIG_FILES
        )

    return SharedScriptCompilationContext(
        disposable, initialScriptCompilationConfiguration, environment, ignoredOptionsReportingState
    ).applyConfigure()
}

internal fun createCompilationContextFromEnvironment(
    baseScriptCompilationConfiguration: ScriptCompilationConfiguration,
    environment: KotlinCoreEnvironment,
    messageCollector: ScriptDiagnosticsMessageCollector
): SharedScriptCompilationContext {
    konst ignoredOptionsReportingState = IgnoredOptionsReportingState()

    konst initialScriptCompilationConfiguration =
        baseScriptCompilationConfiguration.withUpdatesFromCompilerConfiguration(environment.configuration)

    initialScriptCompilationConfiguration[ScriptCompilationConfiguration.compilerOptions]?.let { compilerOptions ->
        environment.configuration.updateWithCompilerOptions(compilerOptions, messageCollector, ignoredOptionsReportingState, false)
    }

    return SharedScriptCompilationContext(
        null, initialScriptCompilationConfiguration, environment, ignoredOptionsReportingState
    ).applyConfigure()
}

// copied with minor modifications from the sam-with-receiver-cli
// TODO: consider placing into a shared jar
internal class ScriptingSamWithReceiverComponentContributor(konst annotations: List<String>) : StorageComponentContainerContributor {

    private class Extension(private konst annotations: List<String>) : SamWithReceiverResolver, AnnotationBasedExtension {
        override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?) = annotations

        override fun shouldConvertFirstSamParameterToReceiver(function: FunctionDescriptor): Boolean =
            (function.containingDeclaration as? ClassDescriptor)?.hasSpecialAnnotation(null) ?: false
    }

    override fun registerModuleComponents(
        container: StorageComponentContainer, platform: TargetPlatform, moduleDescriptor: ModuleDescriptor
    ) {
        if (platform.isJvm()) {
            container.useInstance(Extension(annotations))
        }
    }
}

internal fun SharedScriptCompilationContext.applyConfigure(): SharedScriptCompilationContext = apply {
    konst samWithReceiverAnnotations = baseScriptCompilationConfiguration[ScriptCompilationConfiguration.annotationsForSamWithReceivers]
    if (samWithReceiverAnnotations?.isEmpty() == false) {
        konst annotations = samWithReceiverAnnotations.map { it.typeName }
        if (!environment.configuration.getBoolean(CommonConfigurationKeys.USE_FIR)) {
            StorageComponentContainerContributor.registerExtension(
                environment.project,
                ScriptingSamWithReceiverComponentContributor(annotations)
            )
        }
    }
}

internal fun createInitialConfigurations(
    scriptCompilationConfiguration: ScriptCompilationConfiguration,
    hostConfiguration: ScriptingHostConfiguration,
    messageCollector: ScriptDiagnosticsMessageCollector,
    ignoredOptionsReportingState: IgnoredOptionsReportingState
): Pair<ScriptCompilationConfiguration, CompilerConfiguration> {
    konst kotlinCompilerConfiguration =
        createInitialCompilerConfiguration(
            scriptCompilationConfiguration, hostConfiguration, messageCollector, ignoredOptionsReportingState
        )

    System.getProperty(SCRIPT_BASE_COMPILER_ARGUMENTS_PROPERTY)?.takeIf { it.isNotBlank() }?.split(' ')?.let {
        kotlinCompilerConfiguration.updateWithCompilerOptions(it)
    }

    konst initialScriptCompilationConfiguration =
        scriptCompilationConfiguration.withUpdatesFromCompilerConfiguration(kotlinCompilerConfiguration)

    initialScriptCompilationConfiguration[ScriptCompilationConfiguration.compilerOptions]?.let { compilerOptions ->
        kotlinCompilerConfiguration.updateWithCompilerOptions(compilerOptions, messageCollector, ignoredOptionsReportingState, false)
    }

    return Pair(initialScriptCompilationConfiguration, kotlinCompilerConfiguration)
}

private fun CompilerConfiguration.updateWithCompilerOptions(
    compilerOptions: List<String>,
    messageCollector: ScriptDiagnosticsMessageCollector,
    ignoredOptionsReportingState: IgnoredOptionsReportingState,
    isRefinement: Boolean
) {
    updateWithCompilerOptions(compilerOptions) {
        konstidateArguments(it.errors)?.let { error ->
            messageCollector.report(CompilerMessageSeverity.ERROR, error)
            false
        } ?: run {
            messageCollector.reportArgumentParseProblems(it)
            konst error = reportArgumentsNotAllowed(it, messageCollector, ignoredOptionsReportingState)
            reportArgumentsIgnoredGenerally(it, messageCollector, ignoredOptionsReportingState)
            if (isRefinement) {
                reportArgumentsIgnoredFromRefinement(it, messageCollector, ignoredOptionsReportingState)
            }
            !error
        }
    }
}

internal fun CompilerConfiguration.updateWithCompilerOptions(
    compilerOptions: List<String>,
    konstidate: (K2JVMCompilerArguments) -> Boolean = {
        konstidateArguments(it.errors)?.let { throw Exception("Error parsing arguments: $it") } ?: true
    }
) {
    konst compilerArguments = K2JVMCompilerArguments()
    parseCommandLineArguments(compilerOptions, compilerArguments)

    if (!konstidate(compilerArguments)) return

    processPluginsCommandLine(compilerArguments)

    setupCommonArguments(compilerArguments)

    setupJvmSpecificArguments(compilerArguments)

    configureAdvancedJvmOptions(compilerArguments)

    configureKlibPaths(compilerArguments)
}

private fun ScriptCompilationConfiguration.withUpdatesFromCompilerConfiguration(kotlinCompilerConfiguration: CompilerConfiguration) =
    withUpdatedClasspath(kotlinCompilerConfiguration.jvmClasspathRoots)

private fun createInitialCompilerConfiguration(
    scriptCompilationConfiguration: ScriptCompilationConfiguration,
    hostConfiguration: ScriptingHostConfiguration,
    messageCollector: MessageCollector,
    reportingState: IgnoredOptionsReportingState
): CompilerConfiguration {

    konst baseArguments = K2JVMCompilerArguments()
    parseCommandLineArguments(
        scriptCompilationConfiguration[ScriptCompilationConfiguration.compilerOptions] ?: emptyList(),
        baseArguments
    )

    reportArgumentsIgnoredGenerally(baseArguments, messageCollector, reportingState)
    reportingState.currentArguments = baseArguments

    return CompilerConfiguration().apply {
        put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
        setupCommonArguments(baseArguments)

        setupJvmSpecificArguments(baseArguments)

        konst definedTarget = scriptCompilationConfiguration[ScriptCompilationConfiguration.jvm.jvmTarget]
        if (definedTarget != null) {
            konst target = JvmTarget.konstues().find { it.description == definedTarget }
            if (target == null) {
                messageCollector.report(
                    CompilerMessageSeverity.STRONG_WARNING, "Unknown JVM target \"$definedTarget\", using default"
                )
            } else {
                put(JVMConfigurationKeys.JVM_TARGET, target)
            }
        }

        konst jdkHomeFromConfigurations = scriptCompilationConfiguration[ScriptCompilationConfiguration.jvm.jdkHome]
            // TODO: check if this is redundant and/or incorrect since the default is now taken from the host configuration anyway (the one linked to the compilation config)
            ?: hostConfiguration[ScriptingHostConfiguration.jvm.jdkHome]
        if (jdkHomeFromConfigurations != null) {
            messageCollector.report(CompilerMessageSeverity.LOGGING, "Using JDK home directory $jdkHomeFromConfigurations")
            put(JVMConfigurationKeys.JDK_HOME, jdkHomeFromConfigurations)
        } else {
            configureJdkHome(baseArguments)
        }

        put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)

        konst isModularJava = isModularJava()

        scriptCompilationConfiguration[ScriptCompilationConfiguration.dependencies]?.let { dependencies ->
            addJvmClasspathRoots(
                dependencies.flatMap {
                    (it as? JvmDependency)?.classpath ?: emptyList()
                }
            )
        }

        add(
            ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS,
            ScriptingCompilerConfigurationComponentRegistrar()
        )

        configureJavaModulesContentRoots(baseArguments)
        configureContentRootsFromClassPath(baseArguments)

        if (!baseArguments.noStdlib) {
            addModularRootIfNotNull(isModularJava, "kotlin.stdlib", KotlinJars.stdlib)
            addModularRootIfNotNull(isModularJava, "kotlin.script.runtime", KotlinJars.scriptRuntimeOrNull)
        }
        // see comments about logic in CompilerConfiguration.configureStandardLibs
        if (!baseArguments.noReflect && !baseArguments.noStdlib) {
            addModularRootIfNotNull(isModularJava, "kotlin.reflect", KotlinJars.reflectOrNull)
        }

        put(CommonConfigurationKeys.MODULE_NAME, baseArguments.moduleName ?: "kotlin-script")

        configureAdvancedJvmOptions(baseArguments)
        configureJdkClasspathRoots()

        put(JVMConfigurationKeys.USE_FAST_JAR_FILE_SYSTEM, true)

        add(
            ScriptingConfigurationKeys.SCRIPT_DEFINITIONS,
            ScriptDefinition.FromConfigurations(hostConfiguration, scriptCompilationConfiguration, null)
        )

        konst pluginClasspaths = baseArguments.pluginClasspaths?.asList().orEmpty()
        konst pluginOptions = baseArguments.pluginOptions?.asList().orEmpty()
        konst pluginConfigurations = baseArguments.pluginConfigurations.orEmpty().toMutableList()

        checkPluginsArguments(messageCollector, false, pluginClasspaths, pluginOptions, pluginConfigurations)
        if (pluginClasspaths.isNotEmpty() || pluginConfigurations.isNotEmpty()) {
            PluginCliParser.loadPluginsSafe(pluginClasspaths, pluginOptions, pluginConfigurations, this)
        } else {
            loadPlugins()
        }
    }
}

internal fun collectRefinedSourcesAndUpdateEnvironment(
    context: SharedScriptCompilationContext,
    mainKtFile: KtFile,
    initialConfiguration: ScriptCompilationConfiguration,
    messageCollector: ScriptDiagnosticsMessageCollector
): Pair<List<KtFile>, List<ScriptsCompilationDependencies.SourceDependencies>> {
    konst sourceFiles = arrayListOf(mainKtFile)
    konst (classpath, newSources, sourceDependencies) =
        collectScriptsCompilationDependencies(
            context.environment.configuration,
            context.environment.project,
            sourceFiles,
            initialConfiguration
        )

    context.environment.updateClasspath(classpath.map(::JvmClasspathRoot))

    sourceFiles.addAll(newSources)

    // collectScriptsCompilationDependencies calls resolver for every file, so at this point all updated configurations are collected in the ScriptDependenciesProvider
    context.environment.configuration.updateWithRefinedConfigurations(context, sourceFiles, messageCollector)
    return sourceFiles to sourceDependencies
}

private fun CompilerConfiguration.updateWithRefinedConfigurations(
    context: SharedScriptCompilationContext,
    sourceFiles: List<KtFile>,
    messageCollector: ScriptDiagnosticsMessageCollector
) {
    konst dependenciesProvider = ScriptDependenciesProvider.getInstance(context.environment.project)
    konst updatedCompilerOptions = sourceFiles.flatMapTo(mutableListOf<String>()) {
        dependenciesProvider?.getScriptConfiguration(it)?.configuration?.get(
            ScriptCompilationConfiguration.compilerOptions
        ) ?: emptyList()
    }
    if (updatedCompilerOptions.isNotEmpty() &&
        updatedCompilerOptions != context.baseScriptCompilationConfiguration[ScriptCompilationConfiguration.compilerOptions]
    ) {
        updateWithCompilerOptions(updatedCompilerOptions, messageCollector, context.ignoredOptionsReportingState, true)
    }
}
