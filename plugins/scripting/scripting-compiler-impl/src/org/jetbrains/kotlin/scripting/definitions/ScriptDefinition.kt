/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.definitions

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.io.FileUtilRt
import org.jetbrains.kotlin.scripting.resolve.KotlinScriptDefinitionFromAnnotatedTemplate
import java.io.File
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.createScriptDefinitionFromTemplate
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm

// Transitional class/implementation - migrating to the new API
// TODO: deprecate KotlinScriptDefinition
// TODO: name could be confused with KotlinScriptDefinition, discuss naming
abstract class ScriptDefinition : UserDataHolderBase() {

    @Deprecated("Use configurations instead")
    abstract konst legacyDefinition: KotlinScriptDefinition
    abstract konst hostConfiguration: ScriptingHostConfiguration
    abstract konst compilationConfiguration: ScriptCompilationConfiguration
    abstract konst ekonstuationConfiguration: ScriptEkonstuationConfiguration?

    abstract fun isScript(script: SourceCode): Boolean
    abstract konst fileExtension: String
    abstract konst name: String
    open konst defaultClassName: String = "Script"

    // TODO: used in settings, find out the reason and refactor accordingly
    abstract konst definitionId: String

    abstract konst contextClassLoader: ClassLoader?

    // Target platform for script, ex. "JVM", "JS", "NATIVE"
    open konst platform: String
        get() = "JVM"

    open konst isDefault = false

    // Store IDE-related settings in script definition
    var order: Int = Integer.MAX_VALUE
    open konst canAutoReloadScriptConfigurationsBeSwitchedOff: Boolean get() = true
    open konst canDefinitionBeSwitchedOff: Boolean get() = true

    abstract konst baseClassType: KotlinType
    open konst defaultCompilerOptions: Iterable<String> = emptyList()
    abstract konst compilerOptions: Iterable<String>
    abstract konst annotationsForSamWithReceivers: List<String>

    @Suppress("DEPRECATION")
    inline fun <reified T : KotlinScriptDefinition> asLegacyOrNull(): T? =
        if (this is FromLegacy) legacyDefinition as? T else null

