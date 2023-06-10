/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_services

import junit.framework.TestCase
import org.jetbrains.kotlin.scripting.ide_services.compiler.completion
import org.jetbrains.kotlin.scripting.ide_services.compiler.filterOutShadowedDescriptors
import org.jetbrains.kotlin.scripting.ide_services.compiler.nameFilter
import org.jetbrains.kotlin.scripting.ide_services.test_util.*
import org.jetbrains.kotlin.scripting.resolve.skipExtensionsResolutionForImplicits
import org.jetbrains.kotlin.scripting.resolve.skipExtensionsResolutionForImplicitsExceptInnermost
import org.junit.Ignore
import org.junit.Test
import java.io.Writer
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.classpathFromClass

typealias TestRunConfigurator = (TestConf.Run).() -> Unit

class ReplCompletionAndErrorsAnalysisTest : TestCase() {
    @Test
    fun testTrivial() = test {
        run {
            doCompile
            code = """
                data class AClass(konst memx: Int, konst memy: String)
                data class BClass(konst memz: String, konst mema: AClass)
                konst foobar = 42
                var foobaz = "string"
                konst v = BClass("KKK", AClass(5, "25"))
            """.trimIndent()
        }

        run {
            code = "foo"
            cursor = 3
            expect {
                addCompletion("foobar", "foobar", "Int", "property")
                addCompletion("foobaz", "foobaz", "String", "property")
            }
        }

        run {
            code = "v.mema."
            cursor = 7
            expect {
                completions.mode = ComparisonType.INCLUDES
                addCompletion("memx", "memx", "Int", "property")
                addCompletion("memy", "memy", "String", "property")
            }
        }

        run {
            code = "listO"
            cursor = 5
            expect {
                addCompletion("listOf(", "listOf(T)", "List<T>", "method")
                addCompletion("listOf()", "listOf()", "List<T>", "method")
                addCompletion("listOf(", "listOf(vararg T)", "List<T>", "method")
                addCompletion("listOfNotNull(", "listOfNotNull(T?)", "List<T>", "method")
                addCompletion("listOfNotNull(", "listOfNotNull(vararg T?)", "List<T>", "method")
            }
        }
    }

    @Test
    fun testNoVariantsAfterFinalExpressions() = test {
        fun testNoVariants(testCode: String, testCursor: Int? = null) = run {
            doComplete
            code = testCode
            cursor = testCursor ?: testCode.length
            expect {
                completions.mode = ComparisonType.EQUALS
            }
        }

        testNoVariants("konst x1 = 42")
        testNoVariants("konst x2 = 42.42")
        testNoVariants("konst x3 = 'v'")
        testNoVariants("konst x4 = \"str42\"")

        testNoVariants("konst x5 = 40 + 41 * 42")
        testNoVariants("konst x6 = 40 + 41 * 42", 16) // after 41
        testNoVariants("konst x7 = 40 + 41 * 42", 11) // after 40
        testNoVariants("6 * (2 + 5)")

        testNoVariants("\"aBc\".capitalize()")
        testNoVariants(
            """
                "abc" + "def"
            """.trimIndent()
        )
    }

    @Test
    fun testPackagesImport() = test {
        run {
            cursor = 17
            code = "import java.lang."
            expect {
                completions.mode = ComparisonType.INCLUDES
                addCompletion("Process", "Process", " (java.lang)", "class")
            }
        }
    }

    @Test
    fun testFunctionArgumentNames() = test {
        run {
            doCompile
            code = """
                fun _sf(_someInt: Int = 42, _someString: String = "s") = 1
                fun String.f(_bar: Int) = _bar
                class C(konst _xyz: Int)
            """.trimIndent()
        }
        run {
            code = """_sf(_s"""
            cursor = code.length
            expect {
                addCompletion("_sf(", "_sf(Int = ..., String = ...)", "Int", "method")
                addCompletion("_someInt = ", "_someInt", "Int", "parameter")
                addCompletion("_someString = ", "_someString", "String", "parameter")
            }
        }
        run {
            code = """ "my string".f(_b"""
            cursor = code.length
            expect {
                addCompletion("_bar = ", "_bar", "Int", "parameter")
            }
        }
        run {
            code = "C(_x"
            cursor = code.length
            expect {
                addCompletion("_xyz = ", "_xyz", "Int", "parameter")
            }
        }
    }

