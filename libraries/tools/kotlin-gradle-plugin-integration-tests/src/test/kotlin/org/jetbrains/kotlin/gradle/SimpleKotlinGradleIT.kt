package org.jetbrains.kotlin.gradle

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.configuration.WarningMode
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilerExecutionStrategy
import org.jetbrains.kotlin.gradle.testbase.*
import org.junit.jupiter.api.DisplayName
import java.util.zip.ZipFile
import kotlin.io.path.*

@JvmGradlePluginTests
@DisplayName("KGP simple tests")
class SimpleKotlinGradleIT : KGPBaseTest() {

    @GradleTest
    @DisplayName("On second run common tasks should be up-to-date")
    fun testSimpleCompile(gradleVersion: GradleVersion) {
        project(
            projectName = "simpleProject",
            gradleVersion = gradleVersion,
            buildOptions = defaultBuildOptions.copy(logLevel = LogLevel.DEBUG),
        ) {
            build("compileDeployKotlin", "build") {
                assertOutputContains("Finished executing kotlin compiler using ${KotlinCompilerExecutionStrategy.DAEMON} strategy")
                assertFileInProjectExists("build/reports/tests/test/classes/demo.TestSource.html")
                assertTasksExecuted(":compileKotlin", ":compileTestKotlin", ":compileDeployKotlin")
            }

            build("compileDeployKotlin", "build") {
                assertTasksUpToDate(
                    ":compileKotlin",
                    ":compileTestKotlin",
                    ":compileDeployKotlin",
                    ":compileJava"
                )
            }
        }
    }

    @GradleTest
    @DisplayName("Plugin allows to suppress all warnings")
    fun testSuppressWarnings(gradleVersion: GradleVersion) {
        project("suppressWarnings", gradleVersion) {
            build("build") {
                assertTasksExecuted(":compileKotlin")
                assertOutputDoesNotContain("""w: [^\r\n]*?\.kt""".toRegex())
            }
        }
    }

    @GradleTest
    @DisplayName("Plugin should allow to add custom Kotlin directory")
    fun testKotlinCustomDirectory(gradleVersion: GradleVersion) {
        project("customSrcDir", gradleVersion) {
            build("build")
        }
    }

    @GradleTest
    @DisplayName("Plugin should correctly handle additional java source directories")
    fun testKotlinExtraJavaSrc(gradleVersion: GradleVersion) {
        project("additionalJavaSrc", gradleVersion) {
            build("build")
        }
    }

    @GradleTest
    @DisplayName("Using newer language features with older api level should fail the build")
    fun testLanguageVersion(gradleVersion: GradleVersion) {
        project("languageVersion", gradleVersion) {
            buildAndFail("build") {
                assertOutputContains("Suspend function type is allowed as a supertype only since version 1.6")
            }
        }
    }

    @GradleTest
    @DisplayName("Compilation should fail on unknown JVM target")
    fun testJvmTarget(gradleVersion: GradleVersion) {
        project("jvmTarget", gradleVersion) {
            buildAndFail("build") {
                assertOutputContains("Unknown Kotlin JVM target: 1.7")
            }
        }
    }

    @GradleTest
    @DisplayName("Should produce '.kotlin_module' file with specified name")
    fun testModuleName(gradleVersion: GradleVersion) {
        project("moduleName", gradleVersion) {
            build("build") {
                assertFileInProjectExists("build/classes/kotlin/main/META-INF/FLAG.kotlin_module")
                assertFileInProjectNotExists("build/classes/kotlin/main/META-INF/moduleName.kotlin_module")
                assertOutputDoesNotContain("Argument -module-name is passed multiple times")
            }
        }
    }

    @GradleTest
    @DisplayName("Compile task destination dir should be configured on configuration phase")
    fun testDestinationDirReferencedDuringEkonstuation(gradleVersion: GradleVersion) {
        project("destinationDirReferencedDuringEkonstuation", gradleVersion) {
            build("build") {
                assertOutputContains("foo.GreeterTest > testHelloWorld PASSED")
            }
        }
    }

    @GradleTest
    @DisplayName("Plugin correctly handle redefined build dir location")
    fun testBuildDirLazyEkonstuation(gradleVersion: GradleVersion) {
        project("kotlinProject", gradleVersion) {
            // Change the build directory in the end of the build script:
            konst customBuildDirName = "customBuild"
            buildGradle.append(
                "buildDir = '$customBuildDirName'"
            )

            build("build") {
                assertDirectoryInProjectExists("$customBuildDirName/classes")
                assertFileInProjectNotExists("build")
            }
        }
    }

