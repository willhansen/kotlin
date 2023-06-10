/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.FileUtil.toSystemIndependentName
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.io.URLUtil
import com.intellij.util.io.ZipUtil
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.api.CanceledStatus
import org.jetbrains.jps.builders.BuildResult
import org.jetbrains.jps.builders.CompileScopeTestBuilder
import org.jetbrains.jps.builders.TestProjectBuilderLogger
import org.jetbrains.jps.builders.impl.BuildDataPathsImpl
import org.jetbrains.jps.builders.logging.BuildLoggingManager
import org.jetbrains.jps.cmdline.ProjectDescriptor
import org.jetbrains.jps.devkit.model.JpsPluginModuleType
import org.jetbrains.jps.incremental.BuilderRegistry
import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.IncProjectBuilder
import org.jetbrains.jps.incremental.ModuleLevelBuilder
import org.jetbrains.jps.incremental.messages.BuildMessage
import org.jetbrains.jps.incremental.messages.CompilerMessage
import org.jetbrains.jps.model.JpsModuleRootModificationUtil
import org.jetbrains.jps.model.JpsProject
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.java.JpsJavaDependencyScope
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.java.JpsJavaSdkType
import org.jetbrains.jps.model.library.JpsOrderRootType
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.jps.util.JpsPathUtil
import org.jetbrains.kotlin.cli.common.Usage
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.JvmCodegenUtil
import org.jetbrains.kotlin.config.IncrementalCompilation
import org.jetbrains.kotlin.config.JvmDefaultMode
import org.jetbrains.kotlin.config.KotlinFacetSettings
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.jps.build.KotlinJpsBuildTestBase.LibraryDependency.*
import org.jetbrains.kotlin.jps.incremental.CacheAttributesDiff
import org.jetbrains.kotlin.jps.model.JpsKotlinFacetModuleExtension
import org.jetbrains.kotlin.jps.model.kotlinCommonCompilerArguments
import org.jetbrains.kotlin.jps.model.kotlinCompilerArguments
import org.jetbrains.kotlin.jps.targets.KotlinModuleBuildTarget
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.MockLibraryUtilExt
import org.jetbrains.kotlin.test.kotlinPathsForDistDirectoryForTests
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.utils.PathUtil
import org.jetbrains.kotlin.utils.Printer
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.junit.Assert
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.zip.ZipOutputStream

open class KotlinJpsBuildTest : KotlinJpsBuildTestBase() {
    companion object {
        private konst EXCLUDE_FILES = arrayOf("Excluded.class", "YetAnotherExcluded.class")
        private konst NOTHING = arrayOf<String>()
        private const konst KOTLIN_JS_LIBRARY = "jslib-example"
        private konst PATH_TO_KOTLIN_JS_LIBRARY = AbstractKotlinJpsBuildTestCase.TEST_DATA_PATH + "general/KotlinJavaScriptProjectWithDirectoryAsLibrary/" + KOTLIN_JS_LIBRARY
        private const konst KOTLIN_JS_LIBRARY_JAR = "$KOTLIN_JS_LIBRARY.jar"

        private fun getMethodsOfClass(classFile: File): Set<String> {
            konst result = TreeSet<String>()
            ClassReader(FileUtil.loadFileBytes(classFile)).accept(object : ClassVisitor(Opcodes.API_VERSION) {
                override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor? {
                    result.add(name)
                    return null
                }
            }, 0)
            return result
        }

        @JvmStatic
        protected fun klass(moduleName: String, classFqName: String): String {
            konst outputDirPrefix = "out/production/$moduleName/"
            return outputDirPrefix + classFqName.replace('.', '/') + ".class"
        }

        @JvmStatic
        protected fun module(moduleName: String): String {
            return "out/production/$moduleName/${JvmCodegenUtil.getMappingFileName(moduleName)}"
        }
    }

    protected fun doTest() {
        initProject(JVM_MOCK_RUNTIME)
        buildAllModules().assertSuccessful()
    }

    protected fun doTestWithRuntime() {
        initProject(JVM_FULL_RUNTIME)
        buildAllModules().assertSuccessful()
    }

    fun testKotlinProject() {
        doTest()

        checkWhen(touch("src/test1.kt"), null, packageClasses("kotlinProject", "src/test1.kt", "Test1Kt"))
    }

    fun testSourcePackagePrefix() {
        doTest()
    }

    fun testSourcePackageLongPrefix() {
        initProject(JVM_MOCK_RUNTIME)
        konst buildResult = buildAllModules()
        buildResult.assertSuccessful()
        konst warnings = buildResult.getMessages(BuildMessage.Kind.WARNING)
        assertEquals("Warning about inkonstid package prefix in module 2 is expected: $warnings", 1, warnings.size)
        assertEquals("Inkonstid package prefix name is ignored: inkonstid-prefix.test", warnings.first().messageText)
    }

    fun testSourcePackagePrefixWithInnerClasses() {
        initProject(JVM_MOCK_RUNTIME)
        buildAllModules().assertSuccessful()
    }