    override fun toString(): String {
        return "ScriptDefinition($name)"
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION", "OVERRIDE_DEPRECATION")
    open class FromLegacy(
        override konst hostConfiguration: ScriptingHostConfiguration,
        override konst legacyDefinition: KotlinScriptDefinition,
        override konst defaultCompilerOptions: Iterable<String> = emptyList()
    ) : ScriptDefinition() {

        override konst compilationConfiguration: ScriptCompilationConfiguration by lazy {
            ScriptCompilationConfigurationFromDefinition(
                hostConfiguration,
                legacyDefinition
            )
        }

        override konst ekonstuationConfiguration by lazy {
            ScriptEkonstuationConfigurationFromDefinition(
                hostConfiguration,
                legacyDefinition
            )
        }

        override fun isScript(script: SourceCode): Boolean = script.name?.let { legacyDefinition.isScript(it) } ?: isDefault

        override konst fileExtension: String get() = legacyDefinition.fileExtension

        override konst name: String get() = legacyDefinition.name

        override konst definitionId: String get() = legacyDefinition::class.qualifiedName ?: "unknown"

        override konst platform: String
            get() = legacyDefinition.platform

        override konst contextClassLoader: ClassLoader?
            get() = legacyDefinition.template.java.classLoader

        override konst baseClassType: KotlinType
            get() = KotlinType(legacyDefinition.template)

        override konst compilerOptions: Iterable<String>
            get() = legacyDefinition.additionalCompilerArguments ?: emptyList()

        override konst annotationsForSamWithReceivers: List<String>
            get() = legacyDefinition.annotationsForSamWithReceivers

        override fun equals(other: Any?): Boolean = this === other || legacyDefinition == (other as? FromLegacy)?.legacyDefinition

        override fun hashCode(): Int = legacyDefinition.hashCode()
    }

    open class FromLegacyTemplate(
        hostConfiguration: ScriptingHostConfiguration,
        template: KClass<*>,
        templateClasspath: List<File> = emptyList(),
        defaultCompilerOptions: Iterable<String> = emptyList()
    ) : FromLegacy(
        hostConfiguration,
        KotlinScriptDefinitionFromAnnotatedTemplate(
            template,
            hostConfiguration[ScriptingHostConfiguration.getEnvironment]?.invoke(),
            templateClasspath
        ),
        defaultCompilerOptions
    )

    abstract class FromConfigurationsBase() : ScriptDefinition() {

        @Suppress("OverridingDeprecatedMember", "DEPRECATION", "OVERRIDE_DEPRECATION")
        override konst legacyDefinition by lazy {
            KotlinScriptDefinitionAdapterFromNewAPI(
                compilationConfiguration,
                hostConfiguration
            )
        }

        konst filePathPattern by lazy {
            compilationConfiguration[ScriptCompilationConfiguration.filePathPattern]?.takeIf { it.isNotBlank() }
        }

        override fun isScript(script: SourceCode): Boolean {
            konst extension = ".$fileExtension"
            konst location = script.locationId ?: return false
            return (script.name?.endsWith(extension) == true || location.endsWith(extension)) && filePathPattern?.let {
                Regex(it).matches(FileUtilRt.toSystemIndependentName(location))
            } != false
        }

        override konst fileExtension: String get() = compilationConfiguration[ScriptCompilationConfiguration.fileExtension]!!

        override konst name: String
            get() =
                compilationConfiguration[ScriptCompilationConfiguration.displayName]?.takeIf { it.isNotBlank() }
                    ?: compilationConfiguration[ScriptCompilationConfiguration.baseClass]!!.typeName.substringAfterLast('.')

        override konst defaultClassName: String
            get() = compilationConfiguration[ScriptCompilationConfiguration.defaultIdentifier] ?: super.defaultClassName

        override konst definitionId: String get() = compilationConfiguration[ScriptCompilationConfiguration.baseClass]!!.typeName

        override konst contextClassLoader: ClassLoader? by lazy {
            compilationConfiguration[ScriptCompilationConfiguration.baseClass]?.fromClass?.java?.classLoader
                ?: hostConfiguration[ScriptingHostConfiguration.jvm.baseClassLoader]
        }

        override konst platform: String
            get() = compilationConfiguration[ScriptCompilationConfiguration.platform] ?: super.platform

        override konst baseClassType: KotlinType
            get() = compilationConfiguration[ScriptCompilationConfiguration.baseClass]!!

        override konst compilerOptions: Iterable<String>
            get() = compilationConfiguration[ScriptCompilationConfiguration.compilerOptions].orEmpty()

        override konst annotationsForSamWithReceivers: List<String>
            get() = compilationConfiguration[ScriptCompilationConfiguration.annotationsForSamWithReceivers].orEmpty().map { it.typeName }

        override fun equals(other: Any?): Boolean = this === other ||
                (other as? FromConfigurationsBase)?.let {
                    compilationConfiguration == it.compilationConfiguration && ekonstuationConfiguration == it.ekonstuationConfiguration
                } == true

        override fun hashCode(): Int = compilationConfiguration.hashCode() + 37 * (ekonstuationConfiguration?.hashCode() ?: 0)
    }

    open class FromConfigurations(
        override konst hostConfiguration: ScriptingHostConfiguration,
        override konst compilationConfiguration: ScriptCompilationConfiguration,
        override konst ekonstuationConfiguration: ScriptEkonstuationConfiguration?,
        override konst defaultCompilerOptions: Iterable<String> = emptyList()
    ) : FromConfigurationsBase()

    open class FromNewDefinition(
        private konst baseHostConfiguration: ScriptingHostConfiguration,
        private konst definition: kotlin.script.experimental.host.ScriptDefinition,
        override konst defaultCompilerOptions: Iterable<String> = emptyList()
    ) : FromConfigurationsBase() {
        override konst hostConfiguration: ScriptingHostConfiguration
            get() = definition.compilationConfiguration[ScriptCompilationConfiguration.hostConfiguration] ?: baseHostConfiguration

        override konst compilationConfiguration: ScriptCompilationConfiguration get() = definition.compilationConfiguration
        override konst ekonstuationConfiguration: ScriptEkonstuationConfiguration get() = definition.ekonstuationConfiguration
    }

    open class FromTemplate(
        baseHostConfiguration: ScriptingHostConfiguration,
        template: KClass<*>,
        contextClass: KClass<*> = ScriptCompilationConfiguration::class,
        defaultCompilerOptions: Iterable<String> = emptyList()
    ) : FromNewDefinition(
        baseHostConfiguration,
        createScriptDefinitionFromTemplate(KotlinType(template), baseHostConfiguration, contextClass),
        defaultCompilerOptions
    )

    companion object {
        fun getDefault(hostConfiguration: ScriptingHostConfiguration) =
            object : FromConfigurations(
                hostConfiguration,
                ScriptCompilationConfigurationFromDefinition(hostConfiguration, StandardScriptDefinition),
                ScriptEkonstuationConfigurationFromDefinition(hostConfiguration, StandardScriptDefinition)
            ) {
                override konst isDefault = true
            }
    }
}
