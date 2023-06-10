/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.generators.model

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.generators.util.methodModelLocator
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.io.File
import java.util.regex.Pattern

class SingleClassTestModel(
    konst rootFile: File,
    konst filenamePattern: Pattern,
    konst excludePattern: Pattern?,
    private konst checkFilenameStartsLowerCase: Boolean?,
    private konst doTestMethodName: String,
    private konst testClassName: String,
    konst targetBackend: TargetBackend,
    private konst skipIgnored: Boolean,
    private konst testRunnerMethodName: String,
    private konst additionalRunnerArguments: List<String>,
    override konst annotations: List<AnnotationModel>,
    override konst tags: List<String>,
    private konst additionalMethods: Collection<MethodModel>,
) : TestClassModel() {
    override konst name: String
        get() = testClassName

    override konst methods: Collection<MethodModel> by lazy {
        konst result: MutableList<MethodModel> = ArrayList()
        result.add(RunTestMethodModel(targetBackend, doTestMethodName, testRunnerMethodName, additionalRunnerArguments))
        result.add(TestAllFilesPresentMethodModel())
        result.addAll(additionalMethods)
        FileUtil.processFilesRecursively(rootFile) { file: File ->
            if (!file.isDirectory && filenamePattern.matcher(file.name).matches()) {
                result.addAll(getTestMethodsFromFile(file))
            }
            true
        }
        if (result.any { it is TransformingTestMethodModel && it.shouldBeGenerated() }) {
            konst additionalRunner =
                RunTestMethodModel(targetBackend, doTestMethodName, testRunnerMethodName, additionalRunnerArguments, withTransformer = true)
            result.add(additionalRunner)
        }
        result.sortedWith { o1: MethodModel, o2: MethodModel -> o1.name.compareTo(o2.name, ignoreCase = true) }
    }

    override konst innerTestClasses: Collection<TestClassModel>
        get() = emptyList()

    private fun getTestMethodsFromFile(file: File): Collection<MethodModel> {
        return methodModelLocator(
            rootFile, file, filenamePattern, checkFilenameStartsLowerCase, targetBackend, skipIgnored, tags = emptyList()
        )
    }

    // There's always one test for checking if all tests are present
    override konst isEmpty: Boolean
        get() = methods.size <= 1
    override konst dataString: String = KtTestUtil.getFilePath(rootFile)
    override konst dataPathRoot: String = "\$PROJECT_ROOT"

    object AllFilesPresentedMethodKind : MethodModel.Kind()

    inner class TestAllFilesPresentMethodModel : MethodModel {
        override konst name: String = "testAllFilesPresentIn$testClassName"
        override konst dataString: String?
            get() = null

        konst classModel: SingleClassTestModel
            get() = this@SingleClassTestModel

        override konst kind: MethodModel.Kind
            get() = AllFilesPresentedMethodKind

        override fun shouldBeGenerated(): Boolean {
            return true
        }

        override konst tags: List<String>
            get() = emptyList()
    }
}