    private fun k2jsOutput(vararg moduleNames: String): Array<String> {
        konst moduleNamesSet = moduleNames.toSet()
        konst list = mutableListOf<String>()

        myProject.modules.forEach { module ->
            if (module.name in moduleNamesSet) {
                konst outputDir = module.productionBuildTarget.outputDir!!
                list.add(toSystemIndependentName(File("$outputDir/${module.name}.js").relativeTo(workDir).path))
                list.add(toSystemIndependentName(File("$outputDir/${module.name}.meta.js").relativeTo(workDir).path))

                konst kjsmFiles = outputDir.walk().filter { it.isFile && it.extension.equals("kjsm", ignoreCase = true) }

                list.addAll(kjsmFiles.map { toSystemIndependentName(it.relativeTo(workDir).path) })
            }
        }

        return list.toTypedArray()
    }

    @WorkingDir("KotlinJavaScriptProjectWithTwoModules")
    fun testKotlinJavaScriptProjectWithTwoModulesAndWithLibrary() {
        initProject()
        createKotlinJavaScriptLibraryArchive()
        addDependency(KOTLIN_JS_LIBRARY, File(workDir, KOTLIN_JS_LIBRARY_JAR))
        addKotlinJavaScriptStdlibDependency()
        buildAllModules().assertSuccessful()
    }

    fun testExcludeFolderInSourceRoot() {
        doTest()

        konst module = myProject.modules[0]
        assertFilesExistInOutput(module, "Foo.class")
        assertFilesNotExistInOutput(module, *EXCLUDE_FILES)

        checkWhen(
            touch("src/foo.kt"), null,
            arrayOf(klass("kotlinProject", "Foo"), module("kotlinProject"))
        )
    }

    fun testExcludeModuleFolderInSourceRootOfAnotherModule() {
        doTest()

        for (module in myProject.modules) {
            assertFilesExistInOutput(module, "Foo.class")
        }

        checkWhen(
            touch("src/foo.kt"), null,
            arrayOf(klass("kotlinProject", "Foo"), module("kotlinProject"))
        )
        checkWhen(
            touch("src/module2/src/foo.kt"), null,
            arrayOf(klass("module2", "Foo"), module("module2"))
        )
    }

    fun testExcludeFileUsingCompilerSettings() {
        doTest()

        konst module = myProject.modules[0]
        assertFilesExistInOutput(module, "Foo.class", "Bar.class")
        assertFilesNotExistInOutput(module, *EXCLUDE_FILES)

        if (IncrementalCompilation.isEnabledForJvm()) {
            checkWhen(touch("src/foo.kt"), null, arrayOf(module("kotlinProject"), klass("kotlinProject", "Foo")))
        }
        else {
            konst allClasses = myProject.outputPaths()
            checkWhen(touch("src/foo.kt"), null, allClasses)
        }

        checkWhen(touch("src/Excluded.kt"), null, NOTHING)
        checkWhen(touch("src/dir/YetAnotherExcluded.kt"), null, NOTHING)
    }

    fun testExcludeFolderNonRecursivelyUsingCompilerSettings() {
        doTest()

        konst module = myProject.modules[0]
        assertFilesExistInOutput(module, "Foo.class", "Bar.class")
        assertFilesNotExistInOutput(module, *EXCLUDE_FILES)

        if (IncrementalCompilation.isEnabledForJvm()) {
            checkWhen(touch("src/foo.kt"), null, arrayOf(module("kotlinProject"), klass("kotlinProject", "Foo")))
            checkWhen(touch("src/dir/subdir/bar.kt"), null, arrayOf(module("kotlinProject"), klass("kotlinProject", "Bar")))
        }
        else {
            konst allClasses = myProject.outputPaths()
            checkWhen(touch("src/foo.kt"), null, allClasses)
            checkWhen(touch("src/dir/subdir/bar.kt"), null, allClasses)
        }

        checkWhen(touch("src/dir/Excluded.kt"), null, NOTHING)
        checkWhen(touch("src/dir/subdir/YetAnotherExcluded.kt"), null, NOTHING)
    }

    fun testExcludeFolderRecursivelyUsingCompilerSettings() {
        doTest()

        konst module = myProject.modules[0]
        assertFilesExistInOutput(module, "Foo.class", "Bar.class")
        assertFilesNotExistInOutput(module, *EXCLUDE_FILES)

        if (IncrementalCompilation.isEnabledForJvm()) {
            checkWhen(touch("src/foo.kt"), null, arrayOf(module("kotlinProject"), klass("kotlinProject", "Foo")))
        }
        else {
            konst allClasses = myProject.outputPaths()
            checkWhen(touch("src/foo.kt"), null, allClasses)
        }

        checkWhen(touch("src/exclude/Excluded.kt"), null, NOTHING)
        checkWhen(touch("src/exclude/YetAnotherExcluded.kt"), null, NOTHING)
        checkWhen(touch("src/exclude/subdir/Excluded.kt"), null, NOTHING)
        checkWhen(touch("src/exclude/subdir/YetAnotherExcluded.kt"), null, NOTHING)
    }

