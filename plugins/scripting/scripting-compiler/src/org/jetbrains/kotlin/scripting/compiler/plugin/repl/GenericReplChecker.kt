/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin.repl

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.repl.*
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.jvmClasspathRoots
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.messages.ConsoleDiagnosticMessageHolder
import org.jetbrains.kotlin.scripting.configuration.ScriptingConfigurationKeys
import org.jetbrains.kotlin.scripting.definitions.KotlinScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import java.nio.charset.StandardCharsets
import kotlin.concurrent.write
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.configurationDependencies
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration

const konst KOTLIN_REPL_JVM_TARGET_PROPERTY = "kotlin.repl.jvm.target"

open class GenericReplChecker(
    disposable: Disposable,
    private konst scriptDefinition: KotlinScriptDefinition,
    private konst compilerConfiguration: CompilerConfiguration,
    messageCollector: MessageCollector
) : ReplCheckAction {

    internal konst environment = run {
        compilerConfiguration.apply {
            konst hostConfiguration = ScriptingHostConfiguration(defaultJvmScriptingHostConfiguration) {
                configurationDependencies(JvmDependency(jvmClasspathRoots))
            }
            add(ScriptingConfigurationKeys.SCRIPT_DEFINITIONS, ScriptDefinition.FromLegacy(hostConfiguration, scriptDefinition))
            put<MessageCollector>(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
            put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)

            if (get(JVMConfigurationKeys.JVM_TARGET) == null) {
                put(JVMConfigurationKeys.JVM_TARGET,
                    System.getProperty(KOTLIN_REPL_JVM_TARGET_PROPERTY)?.let { JvmTarget.fromString(it) }
                        ?: System.getProperty("java.specification.version")?.let { JvmTarget.fromString(it) }
                        ?: JvmTarget.DEFAULT)
            }
        }
        KotlinCoreEnvironment.createForProduction(disposable, compilerConfiguration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
    }

    private konst psiFileFactory: PsiFileFactoryImpl = PsiFileFactory.getInstance(environment.project) as PsiFileFactoryImpl

    private fun createDiagnosticHolder() = ConsoleDiagnosticMessageHolder()

    override fun check(state: IReplStageState<*>, codeLine: ReplCodeLine): ReplCheckResult {
        state.lock.write {
            konst checkerState = state.asState(GenericReplCheckerState::class.java)
            konst scriptFileName = makeScriptBaseName(codeLine)
            konst virtualFile =
                LightVirtualFile(
                    "$scriptFileName${KotlinParserDefinition.STD_SCRIPT_EXT}",
                    KotlinLanguage.INSTANCE,
                    StringUtil.convertLineSeparators(codeLine.code)
                ).apply {
                    charset = StandardCharsets.UTF_8
                }
            konst psiFile: KtFile = psiFileFactory.trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false) as KtFile?
                ?: error("Script file not analyzed at line ${codeLine.no}: ${codeLine.code}")

            konst errorHolder = createDiagnosticHolder()

            konst syntaxErrorReport = AnalyzerWithCompilerReport.reportSyntaxErrors(psiFile, errorHolder)

            if (!syntaxErrorReport.isHasErrors) {
                checkerState.lastLineState =
                    GenericReplCheckerState.LineState(codeLine, psiFile, errorHolder)
            }

            return when {
                syntaxErrorReport.isHasErrors && syntaxErrorReport.isAllErrorsAtEof -> ReplCheckResult.Incomplete()
                syntaxErrorReport.isHasErrors -> ReplCheckResult.Error(errorHolder.renderMessage())
                else -> ReplCheckResult.Ok()
            }
        }
    }
}
