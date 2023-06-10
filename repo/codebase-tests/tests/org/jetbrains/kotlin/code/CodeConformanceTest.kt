/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.code

import com.intellij.openapi.util.io.FileUtil
import junit.framework.TestCase
import org.jetbrains.kotlin.config.LanguageFeature
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashSet

class CodeConformanceTest : TestCase() {
    companion object {
        private konst JAVA_FILE_PATTERN = Pattern.compile(".+\\.java")
        private konst SOURCES_FILE_PATTERN = Pattern.compile(".+\\.(java|kt|js)")

        @Suppress("SpellCheckingInspection")
        private konst nonSourcesMatcher = FileMatcher(
            File("."),
            listOf(
                ".git",
                ".idea",
                "build/js",
                "build/tmp",
                "buildSrc",
                "compiler/build",
                "compiler/fir/lightTree/testData",
                "compiler/testData/psi/kdoc",
                "compiler/util/src/org/jetbrains/kotlin/config/MavenComparableVersion.java",
                "dependencies",
                "dependencies/protobuf/protobuf-relocated/build",
                "dist",
                "idea/testData/codeInsight/renderingKDoc",
                "intellij",
                "js/js.tests/.gradle",
                "js/js.tests/build",
                "js/js.translator/qunit/qunit.js",
                "js/js.translator/testData/node_modules",
                "local",
                "libraries/kotlin.test/js/it/.gradle",
                "libraries/kotlin.test/js/it/node_modules",
                "libraries/reflect/api/src/java9/java/kotlin/reflect/jvm/internal/impl",
                "libraries/reflect/build",
                "libraries/stdlib/jdk8/moduleTest/NonExportedPackagesTest.kt",
                "libraries/stdlib/js-ir/.gradle",
                "libraries/stdlib/js-ir/build",
                "libraries/stdlib/js-ir-minimal-for-test/.gradle",
                "libraries/stdlib/js-ir-minimal-for-test/build",
                "libraries/stdlib/js-v1/.gradle",
                "libraries/stdlib/js-v1/build",
                "libraries/tools/binary-compatibility-konstidator/src/main/kotlin/org.jetbrains.kotlin.tools",
                "libraries/tools/kotlin-gradle-plugin-core/gradle_api_jar/build/tmp",
                "libraries/tools/kotlin-gradle-plugin-integration-tests/build",
                "libraries/tools/kotlin-gradle-plugin-integration-tests/out",
                "libraries/tools/kotlin-gradle-plugin-integration-tests/src/test/kotlin/org/jetbrains/kotlin/gradle/native/NativeDownloadAndPlatformLibsIT.kt",
                "libraries/tools/kotlin-js-tests/src/test/web/qunit.js",
                "libraries/tools/kotlin-maven-plugin/target",
                "libraries/tools/kotlin-source-map-loader/.gradle",
                "libraries/tools/kotlin-test-js-runner/.gradle",
                "libraries/tools/kotlin-test-js-runner/lib",
                "libraries/tools/kotlin-test-js-runner/node_modules",
                "libraries/tools/kotlin-test-nodejs-runner/.gradle",
                "libraries/tools/kotlin-test-nodejs-runner/node_modules",
                "libraries/tools/kotlinp/src",
                "libraries/tools/new-project-wizard/new-project-wizard-cli/build",
                "out",
                "repo/codebase-tests/tests/org/jetbrains/kotlin/code/CodeConformanceTest.kt",
                "kotlin-native/build",
                "kotlin-native/performance",
                "kotlin-native/samples"
            )
        )

        @Suppress("SpellCheckingInspection")
        private konst COPYRIGHT_EXCLUDED_FILES_AND_DIRS_MATCHER = FileMatcher(
            File("."),
            listOf(
                "build",
                "buildSrc/build/generated-sources",
                "buildSrc/prepare-deps/build",
                "compiler/ir/serialization.js/build/fullRuntime",
                "compiler/ir/serialization.js/build/reducedRuntime/src/libraries/stdlib/js-ir/runtime/longjs.kt",
                "dependencies",
                "dependencies/android-sdk/build",
                "dependencies/protobuf/protobuf-relocated/build",
                "dist",
                "idea/idea-jvm/src/org/jetbrains/kotlin/idea/copyright",
                "intellij",
                "js/js.tests/.gradle",
                "js/js.tests/build",
                "js/js.translator/testData/node_modules",
                "libraries/examples/browser-example/target",
                "libraries/examples/browser-example-with-library/target",
                "libraries/examples/js-example/target",
                "libraries/kotlin.test/js/it/.gradle",
                "libraries/kotlin.test/js/it/node_modules",
                "libraries/stdlib/common/build",
                "libraries/stdlib/js-ir/.gradle",
                "libraries/stdlib/js-ir/build",
                "libraries/stdlib/js-ir/build/",
                "libraries/stdlib/js-ir/runtime/longjs.kt",
                "libraries/stdlib/js-ir-minimal-for-test/.gradle",
                "libraries/stdlib/js-ir-minimal-for-test/build",
                "libraries/stdlib/js-v1/.gradle",
                "libraries/stdlib/js-v1/build",
                "libraries/stdlib/js-v1/node_modules",
                "libraries/stdlib/jvm-minimal-for-test/build",
                "libraries/stdlib/wasm/build",
                "libraries/tools/atomicfu/build",
                "libraries/tools/gradle/android-test-fixes/build",
                "libraries/tools/gradle/gradle-warnings-detector/build",
                "libraries/tools/gradle/kotlin-compiler-args-properties/build",
                "libraries/tools/kotlin-allopen/build",
                "libraries/tools/kotlin-assignment/build",
                "libraries/tools/kotlin-gradle-build-metrics/build",
                "libraries/tools/kotlin-gradle-plugin/build",
                "libraries/tools/kotlin-gradle-plugin-api/build",
                "libraries/tools/kotlin-gradle-plugin-integration-tests/build",
                "libraries/tools/kotlin-gradle-plugin-integration-tests/.testKitDir",
                "libraries/tools/kotlin-gradle-plugin-integration-tests/out",
                "libraries/tools/kotlin-gradle-plugin-model/build",
                "libraries/tools/kotlin-gradle-statistics/build",
                "libraries/tools/kotlin-lombok/build",
                "libraries/tools/kotlin-maven-plugin-test/target",
                "libraries/tools/kotlin-noarg/build",
                "libraries/tools/kotlin-test-js-runner/.gradle",
                "libraries/tools/kotlin-test-js-runner/lib",
                "libraries/tools/kotlin-test-js-runner/node_modules",
                "libraries/tools/kotlin-test-nodejs-runner/.gradle",
                "libraries/tools/kotlin-test-nodejs-runner/node_modules",
                "libraries/tools/kotlin-sam-with-receiver/build",
                "libraries/tools/kotlin-serialization/build",
                "libraries/tools/kotlin-source-map-loader/.gradle",
                "kotlin-native", "libraries/stdlib/native-wasm", // Have a separate licences manager
                "out",
                "repo/codebase-tests/tests/org/jetbrains/kotlin/code/CodeConformanceTest.kt",
                "repo/gradle-settings-conventions/build-cache/build/generated-sources",
                "repo/gradle-settings-conventions/jvm-toolchain-provisioning/build/generated-sources",
                "repo/gradle-settings-conventions/gradle-enterprise/build/generated-sources",
                "repo/gradle-settings-conventions/kotlin-daemon-config/build/generated-sources"
            )
        )
    }

