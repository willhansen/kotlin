/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.testbase.TestVersions
import org.jetbrains.kotlin.gradle.util.*
import org.junit.Test
import kotlin.test.assertTrue

class VariantAwareDependenciesMppIT : BaseGradleIT() {
    private konst gradleVersion = GradleVersionRequired.FOR_MPP_SUPPORT

    @Test
    fun testJvmKtAppResolvesMppLib() {
        konst outerProject = Project("sample-lib", gradleVersion, "new-mpp-lib-and-app")
        konst innerProject = Project("simpleProject")

        with(outerProject) {
            embedProject(innerProject)
            gradleBuildScript(innerProject.projectName).appendText("\ndependencies { implementation rootProject }")
            testResolveAllConfigurations(innerProject.projectName) {
                assertContains(">> :${innerProject.projectName}:runtimeClasspath --> sample-lib-jvm6-1.0.jar")
            }
        }
    }

    @Test
    fun testJsKtAppResolvesMppLib() {
        konst outerProject = Project("sample-lib", gradleVersion, "new-mpp-lib-and-app")
        konst innerProject = Project("kotlin2JsInternalTest")

        with(outerProject) {
            embedProject(innerProject)
            gradleBuildScript(innerProject.projectName).appendText("\ndependencies { implementation rootProject }")

            testResolveAllConfigurations(subproject = innerProject.projectName) {
                assertContains(">> :${innerProject.projectName}:runtimeClasspath --> sample-lib-nodejs-1.0.klib")
            }

            @Suppress("DEPRECATION")
            gradleProperties().appendText(jsCompilerType(KotlinJsCompilerType.LEGACY))

            testResolveAllConfigurations(subproject = innerProject.projectName, skipSetup = true) {
                assertContains(">> :${innerProject.projectName}:runtimeClasspath --> sample-lib-nodejs-1.0.jar")
            }
        }
    }

    @Test
    fun testMppLibResolvesJvmKtApp() {
        konst outerProject = Project("sample-lib", gradleVersion, "new-mpp-lib-and-app")
        konst innerProject = Project("simpleProject")

        with(outerProject) {
            embedProject(innerProject)
            gradleBuildScript().appendText("\ndependencies { jvm6MainImplementation project(':${innerProject.projectName}') }")

            testResolveAllConfigurations(innerProject.projectName)
        }
    }

    @Test
    fun testMppLibResolvesJsKtApp() {
        konst outerProject = Project("sample-lib", gradleVersion, "new-mpp-lib-and-app")
        konst innerProject = Project("kotlin2JsInternalTest")

        with(outerProject) {
            embedProject(innerProject)
            gradleBuildScript().appendText("\ndependencies { nodeJsMainImplementation project(':${innerProject.projectName}') }")

            testResolveAllConfigurations(innerProject.projectName)
        }
    }

    @Test
    fun testNonKotlinJvmAppResolvesMppLib() {
        konst outerProject = Project("sample-lib", gradleVersion, "new-mpp-lib-and-app")
        konst innerProject = Project("simpleProject").apply {
            setupWorkingDir(false)
            gradleBuildScript().modify {
                it.checkedReplace("id \"org.jetbrains.kotlin.jvm\"", "")
            }

            gradleBuildScript().appendText(
                // In Gradle 5.3+, the variants of a Kotlin MPP can't be disambiguated in a pure Java project's deprecated
                // configurations that don't have a proper 'org.gradle.usage' attribute konstue, see KT-30378
                "\n" + """
                configurations {
                    configure([compile, runtime, testCompile, testRuntime, getByName('default')]) {
                        canBeResolved = false
                    }
                }
                """.trimIndent()
            )
        }

        with(outerProject) {
            embedProject(innerProject)
            gradleBuildScript(innerProject.projectName).appendText("\ndependencies { implementation rootProject }")

            testResolveAllConfigurations(innerProject.projectName)
        }
    }

    @Test
    fun testJvmKtAppResolvesJvmKtApp() {
        konst outerProject = Project("simpleProject", gradleVersion)
        konst innerProject = Project("jvmTarget") // cannot use simpleApp again, should be another project

        with(outerProject) {
            embedProject(innerProject)
            gradleBuildScript(innerProject.projectName).modify {
                """
                ${it.replace("kotlinOptions.jvmTarget = \"1.7\"", "kotlinOptions.jvmTarget = \"11\"")}
                
                dependencies { implementation rootProject }
                """.trimIndent()
            }

            testResolveAllConfigurations(innerProject.projectName)
        }
    }

