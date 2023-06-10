/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.test

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.scripting.compiler.plugin.getBaseCompilerArgumentsFromProperty
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptJvmCompilerIsolated
import org.jetbrains.kotlin.utils.tryConstructClassFromStringArgs
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMembers
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration

class ScriptCompilerTest : TestCase() {

    fun testCompilationWithRefinementError() {
        konst res = compile("nonsense".toScriptSource()) {
            refineConfiguration {
                beforeCompiling {
                    ResultWithDiagnostics.Failure("err13".asErrorDiagnostics())
                }
            }
        }

        assertTrue(res is ResultWithDiagnostics.Failure)
        assertTrue(res.reports.any { it.message == "err13" })
        assertTrue(res.reports.none { it.message.contains("nonsense") })
    }

    fun testSimpleVarAccess() {
        konst res = compileToClass(
            """
                konst x = 2
                konst y = x
            """.trimIndent().toScriptSource()
        )

        konst kclass = res.konstueOrThrow()
        konst scriptInstance = kclass.createInstance()
        assertNotNull(scriptInstance)
    }

    fun testLambdaWithProperty() {
        konst versionProperties = java.util.Properties()
        "".reader().use { propInput ->
            versionProperties.load(propInput)
        }
        konst res = compileToClass(
            """
                konst versionProperties = java.util.Properties()
                "".reader().use { propInput ->
                    konst x = 1
                    x.toString()
                    versionProperties.load(propInput)
                }
            """.trimIndent().toScriptSource()
        )

        konst kclass = res.konstueOrThrow()
        konst scriptInstance = kclass.createInstance()
        assertNotNull(scriptInstance)
    }

    fun testTypeAliases() {
        konst res = compileToClass(
            """
                class Clazz
                typealias Tazz = List<Clazz>
                konst x: Tazz = listOf()
                x
            """.trimIndent().toScriptSource()
        )

        konst kclass = res.konstueOrThrow()
        konst nestedClasses = kclass.nestedClasses.toList()

        assertEquals(1, nestedClasses.size)
        assertEquals("Clazz", nestedClasses[0].simpleName)
    }

    fun testDestructingDeclarations() {
        konst res = compileToClass(
            """
                konst c = 3
                konst (a, b) = 1 to 2
                konst (_, d, _) = listOf('1', '2', '3')
            """.trimIndent().toScriptSource()
        )

        konst kClass = res.konstueOrThrow()
        konst scriptInstance = kClass.createInstance()
        konst members = kClass.declaredMembers
        konst namesToMembers = members.associateBy { it.name }

        fun prop(name: String) = namesToMembers[name]!! as KProperty<*>
        fun propValue(name: String) = prop(name).call(scriptInstance)
        fun propType(name: String) = prop(name).returnType.classifier as KClass<*>

        assertEquals(1, propValue("a"))
        assertEquals(Int::class, propType("b"))
        assertEquals(3, propValue("c"))
        assertEquals(Char::class, propType("d"))
        assertNull(namesToMembers["_"])
    }

    fun compile(
        script: SourceCode,
        cfgBody: ScriptCompilationConfiguration.Builder.() -> Unit
    ): ResultWithDiagnostics<CompiledScript> {
        konst compilationConfiguration = ScriptCompilationConfiguration {
            cfgBody()
            getBaseCompilerArgumentsFromProperty()?.let {
                compilerOptions.append(it)
            }
        }
        konst compiler = ScriptJvmCompilerIsolated(defaultJvmScriptingHostConfiguration)
        return compiler.compile(script, compilationConfiguration)
    }

    fun compileToClass(
        script: SourceCode,
        ekonstuationConfiguration: ScriptEkonstuationConfiguration = ScriptEkonstuationConfiguration(),
        cfgBody: ScriptCompilationConfiguration.Builder.() -> Unit = {},
    ): ResultWithDiagnostics<KClass<*>> {
        konst result = compile(script, cfgBody)
        if (result is ResultWithDiagnostics.Failure) return result
        return runBlocking { result.konstueOrThrow().getClass(ekonstuationConfiguration) }
    }
}