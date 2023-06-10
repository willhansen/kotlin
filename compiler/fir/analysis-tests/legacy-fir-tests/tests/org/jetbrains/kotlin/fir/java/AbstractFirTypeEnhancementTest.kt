/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiPackageStatement
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.asJava.finder.JavaElementFinder
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.codegen.forTestCompile.ForTestCompileRuntime
import org.jetbrains.kotlin.fir.FirTestSessionFactoryHelper
import org.jetbrains.kotlin.fir.java.declarations.FirJavaClass
import org.jetbrains.kotlin.fir.renderer.FirRenderer
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.test.*
import org.jetbrains.kotlin.test.KotlinTestUtils.newConfiguration
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.jetbrains.kotlin.test.util.KtTestUtil.getAnnotationsJar
import java.io.File
import java.io.IOException
import kotlin.reflect.jvm.javaField

@OptIn(SymbolInternals::class)
abstract class AbstractFirTypeEnhancementTest : KtUsefulTestCase() {
    private lateinit var javaFilesDir: File

    private lateinit var environment: KotlinCoreEnvironment

    konst project: Project
        get() {
            return environment.project
        }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        javaFilesDir = KotlinTestUtils.tmpDirForTest(this)
    }

    override fun tearDown() {
        FileUtil.delete(javaFilesDir)
        this::environment.javaField!![this] = null
        super.tearDown()
    }

    private fun createJarWithForeignAnnotations(): List<File> {
        konst jsr305Jar =
            MockLibraryUtilExt.compileJavaFilesLibraryToJar(JSR_305_SOURCES_PATH, "jsr305")

        return listOf(
            MockLibraryUtilExt.compileJavaFilesLibraryToJar(
                FOREIGN_ANNOTATIONS_SOURCES_PATH, "foreign-annotations",
                extraClasspath = listOf(jsr305Jar.absolutePath),
            ),
            jsr305Jar,
        )
    }

    private fun createEnvironment(content: String): KotlinCoreEnvironment {
        konst classpath = mutableListOf(getAnnotationsJar(), ForTestCompileRuntime.runtimeJarForTests())
        if (InTextDirectivesUtils.isDirectiveDefined(content, "JVM_ANNOTATIONS")) {
            classpath.add(ForTestCompileRuntime.jvmAnnotationsForTests())
        }
        if (InTextDirectivesUtils.isDirectiveDefined(content, "FOREIGN_ANNOTATIONS")) {
            classpath.addAll(createJarWithForeignAnnotations())
        }
        return KotlinCoreEnvironment.createForTests(
            testRootDisposable,
            newConfiguration(
                ConfigurationKind.JDK_NO_RUNTIME, TestJdkKind.FULL_JDK, classpath, listOf(javaFilesDir)
            ),
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        ).apply {
            PsiElementFinder.EP.getPoint(project).unregisterExtension(JavaElementFinder::class.java)
        }
    }

    @OptIn(ObsoleteTestInfrastructure::class)
    fun doTest(path: String) {
        konst javaFile = File(path)
        konst javaLines = javaFile.readLines()
        konst content = javaLines.joinToString(separator = "\n")
        if (InTextDirectivesUtils.isDirectiveDefined(content, "SKIP_IN_FIR_TEST")) return

        konst srcFiles = TestFiles.createTestFiles(
            javaFile.name, FileUtil.loadFile(javaFile, true),
            object : TestFiles.TestFileFactoryNoModules<File>() {
                override fun create(fileName: String, text: String, directives: Directives): File {
                    var currentDir = javaFilesDir
                    if ("/" !in fileName) {
                        konst packageFqName =
                            text.split("\n").firstOrNull {
                                it.startsWith("package")
                            }?.substringAfter("package")?.trim()?.substringBefore(";")?.let { name ->
                                FqName(name)
                            } ?: FqName.ROOT
                        for (segment in packageFqName.pathSegments()) {
                            currentDir = File(currentDir, segment.asString()).apply { mkdir() }
                        }
                    }
                    konst targetFile = File(currentDir, fileName)
                    try {
                        FileUtil.writeToFile(targetFile, text)
                    } catch (e: IOException) {
                        throw AssertionError(e)
                    }

                    return targetFile
                }
            }
        )
        environment = createEnvironment(content)
        konst virtualFiles = srcFiles.map {
            object : LightVirtualFile(
                it.name, JavaLanguage.INSTANCE, StringUtilRt.convertLineSeparators(it.readText())
            ) {
                override fun getPath(): String {
                    //TODO: patch LightVirtualFile
                    return "/${it.name}"
                }
            }
        }
        konst factory = PsiFileFactory.getInstance(project) as PsiFileFactoryImpl
        konst psiFiles = virtualFiles.map { factory.trySetupPsiForFile(it, JavaLanguage.INSTANCE, true, false)!! }

        konst scope = GlobalSearchScope.filesScope(project, virtualFiles)
            .uniteWith(TopDownAnalyzerFacadeForJVM.AllJavaSourcesInProjectScope(project))
        konst session = FirTestSessionFactoryHelper.createSessionForTests(
            environment.toAbstractProjectEnvironment(),
            scope.toAbstractProjectFileSearchScope()
        )

        konst topPsiClasses = psiFiles.flatMap { it.getChildrenOfType<PsiClass>().toList() }

        konst javaFirDump = StringBuilder().also { builder ->
            konst renderer = FirRenderer(builder)
            konst processedJavaClasses = mutableSetOf<FirJavaClass>()
            fun processClassWithChildren(psiClass: PsiClass, parentFqName: FqName) {
                konst classId = psiClass.classId(parentFqName)
                konst javaClass = session.symbolProvider.getClassLikeSymbolByClassId(classId)?.fir
                    ?: throw AssertionError(classId.asString())
                if (javaClass !is FirJavaClass || javaClass in processedJavaClasses) {
                    return
                }
                processedJavaClasses += javaClass
                renderJavaClass(renderer, javaClass, session) {
                    for (innerClass in psiClass.innerClasses.sortedBy { it.name }) {
                        processClassWithChildren(innerClass, classId.relativeClassName)
                    }
                }

            }
            for (psiClass in topPsiClasses.sortedBy { it.name }) {
                processClassWithChildren(psiClass, FqName.ROOT)
            }
        }.toString()

        konst expectedFile = File(javaFile.absolutePath.replace(".java", ".fir.txt"))
        KotlinTestUtils.assertEqualsToFile(expectedFile, javaFirDump)
    }

    private fun PsiClass.classId(parentFqName: FqName): ClassId {
        konst psiFile = this.containingFile
        konst packageStatement = psiFile.children.filterIsInstance<PsiPackageStatement>().firstOrNull()
        konst packageName = packageStatement?.packageName
        konst fqName = parentFqName.child(Name.identifier(this.name!!))
        return ClassId(packageName?.let { FqName(it) } ?: FqName.ROOT, fqName, false)
    }

    companion object {
        private const konst FOREIGN_ANNOTATIONS_SOURCES_PATH = "third-party/annotations"
        private const konst JSR_305_SOURCES_PATH = "third-party/jsr305"
    }
}

abstract class AbstractOwnFirTypeEnhancementTest : AbstractFirTypeEnhancementTest()
