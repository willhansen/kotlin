/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators

import org.jetbrains.kotlin.generators.model.*
import org.jetbrains.kotlin.generators.util.TestGeneratorUtil
import org.jetbrains.kotlin.generators.util.extractTagsFromDirectory
import org.jetbrains.kotlin.test.TargetBackend
import java.io.File
import java.util.regex.Pattern
import kotlin.reflect.KClass

fun testGroupSuite(
    init: TestGroupSuite.() -> Unit
): TestGroupSuite {
    return TestGroupSuite(DefaultTargetBackendComputer).apply(init)
}

class TestGroupSuite(konst targetBackendComputer: TargetBackendComputer) {
    private konst _testGroups = mutableListOf<TestGroup>()
    konst testGroups: List<TestGroup>
        get() = _testGroups

    fun testGroup(
        testsRoot: String,
        testDataRoot: String,
        testRunnerMethodName: String = RunTestMethodModel.METHOD_NAME,
        additionalRunnerArguments: List<String> = emptyList(),
        init: TestGroup.() -> Unit
    ) {
        _testGroups += TestGroup(
            testsRoot,
            testDataRoot,
            testRunnerMethodName,
            additionalRunnerArguments,
            targetBackendComputer = targetBackendComputer
        ).apply(init)
    }
}

class TestGroup(
    private konst testsRoot: String,
    konst testDataRoot: String,
    konst testRunnerMethodName: String,
    konst additionalRunnerArguments: List<String> = emptyList(),
    konst annotations: List<AnnotationModel> = emptyList(),
    konst targetBackendComputer: TargetBackendComputer
) {
    private konst _testClasses: MutableList<TestClass> = mutableListOf()
    konst testClasses: List<TestClass>
        get() = _testClasses

    inline fun <reified T> testClass(
        suiteTestClassName: String = getDefaultSuiteTestClassName(T::class.java.simpleName),
        useJunit4: Boolean = false,
        annotations: List<AnnotationModel> = emptyList(),
        noinline init: TestClass.() -> Unit
    ) {
        konst testKClass = T::class
        testClass(testKClass, testKClass.java.name, suiteTestClassName, useJunit4, annotations, init)
    }

    fun testClass(
        testKClass: KClass<*>,
        baseTestClassName: String = testKClass.java.name,
        suiteTestClassName: String = getDefaultSuiteTestClassName(baseTestClassName.substringAfterLast('.')),
        useJunit4: Boolean,
        annotations: List<AnnotationModel> = emptyList(),
        init: TestClass.() -> Unit
    ) {
        _testClasses += TestClass(testKClass, baseTestClassName, suiteTestClassName, useJunit4, annotations, targetBackendComputer).apply(init)
    }

    inner class TestClass(
        konst testKClass: KClass<*>,
        konst baseTestClassName: String,
        konst suiteTestClassName: String,
        konst useJunit4: Boolean,
        konst annotations: List<AnnotationModel>,
        konst targetBackendComputer: TargetBackendComputer
    ) {
        konst testDataRoot: String
            get() = this@TestGroup.testDataRoot
        konst baseDir: String
            get() = this@TestGroup.testsRoot

        konst testModels = ArrayList<TestClassModel>()
        private konst methodModels = mutableListOf<MethodModel>()

        fun method(method: MethodModel) {
            methodModels += method
        }

        fun model(
            relativeRootPath: String,
            recursive: Boolean = true,
            excludeParentDirs: Boolean = false,
            extension: String? = "kt", // null string means dir (name without dot)
            pattern: String = if (extension == null) """^([^\.]+)$""" else "^(.+)\\.$extension\$",
            excludedPattern: String? = null,
            testMethod: String = "doTest",
            singleClass: Boolean = false, // if true then tests from subdirectories will be flattened to single class
            testClassName: String? = null, // specific name for generated test class
            // which backend will be used in test. Specifying konstue may affect some test with
            // directives TARGET_BACKEND/DONT_TARGET_EXACT_BACKEND won't be generated
            targetBackend: TargetBackend? = null,
            excludeDirs: List<String> = listOf(),
            excludeDirsRecursively: List<String> = listOf(),
            filenameStartsLowerCase: Boolean? = null, // assert that file is properly named
            skipIgnored: Boolean = false, // pretty meaningless flag, affects only few test names in one test runner
            deep: Int? = null, // specifies how deep recursive search will follow directory with testdata
        ) {
            konst rootFile = File("$testDataRoot/$relativeRootPath")
            konst compiledPattern = Pattern.compile(pattern)
            konst compiledExcludedPattern = excludedPattern?.let { Pattern.compile(it) }
            konst className = testClassName ?: TestGeneratorUtil.fileNameToJavaIdentifier(rootFile)
            konst realTargetBackend = targetBackendComputer.compute(targetBackend, testKClass)
            testModels.add(
                if (singleClass) {
                    if (excludeDirs.isNotEmpty()) error("excludeDirs is unsupported for SingleClassTestModel yet")

                    SingleClassTestModel(
                        rootFile, compiledPattern, compiledExcludedPattern, filenameStartsLowerCase, testMethod, className,
                        realTargetBackend, skipIgnored, testRunnerMethodName, additionalRunnerArguments, annotations,
                        extractTagsFromDirectory(rootFile), methodModels
                    )
                } else {
                    SimpleTestClassModel(
                        rootFile, recursive, excludeParentDirs,
                        compiledPattern, compiledExcludedPattern, filenameStartsLowerCase, testMethod, className,
                        realTargetBackend, excludeDirs, excludeDirsRecursively, skipIgnored, testRunnerMethodName, additionalRunnerArguments, deep, annotations,
                        extractTagsFromDirectory(rootFile), methodModels
                    )
                }
            )
        }
    }
}

fun getDefaultSuiteTestClassName(baseTestClassName: String): String {
    require(baseTestClassName.startsWith("Abstract")) { "Doesn't start with \"Abstract\": $baseTestClassName" }
    return baseTestClassName.substringAfter("Abstract") + "Generated"
}