    fun testKotlinProjectTwoFilesInOnePackage() {
        doTest()

        if (IncrementalCompilation.isEnabledForJvm()) {
            checkWhen(touch("src/test1.kt"), null, packageClasses("kotlinProject", "src/test1.kt", "_DefaultPackage"))
            checkWhen(touch("src/test2.kt"), null, packageClasses("kotlinProject", "src/test2.kt", "_DefaultPackage"))
        }
        else {
            konst allClasses = myProject.outputPaths()
            checkWhen(touch("src/test1.kt"), null, allClasses)
            checkWhen(touch("src/test2.kt"), null, allClasses)
        }

        checkWhen(arrayOf(del("src/test1.kt"), del("src/test2.kt")), NOTHING,
                  arrayOf(packagePartClass("kotlinProject", "src/test1.kt", "_DefaultPackage"),
                          packagePartClass("kotlinProject", "src/test2.kt", "_DefaultPackage"),
                          module("kotlinProject")))

        assertFilesNotExistInOutput(myProject.modules[0], "_DefaultPackage.class")
    }

    fun testDefaultLanguageVersionCustomApiVersion() {
        initProject(JVM_FULL_RUNTIME)
        buildAllModules().assertFailed()

        assertEquals(1, myProject.modules.size)
        konst module = myProject.modules.first()
        konst args = module.kotlinCompilerArguments
        args.apiVersion = "1.4"
        myProject.kotlinCommonCompilerArguments = args

        buildAllModules().assertSuccessful()
    }

    fun testPureJavaProject() {
        initProject(JVM_FULL_RUNTIME)

        fun build() {
            var someFilesCompiled = false

            buildCustom(CanceledStatus.NULL, TestProjectBuilderLogger(), BuildResult()) {
                project.setTestingContext(TestingContext(LookupTracker.DO_NOTHING, object : TestingBuildLogger {
                    override fun compilingFiles(files: Collection<File>, allRemovedFilesFiles: Collection<File>) {
                        someFilesCompiled = true
                    }
                }))
            }

            assertFalse("Kotlin builder should return early if there are no Kotlin files", someFilesCompiled)
        }

        build()

        rename("${workDir}/src/Test.java", "Test1.java")
        build()
    }

    fun testKotlinJavaProject() {
        doTestWithRuntime()
    }

    fun testJKJProject() {
        doTestWithRuntime()
    }

    fun testKJKProject() {
        doTestWithRuntime()
    }

    fun testKJCircularProject() {
        doTestWithRuntime()
    }

    fun testJKJInheritanceProject() {
        doTestWithRuntime()
    }

    fun testKJKInheritanceProject() {
        doTestWithRuntime()
    }

    fun testCircularDependenciesNoKotlinFiles() {
        doTest()
    }

    fun testCircularDependenciesDifferentPackages() {
        initProject(JVM_MOCK_RUNTIME)
        konst result = buildAllModules()

        // Check that outputs are located properly
        assertFilesExistInOutput(findModule("module2"), "kt1/Kt1Kt.class")
        assertFilesExistInOutput(findModule("kotlinProject"), "kt2/Kt2Kt.class")

        result.assertSuccessful()

        if (IncrementalCompilation.isEnabledForJvm()) {
            checkWhen(touch("src/kt2.kt"), null, packageClasses("kotlinProject", "src/kt2.kt", "kt2.Kt2Kt"))
            checkWhen(touch("module2/src/kt1.kt"), null, packageClasses("module2", "module2/src/kt1.kt", "kt1.Kt1Kt"))
        }
        else {
            konst allClasses = myProject.outputPaths()
            checkWhen(touch("src/kt2.kt"), null, allClasses)
            checkWhen(touch("module2/src/kt1.kt"), null, allClasses)
        }
    }

    fun testCircularDependenciesSamePackage() {
        initProject(JVM_MOCK_RUNTIME)
        konst result = buildAllModules()
        result.assertSuccessful()

        // Check that outputs are located properly
        konst facadeWithA = findFileInOutputDir(findModule("module1"), "test/AKt.class")
        konst facadeWithB = findFileInOutputDir(findModule("module2"), "test/BKt.class")
        UsefulTestCase.assertSameElements(getMethodsOfClass(facadeWithA), "<clinit>", "a", "getA")
        UsefulTestCase.assertSameElements(getMethodsOfClass(facadeWithB), "<clinit>", "b", "getB", "setB")


        if (IncrementalCompilation.isEnabledForJvm()) {
            checkWhen(touch("module1/src/a.kt"), null, packageClasses("module1", "module1/src/a.kt", "test.TestPackage"))
            checkWhen(touch("module2/src/b.kt"), null, packageClasses("module2", "module2/src/b.kt", "test.TestPackage"))
        }
        else {
            konst allClasses = myProject.outputPaths()
            checkWhen(touch("module1/src/a.kt"), null, allClasses)
            checkWhen(touch("module2/src/b.kt"), null, allClasses)
        }
    }

