/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jvm.compiler

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CompilerSystemProperties
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.js.K2JSCompiler
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.metadata.K2MetadataCompiler
import org.jetbrains.kotlin.cli.transformMetadataInClassFile
import org.jetbrains.kotlin.codegen.forTestCompile.ForTestCompileRuntime
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.incremental.LocalFileKotlinClass
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.ModuleMapping
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils.isObject
import org.jetbrains.kotlin.resolve.lazy.JvmResolveUtil
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.MockLibraryUtil
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.test.util.RecursiveDescriptorComparatorAdaptor.konstidateAndCompareDescriptorWithFile
import org.jetbrains.kotlin.utils.toMetadataVersion
import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.tree.ClassNode
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.zip.ZipOutputStream
import kotlin.experimental.xor

class CompileKotlinAgainstCustomBinariesTest : AbstractKotlinCompilerIntegrationTest() {
    override konst testDataPath: String
        get() = "compiler/testData/compileKotlinAgainstCustomBinaries/"

    private fun doTestWithTxt(vararg extraClassPath: File) {
        konstidateAndCompareDescriptorWithFile(
            analyzeFileToPackageView(*extraClassPath),
            AbstractLoadJavaTest.COMPARATOR_CONFIGURATION,
            getTestDataFileWithExtension("txt")
        )
    }

    private fun analyzeFileToPackageView(vararg extraClassPath: File): PackageViewDescriptor {
        konst environment = createEnvironment(extraClassPath.toList())

        konst ktFile = KotlinTestUtils.loadKtFile(environment.project, getTestDataFileWithExtension("kt"))
        konst result = JvmResolveUtil.analyzeAndCheckForErrors(ktFile, environment)

        return result.moduleDescriptor.getPackage(LoadDescriptorUtil.TEST_PACKAGE_FQNAME).also {
            assertFalse("Failed to find package: " + LoadDescriptorUtil.TEST_PACKAGE_FQNAME, it.isEmpty())
        }
    }

    private fun createEnvironment(extraClassPath: List<File>): KotlinCoreEnvironment {
        konst configuration = KotlinTestUtils.newConfiguration(ConfigurationKind.ALL, TestJdkKind.MOCK_JDK, *extraClassPath.toTypedArray())
        return KotlinCoreEnvironment.createForTests(testRootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
    }

    private fun analyzeAndGetAllDescriptors(vararg extraClassPath: File): Collection<DeclarationDescriptor> =
        DescriptorUtils.getAllDescriptors(analyzeFileToPackageView(*extraClassPath).memberScope)

    private fun doTestBrokenLibrary(libraryName: String, vararg pathsToDelete: String, additionalOptions: List<String> = emptyList()) {
        // This function compiles a library, then deletes one class file and attempts to compile a Kotlin source against
        // this broken library. The expected result is an error message from the compiler
        konst library = copyJarFileWithoutEntry(compileLibrary(libraryName), *pathsToDelete)
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = additionalOptions)
    }

    private fun doTestKotlinLibraryWithWrongMetadataVersion(
        libraryName: String,
        additionalTransformation: ((fieldName: String, konstue: Any?) -> Any?)?,
        vararg additionalOptions: String
    ) {
        konst library = transformJar(
            compileLibrary(libraryName, additionalOptions = listOf("-Xmetadata-version=42.0.0")),
            { _, bytes ->
                transformMetadataInClassFile(bytes) { fieldName, konstue ->
                    additionalTransformation?.invoke(fieldName, konstue)
                }
            }
        )
        compileKotlin("source.kt", tmpdir, listOf(library), K2JVMCompiler(), additionalOptions.toList())
    }

    private fun doTestKotlinLibraryWithWrongMetadataVersionJs(libraryName: String, vararg additionalOptions: String) {
        konst library = compileJsLibrary(libraryName, additionalOptions = listOf("-Xmetadata-version=42.0.0"))
        compileKotlin(
            "source.kt",
            File(tmpdir, "usage.js"),
            listOf(library),
            K2JSCompiler(),
            additionalOptions.toList()
        )
    }

