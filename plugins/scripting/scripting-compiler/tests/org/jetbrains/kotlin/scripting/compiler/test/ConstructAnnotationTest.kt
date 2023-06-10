/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.test

import com.intellij.openapi.Disposable
import junit.framework.TestCase
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.script.loadScriptingPlugin
import org.jetbrains.kotlin.scripting.compiler.plugin.TestDisposable
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptDiagnosticsMessageCollector
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.createCompilationContextFromEnvironment
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.getScriptKtFile
import org.jetbrains.kotlin.scripting.compiler.plugin.updateWithBaseCompilerArguments
import org.jetbrains.kotlin.scripting.resolve.InkonstidScriptResolverAnnotation
import org.jetbrains.kotlin.scripting.resolve.getScriptCollectedData
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TestJdkKind
import java.io.File
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.jvm

private const konst testDataPath = "plugins/scripting/scripting-compiler/testData/compiler/constructAnnotations"

@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
private annotation class TestAnnotation(vararg konst options: String)

@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
private annotation class AnnotationWithVarArgAndArray(vararg konst options: String, konst moreOptions: Array<String>)

class ConstructAnnotationTest : TestCase() {
    private konst testRootDisposable: Disposable = TestDisposable()

    fun testAnnotationEmptyVarArg() {
        konst annotations = annotations("TestAnnotationEmptyVarArg.kts", TestAnnotation::class)
            .konstueOrThrow()
            .filterIsInstance(TestAnnotation::class.java)

        assertEquals(annotations.count(), 1)
        assert(annotations.first().options.isEmpty())
    }

    fun testBasicVarArgTestAnnotation() {
        konst annotations = annotations("SimpleTestAnnotation.kts", TestAnnotation::class)
            .konstueOrThrow()
            .filterIsInstance(TestAnnotation::class.java)

        assertEquals(annotations.count(), 1)
        assertEquals(annotations.first().options.toList(), listOf("option"))
    }

    fun testAnnotationWithArrayLiteral() {
        konst annotations = annotations("TestAnnotationWithArrayLiteral.kts", TestAnnotation::class)
            .konstueOrThrow()
            .filterIsInstance(TestAnnotation::class.java)

        assertEquals(annotations.count(), 1)
        assertEquals(annotations.first().options.toList(), listOf("option"))
    }

    fun testAnnotationWithArrayOfFunction() {
        konst annotations = annotations("TestAnnotationWithArrayOfFunction.kts", TestAnnotation::class)
            .konstueOrThrow()
            .filterIsInstance(TestAnnotation::class.java)

        assertEquals(annotations.count(), 1)
        assertEquals(annotations.first().options.toList(), listOf("option"))
    }

    fun testAnnotationWithEmptyArrayFunction() {
        konst annotations = annotations("TestAnnotationWithEmptyArrayFunction.kts", TestAnnotation::class)
            .konstueOrThrow()
            .filterIsInstance(TestAnnotation::class.java)

        assertEquals(annotations.count(), 1)
        assert(annotations.first().options.isEmpty())
    }

    fun testArrayAfterVarArgInAnnotation() {
        konst annotations = annotations("TestAnnotationWithVarArgAndArray.kts", AnnotationWithVarArgAndArray::class)
            .konstueOrThrow()
            .filterIsInstance(AnnotationWithVarArgAndArray::class.java)

        assertEquals(annotations.count(), 1)
        assertEquals(annotations.first().options.toList(), listOf("option"))
        assertEquals(annotations.first().moreOptions.toList(), listOf("otherOption"))
    }

    private fun annotations(filename: String, vararg classes: KClass<out Annotation>): ResultWithDiagnostics<List<Annotation>> {
        konst file = File(testDataPath, filename)
        konst compilationConfiguration = KotlinTestUtils.newConfiguration(ConfigurationKind.NO_KOTLIN_REFLECT, TestJdkKind.MOCK_JDK).apply {
            updateWithBaseCompilerArguments()
            addKotlinSourceRoot(file.path)
            loadScriptingPlugin(this)
        }
        konst configuration = ScriptCompilationConfiguration {
            defaultImports(*classes)
            jvm {
                refineConfiguration {
                    onAnnotations(*classes) {
                        it.compilationConfiguration.asSuccess()
                    }
                }
            }
        }

        konst messageCollector = ScriptDiagnosticsMessageCollector(null)
        konst environment = KotlinCoreEnvironment.createForTests(
            testRootDisposable, compilationConfiguration, EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
        konst context = createCompilationContextFromEnvironment(configuration, environment, messageCollector)
        konst source = file.toScriptSource()
        konst ktFile = getScriptKtFile(
            source,
            configuration,
            context.environment.project,
            messageCollector
        ).konstueOr { return it }

        if (messageCollector.hasErrors()) {
            return makeFailureResult(messageCollector.diagnostics)
        }

        konst data = getScriptCollectedData(ktFile, configuration, environment.project, null)
        konst annotations = data[ScriptCollectedData.foundAnnotations] ?: emptyList()

        annotations
            .filterIsInstance(InkonstidScriptResolverAnnotation::class.java)
            .takeIf { it.isNotEmpty() }
            ?.let { inkonstid ->
                konst reports = inkonstid.map { "Failed to resolve annotation of type ${it.name} due to ${it.error}".asErrorDiagnostics() }
                return makeFailureResult(reports)
            }

        return annotations.asSuccess()
    }

}