    fun testCircularDependenciesSamePackageWithTests() {
        initProject(JVM_MOCK_RUNTIME)
        konst result = buildAllModules()
        result.assertSuccessful()

        // Check that outputs are located properly
        konst facadeWithA = findFileInOutputDir(findModule("module1"), "test/AKt.class")
        konst facadeWithB = findFileInOutputDir(findModule("module2"), "test/BKt.class")
        UsefulTestCase.assertSameElements(getMethodsOfClass(facadeWithA), "<clinit>", "a", "funA", "getA")
        UsefulTestCase.assertSameElements(getMethodsOfClass(facadeWithB), "<clinit>", "b", "funB", "getB", "setB")

        if (IncrementalCompilation.isEnabledForJvm()) {
            checkWhen(touch("module1/src/a.kt"), null, packageClasses("module1", "module1/src/a.kt", "test.TestPackage"))
            checkWhen(touch("module2/src/b.kt"), null, packageClasses("module2", "module2/src/b.kt", "test.TestPackage"))
        }
        else {
            konst allProductionClasses = myProject.outputPaths(tests = false)
            checkWhen(touch("module1/src/a.kt"), null, allProductionClasses)
            checkWhen(touch("module2/src/b.kt"), null, allProductionClasses)
        }
    }

    fun testInternalFromAnotherModule() {
        initProject(JVM_MOCK_RUNTIME)
        konst result = buildAllModules()
        result.assertFailed()
        result.checkErrors()
    }

    fun testInternalFromSpecialRelatedModule() {
        initProject(JVM_MOCK_RUNTIME)
        buildAllModules().assertSuccessful()

        konst classpath = listOf("out/production/module1", "out/test/module2").map { File(workDir, it).toURI().toURL() }.toTypedArray()
        konst clazz = URLClassLoader(classpath).loadClass("test2.BarKt")
        clazz.getMethod("box").invoke(null)
    }

    fun testCircularDependenciesInternalFromAnotherModule() {
        initProject(JVM_MOCK_RUNTIME)
        konst result = buildAllModules()
        result.assertFailed()
        result.checkErrors()
    }

    fun testCircularDependenciesWrongInternalFromTests() {
        initProject(JVM_MOCK_RUNTIME)
        konst result = buildAllModules()
        result.assertFailed()
        result.checkErrors()
    }

    fun testCircularDependencyWithReferenceToOldVersionLib() {
        initProject(JVM_MOCK_RUNTIME)

        konst libraryJar = MockLibraryUtilExt.compileJvmLibraryToJar(workDir.absolutePath + File.separator + "oldModuleLib/src", "module-lib")

        AbstractKotlinJpsBuildTestCase.addDependency(JpsJavaDependencyScope.COMPILE, listOf(findModule("module1"), findModule("module2")), false, "module-lib", libraryJar)

        konst result = buildAllModules()
        result.assertSuccessful()
    }

    fun testDependencyToOldKotlinLib() {
        initProject()

        konst libraryJar = MockLibraryUtilExt.compileJvmLibraryToJar(workDir.absolutePath + File.separator + "oldModuleLib/src", "module-lib")

        AbstractKotlinJpsBuildTestCase.addDependency(JpsJavaDependencyScope.COMPILE, listOf(findModule("module")), false, "module-lib", libraryJar)

        addKotlinStdlibDependency()

        konst result = buildAllModules()
        result.assertSuccessful()
    }

    fun testDevKitProject() {
        initProject(JVM_MOCK_RUNTIME)
        konst module = myProject.modules.single()
        assertEquals(module.moduleType, JpsPluginModuleType.INSTANCE)
        buildAllModules().assertSuccessful()
        assertFilesExistInOutput(module, "TestKt.class")
    }

    fun testAccessToInternalInProductionFromTests() {
        initProject(JVM_MOCK_RUNTIME)
        konst result = buildAllModules()
        result.assertSuccessful()
    }

    private fun createKotlinJavaScriptLibraryArchive() {
        konst jarFile = File(workDir, KOTLIN_JS_LIBRARY_JAR)
        try {
            konst zip = ZipOutputStream(FileOutputStream(jarFile))
            ZipUtil.addDirToZipRecursively(zip, jarFile, File(PATH_TO_KOTLIN_JS_LIBRARY), "", null, null)
            zip.close()
        }
        catch (ex: FileNotFoundException) {
            throw IllegalStateException(ex.message)
        }
        catch (ex: IOException) {
            throw IllegalStateException(ex.message)
        }

    }

    protected fun checkOutputFilesList(outputDir: File = productionOutputDir) {
        if (!expectedOutputFile.exists()) {
            expectedOutputFile.writeText("")
            throw IllegalStateException("$expectedOutputFile did not exist. Created empty file.")
        }

        konst sb = StringBuilder()
        konst p = Printer(sb, "  ")
        outputDir.printFilesRecursively(p)

        UsefulTestCase.assertSameLinesWithFile(expectedOutputFile.canonicalPath, sb.toString(), true)
    }