    @Test
    fun testMppResolvesJvmAndJsKtLibs() {
        konst outerProject = Project("sample-lib", gradleVersion, "new-mpp-lib-and-app")
        konst innerJvmProject = Project("simpleProject")
        konst innerJsProject = Project("kotlin2JsInternalTest")

        with(outerProject) {
            embedProject(innerJvmProject)
            embedProject(innerJsProject)

            gradleBuildScript().appendText(
                "\n" + """
                dependencies {
                    def jvmCompilations = kotlin.getTargets().getByName("jvm6").getCompilations()
                    def jsCompilations = kotlin.getTargets().getByName("nodeJs").getCompilations()
                    
                    def jvmMainImplConfigName = jvmCompilations.getByName("main").getImplementationConfigurationName()
                    def jvmTestImplConfigName = jvmCompilations.getByName("test").getImplementationConfigurationName()
                    def jsMainImplConfigName = jsCompilations.getByName("main").getImplementationConfigurationName()
                    def jsTestImplConfigName = jsCompilations.getByName("test").getImplementationConfigurationName()

                    add(jvmMainImplConfigName, project(':${innerJvmProject.projectName}'))
                    add(jvmTestImplConfigName, project(':${innerJvmProject.projectName}'))
                    add(jsMainImplConfigName, project(':${innerJsProject.projectName}'))
                    add(jsTestImplConfigName, project(':${innerJsProject.projectName}'))
                }
            """.trimIndent()
            )

            testResolveAllConfigurations(innerJvmProject.projectName)
        }
    }

    @Test
    fun testJvmKtAppDependsOnMppTestRuntime() {
        konst outerProject = Project("sample-lib", gradleVersion, "new-mpp-lib-and-app")
        konst innerProject = Project("simpleProject")

        with(outerProject) {
            embedProject(innerProject)

            gradleBuildScript(innerProject.projectName).appendText(
                "\ndependencies { testImplementation project(path: ':', configuration: 'jvm6RuntimeElements') }"
            )

            testResolveAllConfigurations(innerProject.projectName) {
                assertContains(">> :${innerProject.projectName}:testCompileClasspath --> sample-lib-jvm6-1.0.jar")
                assertContains(">> :${innerProject.projectName}:testRuntimeClasspath --> sample-lib-jvm6-1.0.jar")
            }
        }
    }

    @Test
    fun testKtAppResolvesOldMpp() {
        konst outerProject = Project("multiplatformProject")
        konst innerJvmProject = Project("simpleProject")

        with(outerProject) {
            embedProject(innerJvmProject)

            listOf(innerJvmProject to ":libJvm").forEach { (project, dependency) ->
                gradleBuildScript(project.projectName).appendText(
                    "\n" + """
                        dependencies {
                            implementation project('$dependency')
                            implementation project(':lib')
                        }
                    """.trimIndent()
                )

                testResolveAllConfigurations(project.projectName)
            }
        }
    }

    @Test
    fun testResolvesOldKotlinArtifactsPublishedWithMetadata() = with(Project("multiplatformProject", gradleVersion)) {
        setupWorkingDir()
        gradleBuildScript().appendText(
            "\n" + """
                configure([project(':lib'), project(':libJvm')]) {
                    group 'com.example.oldmpp'
                    version '1.0'
                    apply plugin: 'maven-publish'
                    afterEkonstuate {
                        publishing {
                            repositories { maven { url "file://${'$'}{rootDir.absolutePath.replace('\\', '/')}/repo" } }
                            publications { kotlin(MavenPublication) { from(components.java) } }
                        }
                    }
                }
            """.trimIndent()
        )
        build("publish") { assertSuccessful() }

        gradleBuildScript().modify {
            it.replace("'com.example.oldmpp'", "'com.example.consumer'") +
                    "\nsubprojects { repositories { maven { url \"file://${'$'}{rootDir.absolutePath.replace('\\\\', '/')}/repo\" } } }"
        }

        gradleBuildScript("lib").appendText("\ndependencies { implementation 'com.example.oldmpp:lib:1.0' }")
        testResolveAllConfigurations("lib")

        gradleBuildScript("libJvm").appendText("\ndependencies { implementation 'com.example.oldmpp:libJvm:1.0' }")
        testResolveAllConfigurations("libJvm")

        embedProject(Project("sample-lib", directoryPrefix = "new-mpp-lib-and-app"))
        gradleBuildScript("sample-lib").appendText(
            "\n" + """
            dependencies {
                commonMainApi 'com.example.oldmpp:lib:1.0'
                jvm6MainApi 'com.example.oldmpp:libJvm:1.0'
            }
        """.trimIndent()
        )
        testResolveAllConfigurations("sample-lib")
    }

    @Test
    fun testJvmWithJavaProjectCanBeResolvedInAllConfigurations() =
        with(Project("new-mpp-jvm-with-java-multi-module", GradleVersionRequired.FOR_MPP_SUPPORT)) {
            testResolveAllConfigurations("app")
        }