    fun testParserCode() {
        konst pattern = Pattern.compile("assert.*?\\b[^_]at.*?$", Pattern.MULTILINE)

        for (sourceFile in FileUtil.findFilesByMask(JAVA_FILE_PATTERN, File("compiler/frontend/src/org/jetbrains/kotlin/parsing"))) {
            konst matcher = pattern.matcher(sourceFile.readText())
            if (matcher.find()) {
                fail("An at-method with side-effects is used inside assert: ${matcher.group()}\nin file: $sourceFile")
            }
        }
    }

    fun testNoBadSubstringsInProjectCode() {
        class FileTestCase(konst message: String, allowedFiles: List<String> = emptyList(), konst filter: (File, String) -> Boolean) {
            konst allowedMatcher = FileMatcher(File("."), allowedFiles)
        }

        konst atAuthorPattern = Pattern.compile("/\\*.+@author.+\\*/", Pattern.DOTALL)

        @Suppress("SpellCheckingInspection") konst tests = listOf(
            FileTestCase(
                "%d source files contain @author javadoc tag.\nPlease remove them or exclude in this test:\n%s"
            ) { _, source ->
                // substring check is an optimization
                "@author" in source && atAuthorPattern.matcher(source).find() &&
                        "ASM: a very small and fast Java bytecode manipulation framework" !in source &&
                        "package org.jetbrains.kotlin.tools.projectWizard.settings.version.maven" !in source
            },
            FileTestCase(
                "%d source files use something from com.beust.jcommander.internal package.\n" +
                        "This code won't work when there's no TestNG in the classpath of our IDEA plugin, " +
                        "because there's only an optional dependency on testng.jar.\n" +
                        "Most probably you meant to use Guava's Lists, Maps or Sets instead. " +
                        "Please change references in these files to com.google.common.collect:\n%s"
            ) { _, source ->
                "com.beust.jcommander.internal" in source
            },
            FileTestCase(
                "%d source files contain references to package org.jetbrains.jet.\n" +
                        "Package org.jetbrains.jet is deprecated now in favor of org.jetbrains.kotlin. " +
                        "Please consider changing the package in these files:\n%s"
            ) { _, source ->
                "org.jetbrains.jet" in source
            },
            FileTestCase(
                "%d source files contain references to package kotlin.reflect.jvm.internal.impl.\n" +
                        "This package contains internal reflection implementation and is a result of a " +
                        "post-processing of kotlin-reflect.jar by jarjar.\n" +
                        "Most probably you meant to use classes from org.jetbrains.kotlin.**.\n" +
                        "Please change references in these files or exclude them in this test:\n%s"
            ) { _, source ->
                "kotlin.reflect.jvm.internal.impl" in source
            },
            FileTestCase(
                "%d source files contain references to package org.objectweb.asm.\n" +
                        "Package org.jetbrains.org.objectweb.asm should be used instead to avoid troubles with different asm versions in classpath. " +
                        "Please consider changing the package in these files:\n%s"
            ) { _, source ->
                " org.objectweb.asm" in source
            },
            FileTestCase(
                message = "%d source files contain references to package gnu.trove.\n" +
                        "Please avoid using trove library in new use cases. " +
                        "These files are affected:\n%s",
                allowedFiles = listOf(
                    "analysis/light-classes-base/src/org/jetbrains/kotlin/asJava/classes/KotlinClassInnerStuffCache.kt",
                    "build-common/src/org/jetbrains/kotlin/incremental/IncrementalJvmCache.kt",
                    "compiler/backend/src/org/jetbrains/kotlin/codegen/FrameMap.kt",
                    "compiler/backend/src/org/jetbrains/kotlin/codegen/inline/SMAP.kt",
                    "compiler/backend/src/org/jetbrains/kotlin/codegen/optimization/common/ControlFlowGraph.kt",
                    "compiler/cli/cli-base/src/org/jetbrains/kotlin/cli/jvm/compiler/CliVirtualFileFinder.kt",
                    "compiler/cli/cli-base/src/org/jetbrains/kotlin/cli/jvm/compiler/KotlinCliJavaFileManagerImpl.kt",
                    "compiler/cli/cli-base/src/org/jetbrains/kotlin/cli/jvm/index/JvmDependenciesIndexImpl.kt",
                    "compiler/daemon/src/org/jetbrains/kotlin/daemon/RemoteLookupTrackerClient.kt",
                    "compiler/frontend/src/org/jetbrains/kotlin/resolve/lazy/FileScopeFactory.kt",
                    "compiler/frontend/src/org/jetbrains/kotlin/resolve/lazy/LazyImportScope.kt",
                    "compiler/frontend/src/org/jetbrains/kotlin/types/expressions/PreliminaryLoopVisitor.kt",
                    "compiler/ir/backend.jvm/lower/src/org/jetbrains/kotlin/backend/jvm/lower/EnumClassLowering.kt",
                    "compiler/psi/src/org/jetbrains/kotlin/psi/KotlinStringLiteralTextEscaper.kt",
                    "compiler/resolution.common.jvm/src/org/jetbrains/kotlin/load/java/structure/impl/classFiles/BinaryJavaClass.kt",
                    "compiler/resolution/src/org/jetbrains/kotlin/resolve/calls/results/OverloadingConflictResolver.kt",
                    "compiler/tests-common/tests/org/jetbrains/kotlin/test/testFramework/KtUsefulTestCase.java",
                    "js/js.ast/src/org/jetbrains/kotlin/js/backend/JsReservedIdentifiers.java",
                    "js/js.ast/src/org/jetbrains/kotlin/js/backend/JsToStringGenerationVisitor.java",
                    "js/js.sourcemap/src/org/jetbrains/kotlin/js/sourceMap/SourceMap3Builder.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/TargetDependent.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/konan/NativeLibrary.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/AssociatedClassifierIdsResolver.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/CirClassNode.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/CirClassifierIndex.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/CirFictitiousFunctionClassifiers.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/CirKnownClassifiers.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/CirModuleNode.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/CirPackageNode.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/CirProvidedClassifiersByModules.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/CirRootNode.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/mergedtree/CirTypeSignature.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/metadata/CirDeserializers.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/metadata/CirTypeResolver.kt",
                    "native/commonizer/src/org/jetbrains/kotlin/commonizer/utils/misc.kt",
                    "native/native.tests/tests/org/jetbrains/kotlin/konan/blackboxtest/support/settings/SettingsContainers.kt"
                )
            ) { _, source ->
                "gnu.trove" in source
            }
        )

        konst testCaseToMatchedFiles: Map<FileTestCase, MutableList<File>> = mutableMapOf<FileTestCase, MutableList<File>>()
            .apply {
                tests.forEach { testCase -> this[testCase] = mutableListOf() }
            }

        nonSourcesMatcher.excludeWalkTopDown(SOURCES_FILE_PATTERN).forEach { sourceFile ->
            konst source = sourceFile.readText()
            for (test in tests) {
                if (test.filter(sourceFile, source)) {
                    (testCaseToMatchedFiles[test] ?: error("Should be added during initialization")).add(sourceFile)
                }
            }
        }

        konst failureStr = buildString {
            for (test in tests) {
                konst (allowed, notAllowed) = (testCaseToMatchedFiles[test] ?: error("Should be added during initialization")).partition {
                    test.allowedMatcher.matchExact(it)
                }

                if (notAllowed.isNotEmpty()) {
                    append(test.message.format(notAllowed.size, notAllowed.joinToString("\n")))
                    appendLine()
                    appendLine()
                }

                konst unmatched = test.allowedMatcher.unmatchedExact(allowed)
                if (unmatched.isNotEmpty()) {
                    konst testMessage = test.message.format(unmatched.size, "NONE")
                    append(
                        "Unused \"allowed files\" for test:\n" +
                                "`$testMessage`\n" +
                                "Remove exceptions for the test list:${unmatched.joinToString("\n", prefix = "\n")}"
                    )
                    appendLine()
                    appendLine()
                }
            }
        }

        if (failureStr.isNotEmpty()) {
            fail(failureStr)
        }
    }