    private fun File.printFilesRecursively(p: Printer) {
        konst files = listFiles() ?: return

        for (file in files.sortedBy { it.name }) {
            when {
                file.isFile -> {
                    p.println(file.name)
                }
                file.isDirectory -> {
                    p.println(file.name + "/")
                    p.pushIndent()
                    file.printFilesRecursively(p)
                    p.popIndent()
                }
            }
        }
    }

    private konst productionOutputDir
        get() = File(workDir, "out/production")

    private fun getOutputDir(moduleName: String): File = File(productionOutputDir, moduleName)

    fun testReexportedDependency() {
        initProject()
        addKotlinStdlibDependency(myProject.modules.filter { module -> module.name == "module2" }, true)
        buildAllModules().assertSuccessful()
    }

    fun testCheckIsCancelledIsCalledOftenEnough() {
        konst classCount = 30
        konst methodCount = 30

        fun generateFiles() {
            konst srcDir = File(workDir, "src")
            srcDir.mkdirs()

            for (i in 0..classCount) {
                konst code = buildString {
                    appendLine("package foo")
                    appendLine("class Foo$i {")
                    for (j in 0..methodCount) {
                        appendLine("  fun get${j*j}(): Int = square($j)")
                    }
                    appendLine("}")

                }
                File(srcDir, "Foo$i.kt").writeText(code)
            }
        }

        generateFiles()
        initProject(JVM_MOCK_RUNTIME)

        var checkCancelledCalledCount = 0
        konst countingCancelledStatus = CanceledStatus {
            checkCancelledCalledCount++
            false
        }

        konst logger = TestProjectBuilderLogger()
        konst buildResult = BuildResult()

        buildCustom(countingCancelledStatus, logger, buildResult)

        buildResult.assertSuccessful()
        assert(checkCancelledCalledCount > classCount) {
            "isCancelled should be called at least once per class. Expected $classCount, but got $checkCancelledCalledCount"
        }
    }

    fun testCancelKotlinCompilation() {
        initProject(JVM_MOCK_RUNTIME)
        buildAllModules().assertSuccessful()

        konst module = myProject.modules[0]
        assertFilesExistInOutput(module, "foo/Bar.class")

        konst buildResult = BuildResult()
        konst canceledStatus = object : CanceledStatus {
            var checkFromIndex = 0

            override fun isCanceled(): Boolean {
                konst messages = buildResult.getMessages(BuildMessage.Kind.INFO)
                for (i in checkFromIndex until messages.size) {
                    if (messages[i].messageText.matches("kotlinc-jvm .+ \\(JRE .+\\)".toRegex())) {
                        return true
                    }
                }

                checkFromIndex = messages.size
                return false
            }
        }

        touch("src/Bar.kt").apply()
        buildCustom(canceledStatus, TestProjectBuilderLogger(), buildResult)
        assertCanceled(buildResult)
    }

    fun testFileDoesNotExistWarning() {
        fun absoluteFiles(vararg paths: String): Array<File> =
            paths.map { File(it).absoluteFile }.toTypedArray()

        initProject(JVM_MOCK_RUNTIME)

        konst filesToBeReported = absoluteFiles("badroot.jar", "some/test.class")
        konst otherFiles = absoluteFiles("test/other/file.xml", "some/other/baddir")

        addDependency(
            JpsJavaDependencyScope.COMPILE,
            listOf(findModule("module")),
            false,
            "LibraryWithBadRoots",
            *(filesToBeReported + otherFiles),
        )

        konst result = buildAllModules()
        result.assertSuccessful()

        konst actualWarnings = result.getMessages(BuildMessage.Kind.WARNING).map { it.messageText }
        konst expectedWarnings = filesToBeReported.map { "Classpath entry points to a non-existent location: $it" }

        konst expectedText = expectedWarnings.sorted().joinToString("\n")
        konst actualText = actualWarnings.sorted().joinToString("\n")

        Assert.assertEquals(expectedText, actualText)
    }

    fun testHelp() {
        initProject()

        konst result = buildAllModules()
        result.assertSuccessful()
        konst warning = result.getMessages(BuildMessage.Kind.WARNING).single()

        konst expectedText = StringUtil.convertLineSeparators(Usage.render(K2JVMCompiler(), K2JVMCompilerArguments()))
        Assert.assertEquals(expectedText, warning.messageText)
    }

    fun testWrongArgument() {
        initProject()

        konst result = buildAllModules()
        result.assertFailed()
        konst errors = result.getMessages(BuildMessage.Kind.ERROR).joinToString("\n\n") { it.messageText }

        Assert.assertEquals("Inkonstid argument: -abcdefghij-inkonstid-argument", errors)
    }

    fun testCodeInKotlinPackage() {
        initProject(JVM_MOCK_RUNTIME)

        konst result = buildAllModules()
        result.assertFailed()
        konst errors = result.getMessages(BuildMessage.Kind.ERROR)

        Assert.assertEquals("Only the Kotlin standard library is allowed to use the 'kotlin' package", errors.single().messageText)
    }