    @GradleTest
    @DisplayName("Should correctly work with Groovy lang modules")
    fun testGroovyInterop(gradleVersion: GradleVersion) {
        project("groovyInterop", gradleVersion) {
            build("build") {
                assertTasksExecuted(":test")
                assertOutputContains("GroovyInteropTest > parametersInInnerClassConstructor PASSED")
                assertOutputContains("GroovyInteropTest > classWithReferenceToInner PASSED")
                assertOutputContains("GroovyInteropTest > groovyTraitAccessor PASSED")
                assertOutputContains("GroovyInteropTest > parametersInEnumConstructor PASSED")
            }
        }
    }

    //Proguard corrupts RuntimeInvisibleParameterAnnotations/RuntimeVisibleParameterAnnotations tables:
    // https://sourceforge.net/p/proguard/bugs/735/
    // Gradle 7 compatibility issue: https://github.com/Guardsquare/proguard/issues/136
    @GradleTest
    @GradleTestVersions(maxVersion = TestVersions.Gradle.G_6_8)
    @DisplayName("Should correctly interop with ProGuard")
    fun testInteropWithProguarded(gradleVersion: GradleVersion) {
        project(
            "interopWithProguarded",
            gradleVersion,
            buildOptions = defaultBuildOptions.copy(warningMode = WarningMode.Summary)
        ) {
            build("build") {
                assertTasksExecuted(":test")
                assertOutputContains("InteropWithProguardedTest > parametersInInnerKotlinClassConstructor PASSED")
                assertOutputContains("InteropWithProguardedTest > parametersInInnerJavaClassConstructor PASSED")
                assertOutputContains("InteropWithProguardedTest > parametersInJavaEnumConstructor PASSED")
                assertOutputContains("InteropWithProguardedTest > parametersInKotlinEnumConstructor PASSED")
            }
        }
    }

    @GradleTest
    @DisplayName("Should correctly work with Scala lang modules")
    fun testScalaInterop(gradleVersion: GradleVersion) {
        project("scalaInterop", gradleVersion) {
            build("build") {
                assertTasksExecuted(":test")
                assertOutputContains("ScalaInteropTest > parametersInInnerClassConstructor PASSED")
            }
        }
    }

    @GradleTest
    @DisplayName("Should not produce kotlin-stdlib version conflict on Kotlin files compilation in 'buildSrc' module")
    internal fun testKotlinDslStdlibVersionConflict(gradleVersion: GradleVersion) {
        project(
            projectName = "buildSrcUsingKotlinCompilationAndKotlinPlugin",
            gradleVersion,
        ) {
            listOf(
                "compileClasspath",
                "compileOnly",
                "runtimeClasspath"
            ).forEach { configuration ->
                build("-p", "buildSrc", "dependencies", "--configuration", configuration) {
                    listOf(
                        "org.jetbrains.kotlin:kotlin-stdlib:${buildOptions.kotlinVersion}",
                        "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${buildOptions.kotlinVersion}",
                        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${buildOptions.kotlinVersion}",
                        "org.jetbrains.kotlin:kotlin-stdlib-common:${buildOptions.kotlinVersion}",
                        "org.jetbrains.kotlin:kotlin-reflect:${buildOptions.kotlinVersion}",
                        "org.jetbrains.kotlin:kotlin-script-runtime:${buildOptions.kotlinVersion}"
                    ).forEach { assertOutputDoesNotContain(it) }
                }
            }

            build("assemble")
        }
    }

    @GradleTest
    @DisplayName("Should be compatible with project isolation")
    @GradleTestVersions(minVersion = TestVersions.Gradle.G_7_1)
    fun testProjectIsolation(gradleVersion: GradleVersion) {
        project(
            projectName = "instantExecution",
            gradleVersion = gradleVersion,
            buildOptions = defaultBuildOptions.copy(configurationCache = true, projectIsolation = true),
        ) {
            build(":main-project:compileKotlin")
        }
    }

    @DisplayName("Proper Gradle plugin variant is used")
    @GradleTestVersions(
        additionalVersions = [TestVersions.Gradle.G_7_0, TestVersions.Gradle.G_7_1, TestVersions.Gradle.G_7_3, TestVersions.Gradle.G_7_4, TestVersions.Gradle.G_7_5],
        maxVersion = TestVersions.Gradle.G_7_6
    )
    @GradleTest
    internal fun pluginVariantIsUsed(gradleVersion: GradleVersion) {
        project("kotlinProject", gradleVersion) {
            build("tasks") {
                konst expectedVariant = when (gradleVersion) {
                    GradleVersion.version(TestVersions.Gradle.G_7_6) -> "gradle76"
                    GradleVersion.version(TestVersions.Gradle.G_7_5) -> "gradle75"
                    GradleVersion.version(TestVersions.Gradle.G_7_4) -> "gradle74"
                    in GradleVersion.version(TestVersions.Gradle.G_7_1)..GradleVersion.version(TestVersions.Gradle.G_7_3) -> "gradle71"
                    GradleVersion.version(TestVersions.Gradle.G_7_0) -> "gradle70"
                    else -> "main"
                }

                assertOutputContains("Using Kotlin Gradle Plugin $expectedVariant variant")
            }
        }
    }