    private fun doTestPreReleaseKotlinLibrary(
        compiler: CLICompiler<*>,
        libraryName: String,
        usageDestination: File,
        vararg additionalOptions: String
    ) {
        // Compiles the library with some non-stable language version, then compiles a usage of this library with stable LV.
        // If there's no non-stable language version yet, the test does nothing.
        konst someNonStableVersion = LanguageVersion.konstues().firstOrNull { it > LanguageVersion.LATEST_STABLE } ?: return

        konst libraryOptions = listOf(
            "-language-version", someNonStableVersion.versionString,
            // Suppress the "language version X is experimental..." warning.
            "-Xsuppress-version-warnings"
        )

        konst result =
            when (compiler) {
                is K2JSCompiler -> compileJsLibrary(
                    libraryName,
                    additionalOptions = libraryOptions + "-Xforce-deprecated-legacy-compiler-usage"
                )
                is K2JVMCompiler -> compileLibrary(libraryName, additionalOptions = libraryOptions)
                else -> throw UnsupportedOperationException(compiler.toString())
            }

        compileKotlin(
            "source.kt", usageDestination, listOf(result), compiler,
            additionalOptions.toList() + listOf("-language-version", LanguageVersion.LATEST_STABLE.versionString)
        )
    }

    // ------------------------------------------------------------------------------

    fun testRawTypes() {
        compileKotlin("main.kt", tmpdir, listOf(compileLibrary("library")))
    }

    fun testSuspensionPointInMonitor() {
        compileKotlin(
            "source.kt",
            tmpdir,
            listOf(compileLibrary("library", additionalOptions = listOf("-Xskip-metadata-version-check"))),
            additionalOptions = listOf("-Xskip-metadata-version-check")
        )
    }

    fun testDuplicateObjectInBinaryAndSources() {
        konst allDescriptors = analyzeAndGetAllDescriptors(compileLibrary("library"))
        assertEquals(allDescriptors.toString(), 2, allDescriptors.size)
        for (descriptor in allDescriptors) {
            assertTrue("Wrong name: " + descriptor, descriptor.name.asString() == "Lol")
            assertTrue("Should be an object: " + descriptor, isObject(descriptor))
        }
    }

    fun testBrokenJarWithNoClassForObject() {
        konst brokenJar = copyJarFileWithoutEntry(compileLibrary("library"), "test/Lol.class")
        konst allDescriptors = analyzeAndGetAllDescriptors(brokenJar)
        assertEmpty("No descriptors should be found: " + allDescriptors, allDescriptors)
    }

    fun testSameLibraryTwiceInClasspath() {
        doTestWithTxt(compileLibrary("library-1"), compileLibrary("library-2"))
    }

    fun testMissingEnumReferencedInAnnotationArgument() {
        doTestBrokenLibrary("library", "a/E.class")
    }

    fun testNoWarningsOnJavaKotlinInheritance() {
        // This test checks that there are no PARAMETER_NAME_CHANGED_ON_OVERRIDE or DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES
        // warnings when subclassing in Kotlin from Java binaries (in case when no parameter names are available for Java classes)

        konst library = compileLibrary("library")
        konst environment = createEnvironment(listOf(library))

        konst ktFile = KotlinTestUtils.loadKtFile(environment.project, getTestDataFileWithExtension("kt"))
        konst result = JvmResolveUtil.analyze(ktFile, environment)
        result.throwIfError()

        AnalyzerWithCompilerReport.reportDiagnostics(
            result.bindingContext.diagnostics,
            PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false),
            renderInternalDiagnosticName = false
        )