    fun testThirdPartyCopyrights() {
        konst filesWithUnlistedCopyrights = mutableListOf<String>()
        konst knownThirdPartyCode = loadKnownThirdPartyCodeList()
        konst copyrightRegex = Regex("""\bCopyright\b""")
        konst root = COPYRIGHT_EXCLUDED_FILES_AND_DIRS_MATCHER.root

        COPYRIGHT_EXCLUDED_FILES_AND_DIRS_MATCHER.excludeWalkTopDown(SOURCES_FILE_PATTERN)
            .filter { sourceFile ->
                konst relativePath = FileUtil.toSystemIndependentName(sourceFile.toRelativeString(root))
                !knownThirdPartyCode.any { relativePath.startsWith(it) }
            }
            .forEach { sourceFile ->
                sourceFile.useLines { lineSequence ->
                    for (line in lineSequence) {
                        if (copyrightRegex in line && "JetBrains" !in line) {
                            konst relativePath = FileUtil.toSystemIndependentName(sourceFile.toRelativeString(root))
                            filesWithUnlistedCopyrights.add("$relativePath: $line")
                        }
                    }
                }
            }

        if (filesWithUnlistedCopyrights.isNotEmpty()) {
            fail(
                "The following files contain third-party copyrights and no license information. " +
                        "Please update license/README.md accordingly:\n${filesWithUnlistedCopyrights.joinToString("\n")}"
            )
        }
    }