    @Test
    fun testCompletionInsideFunctions() = test {
        run {
            konst statement = "konst a = _f"
            code = """
                fun dontCompleteMe(_foo: Int, bar: String) {
                    konst _foo2 = ""
                    $statement
                }
            """.trimIndent()
            cursor = code.indexOf(statement) + statement.length
            expect {
                addCompletion("_foo2", "_foo2", "String", "property")
                addCompletion("_foo", "_foo", "Int", "parameter")
            }
        }
    }

    @Test
    fun testDeprecatedCompletion() = test {
        run {
            doCompile
            code = """
                @Deprecated("deprecated")
                class Clazz1
                
                @Deprecated("deprecated", level=DeprecationLevel.ERROR)
                class Clazz2
            """.trimIndent()
        }
        run {
            doComplete
            code = """
                @Deprecated("deprecated1", level=DeprecationLevel.WA)
                class Clazz3
                
                @Deprecated("deprecated1", level=kotlin.annotation.AnnotationRetention.SOURCE)
                class Clazz4
                
                Claz
            """.trimIndent()
            cursor = code.length
            expect {
                addCompletion("Clazz3", "Clazz3", " (Line_2_simplescript)", "class", DeprecationLevel.WARNING)
                addCompletion("Clazz4", "Clazz4", " (Line_2_simplescript)", "class", DeprecationLevel.WARNING)
                addCompletion("Clazz1", "Clazz1", " (Line_1_simplescript)", "class", DeprecationLevel.WARNING)
                addCompletion("Clazz2", "Clazz2", " (Line_1_simplescript)", "class", DeprecationLevel.ERROR)
            }
        }
    }

    @Test
    fun testExtensionMethods() = test {
        run {
            doCompile
            code = """
                class AClass(konst c_prop_x: Int) {
                    fun filter(xxx: (AClass).() -> Boolean): AClass {
                        return this
                    }
                }
                konst AClass.c_prop_y: Int
                    get() = c_prop_x * c_prop_x
                
                fun AClass.c_meth_z(v: Int) = v * c_prop_y
                konst df = AClass(10)
                konst c_zzz = "some string"
            """.trimIndent()
        }

        run {
            code = "df.filter{ c_ }"
            cursor = 13
            expect {
                addCompletion("c_prop_x", "c_prop_x", "Int", "property")
                addCompletion("c_zzz", "c_zzz", "String", "property")
                addCompletion("c_prop_y", "c_prop_y", "Int", "property")
                addCompletion("c_meth_z(", "c_meth_z(Int)", "Int", "method")
            }
        }

        run {
            code = "df.fil"
            cursor = 6
            expect {
                completions.check { actual ->
                    assertEquals(
                        SourceCodeCompletionVariant(
                            "filter { ", "filter(Line_1_simplescript.AClass.() -> ...", "Line_1_simplescript.AClass", "method"
                        ), actual.single { it.tail != "keyword" }
                    )
                }
            }
        }
    }

    @Test
    fun testBacktickedFields() = test {
        run {
            doCompile
            code = """
                class AClass(konst `c_prop   x y z`: Int)
                konst df = AClass(33)
            """.trimIndent()
        }

        run {
            code = "df.c_"
            cursor = 5
            expect {
                addCompletion("`c_prop   x y z`", "`c_prop   x y z`", "Int", "property")
            }
        }
    }

    @Test
    fun testListErrors() = test {
        run {
            doCompile
            code = """
                data class AClass(konst memx: Int, konst memy: String)
                data class BClass(konst memz: String, konst mema: AClass)
                konst foobar = 42
                var foobaz = "string"
                konst v = BClass("KKK", AClass(5, "25"))
            """.trimIndent()
            expect {
                errors.mode = ComparisonType.EQUALS
            }
        }

        run {
            code = """
                konst a = AClass("42", 3.14)
                konst b: Int = "str"
                konst c = foob
            """.trimIndent()
            expect {
                addError(1, 16, 1, 20, "Type mismatch: inferred type is String but Int was expected", "ERROR")
                addError(1, 22, 1, 26, "The floating-point literal does not conform to the expected type String", "ERROR")
                addError(2, 14, 2, 19, "Type mismatch: inferred type is String but Int was expected", "ERROR")
                addError(3, 9, 3, 13, "Unresolved reference: foob", "ERROR")
            }
        }
    }

