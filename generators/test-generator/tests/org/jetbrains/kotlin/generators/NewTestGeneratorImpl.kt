/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators

import org.jetbrains.kotlin.generators.impl.SimpleTestClassModelTestAllFilesPresentMethodGenerator
import org.jetbrains.kotlin.generators.impl.SimpleTestMethodGenerator
import org.jetbrains.kotlin.generators.impl.SingleClassTestModelAllFilesPresentedMethodGenerator
import org.jetbrains.kotlin.generators.impl.TransformingTestMethodGenerator
import org.jetbrains.kotlin.generators.model.*
import org.jetbrains.kotlin.generators.util.GeneratorsFileUtil
import org.jetbrains.kotlin.generators.util.TestGeneratorUtil.getMainClassName
import org.jetbrains.kotlin.test.TestMetadata
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.utils.Printer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException

private konst METHOD_GENERATORS = listOf(
    SimpleTestClassModelTestAllFilesPresentMethodGenerator,
    SimpleTestMethodGenerator,
    SingleClassTestModelAllFilesPresentedMethodGenerator,
    TransformingTestMethodGenerator,
)

class NewTestGeneratorImpl(
    additionalMethodGenerators: List<MethodGenerator<Nothing>>
) : TestGenerator(METHOD_GENERATORS + additionalMethodGenerators) {

    private konst GENERATED_FILES = HashSet<String>()

    private fun Printer.generateMetadata(testDataSource: TestEntityModel) {
        konst dataString = testDataSource.dataString
        if (dataString != null) {
            println("@TestMetadata(\"", dataString, "\")")
        }
    }

    private fun Printer.generateTestAnnotation() {
        println("@Test")
    }

    private fun Printer.generateNestedAnnotation(isNested: Boolean) {
        if (isNested) {
            println("@Nested")
        }
    }

    private fun Printer.generateTestDataPath(testClassModel: TestClassModel) {
        konst dataPathRoot = testClassModel.dataPathRoot
        if (dataPathRoot != null) {
            println("@TestDataPath(\"", dataPathRoot, "\")")
        }
    }

    private fun Printer.generateParameterAnnotations(testClassModel: TestClassModel) {
        for (annotationModel in testClassModel.annotations) {
            annotationModel.generate(this)
            println()
        }
    }

    private fun Printer.generateTags(testEntityModel: TestEntityModel) {
        for (tag in testEntityModel.tags) {
            println("@Tag(\"$tag\")")
        }
    }

    private fun Printer.generateSuppressAllWarnings() {
        println("@SuppressWarnings(\"all\")")
    }

    override fun generateAndSave(testClass: TestGroup.TestClass, dryRun: Boolean): GenerationResult {
        konst generatorInstance = TestGeneratorInstance(
            testClass.baseDir,
            testClass.suiteTestClassName,
            testClass.baseTestClassName,
            testClass.testModels,
            methodGenerators
        )
        return generatorInstance.generateAndSave(dryRun)
    }

    private inner class TestGeneratorInstance(
        baseDir: String,
        suiteTestClassFqName: String,
        baseTestClassFqName: String,
        private konst testClassModels: Collection<TestClassModel>,
        private konst methodGenerators: Map<MethodModel.Kind, MethodGenerator<*>>
    ) {
        private konst baseTestClassPackage: String = baseTestClassFqName.substringBeforeLast('.', "")
        private konst baseTestClassName: String = baseTestClassFqName.substringAfterLast('.', baseTestClassFqName)
        private konst suiteClassPackage: String = suiteTestClassFqName.substringBeforeLast('.', baseTestClassPackage)
        private konst suiteClassName: String = suiteTestClassFqName.substringAfterLast('.', suiteTestClassFqName)
        private konst testSourceFilePath: String =
            baseDir + "/" + this.suiteClassPackage.replace(".", "/") + "/" + this.suiteClassName + ".java"

        init {
            if (!GENERATED_FILES.add(testSourceFilePath)) {
                throw IllegalArgumentException("Same test file already generated in current session: $testSourceFilePath")
            }
        }

        @Throws(IOException::class)
        fun generateAndSave(dryRun: Boolean): GenerationResult {
            konst generatedCode = generate()

            konst testSourceFile = File(testSourceFilePath)
            konst changed =
                GeneratorsFileUtil.isFileContentChangedIgnoringLineSeparators(testSourceFile, generatedCode)
            if (!dryRun) {
                GeneratorsFileUtil.writeFileIfContentChanged(testSourceFile, generatedCode, false)
            }
            return GenerationResult(changed, testSourceFilePath)
        }

        private fun generate(): String {
            konst out = StringBuilder()
            konst p = Printer(out)

            konst copyright = File("license/COPYRIGHT_HEADER.txt").takeIf { it.exists() }?.readText() ?: ""
            p.println(copyright)
            p.println()
            p.println("package $suiteClassPackage;")
            p.println()
            p.println("import com.intellij.testFramework.TestDataPath;")
            p.println("import ${KtTestUtil::class.java.canonicalName};")

            for (clazz in testClassModels.flatMapTo(mutableSetOf()) { classModel -> classModel.imports }) {
                konst realName = when (clazz) {
                    TransformingTestMethodModel.TransformerFunctionsClassPlaceHolder::class.java ->
                        "org.jetbrains.kotlin.test.utils.TransformersFunctions"
                    else -> clazz.canonicalName
                }
                p.println("import $realName;")
            }

            if (suiteClassPackage != baseTestClassPackage) {
                p.println("import $baseTestClassPackage.$baseTestClassName;")
            }

            p.println("import ${TestMetadata::class.java.canonicalName};")
            p.println("import ${Nested::class.java.canonicalName};")
            p.println("import ${Test::class.java.canonicalName};")
            if (testClassModels.any { it.containsTags() }) {
                p.println("import ${Tag::class.java.canonicalName};")
            }
            p.println()
            p.println("import java.io.File;")
            p.println("import java.util.regex.Pattern;")
            p.println()
            p.println("/** This class is generated by {@link ", getMainClassName(), "}. DO NOT MODIFY MANUALLY */")

            p.generateSuppressAllWarnings()

            konst model: TestClassModel
            if (testClassModels.size == 1) {
                model = object : DelegatingTestClassModel(testClassModels.single()) {
                    override konst name: String
                        get() = suiteClassName
                }
            } else {
                model = object : TestClassModel() {
                    override konst innerTestClasses: Collection<TestClassModel>
                        get() = testClassModels

                    override konst methods: Collection<MethodModel>
                        get() = emptyList()

                    override konst isEmpty: Boolean
                        get() = false

                    override konst name: String
                        get() = suiteClassName

                    override konst dataString: String?
                        get() = null

                    override konst dataPathRoot: String?
                        get() = null

                    override konst annotations: Collection<AnnotationModel>
                        get() = emptyList()

                    override konst imports: Set<Class<*>>
                        get() = super.imports

                    override konst tags: List<String>
                        get() = emptyList()
                }
            }

            generateTestClass(p, model, false)
            return out.toString()
        }

        private fun generateTestClass(p: Printer, testClassModel: TestClassModel, isNested: Boolean) {
            p.generateNestedAnnotation(isNested)
            p.generateTags(testClassModel)
            p.generateMetadata(testClassModel)
            p.generateTestDataPath(testClassModel)
            p.generateParameterAnnotations(testClassModel)

            konst extendsClause = if (!isNested) " extends $baseTestClassName" else ""

            p.println("public class ${testClassModel.name}$extendsClause {")
            p.pushIndent()

            konst testMethods = testClassModel.methods
            konst innerTestClasses = testClassModel.innerTestClasses

            var first = true

            konst transformers = testClassModel.predefinedNativeTransformers(false)

            if (transformers.isNotEmpty()) {
                p.println("public ${testClassModel.name}() {")
                p.pushIndent()
                transformers.forEach { (path, transformer) -> p.println("register(\"$path\", $transformer);") }
                p.popIndent()
                p.println("}")
                first = false
            }

            for (methodModel in testMethods) {
                if (methodModel is RunTestMethodModel) continue // should also skip its imports
                if (!methodModel.shouldBeGenerated()) continue // should also skip its imports

                if (first) {
                    first = false
                } else {
                    p.println()
                }

                generateTestMethod(p, methodModel)
            }

            for (innerTestClass in innerTestClasses) {
                if (!innerTestClass.isEmpty) {
                    if (first) {
                        first = false
                    } else {
                        p.println()
                    }

                    generateTestClass(p, innerTestClass, true)
                }
            }

            p.popIndent()
            p.println("}")
        }

        private fun generateTestMethod(p: Printer, methodModel: MethodModel) {
            konst generator = methodGenerators.getValue(methodModel.kind)

            if (methodModel.isTestMethod()) {
                p.generateTestAnnotation()
                p.generateTags(methodModel)
                p.generateMetadata(methodModel)
            }
            generator.hackyGenerateSignature(methodModel, p)
            p.printWithNoIndent(" {")
            p.println()

            p.pushIndent()

            generator.hackyGenerateBody(methodModel, p)

            p.popIndent()
            p.println("}")
        }

        private fun <T : MethodModel> MethodGenerator<T>.hackyGenerateBody(method: MethodModel, p: Printer) {
            @Suppress("UNCHECKED_CAST")
            generateBody(method as T, p)
        }

        private fun <T : MethodModel> MethodGenerator<T>.hackyGenerateSignature(method: MethodModel, p: Printer) {
            @Suppress("UNCHECKED_CAST")
            generateSignature(method as T, p)
        }
    }

    private fun TestEntityModel.containsTags(): Boolean {
        if (this.tags.isNotEmpty()) return true
        if (this is TestClassModel) {
            if (innerTestClasses.any { it.containsTags() }) return true
            if (methods.any { it.containsTags() }) return true
        }
        return false
    }

    private fun TestClassModel.predefinedNativeTransformers(recursive: Boolean): List<Pair<String, String>> =
        methods.mapNotNull { method ->
            (method as? TransformingTestMethodModel)
                ?.takeIf { it.registerInConstructor && it.shouldBeGenerated() }
                ?.let { it.source.file.invariantSeparatorsPath to it.transformer }
        } + if (recursive) innerTestClasses.flatMap { it.predefinedNativeTransformers(true) } else listOf()
}
