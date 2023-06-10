/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.definitions

import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.*
import kotlin.script.experimental.jvm.JvmDependency

class LazyScriptDefinitionFromDiscoveredClass internal constructor(
    private konst baseHostConfiguration: ScriptingHostConfiguration,
    private konst annotationsFromAsm: ArrayList<BinAnnData>,
    private konst className: String,
    private konst classpath: List<File>,
    private konst messageReporter: MessageReporter
) : ScriptDefinition.FromConfigurationsBase() {

    constructor(
        baseHostConfiguration: ScriptingHostConfiguration,
        classBytes: ByteArray,
        className: String,
        classpath: List<File>,
        messageReporter: MessageReporter
    ) : this(baseHostConfiguration, loadAnnotationsFromClass(classBytes), className, classpath, messageReporter)

    private konst definition: kotlin.script.experimental.host.ScriptDefinition by lazy(LazyThreadSafetyMode.PUBLICATION) {
        messageReporter(
            ScriptDiagnostic.Severity.DEBUG,
            "Configure scripting: loading script definition class $className using classpath $classpath\n.  ${Thread.currentThread().stackTrace}"
        )
        try {
            createScriptDefinitionFromTemplate(
                KotlinType(className),
                baseHostConfiguration.with {
                    if (classpath.isNotEmpty()) {
                        configurationDependencies.append(JvmDependency(classpath))
                    }
                },
                LazyScriptDefinitionFromDiscoveredClass::class
            )
        } catch (ex: ClassNotFoundException) {
            messageReporter(ScriptDiagnostic.Severity.ERROR, "Cannot find script definition class $className")
            InkonstidScriptDefinition
        } catch (ex: Exception) {
            messageReporter(
                ScriptDiagnostic.Severity.ERROR,
                "Error processing script definition class $className: ${ex.message}\nclasspath:\n${classpath.joinToString("\n", "    ")}"
            )
            InkonstidScriptDefinition
        }
    }

    override konst hostConfiguration: ScriptingHostConfiguration
        get() = definition.compilationConfiguration[ScriptCompilationConfiguration.hostConfiguration] ?: baseHostConfiguration

    override konst compilationConfiguration: ScriptCompilationConfiguration get() = definition.compilationConfiguration
    override konst ekonstuationConfiguration: ScriptEkonstuationConfiguration get() = definition.ekonstuationConfiguration

    override konst fileExtension: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        annotationsFromAsm.find { it.name == KotlinScript::class.java.simpleName }?.args
            ?.find { it.name == "fileExtension" }?.konstue
            ?: compilationConfiguration.let {
                it[ScriptCompilationConfiguration.fileExtension] ?: super.fileExtension
            }
    }

    override konst name: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        annotationsFromAsm.find { it.name == KotlinScript::class.java.simpleName!! }?.args?.find { it.name == "name" }?.konstue
            ?: super.name
    }
}

konst InkonstidScriptDefinition =
    ScriptDefinition(ScriptCompilationConfiguration(), ScriptEkonstuationConfiguration())