    @Test
    fun testIncompleteCode() = test {
        run {
            doErrorCheck

            code = "fun g(): Int { return 1"

            expect {
                addError(1, 24, 1, 24, "Expecting '}'", "ERROR")
                errors.add(ScriptDiagnostic(ScriptDiagnostic.incompleteCode, "Incomplete code"))
            }
        }
    }

    @Test
    fun testCompletionDuplication() = test {
        for (i in 1..6) {
            run {
                if (i == 5) doCompile
                if (i % 2 == 1) doErrorCheck

                konst konstue = "a".repeat(i)
                code = "konst ddddd = \"$konstue\""
                cursor = 13 + i
            }
        }

        run {
            code = "dd"
            cursor = 2
            expect {
                addCompletion("ddddd", "ddddd", "String", "property")
            }
        }
    }

    @Test
    fun testAnalyze() = test {
        run {
            code = """
                konst foo = 42
                foo
            """.trimIndent()
            expect {
                errors.mode = ComparisonType.EQUALS
                resultType = "Int"
            }
        }
    }

    @Test
    fun testNameFilter() = test {
        run {
            code = """
                konst xxxyyy = 42
                konst yyyxxx = 43
            """.trimIndent()
            doCompile
        }

        run {
            code = "xxx"
            cursor = 3
            expect {
                addCompletion("xxxyyy", "xxxyyy", "Int", "property")
            }
        }

        run {
            code = "xxx"
            cursor = 3
            compilationConfiguration = ScriptCompilationConfiguration {
                completion {
                    nameFilter { name, namePart -> name.endsWith(namePart) }
                }
            }
            expect {
                addCompletion("yyyxxx", "yyyxxx", "Int", "property")
            }
        }
    }

    @Test
    fun testShadowedDescriptors() = test {
        for (i in 1..2)
            run {
                code = """
                    konst xxxyyy = 42
                """.trimIndent()
                doCompile
            }

        for ((flag, expectedSize) in listOf(true to 1, false to 2))
            run {
                code = "xxx"
                cursor = 3
                compilationConfiguration = ScriptCompilationConfiguration {
                    completion {
                        filterOutShadowedDescriptors(flag)
                    }
                }
                expect {
                    completions.size = expectedSize
                }
            }
    }

    @Test
    fun testImplicitExtensions() = test {
        run {
            code = """
                class A {
                    fun String.foooo() = 42
                }
            """.trimIndent()
            doCompile
        }
        run {
            code = """
                with(A()) {
                    with("bar") {
                        foo
                    }
                }
            """.trimIndent()
            cursor = code.indexOf("foo") + 3
            expect {
                completions.mode = ComparisonType.EQUALS
                addCompletion("foooo()", "foooo()", "Int", "method")
            }
        }
    }

    @Test
    fun testDefaultImports() = test {
        run(setupDefaultImportsCompletionRun)
    }

    @Test
    fun testImportCompletion() = test {
        run {
            code = """
                import kotl
            """.trimIndent()
            cursor = 11
            expect {
                completions.mode = ComparisonType.INCLUDES
                addCompletion("kotlin", "kotlin", "package kotlin", "package")
            }
        }
    }

    @Test
    fun testProtectedInheritedMemberCompletion() = test {
        run {
            code = """
                open class Base {
                    private konst xyz1: Float = 7.0f
                    protected konst xyz2: Int = 42
                    internal konst xyz3: String = ""
                    public konst xyz4: Byte = 8
                }
            """.trimIndent()
            doCompile
        }

        run {
            konst definition = "konst c = x"
            code = """
                object : Base() {
                    fun g() {
                        $definition
                    }
                }
            """.trimIndent()
            cursor = code.indexOf(definition) + definition.length

            expect {
                addCompletion("xyz2", "xyz2", "Int", "property")
                addCompletion("xyz3", "xyz3", "String", "property")
                addCompletion("xyz4", "xyz4", "Byte", "property")
            }
        }
    }

    @Ignore("Should be fixed by KT-39314")
    @Test
    fun ignore_testDefaultImportsNotFirst() = test {
        run {
            code = "1"
            doCompile
        }
        run(setupDefaultImportsCompletionRun)
    }