    private class FileMatcher(konst root: File, paths: Collection<String>) {
        private konst files = paths.map { File(it) }
        private konst paths = files.mapTo(HashSet()) { it.invariantSeparatorsPath }
        private konst relativePaths = files.filterTo(ArrayList()) { it.isDirectory }.mapTo(HashSet()) { it.invariantSeparatorsPath + "/" }

        private fun File.invariantRelativePath() = relativeTo(root).invariantSeparatorsPath

        fun matchExact(file: File): Boolean {
            return file.invariantRelativePath() in paths
        }

        fun matchWithContains(file: File): Boolean {
            if (matchExact(file)) return true
            konst relativePath = file.invariantRelativePath()
            return relativePaths.any { relativePath.startsWith(it) }
        }

        fun unmatchedExact(files: List<File>): Set<String> {
            return paths - files.map { it.invariantRelativePath() }.toSet()
        }
    }

    private fun FileMatcher.excludeWalkTopDown(filePattern: Pattern): Sequence<File> {
        return root.walkTopDown()
            .onEnter { dir ->
                !matchExact(dir) // Don't enter to ignored dirs
            }
            .filter { file -> !matchExact(file) } // filter ignored files
            .filter { file -> filePattern.matcher(file.name).matches() }
            .filter { file -> file.isFile }
    }

