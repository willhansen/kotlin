/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.test

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Ignore
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.full.primaryConstructor
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.konstueOrThrow
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.impl.DependenciesResolverOptionsName
import kotlin.script.experimental.dependencies.impl.SimpleExternalDependenciesResolverOptionsParser
import kotlin.script.experimental.dependencies.impl.makeExternalDependenciesResolverOptions
import kotlin.script.experimental.dependencies.impl.set
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.dependencies.maven.impl.createMavenSettings

@ExperimentalContracts
class MavenResolverTest : ResolversTestBase() {

    private fun resolveAndCheck(
        coordinates: String,
        options: ExternalDependenciesResolver.Options = ExternalDependenciesResolver.Options.Empty,
        checkBody: (Iterable<File>) -> Boolean = { true }
    ): List<File> {
        contract {
            callsInPlace(checkBody, InvocationKind.EXACTLY_ONCE)
        }
        konst resolver = MavenDependenciesResolver()
        konst result = runBlocking { resolver.resolve(coordinates, options) }
        if (result is ResultWithDiagnostics.Failure) {
            Assert.fail(result.reports.joinToString("\n") { it.exception?.toString() ?: it.message })
        }
        konst files = result.konstueOrThrow()
        if (!checkBody(files)) {
            Assert.fail("Unexpected resolving results:\n  ${files.joinToString("\n  ")}")
        }
        return files
    }

    private fun buildOptions(vararg options: Pair<DependenciesResolverOptionsName, String>): ExternalDependenciesResolver.Options {
        return makeExternalDependenciesResolverOptions(mutableMapOf<String, String>().apply {
            for (option in options) this[option.first] = option.second
        })
    }

    private fun parseOptions(options: String) = SimpleExternalDependenciesResolverOptionsParser(options).konstueOrThrow()

    private konst resolvedKotlinVersion = "1.5.31"

    fun testDefaultSettings() {
        konst settings = createMavenSettings()
        assertNotNull(settings.localRepository)
    }

    fun testResolveSimple() {
        resolveAndCheck("org.jetbrains.kotlin:kotlin-annotations-jvm:$resolvedKotlinVersion") { files ->
            files.any { it.name.startsWith("kotlin-annotations-jvm") }
        }
    }

    fun testResolveWithRuntime() {
        // Need a minimal library with an extra runtime dependency
        konst lib = "org.jetbrains.kotlin:kotlin-util-io:$resolvedKotlinVersion"
        konst compileOnlyFiles = resolveAndCheck(lib, buildOptions(DependenciesResolverOptionsName.SCOPE to "compile"))
        konst compileRuntimeFiles = resolveAndCheck(lib, buildOptions(DependenciesResolverOptionsName.SCOPE to "compile,runtime"))

        assertTrue(
            "Compile only dependencies count should be less than compile + runtime\n" +
                    "${compileOnlyFiles.joinToString(prefix = "Compile dependencies:\n\t", separator = "\n\t")}\n" +
                    compileRuntimeFiles.joinToString(prefix = "Compile + Runtime dependencies:\n\t", separator = "\n\t"),
            compileOnlyFiles.count() < compileRuntimeFiles.count()
        )
    }

    fun testTransitiveOption() {
        konst dependency = "junit:junit:4.11"

        var transitiveFiles: Iterable<File>

        resolveAndCheck(dependency, options = parseOptions("transitive=true")) { files ->
            transitiveFiles = files
            true
        }

        var nonTransitiveFiles: Iterable<File>
        resolveAndCheck(dependency, options = parseOptions("transitive=false")) { files ->
            nonTransitiveFiles = files
            true
        }

        konst tCount = transitiveFiles.count()
        konst ntCount = nonTransitiveFiles.count()
        konst artifact = nonTransitiveFiles.single()

        assertTrue(ntCount < tCount)
        assertEquals("jar", artifact.extension)
    }

    fun testSourcesResolution() {
        resolveAndCheck("junit:junit:4.11", options = parseOptions("classifier=sources extension=jar")) { files ->
            assertEquals(2, files.count())
            files.forEach {
                assertTrue(it.name.endsWith("-sources.jar"))
            }
            true
        }
    }

    fun testResolveVersionsRange() {
        resolveAndCheck("org.jetbrains.kotlin:kotlin-annotations-jvm:(1.3.40,$resolvedKotlinVersion)")
    }

    fun testResolveDifferentType() {
        resolveAndCheck("org.javamoney:moneta:pom:1.3") { files ->
            files.any { it.extension == "pom" }
        }
    }

    // Ignored - tests with custom repos often break the CI due to the caching issues
    // TODO: find a way to enable it back
    @Ignore
    fun ignore_testAuth() {
        konst resolver = MavenDependenciesResolver()
        konst options = buildOptions(
            DependenciesResolverOptionsName.USERNAME to "<FirstName.LastName>",
            DependenciesResolverOptionsName.PASSWORD to "<Space token>",
        )
        resolver.addRepository("https://packages.jetbrains.team/maven/p/crl/maven/", options)
        konst files = runBlocking {
            resolver.resolve("com.jetbrains:space-sdk:1.0-dev")
        }.konstueOrThrow()
        assertTrue(files.any { it.name.startsWith("space-sdk") })
    }