    @Test
    fun testLongCompilationsWithImport() = test {
        // This test normally completes in about 5-10s
        // Log should show slow _linear_ compilation time growth

        konst compileWriter = System.out.writer()

        for (i in 1..120) {
            run {
                code = """
                    import kotlin.math.*
                    konst dataFrame = mapOf("x" to sin(3.0))
                    konst e = "str"
                """.trimIndent()
                doCompile

                loggingInfo = CSVLoggingInfo(compile = CSVLoggingInfoItem(compileWriter, i, "compile;"))
            }
        }
    }

    @Test
    fun testLongRunningCompilationWithReceiver() = test {
        // This test normally completes in about 8-13s
        // Removing skip* configuration parameters should slow down the test (2-3 times)

        konst conf = ScriptCompilationConfiguration {
            jvm {
                updateClasspath(classpathFromClass<TestReceiver1>())
            }
            implicitReceivers(TestReceiver1::class, TestReceiver2::class)
            skipExtensionsResolutionForImplicits(KotlinType(TestReceiver1::class))
            skipExtensionsResolutionForImplicitsExceptInnermost(KotlinType(TestReceiver2::class))
        }

        konst writer = System.out.writer()
        for (i in 1..200) {
            run(longCompilationRun(writer, i, conf))
            run {
                compilationConfiguration = conf

                code = """
                    konst x = xyz
                """.trimIndent()
                cursor = 11

                expect {
                    completions.mode = ComparisonType.EQUALS
                    addCompletion("xyz1", "xyz1", "Int", "property")
                    addCompletion("xyz2", "xyz2", "Int", "property")
                }

                loggingInfo = CSVLoggingInfo(complete = CSVLoggingInfoItem(writer, i, "complete;"))
            }
        }
    }

    private konst setupDefaultImportsCompletionRun: TestRunConfigurator = {
        compilationConfiguration = ScriptCompilationConfiguration {
            defaultImports(listOf("kotlin.math.atan"))
        }

        code = "ata"
        cursor = 3

        expect {
            completions.mode = ComparisonType.INCLUDES
            addCompletion("atan(", "atan(Double)", "Double", "method")
        }
    }
}

// Artificial split into several testsuites, to speed up parallel testing
class IdeServicesLongRunningTest1 : TestCase() {
    @Test
    fun testLongRunningCompletion() = test {
        // This test normally completes in about 15-25s
        // Log should show slow _linear_ compilation/completion time growth

        konst compileWriter = System.out.writer() // FileWriter("$csvDir/compilations.csv")
        konst completeWriter = System.out.writer() // FileWriter("$csvDir/completions.csv")

        for (i in 1..230) {
            run(longCompilationRun(compileWriter, i))
            run(longCompletionRun(completeWriter, i))
        }
    }
}

// Artificial split into several testsuites, to speed up parallel testing
class IdeServicesLongRunningTest2 : TestCase() {
    @Test
    fun testLongRunningCompilation() = test {
        // This test normally completes in about 10-20s

        konst writer = System.out.writer()
        for (i in 1..500) {
            run(longCompilationRun(writer, i))
        }
    }
}

@Suppress("unused")
class TestReceiver1(konst xyz1: Int = 42)

@Suppress("unused")
class TestReceiver2(konst xyz2: Int = 42)

private fun longCompilationRun(writer: Writer, i: Int, conf: ScriptCompilationConfiguration? = null): TestRunConfigurator {
    return {
        conf?.let {
            compilationConfiguration = it
        }

        code = """
            konst dataFrame = mapOf("x" to 45)
            konst e = "str"
        """.trimIndent()
        doCompile

        loggingInfo = CSVLoggingInfo(compile = CSVLoggingInfoItem(writer, i, "compile;"))
    }
}

private fun longCompletionRun(writer: Writer, i: Int, conf: ScriptCompilationConfiguration? = null): TestRunConfigurator {
    return {
        conf?.let {
            compilationConfiguration = it
        }

        code = """
            konst x = mapOf("a" to dataFrame., "b" to 12, e to 42)
        """.trimIndent()
        cursor = 31

        doComplete
        expect {
            completions.mode = ComparisonType.INCLUDES
            addCompletion("entries", "entries", "Set<Map.Entry<String, Int>>", "property")
        }

        loggingInfo = CSVLoggingInfo(complete = CSVLoggingInfoItem(writer, i, "complete;"))
    }
}

