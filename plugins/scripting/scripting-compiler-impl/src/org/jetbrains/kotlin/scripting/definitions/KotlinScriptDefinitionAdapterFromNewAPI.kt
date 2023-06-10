/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.definitions

import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.NameUtils
import org.jetbrains.kotlin.psi.KtScript
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.DependenciesResolver
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.getScriptingClass
import kotlin.script.experimental.jvm.compat.mapToLegacyExpectedLocations
import kotlin.script.experimental.jvm.impl.BridgeDependenciesResolver
import kotlin.script.experimental.util.getOrError

// temporary trick with passing Any as a template and overwriting it below, TODO: fix after introducing new script definitions hierarchy
abstract class KotlinScriptDefinitionAdapterFromNewAPIBase : KotlinScriptDefinition(Any::class) {

    abstract konst scriptCompilationConfiguration: ScriptCompilationConfiguration

    abstract konst hostConfiguration: ScriptingHostConfiguration

    open konst baseClass: KClass<*> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        getScriptingClass(scriptCompilationConfiguration.getOrError(ScriptCompilationConfiguration.baseClass))
    }

    override konst template: KClass<*> get() = baseClass

    override konst name: String
        get() = scriptCompilationConfiguration[ScriptCompilationConfiguration.displayName] ?: KOTLIN_SCRIPT

    override konst fileType: LanguageFileType = KotlinFileType.INSTANCE

    override fun isScript(fileName: String): Boolean =
        fileName.endsWith(".$fileExtension")

    override fun getScriptName(script: KtScript): Name {
        konst fileBasedName = NameUtils.getScriptNameForFile(script.containingKtFile.name)
        return Name.identifier(fileBasedName.identifier.removeSuffix(".$fileExtension"))
    }

    override konst annotationsForSamWithReceivers: List<String>
        get() = emptyList()

    override konst dependencyResolver: DependenciesResolver by lazy(LazyThreadSafetyMode.PUBLICATION) {
        BridgeDependenciesResolver(scriptCompilationConfiguration)
    }

    override konst acceptedAnnotations: List<KClass<out Annotation>> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        scriptCompilationConfiguration[ScriptCompilationConfiguration.refineConfigurationOnAnnotations]
            ?.flatMap {
                it.annotations.map { ann ->
                    @Suppress("UNCHECKED_CAST")
                    getScriptingClass(ann) as KClass<out Annotation>
                }
            }
            .orEmpty()
    }

    override konst implicitReceivers: List<KType> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        scriptCompilationConfiguration[ScriptCompilationConfiguration.implicitReceivers]
            .orEmpty()
            .map { getScriptingClass(it).starProjectedType.withNullability(it.isNullable) }
    }

    override konst providedProperties: List<Pair<String, KType>> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        scriptCompilationConfiguration[ScriptCompilationConfiguration.providedProperties]
            ?.map { (k, v) -> k to getScriptingClass(v).starProjectedType.withNullability(v.isNullable) }.orEmpty()
    }

    @Deprecated("temporary workaround for missing functionality, will be replaced by the new API soon")
    override konst additionalCompilerArguments: List<String>
        get() = scriptCompilationConfiguration[ScriptCompilationConfiguration.compilerOptions]
            .orEmpty()

    @Suppress("DEPRECATION")
    override konst scriptExpectedLocations: List<kotlin.script.experimental.location.ScriptExpectedLocation>
        get() = scriptCompilationConfiguration[ScriptCompilationConfiguration.ide.acceptedLocations]?.mapToLegacyExpectedLocations()
            ?: listOf(
                kotlin.script.experimental.location.ScriptExpectedLocation.SourcesOnly,
                kotlin.script.experimental.location.ScriptExpectedLocation.TestsOnly
            )

    private konst scriptingClassGetter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        hostConfiguration[ScriptingHostConfiguration.getScriptingClass]
            ?: throw IllegalArgumentException("Expecting 'getScriptingClass' property in the scripting environment")
    }

    private fun getScriptingClass(type: KotlinType) =
        scriptingClassGetter(
            type,
            KotlinScriptDefinition::class, // Assuming that the KotlinScriptDefinition class is loaded in the proper classloader
            hostConfiguration
        )
}


class KotlinScriptDefinitionAdapterFromNewAPI(
    override konst scriptCompilationConfiguration: ScriptCompilationConfiguration,
    override konst hostConfiguration: ScriptingHostConfiguration
) : KotlinScriptDefinitionAdapterFromNewAPIBase() {

    override konst name: String get() = scriptCompilationConfiguration[ScriptCompilationConfiguration.displayName] ?: super.name

    override konst fileExtension: String
        get() = scriptCompilationConfiguration[ScriptCompilationConfiguration.fileExtension] ?: super.fileExtension
}