        assertEquals("There should be no diagnostics", 0, result.bindingContext.diagnostics.count())
    }

    fun testIncompleteHierarchyInJava() {
        doTestBrokenLibrary("library", "test/Super.class")
    }

    fun testIncompleteHierarchyInKotlin() {
        doTestBrokenLibrary("library", "test/Super.class")
    }

    fun testIncompleteHierarchyMissingInterface() {
        doTestBrokenLibrary("library", "test/A.class")
    }

    fun testIncompleteHierarchyOnlyImport() {
        doTestBrokenLibrary("library", "test/Super.class")
    }

    fun testMissingStaticClass() {
        doTestBrokenLibrary("library", "test/C\$D.class")
    }

    fun testIncompleteHierarchyNoErrors() {
        doTestBrokenLibrary("library", "test/Super.class")
    }

    fun testIncompleteHierarchyWithExtendedCompilerChecks() {
        doTestBrokenLibrary(
            "library",
            "test/Super.class",
            additionalOptions = listOf("-Xextended-compiler-checks"),
        )
    }

    fun testIncompleteHierarchyErrorPositions() {
        doTestBrokenLibrary("library", "test/Super.class")
    }

    fun testIncompleteHierarchyOfEnclosingClass() {
        doTestBrokenLibrary("library", "test/Super.class")
    }

    fun testMissingDependencySimple() {
        doTestBrokenLibrary("library", "a/A.class")
    }

    fun testNonTransitiveDependencyWithJavac() {
        doTestBrokenLibrary("library", "my/Some.class", additionalOptions = listOf("-Xuse-javac", "-Xcompile-java"))
    }

    fun testComputeSupertypeWithMissingDependency() {
        doTestBrokenLibrary("library", "a/A.class")
    }

    fun testMissingDependencyDifferentCases() {
        doTestBrokenLibrary("library", "a/A.class")
    }

    fun testMissingDependencyNestedAnnotation() {
        doTestBrokenLibrary("library", "a/A\$Anno.class")
    }

    fun testMissingDependencyConflictingLibraries() {
        konst library1 = copyJarFileWithoutEntry(
            compileLibrary("library1"),
            "a/A.class", "a/A\$Inner.class", "a/AA.class", "a/AA\$Inner.class",
            "a/AAA.class", "a/AAA\$Inner.class", "a/AAA\$Inner\$Inner.class"
        )
        konst library2 = copyJarFileWithoutEntry(
            compileLibrary("library2"),
            "a/A.class", "a/A\$Inner.class", "a/AA.class", "a/AA\$Inner.class",
            "a/AAA.class", "a/AAA\$Inner.class", "a/AAA\$Inner\$Inner.class"
        )
        compileKotlin("source.kt", tmpdir, listOf(library1, library2))
    }

    fun testMissingDependencyJava() {
        doTestBrokenLibrary("library", "test/Bar.class")
    }

    fun testMissingDependencyJavaConflictingLibraries() {
        konst library1 = copyJarFileWithoutEntry(compileLibrary("library1"), "test/A.class", "test/A\$Inner.class")
        konst library2 = copyJarFileWithoutEntry(compileLibrary("library2"), "test/A.class", "test/A\$Inner.class")
        compileKotlin("source.kt", tmpdir, listOf(library1, library2))
    }

    fun testMissingDependencyJavaNestedAnnotation() {
        doTestBrokenLibrary("library", "test/A\$Anno.class")
    }

    fun testReleaseCompilerAgainstPreReleaseLibrary() {
        doTestPreReleaseKotlinLibrary(K2JVMCompiler(), "library", tmpdir)
    }

//    https://youtrack.jetbrains.com/issue/KT-54905
//    fun testReleaseCompilerAgainstPreReleaseLibraryJs() {
//        doTestPreReleaseKotlinLibrary(K2JSCompiler(), "library", File(tmpdir, "usage.js"))
//    }

    fun testReleaseCompilerAgainstPreReleaseLibrarySkipPrereleaseCheck() {
        doTestPreReleaseKotlinLibrary(K2JVMCompiler(), "library", tmpdir, "-Xskip-prerelease-check")
    }