    @DisplayName("Validate Gradle plugins inputs")
    @GradleTestVersions(minVersion = TestVersions.Gradle.MAX_SUPPORTED) // Always should use only latest Gradle version
    @GradleTest
    internal fun konstidatePluginInputs(gradleVersion: GradleVersion) {
        project("kotlinProject", gradleVersion) {
            buildGradle.modify {
                """
                plugins {
                    id "konstidate-external-gradle-plugin"
                ${it.substringAfter("plugins {")}
                """.trimIndent()
            }

            build("konstidateExternalPlugins")
        }
    }

    @DisplayName("Accessing Kotlin SourceSet in KotlinDSL")
    @GradleTestVersions(maxVersion = TestVersions.Gradle.G_7_1)
    @GradleTest
    internal fun kotlinDslSourceSets(gradleVersion: GradleVersion) {
        project("sourceSetsKotlinDsl", gradleVersion) {
            build("assemble")
        }
    }

    @DisplayName("KT-53402: ignore non project source changes")
    @GradleTest
    fun ignoreNonProjectSourceChanges(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            konst resources = projectPath.resolve("src/main/resources").createDirectories()
            konst resourceKts = resources.resolve("resource.kts").createFile()
            resourceKts.appendText("lkdfjgkjs inkonstid something")
            build("assemble")
            resourceKts.appendText("kajhgfkh inkonstid something")
            build("assemble")
        }
    }

    @DisplayName("Changing compile task destination directory does not break test compilation")
    @GradleTest
    internal fun customDestinationDir(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            //language=Groovy
            buildGradle.appendText(
                """
                |
                |def compileKotlinTask = tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile.class)
                |
                |compileKotlinTask.configure {
                |    it.destinationDirectory.set(project.layout.buildDirectory.dir("banana"))
                |}
                |
                |def compileKotlinTaskOutput = compileKotlinTask.flatMap { it.destinationDirectory }
                |sourceSets.test.compileClasspath.from(compileKotlinTaskOutput)
                |sourceSets.test.runtimeClasspath.from(compileKotlinTaskOutput)
                |
                """.trimMargin()
            )

            build("build") {
                assertFileInProjectExists("build/banana/demo/KotlinGreetingJoiner.class")
                assertFileInProjectExists("build/libs/simpleProject.jar")
                ZipFile(projectPath.resolve("build/libs/simpleProject.jar").toFile()).use { jar ->
                    assert(jar.entries().asSequence().count { it.name == "demo/KotlinGreetingJoiner.class" } == 1) {
                        "The jar should contain one entry `demo/KotlinGreetingJoiner.class` with no duplicates\n" +
                                jar.entries().asSequence().map { it.name }.joinToString()
                    }
                }
            }
        }
    }

    @DisplayName("Default jar content should not contain duplicates")
    @GradleTest
    internal fun defaultJarContent(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            build("build") {
                assertFileInProjectExists("build/libs/simpleProject.jar")
                ZipFile(projectPath.resolve("build/libs/simpleProject.jar").toFile()).use { jar ->
                    assert(jar.entries().asSequence().count { it.name == "demo/KotlinGreetingJoiner.class" } == 1) {
                        "The jar should contain one entry `demo/KotlinGreetingJoiner.class` with no duplicates\n" +
                                jar.entries().asSequence().map { it.name }.joinToString()
                    }
                }
            }
        }
    }

    @DisplayName("KT-36904: Adding resources to Kotlin source set should work")
    @GradleTest
    internal fun addResourcesKotlinSourceSet(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            konst mainResDir = projectPath.resolve("src/main/resources").apply { createDirectories() }
            konst mainResFile = mainResDir.resolve("main.txt").apply { writeText("Yay, Kotlin!") }

            konst additionalResDir = projectPath.resolve("additionalRes").apply { createDirectory() }
            konst additionalResFile = additionalResDir.resolve("test.txt").apply { writeText("Kotlin!") }

            buildGradle.appendText(
                //language=groovy
                """
                |
                |kotlin {
                |    sourceSets.main.resources.srcDir("additionalRes")
                |}
                """.trimMargin()
            )

            build("jar") {
                assertFileInProjectExists("build/libs/simpleProject.jar")
                ZipFile(projectPath.resolve("build/libs/simpleProject.jar").toFile()).use { jar ->
                    assert(jar.entries().asSequence().count { it.name == mainResFile.name } == 1) {
                        "The jar should contain one entry `${mainResFile.name}` with no duplicates\n" +
                                jar.entries().asSequence().map { it.name }.joinToString()
                    }

                    assert(jar.entries().asSequence().count { it.name == additionalResFile.name } == 1) {
                        "The jar should contain one entry `${additionalResFile.name}` with no duplicates\n" +
                                jar.entries().asSequence().map { it.name }.joinToString()
                    }
                }
            }
        }
    }
}
