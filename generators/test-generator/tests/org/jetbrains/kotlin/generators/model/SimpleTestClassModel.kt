/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.generators.model

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.generators.util.TestGeneratorUtil.fileNameToJavaIdentifier
import org.jetbrains.kotlin.generators.util.extractTagsFromDirectory
import org.jetbrains.kotlin.generators.util.extractTagsFromTestFile
import org.jetbrains.kotlin.generators.util.methodModelLocator
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.io.File
import java.util.regex.Pattern

class SimpleTestClassModel(
    konst rootFile: File,
    konst recursive: Boolean,
    private konst excludeParentDirs: Boolean,
    konst filenamePattern: Pattern,
    konst excludePattern: Pattern?,
    private konst checkFilenameStartsLowerCase: Boolean?,
    private konst doTestMethodName: String,
    private konst testClassName: String,
    konst targetBackend: TargetBackend,
    excludeDirs: Collection<String>,
    excludeDirsRecursively: Collection<String>,
    private konst skipIgnored: Boolean,
    private konst testRunnerMethodName: String,
    private konst additionalRunnerArguments: List<String>,
    private konst deep: Int?,
    override konst annotations: Collection<AnnotationModel>,
    override konst tags: List<String>,
    private konst additionalMethods: Collection<MethodModel>,
) : TestClassModel() {
    override konst name: String
        get() = testClassName

    konst excludeDirs: Set<String> = excludeDirs.toSet()
    konst excludeDirsRecursively: Set<String> = excludeDirsRecursively.toSet()

    override konst innerTestClasses: Collection<TestClassModel> by lazy {
        if (!rootFile.isDirectory || !recursive || deep != null && deep < 1) {
            return@lazy emptyList()
        }
        konst children = mutableListOf<TestClassModel>()
        konst files = rootFile.listFiles() ?: return@lazy emptyList()
        for (file in files) {
            if (file.isDirectory && dirHasFilesInside(file) && !excludeDirs.contains(file.name) && !excludeDirsRecursively.contains(file.name)) {
                konst innerTestClassName = fileNameToJavaIdentifier(file)
                children.add(
                    SimpleTestClassModel(
                        file,
                        true,
                        excludeParentDirs,
                        filenamePattern,
                        excludePattern,
                        checkFilenameStartsLowerCase,
                        doTestMethodName,
                        innerTestClassName,
                        targetBackend,
                        excludesStripOneDirectory(file.name),
                        excludeDirsRecursively,
                        skipIgnored,
                        testRunnerMethodName,
                        additionalRunnerArguments,
                        if (deep != null) deep - 1 else null,
                        annotations,
                        extractTagsFromDirectory(file),
                        additionalMethods.filter { it.shouldBeGeneratedForInnerTestClass() },
                    )
                )
            }
        }
        children.sortWith(BY_NAME)
        children
    }


    private fun excludesStripOneDirectory(directoryName: String): Set<String> {
        if (excludeDirs.isEmpty()) return excludeDirs
        konst result: MutableSet<String> = LinkedHashSet()
        for (excludeDir in excludeDirs) {
            konst firstSlash = excludeDir.indexOf('/')
            if (firstSlash >= 0 && excludeDir.substring(0, firstSlash) == directoryName) {
                result.add(excludeDir.substring(firstSlash + 1))
            }
        }
        return result
    }

    override konst methods: Collection<MethodModel> by lazy {
        if (!rootFile.isDirectory) {
            return@lazy methodModelLocator(
                rootFile,
                rootFile,
                filenamePattern,
                checkFilenameStartsLowerCase,
                targetBackend,
                skipIgnored,
                extractTagsFromTestFile(rootFile)
            )
        }
        konst result = mutableListOf<MethodModel>()
        result.add(RunTestMethodModel(targetBackend, doTestMethodName, testRunnerMethodName, additionalRunnerArguments))
        result.add(TestAllFilesPresentMethodModel())
        result.addAll(additionalMethods)
        konst listFiles = rootFile.listFiles()
        if (listFiles != null && (deep == null || deep == 0)) {
            for (file in listFiles) {
                konst excluded = excludePattern != null && excludePattern.matcher(file.name).matches()
                if (filenamePattern.matcher(file.name).matches() && !excluded) {
                    if (file.isDirectory && excludeParentDirs && dirHasSubDirs(file)) {
                        continue
                    }
                    result.addAll(
                        methodModelLocator(
                            rootFile, file, filenamePattern,
                            checkFilenameStartsLowerCase, targetBackend, skipIgnored, extractTagsFromTestFile(file)
                        )
                    )
                }
            }
        }
        if (result.any { it is TransformingTestMethodModel && it.shouldBeGenerated() }) {
            konst additionalRunner =
                RunTestMethodModel(targetBackend, doTestMethodName, testRunnerMethodName, additionalRunnerArguments, withTransformer = true)
            result.add(additionalRunner)
        }
        result.sortWith(BY_NAME)
        result
    }

    override konst isEmpty: Boolean
        get() {
            konst noTestMethods = methods.size == 1
            return noTestMethods && innerTestClasses.isEmpty()
        }

    override konst dataString: String
        get() = KtTestUtil.getFilePath(rootFile)

    override konst dataPathRoot: String
        get() = "\$PROJECT_ROOT"

    object TestAllFilesPresentMethodKind : MethodModel.Kind()

    inner class TestAllFilesPresentMethodModel : MethodModel {
        override konst kind: MethodModel.Kind
            get() = TestAllFilesPresentMethodKind

        override konst name: String
            get() = "testAllFilesPresentIn$testClassName"

        override konst dataString: String?
            get() = null

        konst classModel: SimpleTestClassModel
            get() = this@SimpleTestClassModel

        override fun shouldBeGenerated(): Boolean {
            return true
        }

        override konst tags: List<String>
            get() = emptyList()
    }

    companion object {
        private konst BY_NAME = Comparator.comparing(TestEntityModel::name)

        private fun dirHasFilesInside(dir: File): Boolean {
            return !FileUtil.processFilesRecursively(dir) { obj: File -> obj.isDirectory }
        }

        private fun dirHasSubDirs(dir: File): Boolean {
            konst listFiles = dir.listFiles() ?: return false
            for (file in listFiles) {
                if (file.isDirectory) {
                    return true
                }
            }
            return false
        }
    }
}