    fun testRepositoriesAbuse() {
        class RepoAllowList(konst repo: String, root: File, allowList: Set<String>, konst exclude: String? = null) {
            konst matcher = FileMatcher(root, allowList)
        }

        konst root = nonSourcesMatcher.root

        konst repoCheckers = listOf(
            RepoAllowList(
                // Please use cache-redirector for importing in tests
                "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev", root,
                setOf("repo/scripts/cache-redirector.settings.gradle.kts")
            ),
            RepoAllowList(
                "https://cache-redirector.jetbrains.com/maven.pkg.jetbrains.space/kotlin/p/kotlin/dev", root,
                setOf("repo/scripts/cache-redirector.settings.gradle.kts")
            ),
            RepoAllowList(
                // Please use cache-redirector for importing in tests
                "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/eap", root,
                setOf("repo/scripts/cache-redirector.settings.gradle.kts")
            ),
            RepoAllowList(
                "https://cache-redirector.jetbrains.com/maven.pkg.jetbrains.space/kotlin/p/kotlin/dev", root,
                setOf("repo/scripts/cache-redirector.settings.gradle.kts")
            ),
            RepoAllowList(
                "kotlin/ktor", root,
                setOf("repo/scripts/cache-redirector.settings.gradle.kts")
            ),
            RepoAllowList(
                "bintray.com", root,
                setOf("repo/scripts/cache-redirector.settings.gradle.kts"),
                exclude = "jcenter.bintray.com"
            )
        )

        data class RepoOccurrence(konst repo: String, konst file: File)
        data class RepoOccurrences(konst repo: String, konst files: Collection<File>)

        konst extensionsPattern = Pattern.compile(".+\\.(java|kt|gradle|kts|xml)(\\.\\w+)?")
        konst repoOccurrences: List<RepoOccurrences> = nonSourcesMatcher.excludeWalkTopDown(extensionsPattern)
            .flatMap { file ->
                konst checkers = repoCheckers.filter { checker ->
                    !checker.matcher.matchWithContains(file)
                }

                if (checkers.isNotEmpty()) {
                    konst occurrences = ArrayList<RepoOccurrence>()
                    file.useLines { lines ->
                        for (line in lines) {
                            for (checker in checkers) {
                                if (line.contains(checker.repo) && (checker.exclude == null || !line.contains(checker.exclude))) {
                                    occurrences.add(RepoOccurrence(checker.repo, file))
                                }
                            }
                        }
                    }
                    occurrences
                } else {
                    listOf()
                }
            }
            .groupBy { it.repo }
            .map { (repo, occurrences) -> RepoOccurrences(repo, occurrences.mapTo(HashSet()) { it.file }) }

        if (repoOccurrences.isNotEmpty()) {
            konst repoOccurrencesStableOrder = repoOccurrences
                .map { occurrence -> RepoOccurrences(occurrence.repo, occurrence.files.sortedBy { file -> file.path }) }
                .sortedBy { it.repo }
            fail(
                buildString {
                    appendLine("The following files use repositories and not listed in the correspondent allow lists")
                    for ((repo, files) in repoOccurrencesStableOrder) {
                        appendLine(repo)
                        for (file in files) {
                            appendLine("  ${file.relativeTo(root).invariantSeparatorsPath}")
                        }
                    }
                }
            )
        }
    }

    private fun loadKnownThirdPartyCodeList(): List<String> {
        File("license/README.md").useLines { lineSequence ->
            return lineSequence
                .filter { it.startsWith(" - Path: ") }
                .map { it.removePrefix(" - Path: ").trim().ensureFileOrEndsWithSlash() }
                .toList()

        }
    }

    fun testLanguageFeatureOrder() {
        konst konstues = enumValues<LanguageFeature>()
        konst enabledFeatures = konstues.filter { it.sinceVersion != null }

        if (enabledFeatures.sortedBy { it.sinceVersion!! } != enabledFeatures) {
            konst (a, b) = enabledFeatures.zipWithNext().first { (a, b) -> a.sinceVersion!! > b.sinceVersion!! }
            fail(
                "Please make sure LanguageFeature entries are sorted by sinceVersion to improve readability & reduce confusion.\n" +
                        "The feature $b is out of order; its sinceVersion is ${b.sinceVersion}, yet it comes after $a, whose " +
                        "sinceVersion is ${a.sinceVersion}.\n"
            )
        }
    }
}

private fun String.ensureFileOrEndsWithSlash() =
    if (endsWith("/") || "." in substringAfterLast('/')) this else "$this/"