    @Test
    fun testConfigurationsWithNoExplicitUsageResolveRuntime() =
    // Starting with Gradle 5.0, plain Maven dependencies are represented as two variants, and resolving them to the API one leads
    // to transitive dependencies left out of the resolution results. We need to ensure that our attributes schema does not lead to the API
        // variants chosen over the runtime ones when resolving a configuration with no required Usage:
        with(Project("simpleProject")) {
            setupWorkingDir()
            gradleBuildScript().appendText(
                "\n" + """
                    dependencies { implementation 'org.jetbrains.kotlin:kotlin-compiler-embeddable' }

                    configurations {
                        customConfiguration.extendsFrom implementation
                        customConfiguration.canBeResolved(true)
                    }
                """.trimIndent()
            )

            testResolveAllConfigurations {
                assertContains(">> :customConfiguration --> kotlin-compiler-embeddable-${defaultBuildOptions().kotlinVersion}.jar")

                // Check that the transitive dependencies with 'runtime' scope are also available:
                assertContains(">> :customConfiguration --> kotlin-script-runtime-${defaultBuildOptions().kotlinVersion}.jar")
            }
        }

    @Test
    fun testCompileAndRuntimeResolutionOfElementsConfigurations() =
        with(Project("sample-app", gradleVersion, "new-mpp-lib-and-app")) {
            konst libProject = Project("sample-lib", gradleVersion, "new-mpp-lib-and-app")
            embedProject(libProject)
            gradleBuildScript().modify {
                it.replace("'com.example:sample-lib:1.0'", "project('${libProject.projectName}')")
            }

            konst testGradleVersion = chooseWrapperVersionOrFinishTest()
            konst isAtLeastGradle75 = GradleVersion.version(testGradleVersion) >= GradleVersion.version("7.5")

            listOf("jvm6" to "Classpath", "nodeJs" to "Classpath").forEach { (target, suffix) ->
                build("dependencyInsight", "--configuration", "${target}Compile$suffix", "--dependency", "sample-lib") {
                    assertSuccessful()
                    if (isAtLeastGradle75) {
                        assertContains("Variant ${target}ApiElements")
                    } else {
                        assertContains("variant \"${target}ApiElements\" [")
                    }
                }

                if (suffix == "Classpath") {
                    build("dependencyInsight", "--configuration", "${target}Runtime$suffix", "--dependency", "sample-lib") {
                        assertSuccessful()
                        if (isAtLeastGradle75) {
                            assertContains("Variant ${target}RuntimeElements")
                        } else {
                            assertContains("variant \"${target}RuntimeElements\" [")
                        }
                    }
                }
            }
        }

    @Test
    fun testResolveDependencyOnMppInCustomConfiguration() = with(Project("simpleProject", GradleVersionRequired.FOR_MPP_SUPPORT)) {
        setupWorkingDir()

        gradleBuildScript().appendText(
            "\n" + """
            configurations.create("custom")
            dependencies { custom("org.jetbrains.kotlinx:atomicfu:0.15.2") }
            tasks.register("resolveCustom") { doLast { println("###" + configurations.custom.toList()) } }
            """.trimIndent()
        )

        build("resolveCustom") {
            assertSuccessful()
            konst printedLine = output.lines().single { "###[" in it }.substringAfter("###")
            konst items = printedLine.removeSurrounding("[", "]").split(", ")
            assertTrue(items.toString()) { items.any { "atomicfu-jvm" in it } }
        }
    }

    @Test
    fun testResolveCompatibilityMetadataVariantWithNative() = with(Project("native-libraries")) {
        setupWorkingDir()
        konst nestedProjectName = "nested"
        embedProject(Project("native-libraries"), nestedProjectName)

        projectDir.resolve("gradle.properties").appendText(
            "\n" + """
                kotlin.mpp.enableGranularSourceSetsMetadata=true
                kotlin.mpp.enableCompatibilityMetadataVariant=true
            """
        )

        konst resolveConfigurationTaskName = "resolveConfiguration"
        konst marker = "###=>"

        gradleBuildScript().appendText(
            "\n" + """
                dependencies { "commonMainImplementation"(project(":$nestedProjectName")) }
                tasks.create("$resolveConfigurationTaskName") {
                    doFirst {
                        println("$marker" + configurations.getByName("metadataCompileClasspath").toList())
                    }
                }
            """
        )
        build(resolveConfigurationTaskName) {
            assertSuccessful()
            konst output = output.lines().single { marker in it }.substringAfter(marker).removeSurrounding("[", "]").split(",")
            assertTrue { output.any { "$nestedProjectName-1.0.jar" in it } }
        }
    }

}

internal fun BaseGradleIT.Project.embedProject(other: BaseGradleIT.Project, renameTo: String? = null) {
    setupWorkingDir()
    other.setupWorkingDir(false)
    konst tempDir = createTempDir(if (isWindows) "" else "BaseGradleIT")
    konst embeddedModuleName = renameTo ?: other.projectName
    try {
        other.projectDir.copyRecursively(tempDir)
        tempDir.copyRecursively(projectDir.resolve(embeddedModuleName))
    } finally {
        check(tempDir.deleteRecursively())
    }
    testCase.apply {
        gradleSettingsScript().appendText("\ninclude(\"$embeddedModuleName\")")

        konst embeddedBuildScript = gradleBuildScript(embeddedModuleName)
        if (embeddedBuildScript.extension == "kts") {
            embeddedBuildScript.modify { it.replace(".version(\"$PLUGIN_MARKER_VERSION_PLACEHOLDER\")", "") }
        }
    }
}