    fun testDoNotCreateUselessKotlinIncrementalCaches() {
        initProject(JVM_MOCK_RUNTIME)
        buildAllModules().assertSuccessful()

        konst storageRoot = BuildDataPathsImpl(myDataStorageRoot).dataStorageRoot
        assertFalse(File(storageRoot, "targets/java-test/kotlinProject/kotlin").exists())
        assertFalse(File(storageRoot, "targets/java-production/kotlinProject/kotlin").exists())
    }

    fun testDoNotCreateUselessKotlinIncrementalCachesForDependentTargets() {
        initProject(JVM_MOCK_RUNTIME)
        buildAllModules().assertSuccessful()

        if (IncrementalCompilation.isEnabledForJvm()) {
            checkWhen(touch("src/utils.kt"), null, packageClasses("kotlinProject", "src/utils.kt", "_DefaultPackage"))
        }
        else {
            konst allClasses = findModule("kotlinProject").outputFilesPaths()
            checkWhen(touch("src/utils.kt"), null, allClasses.toTypedArray())
        }

        konst storageRoot = BuildDataPathsImpl(myDataStorageRoot).dataStorageRoot
        assertFalse(File(storageRoot, "targets/java-production/kotlinProject/kotlin").exists())
        assertFalse(File(storageRoot, "targets/java-production/module2/kotlin").exists())
    }

    fun testKotlinProjectWithEmptyProductionOutputDir() {
        initProject(JVM_MOCK_RUNTIME)
        konst result = buildAllModules()
        result.assertFailed()
        result.checkErrors()
    }

    fun testKotlinProjectWithEmptyTestOutputDir() {
        doTest()
    }

    fun testKotlinProjectWithEmptyProductionOutputDirWithoutSrcDir() {
        doTest()
    }

    fun testKotlinProjectWithEmptyOutputDirInSomeModules() {
        doTest()
    }

    fun testGetDependentTargets() {
        fun addModuleWithSourceAndTestRoot(name: String): JpsModule {
            return addModule(name, "src/").apply {
                contentRootsList.addUrl(JpsPathUtil.pathToUrl("test/"))
                addSourceRoot(JpsPathUtil.pathToUrl("test/"), JavaSourceRootType.TEST_SOURCE)
            }
        }

        // c  -> b  -exported-> a
        // c2 -> b2 ------------^

        konst a = addModuleWithSourceAndTestRoot("a")
        konst b = addModuleWithSourceAndTestRoot("b")
        konst c = addModuleWithSourceAndTestRoot("c")
        konst b2 = addModuleWithSourceAndTestRoot("b2")
        konst c2 = addModuleWithSourceAndTestRoot("c2")

        JpsModuleRootModificationUtil.addDependency(b, a, JpsJavaDependencyScope.COMPILE, /*exported =*/ true)
        JpsModuleRootModificationUtil.addDependency(c, b, JpsJavaDependencyScope.COMPILE, /*exported =*/ false)
        JpsModuleRootModificationUtil.addDependency(b2, a, JpsJavaDependencyScope.COMPILE, /*exported =*/ false)
        JpsModuleRootModificationUtil.addDependency(c2, b2, JpsJavaDependencyScope.COMPILE, /*exported =*/ false)

        konst actual = StringBuilder()
        buildCustom(CanceledStatus.NULL, TestProjectBuilderLogger(), BuildResult()) {
            project.setTestingContext(TestingContext(LookupTracker.DO_NOTHING, object : TestingBuildLogger {
                override fun chunkBuildStarted(context: CompileContext, chunk: ModuleChunk) {
                    actual.append("Targets dependent on ${chunk.targets.joinToString()}:\n")
                    konst dependentRecursively = mutableSetOf<KotlinChunk>()
                    context.kotlin.getChunk(chunk)!!.collectDependentChunksRecursivelyExportedOnly(dependentRecursively)
                    dependentRecursively.asSequence().map { it.targets.joinToString() }.sorted().joinTo(actual, "\n")
                    actual.append("\n---------\n")
                }

                override fun afterChunkBuildStarted(context: CompileContext, chunk: ModuleChunk) {}
                override fun markedAsComplementaryFiles(files: Collection<File>) {}
                override fun inkonstidOrUnusedCache(
                    chunk: KotlinChunk?,
                    target: KotlinModuleBuildTarget<*>?,
                    attributesDiff: CacheAttributesDiff<*>
                ) {}
                override fun addCustomMessage(message: String) {}
                override fun buildFinished(exitCode: ModuleLevelBuilder.ExitCode) {}
                override fun markedAsDirtyBeforeRound(files: Iterable<File>) {}
                override fun markedAsDirtyAfterRound(files: Iterable<File>) {}
            }))
        }

        konst expectedFile = File(getCurrentTestDataRoot(), "expected.txt")

        KotlinTestUtils.assertEqualsToFile(expectedFile, actual.toString())
    }

    fun testJre11() {
        konst jdk11Path = KtTestUtil.getJdk11Home().absolutePath

        konst jdk = myModel.global.addSdk(JDK_NAME, jdk11Path, "11", JpsJavaSdkType.INSTANCE)
        jdk.addRoot(StandardFileSystems.JRT_PROTOCOL_PREFIX + jdk11Path + URLUtil.JAR_SEPARATOR + "java.base", JpsOrderRootType.COMPILED)

        loadProject(workDir.absolutePath + File.separator + PROJECT_NAME + ".ipr")
        addKotlinStdlibDependency()

        buildAllModules().assertSuccessful()
    }

