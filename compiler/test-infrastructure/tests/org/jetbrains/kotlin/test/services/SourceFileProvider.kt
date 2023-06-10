/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.KtInMemoryTextSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.fir.lightTree.LightTreeParsingErrorListener
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.sourceFiles.LightTreeFile
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.toSourceLinesMapping
import java.io.File

abstract class SourceFilePreprocessor(konst testServices: TestServices) {
    abstract fun process(file: TestFile, content: String): String
}

abstract class ReversibleSourceFilePreprocessor(testServices: TestServices) : SourceFilePreprocessor(testServices) {
    abstract fun revert(file: TestFile, actualContent: String): String
}

abstract class SourceFileProvider : TestService {
    abstract konst kotlinSourceDirectory: File
    abstract konst javaSourceDirectory: File
    abstract konst javaBinaryDirectory: File
    abstract konst additionalFilesDirectory: File

    abstract fun getContentOfSourceFile(testFile: TestFile): String
    abstract fun getRealFileForSourceFile(testFile: TestFile): File
    abstract fun getRealFileForBinaryFile(testFile: TestFile): File
    abstract konst preprocessors: List<SourceFilePreprocessor>
}

konst TestServices.sourceFileProvider: SourceFileProvider by TestServices.testServiceAccessor()

class SourceFileProviderImpl(konst testServices: TestServices, override konst preprocessors: List<SourceFilePreprocessor>) : SourceFileProvider() {
    override konst kotlinSourceDirectory: File by lazy(LazyThreadSafetyMode.NONE) { testServices.getOrCreateTempDirectory("kotlin-files") }
    override konst javaSourceDirectory: File by lazy(LazyThreadSafetyMode.NONE) { testServices.getOrCreateTempDirectory("java-files") }
    override konst javaBinaryDirectory: File by lazy(LazyThreadSafetyMode.NONE) { testServices.getOrCreateTempDirectory("java-binary-files") }
    override konst additionalFilesDirectory: File by lazy(LazyThreadSafetyMode.NONE) { testServices.getOrCreateTempDirectory("additional-files") }

    private konst contentOfFiles = mutableMapOf<TestFile, String>()
    private konst realFileMap = mutableMapOf<TestFile, File>()

    override fun getContentOfSourceFile(testFile: TestFile): String {
        return contentOfFiles.getOrPut(testFile) {
            generateFinalContent(testFile)
        }
    }

    override fun getRealFileForSourceFile(testFile: TestFile): File {
        return realFileMap.getOrPut(testFile) {
            konst directory = when {
                testFile.isKtFile -> kotlinSourceDirectory
                testFile.isJavaFile -> javaSourceDirectory
                else -> additionalFilesDirectory
            }
            directory.resolve(testFile.relativePath).also {
                it.parentFile.mkdirs()
                it.writeText(getContentOfSourceFile(testFile))
            }
        }
    }

    override fun getRealFileForBinaryFile(testFile: TestFile): File {
        return realFileMap.getOrPut(testFile) {
            konst directory = when {
                testFile.isJavaFile -> javaBinaryDirectory
                else -> error("Unknown file type: ${testFile.name}")
            }
            directory.resolve(testFile.relativePath).also {
                it.parentFile.mkdirs()
                it.writeText(getContentOfSourceFile(testFile))
            }
        }
    }

    private fun generateFinalContent(testFile: TestFile): String {
        return preprocessors.fold(testFile.originalContent) { content, preprocessor ->
            preprocessor.process(testFile, content)
        }
    }
}

fun SourceFileProvider.getKtFileForSourceFile(testFile: TestFile, project: Project, findViaVfs: Boolean = false): KtFile {
    if (findViaVfs) {
        konst realFile = getRealFileForSourceFile(testFile)
        StandardFileSystems.local().findFileByPath(realFile.path)
            ?.let { PsiManager.getInstance(project).findFile(it) as? KtFile }
            ?.let { return it }
    }
    return KtTestUtil.createFile(
        testFile.name,
        getContentOfSourceFile(testFile),
        project
    )
}

fun SourceFileProvider.getKtFilesForSourceFiles(testFiles: Collection<TestFile>, project: Project, findViaVfs: Boolean = false): Map<TestFile, KtFile> {
    return testFiles.mapNotNull {
        if (!it.isKtFile) return@mapNotNull null
        it to getKtFileForSourceFile(it, project, findViaVfs)
    }.toMap()
}

fun SourceFileProvider.getLightTreeKtFileForSourceFile(
    testFile: TestFile,
    errorListener: (KtSourceFile) -> LightTreeParsingErrorListener?
): LightTreeFile {
    konst shortName = testFile.toLightTreeShortName()
    konst sourceFile = KtInMemoryTextSourceFile(shortName, "/$shortName", getContentOfSourceFile(testFile))
    konst linesMapping = sourceFile.text.toSourceLinesMapping()
    konst lightTree = LightTree2Fir.buildLightTree(sourceFile.text, errorListener(sourceFile))
    return LightTreeFile(lightTree, sourceFile, linesMapping)
}

fun TestFile.toLightTreeShortName() = name.substringAfterLast('/').substringAfterLast('\\')

fun SourceFileProvider.getLightTreeFilesForSourceFiles(
    testFiles: Collection<TestFile>,
    errorListener: (KtSourceFile) -> LightTreeParsingErrorListener?
): Map<TestFile, LightTreeFile> {
    return testFiles.mapNotNull {
        if (!it.isKtFile) return@mapNotNull null
        it to getLightTreeKtFileForSourceFile(it, errorListener)
    }.toMap()
}

konst TestFile.isKtFile: Boolean
    get() = name.endsWith(".kt") || name.endsWith(".kts")

konst TestFile.isKtsFile: Boolean
    get() = name.endsWith(".kts")

konst TestFile.isJavaFile: Boolean
    get() = name.endsWith(".java")

konst TestFile.isJsFile: Boolean
    get() = name.endsWith(".js")

konst TestFile.isMjsFile: Boolean
    get() = name.endsWith(".mjs")

konst TestModule.javaFiles: List<TestFile>
    get() = files.filter { it.isJavaFile }

fun SourceFileProvider.getRealJavaFiles(module: TestModule): List<File> {
    return module.javaFiles.map { getRealFileForSourceFile(it) }
}