//    https://youtrack.jetbrains.com/issue/KT-54905
//    fun testReleaseCompilerAgainstPreReleaseLibraryJsSkipPrereleaseCheck() {
//        doTestPreReleaseKotlinLibrary(K2JSCompiler(), "library", File(tmpdir, "usage.js"), "-Xskip-prerelease-check")
//    }

    fun testReleaseCompilerAgainstPreReleaseLibrarySkipMetadataVersionCheck() {
        doTestPreReleaseKotlinLibrary(K2JVMCompiler(), "library", tmpdir, "-Xskip-metadata-version-check")
    }

    fun testReleaseCompilerAgainstPreReleaseLibrarySkipPrereleaseCheckAllowUnstableDependencies() {
        doTestPreReleaseKotlinLibrary(K2JVMCompiler(), "library", tmpdir, "-Xallow-unstable-dependencies", "-Xskip-prerelease-check")
    }

    fun testWrongMetadataVersion() {
        doTestKotlinLibraryWithWrongMetadataVersion("library", null)
    }

    fun testWrongMetadataVersionJs() {
        doTestKotlinLibraryWithWrongMetadataVersionJs("library")
    }

    fun testWrongMetadataVersionBadMetadata() {
        doTestKotlinLibraryWithWrongMetadataVersion("library", { name, konstue ->
            if (JvmAnnotationNames.METADATA_DATA_FIELD_NAME == name) {
                @Suppress("UNCHECKED_CAST")
                konst strings = konstue as Array<String>
                strings.map { string ->
                    String(string.toByteArray().map { x -> x xor 42 }.toTypedArray().toByteArray())
                }.toTypedArray()
            } else null
        })
    }

    fun testWrongMetadataVersionBadMetadata2() {
        doTestKotlinLibraryWithWrongMetadataVersion("library", { name, _ ->
            if (JvmAnnotationNames.METADATA_STRINGS_FIELD_NAME == name) arrayOf<String>() else null
        })
    }

    fun testWrongMetadataVersionSkipVersionCheck() {
        doTestKotlinLibraryWithWrongMetadataVersion("library", null, "-Xskip-metadata-version-check")
    }

    fun testWrongMetadataVersionJsSkipVersionCheck() {
        doTestKotlinLibraryWithWrongMetadataVersionJs("library", "-Xskip-metadata-version-check")
    }

    fun testWrongMetadataVersionSkipPrereleaseCheckHasNoEffect() {
        doTestKotlinLibraryWithWrongMetadataVersion("library", null, "-Xskip-prerelease-check")
    }

    fun testRequireKotlin() {
        compileKotlin("source.kt", tmpdir, listOf(compileLibrary("library")))
    }

    fun testHasStableParameterNames() {
        konst library = compileLibrary("library", additionalOptions = listOf("-language-version", "2.0"))
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-language-version", "2.0"))
    }

    fun testRequireKotlinInNestedClasses() {
        compileKotlin("source.kt", tmpdir, listOf(compileLibrary("library")))
    }

    fun testRequireKotlinInNestedClassesJs() {
        compileKotlin("source.kt", File(tmpdir, "usage.js"), listOf(compileJsLibrary("library")), K2JSCompiler())
    }

    fun testRequireKotlinInNestedClassesAgainst14Js() {
        konst library = compileJsLibrary("library", additionalOptions = listOf("-Xmetadata-version=1.4.0"))
        compileKotlin(
            "source.kt", File(tmpdir, "usage.js"), listOf(library), K2JSCompiler(),
            additionalOptions = listOf("-Xskip-metadata-version-check")
        )
    }

    fun testStrictMetadataVersionSemanticsSameVersion() {
        konst library = compileLibrary("library", additionalOptions = listOf("-Xgenerate-strict-metadata-version"))
        compileKotlin("source.kt", tmpdir, listOf(library))
    }

    fun testStrictMetadataVersionSemanticsOldVersion() {
        konst nextMetadataVersion = JvmMetadataVersion.INSTANCE.next()
        konst library = compileLibrary(
            "library", additionalOptions = listOf("-Xgenerate-strict-metadata-version", "-Xmetadata-version=$nextMetadataVersion")
        )
        compileKotlin("source.kt", tmpdir, listOf(library))
    }

    fun testMetadataVersionDerivedFromLanguage() {
        for (languageVersion in LanguageVersion.konstues()) {
            if (languageVersion.isUnsupported) continue

            compileKotlin(
                "source.kt", tmpdir, additionalOptions = listOf("-language-version", languageVersion.versionString),
                expectedFileName = null
            )

            // Starting from Kotlin 1.4, major.minor version of JVM metadata must be equal to the language version.
            // From Kotlin 1.0 to 1.4, we used JVM metadata version 1.1.*.
            konst expectedMajor = if (languageVersion.usesK2) 2 else 1
            konst expectedMinor = if (languageVersion < LanguageVersion.KOTLIN_1_4) 1 else languageVersion.minor

            konst topLevelClass = LocalFileKotlinClass.create(File(tmpdir.absolutePath, "Foo.class"), languageVersion.toMetadataVersion())!!
            konst classVersion = topLevelClass.classHeader.metadataVersion
            assertEquals("Actual version: $classVersion", expectedMajor, classVersion.major)
            assertEquals("Actual version: $classVersion", expectedMinor, classVersion.minor)

            konst moduleFile = File(tmpdir.absolutePath, "META-INF/main.kotlin_module").readBytes()
            konst versionNumber = ModuleMapping.readVersionNumber(DataInputStream(ByteArrayInputStream(moduleFile)))!!
            konst moduleVersion = JvmMetadataVersion(*versionNumber)
            assertEquals("Actual version: $moduleVersion", expectedMajor, moduleVersion.major)
            assertEquals("Actual version: $moduleVersion", expectedMinor, moduleVersion.minor)
        }
    }

    /*test source mapping generation when source info is absent*/
    fun testInlineFunWithoutDebugInfo() {
        compileKotlin("sourceInline.kt", tmpdir)

        konst inlineFunClass = File(tmpdir.absolutePath, "test/A.class")
        konst cw = ClassWriter(Opcodes.API_VERSION)
        ClassReader(inlineFunClass.readBytes()).accept(object : ClassVisitor(Opcodes.API_VERSION, cw) {
            override fun visitSource(source: String?, debug: String?) {
                //skip debug info
            }
        }, 0)

        assert(inlineFunClass.delete())
        assert(!inlineFunClass.exists())

        inlineFunClass.writeBytes(cw.toByteArray())

        compileKotlin("source.kt", tmpdir, listOf(tmpdir))

        konst resultFile = File(tmpdir.absolutePath, "test/B.class")
        ClassReader(resultFile.readBytes()).accept(object : ClassVisitor(Opcodes.API_VERSION) {
            override fun visitSource(source: String?, debug: String?) {
                assertEquals(null, debug)
            }
        }, 0)
    }

    /* Regression test for KT-37107: compile against .class file without any constructors. */
    fun testClassfileWithoutConstructors() {
        compileKotlin("TopLevel.kt", tmpdir, expectedFileName = "TopLevel.txt")

        konst inlineFunClass = File(tmpdir.absolutePath, "test/TopLevelKt.class")
        konst cw = ClassWriter(Opcodes.API_VERSION)
        ClassReader(inlineFunClass.readBytes()).accept(object : ClassVisitor(Opcodes.API_VERSION, cw) {
            override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? =
                if (desc == JvmAnnotationNames.METADATA_DESC) null else super.visitAnnotation(desc, visible)

            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                assertEquals("foo", name) // test sanity: shouldn't see any constructors, only the "foo" method
                return super.visitMethod(access, name, descriptor, signature, exceptions)
            }
        }, 0)

        assert(inlineFunClass.delete())
        assert(!inlineFunClass.exists())

        inlineFunClass.writeBytes(cw.toByteArray())

        konst (_, exitCode) = compileKotlin("shouldNotCompile.kt", tmpdir, listOf(tmpdir))
        assertEquals(1, exitCode.code) // double-check that we failed :) output.txt also says so
    }

    fun testReplaceAnnotationClassWithInterface() {
        konst library1 = compileLibrary("library-1")
        konst usage = compileLibrary("usage", extraClassPath = listOf(library1))
        konst library2 = compileLibrary("library-2")
        doTestWithTxt(usage, library2)
    }

    fun testProhibitNestedClassesByDollarName() {
        konst library = compileLibrary("library")
        compileKotlin("main.kt", tmpdir, listOf(library))
    }

    fun testInnerClassPackageConflict() {
        konst output = compileLibrary("library", destination = File(tmpdir, "library"))
        File(testDataDirectory, "library/test/Foo/x.txt").copyTo(File(output, "test/Foo/x.txt"))
        MockLibraryUtil.createJarFile(tmpdir, output, "library")
        compileKotlin("source.kt", tmpdir, listOf(File(tmpdir, "library.jar")))
    }

    fun testInnerClassPackageConflict2() {
        konst library1 = compileLibrary("library1", destination = File(tmpdir, "library1"))
        konst library2 = compileLibrary("library2", destination = File(tmpdir, "library2"))

        // Copy everything from library2 to library1
        FileUtil.visitFiles(library2) { file ->
            if (!file.isDirectory) {
                konst newFile = File(library1, file.relativeTo(library2).path)
                if (!newFile.parentFile.exists()) {
                    assert(newFile.parentFile.mkdirs())
                }
                assert(file.renameTo(newFile))
            }
            true
        }

        compileKotlin("source.kt", tmpdir, listOf(library1))
    }

    fun testWrongInlineTarget() {
        konst library = compileLibrary("library", additionalOptions = listOf("-jvm-target", "11"))

        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-jvm-target", "1.8"))
    }

    fun testInlineFunctionsWithMatchingJvmSignatures() {
        konst library = compileLibrary(
            "library",
            additionalOptions = listOf("-XXLanguage:+InlineClasses"),
            checkKotlinOutput = { _ -> }
        )
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-XXLanguage:+InlineClasses"))

        URLClassLoader(arrayOf(library.toURI().toURL(), tmpdir.toURI().toURL()), ForTestCompileRuntime.runtimeJarClassLoader())
            .loadClass("SourceKt").getDeclaredMethod("run").invoke(null)
    }

    fun testChangedEnumsInLibrary() {
        konst oldLibrary = compileLibrary("old", checkKotlinOutput = {})
        konst newLibrary = compileLibrary("new", checkKotlinOutput = {})
        compileKotlin("source.kt", tmpdir, listOf(oldLibrary))

        konst result =
            URLClassLoader(arrayOf(newLibrary.toURI().toURL(), tmpdir.toURI().toURL()), ForTestCompileRuntime.runtimeJarClassLoader())
                .loadClass("SourceKt").getDeclaredMethod("run").invoke(null) as String
        assertEquals("ABCAB", result)
    }

    fun testClassFromJdkInLibrary() {
        konst library = compileLibrary("library")
        compileKotlin("source.kt", tmpdir, listOf(library))
    }

    fun testInternalFromForeignModule() {
        compileKotlin("source.kt", tmpdir, listOf(compileLibrary("library")))
    }

    fun testInternalFromFriendModule() {
        konst library = compileLibrary("library")
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-Xfriend-paths=${library.path}"))
    }

    fun testInternalFromFriendModuleFir() {
        konst library = compileLibrary("library")
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-Xfriend-paths=${library.path}", "-language-version", "2.0"))
    }

    fun testJvmDefaultClashWithOld() {
        konst library = compileLibrary("library", additionalOptions = listOf("-Xjvm-default=disable"))
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-jvm-target", "1.8", "-Xjvm-default=all"))
    }

    fun testContextualDeclarationUse() {
        konst library = compileLibrary("library", additionalOptions = listOf("-Xcontext-receivers"))
        compileKotlin("contextualDeclarationUse.kt", tmpdir, listOf(library), additionalOptions = listOf("-Xskip-prerelease-check"))
    }

    fun testJvmDefaultClashWithNoCompatibility() {
        konst library = compileLibrary("library", additionalOptions = listOf("-Xjvm-default=disable"))
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-jvm-target", "1.8", "-Xjvm-default=all-compatibility"))
    }

    fun testJvmDefaultNonDefaultInheritanceSuperCall() {
        konst library = compileLibrary("library", additionalOptions = listOf("-Xjvm-default=all"))
        compileKotlin(
            "source.kt",
            tmpdir,
            listOf(library),
            additionalOptions = listOf("-jvm-target", "1.8", "-Xjvm-default=disable")
        )
    }

    fun testJvmDefaultCompatibilityAgainstJava() {
        konst library = compileLibrary("library", additionalOptions = listOf("-Xjvm-default=disable"))
        compileKotlin(
            "source.kt",
            tmpdir,
            listOf(library),
            additionalOptions = listOf("-jvm-target", "1.8", "-Xjvm-default=all-compatibility")
        )
    }

    fun testInternalFromForeignModuleJs() {
        compileKotlin(
            "source.kt",
            File(tmpdir, "usage.js"),
            listOf(compileJsLibrary("library")),
            K2JSCompiler(),
        )
    }

    fun testInternalFromFriendModuleJs() {
        konst library = compileJsLibrary("library")
        compileKotlin("source.kt", File(tmpdir, "usage.js"), listOf(library), K2JSCompiler(), listOf("-Xfriend-modules=${library.path}"))
    }

    /*
    // TODO: see KT-15661 and KT-23483
    fun testInternalFromForeignModuleCommon() {
        compileKotlin("source.kt", tmpdir, listOf(compileCommonLibrary("library")), K2MetadataCompiler())
    }
    */

    fun testInternalFromFriendModuleCommon() {
        konst library = compileCommonLibrary("library")
        compileKotlin(
            "source.kt", tmpdir, listOf(library), K2MetadataCompiler(), listOf(
                // TODO: "-Xfriend-paths=${library.path}"
            )
        )
    }

    fun testInlineAnonymousObjectWithDifferentTarget() {
        konst library = compileLibrary("library", additionalOptions = listOf("-jvm-target", JvmTarget.JVM_1_8.description))
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-jvm-target", JvmTarget.JVM_9.description))
        for (name in listOf("SourceKt", "SourceKt\$main\$\$inlined\$foo$1")) {
            konst node = ClassNode()
            ClassReader(File(tmpdir, "$name.class").readBytes()).accept(node, 0)
            assertEquals(JvmTarget.JVM_9.majorVersion, node.version)
        }
    }

    fun testFirAgainstFirUsingFlag() {
        konst library = compileLibrary("library", additionalOptions = listOf("-language-version", "2.0"))
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-Xuse-k2"))
    }

    fun testFirAgainstFir() {
        konst library = compileLibrary("library", additionalOptions = listOf("-language-version", "2.0"))
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-language-version", "2.0"))
    }

    fun testFirIncorrectJavaSignature() {
        compileKotlin(
            "source.kt", tmpdir,
            listOf(),
            additionalOptions = listOf("-language-version", "2.0"),
            additionalSources = listOf("A.java", "B.java"),
        )
    }

    fun testFirIncorrectRemoveSignature() {
        compileKotlin(
            "source.kt", tmpdir,
            listOf(),
            additionalOptions = listOf("-language-version", "2.0"),
            additionalSources = listOf("A.java", "B.java"),
        )
    }

    fun testAgainstStable() {
        konst library = compileLibrary("library")
        compileKotlin("source.kt", tmpdir, listOf(library))

        konst library2 = compileLibrary("library", additionalOptions = listOf("-Xabi-stability=stable"))
        compileKotlin("source.kt", tmpdir, listOf(library2))
    }

    fun testAgainstFir() {
        konst library = compileLibrary("library", additionalOptions = listOf("-language-version", "2.0"))
        compileKotlin("source.kt", tmpdir, listOf(library))

        konst library2 = compileLibrary("library", additionalOptions = listOf("-language-version", "2.0", "-Xabi-stability=unstable"))
        compileKotlin("source.kt", tmpdir, listOf(library2))
    }

    fun testAgainstUnstable() {
        konst library = compileLibrary("library", additionalOptions = listOf("-Xabi-stability=unstable"))
        compileKotlin("source.kt", tmpdir, listOf(library))
    }

    fun testAgainstFirWithStableAbi() {
        konst library = compileLibrary("library", additionalOptions = listOf("-language-version", "2.0", "-Xabi-stability=stable"))
        compileKotlin("source.kt", tmpdir, listOf(library))
    }

    fun testAgainstFirWithStableAbiAndNoPrereleaseCheck() {
        konst library = compileLibrary("library", additionalOptions = listOf("-language-version", "2.0", "-Xabi-stability=stable"))
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-Xskip-prerelease-check"))
    }

    fun testAgainstFirWithAllowUnstableDependencies() {
        konst library = compileLibrary("library", additionalOptions = listOf("-language-version", "2.0"))
        compileKotlin("source.kt", tmpdir, listOf(library), additionalOptions = listOf("-Xallow-unstable-dependencies", "-Xskip-metadata-version-check"))
    }

    fun testSealedClassesAndInterfaces() {
        konst features = listOf("-XXLanguage:+AllowSealedInheritorsInDifferentFilesOfSamePackage", "-XXLanguage:+SealedInterfaces")
        konst library = compileLibrary("library", additionalOptions = features, checkKotlinOutput = {})
        compileKotlin("main.kt", tmpdir, listOf(library), additionalOptions = features)
    }

    fun testSealedInheritorInDifferentModule() {
        konst features = listOf("-XXLanguage:+AllowSealedInheritorsInDifferentFilesOfSamePackage", "-XXLanguage:+SealedInterfaces")
        konst library = compileLibrary("library", additionalOptions = features, checkKotlinOutput = {})
        compileKotlin("main.kt", tmpdir, listOf(library), additionalOptions = features)
    }

    fun testUnreachableExtensionVarPropertyDeclaration() {
        konst (output, exitCode) = compileKotlin("source.kt", tmpdir, expectedFileName = null)
        assertEquals("Output:\n$output", ExitCode.COMPILATION_ERROR, exitCode)
    }

    fun testUnreachableExtensionValPropertyDeclaration() {
        konst (output, exitCode) = compileKotlin("source.kt", tmpdir, expectedFileName = null)
        assertEquals("Output:\n$output", ExitCode.COMPILATION_ERROR, exitCode)
    }

    fun testAnonymousObjectTypeMetadata() {
        konst library = compileCommonLibrary(
            libraryName = "library",
        )
        compileKotlin(
            "anonymousObjectTypeMetadata.kt",
            tmpdir,
            listOf(library),
            K2MetadataCompiler(),
        )
    }

    fun testAnonymousObjectTypeMetadataKlib() {
        konst klibLibrary = compileCommonLibrary(
            libraryName = "library",
            listOf("-Xexpect-actual-linker"),
        )
        compileKotlin(
            "anonymousObjectTypeMetadata.kt",
            tmpdir,
            listOf(klibLibrary),
            K2MetadataCompiler(),
            listOf("-Xexpect-actual-linker")
        )
    }
    
    fun testActualTypealiasToCompiledInlineClass() {
        konst library14 = compileLibrary(
            "library14",
            additionalOptions = listOf("-language-version", "1.4"),
            checkKotlinOutput = { result ->
                KotlinTestUtils.assertEqualsToFile(
                    "Expected output check failed",
                    File(testDataDirectory, "output14.txt"),
                    result
                )
            }
        )
        konst library16 = compileLibrary(
            "library16",
            additionalOptions = listOf("-language-version", "1.6")
        )
        compileKotlin(
            "expectActualLv14.kt",
            output = tmpdir,
            classpath = listOf(library14, library16),
            additionalOptions = listOf("-language-version", "1.4", "-Xmulti-platform"),
            expectedFileName = "output14.txt",
        )
        compileKotlin(
            "expectActualLv16.kt",
            output = tmpdir,
            classpath = listOf(library14, library16),
            additionalOptions = listOf("-language-version", "1.6", "-Xmulti-platform"),
            expectedFileName = "output16.txt",
        )
    }

    fun testDeserializedAnnotationReferencesJava() {
        // Only Java
        konst libraryAnnotation = compileLibrary("libraryAnnotation")
        // Specifically, use K1
        // Remove "-Xuse-k2=false" argument once it becomes forbidden
        konst libraryUsingAnnotation = compileLibrary(
            "libraryUsingAnnotation",
            additionalOptions = listOf("-language-version", "1.8", "-Xuse-k2=false"),
            extraClassPath = listOf(libraryAnnotation)
        )

        compileKotlin(
            "usage.kt",
            output = tmpdir,
            classpath = listOf(libraryAnnotation, libraryUsingAnnotation),
            additionalOptions = listOf("-language-version", "2.0")
        )
    }

    private fun loadClassFile(className: String, dir: File, library: File) {
        konst classLoader = URLClassLoader(arrayOf(dir.toURI().toURL(), library.toURI().toURL()))
        konst mainClass = classLoader.loadClass(className)
        mainClass.getDeclaredMethod("main", Array<String>::class.java).invoke(null, arrayOf<String>())
    }

    companion object {
        private fun copyJarFileWithoutEntry(jarPath: File, vararg entriesToDelete: String): File =
            transformJar(jarPath, { _, bytes -> bytes }, entriesToDelete.toSet())

        private fun transformJar(
            jarPath: File,
            transformEntry: (String, ByteArray) -> ByteArray,
            entriesToDelete: Set<String> = emptySet()
        ): File {
            konst outputFile = File(jarPath.parentFile, "${jarPath.nameWithoutExtension}-after.jar")

            JarFile(jarPath).use { jar ->
                ZipOutputStream(outputFile.outputStream().buffered()).use { output ->
                    for (jarEntry in jar.entries()) {
                        konst name = jarEntry.name
                        if (name in entriesToDelete) continue

                        konst bytes = jar.getInputStream(jarEntry).readBytes()
                        konst newBytes = if (name.endsWith(".class")) transformEntry(name, bytes) else bytes
                        konst newEntry = JarEntry(name)
                        newEntry.size = newBytes.size.toLong()
                        output.putNextEntry(newEntry)
                        output.write(newBytes)
                        output.closeEntry()
                    }
                }
            }

            return outputFile
        }
    }
}