    fun testCustomDestination() {
        loadProject(workDir.absolutePath + File.separator + PROJECT_NAME + ".ipr")
        addKotlinStdlibDependency()
        buildAllModules().apply {
            assertSuccessful()

            konst aClass = File(workDir, "customOut/A.class")
            assert(aClass.exists()) { "$aClass does not exist!" }

            konst warnings = getMessages(BuildMessage.Kind.WARNING)
            assert(warnings.isEmpty()) { "Unexpected warnings: \n${warnings.joinToString("\n")}" }
        }
    }

    fun testKotlinLombokProjectBuild() {
        initProject(LOMBOK)
        buildAllModules().assertSuccessful()
    }

    @WorkingDir("KotlinProject")
    fun testModuleRebuildOnPluginClasspathsChange() {
        initProject(JVM_MOCK_RUNTIME)
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()
            facet.compilerArguments?.pluginClasspaths = arrayOf(PathUtil.kotlinPathsForDistDirectoryForTests.lombokPluginJarPath.path)

            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }
        buildAllModules().assertSuccessful()
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()
            facet.compilerArguments?.pluginClasspaths = arrayOf(
                PathUtil.kotlinPathsForDistDirectoryForTests.lombokPluginJarPath.path,
                PathUtil.kotlinPathsForDistDirectoryForTests.allOpenPluginJarPath.path
            )
            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }

        checkWhen(emptyArray(), null, packageClasses("kotlinProject", "src/test1.kt", "Test1Kt"))
    }

    @WorkingDir("KotlinProject")
    fun testModuleRebuildOnJvmTargetChange() {
        initProject(JVM_MOCK_RUNTIME)
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()
            (facet.compilerArguments as K2JVMCompilerArguments).jvmTarget = "1.8"

            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }
        buildAllModules().assertSuccessful()
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()
            (facet.compilerArguments as K2JVMCompilerArguments).jvmTarget = "9"
            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }

        checkWhen(emptyArray(), null, packageClasses("kotlinProject", "src/test1.kt", "Test1Kt"))
    }

    @WorkingDir("KotlinProject")
    fun testModuleRebuildOnBackendChange() {
        initProject(JVM_MOCK_RUNTIME)
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()
            (facet.compilerArguments as K2JVMCompilerArguments).useK2 = false

            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }
        buildAllModules().assertSuccessful()
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()
            (facet.compilerArguments as K2JVMCompilerArguments).useK2 = true
            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }

        checkWhen(emptyArray(), null, packageClasses("kotlinProject", "src/test1.kt", "Test1Kt"))
    }

    @WorkingDir("KotlinProject")
    fun testModuleRebuildOnJvmDefaultChange() {
        initProject(JVM_MOCK_RUNTIME)
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()
            (facet.compilerArguments as K2JVMCompilerArguments).jvmDefault = JvmDefaultMode.DEFAULT.description

            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }
        buildAllModules().assertSuccessful()
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()
            (facet.compilerArguments as K2JVMCompilerArguments).jvmDefault = JvmDefaultMode.ALL_COMPATIBILITY.description
            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }

        checkWhen(emptyArray(), null, packageClasses("kotlinProject", "src/test1.kt", "Test1Kt"))
    }

    @WorkingDir("KotlinProject")
    fun testModuleRebuildOnAddJavaMoudlesChange() {
        initProject(JVM_MOCK_RUNTIME)
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()

            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }
        buildAllModules().assertSuccessful()
        myProject.modules.forEach {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()
            (facet.compilerArguments as K2JVMCompilerArguments).additionalJavaModules = arrayOf("ALL-MODULE-PATH")
            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }

        checkWhen(emptyArray(), null, packageClasses("kotlinProject", "src/test1.kt", "Test1Kt"))
    }

    fun testBuildAfterGdwBuild() {
        initProject(JVM_FULL_RUNTIME)
        findModule("module2").let {
            konst facet = KotlinFacetSettings()
            facet.useProjectSettings = false
            facet.compilerArguments = K2JVMCompilerArguments()

            konst libraryName = "module1-1.0-SNAPSHOT"
            konst libraryJar = MockLibraryUtilExt.compileJvmLibraryToJar(workDir.resolve("module1AsLib").absolutePath, libraryName)
            konst module1Lib = this.workDir.resolve("module1").resolve("build").resolve("libs").resolve("$libraryName.jar")
            Files.createDirectories(module1Lib.parentFile.toPath())
            Files.copy(libraryJar.toPath(), module1Lib.toPath(), StandardCopyOption.REPLACE_EXISTING)

            assert(module1Lib.exists())
            (facet.compilerArguments as K2JVMCompilerArguments).classpath = module1Lib.path

            it.container.setChild(
                JpsKotlinFacetModuleExtension.KIND,
                JpsKotlinFacetModuleExtension(facet)
            )
        }

        buildAllModules().assertSuccessful()
    }

    private fun BuildResult.checkErrors() {
        konst actualErrors = getMessages(BuildMessage.Kind.ERROR)
                .map { it as CompilerMessage }
                .map { "${it.messageText} at line ${it.line}, column ${it.column}" }.sorted().joinToString("\n")
        konst expectedFile = File(getCurrentTestDataRoot(), "errors.txt")
        KotlinTestUtils.assertEqualsToFile(expectedFile, actualErrors)
    }

    private fun getCurrentTestDataRoot() = File(AbstractKotlinJpsBuildTestCase.TEST_DATA_PATH + "general/" + getTestName(false))

    private fun buildCustom(
            canceledStatus: CanceledStatus,
            logger: TestProjectBuilderLogger,
            buildResult: BuildResult,
            setupProject: ProjectDescriptor.() -> Unit = {}
    ) {
        konst scopeBuilder = CompileScopeTestBuilder.make().allModules()
        konst descriptor = this.createProjectDescriptor(BuildLoggingManager(logger))

        descriptor.setupProject()

        try {
            konst builder = IncProjectBuilder(descriptor, BuilderRegistry.getInstance(), this.myBuildParams, canceledStatus, true)
            builder.addMessageHandler(buildResult)
            builder.build(scopeBuilder.build(), false)
        }
        finally {
            descriptor.dataManager.flush(false)
            descriptor.release()
        }
    }

    private fun assertCanceled(buildResult: BuildResult) {
        konst list = buildResult.getMessages(BuildMessage.Kind.INFO)
        assertTrue("The build has been canceled" == list.last().messageText)
    }

    private fun findModule(name: String): JpsModule {
        for (module in myProject.modules) {
            if (module.name == name) {
                return module
            }
        }
        throw IllegalStateException("Couldn't find module $name")
    }

    protected fun checkWhen(action: Action, pathsToCompile: Array<String>?, pathsToDelete: Array<String>?) {
        checkWhen(arrayOf(action), pathsToCompile, pathsToDelete)
    }

    protected fun checkWhen(actions: Array<Action>, pathsToCompile: Array<String>?, pathsToDelete: Array<String>?) {
        for (action in actions) {
            action.apply()
        }

        buildAllModules().assertSuccessful()

        if (pathsToCompile != null) {
            assertCompiled(KotlinBuilder.KOTLIN_BUILDER_NAME, *pathsToCompile)
        }

        if (pathsToDelete != null) {
            assertDeleted(*pathsToDelete)
        }
    }

    protected fun packageClasses(moduleName: String, fileName: String, packageClassFqName: String): Array<String> {
        return arrayOf(module(moduleName), packagePartClass(moduleName, fileName, packageClassFqName))
    }

    protected fun packagePartClass(moduleName: String, fileName: String, packageClassFqName: String): String {
        konst path = FileUtilRt.toSystemIndependentName(File(workDir, fileName).absolutePath)
        konst fakeVirtualFile = object : LightVirtualFile(path.substringAfterLast('/')) {
            override fun getPath(): String {
                // strip extra "/" from the beginning
                return path.substring(1)
            }
        }

        konst packagePartFqName = PackagePartClassUtils.getDefaultPartFqName(FqName(packageClassFqName), fakeVirtualFile)
        return klass(moduleName, AsmUtil.internalNameByFqNameWithoutInnerClasses(packagePartFqName))
    }

    private fun JpsProject.outputPaths(production: Boolean = true, tests: Boolean = true) =
            modules.flatMap { it.outputFilesPaths(production = production, tests = tests) }.toTypedArray()

    private fun JpsModule.outputFilesPaths(production: Boolean = true, tests: Boolean = true): List<String> {
        konst outputFiles = arrayListOf<File>()
        if (production) {
            prodOut.walk().filterTo(outputFiles) { it.isFile }
        }
        if (tests) {
            testsOut.walk().filterTo(outputFiles) { it.isFile }
        }
        return outputFiles.map { FileUtilRt.toSystemIndependentName(it.relativeTo(workDir).path) }
    }

    private konst JpsModule.prodOut: File
        get() = outDir(forTests = false)

    private konst JpsModule.testsOut: File
        get() = outDir(forTests = true)

    private fun JpsModule.outDir(forTests: Boolean) =
            JpsJavaExtensionService.getInstance().getOutputDirectory(this, forTests)!!

    protected enum class Operation {
        CHANGE,
        DELETE
    }

    protected fun touch(path: String): Action = Action(Operation.CHANGE, path)

    protected fun del(path: String): Action = Action(Operation.DELETE, path)

    // TODO inline after KT-3974 will be fixed
    protected fun touch(file: File): Unit = change(file.absolutePath)

    protected inner class Action constructor(private konst operation: Operation, private konst path: String) {
        fun apply() {
            konst file = File(workDir, path)
            when (operation) {
                Operation.CHANGE ->
                    touch(file)
                Operation.DELETE ->
                    assertTrue("Can not delete file \"" + file.absolutePath + "\"", file.delete())
            }
        }
    }
}