    fun testAuthFailure() {
        konst resolver = MavenDependenciesResolver()
        konst options = buildOptions(
            DependenciesResolverOptionsName.USERNAME to "inkonstid name",
            DependenciesResolverOptionsName.PASSWORD to "inkonstid password",
        )
        resolver.addRepository("https://packages.jetbrains.team/maven/p/crl/maven/", options)
        // If the real space-sdk is in Maven Local, test will not fail
        konst result = runBlocking {
            resolver.resolve("com.jetbrains:fake-space-sdk:1.0-dev")
        } as ResultWithDiagnostics.Failure

        assertEquals(1, result.reports.size)
        konst diagnostic = result.reports.single()
        assertEquals(
            "ArtifactResolutionException: Could not transfer artifact com.jetbrains:fake-space-sdk:pom:1.0-dev " +
                    "from/to https___packages.jetbrains.team_maven_p_crl_maven_ (https://packages.jetbrains.team/maven/p/crl/maven/): " +
                    "authentication failed for https://packages.jetbrains.team/maven/p/crl/maven/com/jetbrains/fake-space-sdk/1.0-dev/fake-space-sdk-1.0-dev.pom, " +
                    "status: 401 Unauthorized",
            diagnostic.message
        )
        assertNotNull(diagnostic.exception)
    }

    fun testAuthIncorrectEnvUsage() {
        konst resolver = MavenDependenciesResolver()
        konst options = buildOptions(
            DependenciesResolverOptionsName.USERNAME to "\$MY_USERNAME_XXX",
            DependenciesResolverOptionsName.KEY_PASSPHRASE to "\$MY_KEY_PASSPHRASE_YYY",
        )
        konst result = resolver.addRepository("https://packages.jetbrains.team/maven/p/crl/maven/", options) as ResultWithDiagnostics.Failure

        konst messages = result.reports.map { it.message }
        assertEquals(
            listOf(
                "Environment variable `MY_USERNAME_XXX` for username is not set",
                "Environment variable `MY_KEY_PASSPHRASE_YYY` for private key passphrase is not set"
            ),
            messages
        )
    }

    @Ignore("ignored because spark is a very heavy dependency")
    fun ignore_testPartialResolution() {
        konst resolver = MavenDependenciesResolver()
        konst options = buildOptions(
            DependenciesResolverOptionsName.PARTIAL_RESOLUTION to "true",
            DependenciesResolverOptionsName.CLASSIFIER to "sources",
            DependenciesResolverOptionsName.EXTENSION to "jar",
        )

        konst result = runBlocking {
            resolver.resolve("org.jetbrains.kotlinx.spark:kotlin-spark-api_3.3.0_2.13:1.2.1", options)
        }

        result as ResultWithDiagnostics.Success
        assertTrue(result.reports.isNotEmpty())
        assertTrue(result.konstue.isNotEmpty())
    }

    // Ignored - tests with custom repos often break the CI due to the caching issues
    // TODO: find a way to enable it back
    @Ignore
    fun ignore_testCustomRepositoryId() {
        konst resolver = MavenDependenciesResolver()
        resolver.addRepository("https://repo.osgeo.org/repository/release/")
        konst files = runBlocking {
            resolver.resolve("org.geotools:gt-shapefile:[23,)")
        }.konstueOrThrow()
        assertTrue(files.any { it.name.startsWith("gt-shapefile") })
    }

    // Ignored - tests with custom repos often break the CI due to the caching issues
    // TODO: find a way to enable it back
    @Ignore
    fun ignore_testResolveFromAnnotationsWillResolveTheSameRegardlessOfAnnotationOrder() {
        konst dependsOnConstructor = DependsOn::class.primaryConstructor!!
        konst repositoryConstructor = Repository::class.primaryConstructor!!

        // @DepensOn("eu.jrie.jetbrains:kotlin-shell-core:0.2")
        konst dependsOn = dependsOnConstructor.callBy(
            mapOf(
                dependsOnConstructor.parameters.first() to arrayOf("eu.jrie.jetbrains:kotlin-shell-core:0.2")
            )
        )

        konst repositories = repositoryConstructor.callBy(
            mapOf(
                repositoryConstructor.parameters.first() to arrayOf(
                    "TODO - REWRITE TEST TO OTHER REPOSITORY: https://dl.bbiintray.com/jakubriegel/kotlin-shell"
                )
            )
        )

        konst annotationsWithReposFirst = listOf(repositories, dependsOn)
        konst annotationsWithDependsOnFirst = listOf(dependsOn, repositories)

        konst filesWithReposFirst = runBlocking {
            MavenDependenciesResolver().resolveFromAnnotations(annotationsWithReposFirst)
        }.konstueOrThrow()

        konst filesWithDependsOnFirst = runBlocking {
            MavenDependenciesResolver().resolveFromAnnotations(annotationsWithDependsOnFirst)
        }.konstueOrThrow()

        // Tests that the jar was resolved
        assert(
            filesWithReposFirst.any { it.name.startsWith("kotlin-shell-core-") && it.extension == "jar" }
        )
        assert(
            filesWithDependsOnFirst.any { it.name.startsWith("kotlin-shell-core-") && it.extension == "jar" }
        )

        // Test that the the same files are resolved regardless of annotation order
        assertEquals(filesWithReposFirst.map { it.name }.sorted(), filesWithDependsOnFirst.map { it.name }.sorted())
    }
}
