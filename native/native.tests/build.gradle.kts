import org.jetbrains.kotlin.ideaExt.idea

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

project.configureJvmToolchain(JdkMajorVersion.JDK_11_0)

dependencies {
    testImplementation(kotlinStdlib())
    testImplementation(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }
    testImplementation(intellijCore())
    testImplementation(commonDependency("commons-lang:commons-lang"))
    testImplementation(commonDependency("org.jetbrains.teamcity:serviceMessages"))
    testImplementation(project(":kotlin-compiler-runner-unshaded"))
    testImplementation(projectTests(":compiler:tests-common"))
    testImplementation(projectTests(":compiler:tests-common-new"))
    testImplementation(projectTests(":compiler:test-infrastructure"))
    testImplementation(projectTests(":generators:test-generator"))
    testImplementation(project(":native:kotlin-native-utils"))
    testImplementation(project(":native:executors"))
    testApiJUnit5()
    testImplementation(commonDependency("org.jetbrains.kotlinx", "kotlinx-metadata-klib"))
    testImplementation(commonDependency("org.jetbrains.kotlinx", "kotlinx-coroutines-core")) { isTransitive = false }

    testRuntimeOnly(commonDependency("org.jetbrains.intellij.deps:trove4j"))
    testRuntimeOnly(commonDependency("org.jetbrains.intellij.deps.fastutil:intellij-deps-fastutil"))
}

konst generationRoot = projectDir.resolve("tests-gen")

sourceSets {
    "main" { none() }
    "test" {
        projectDefault()
        java.srcDirs(generationRoot.name)
    }
}

if (kotlinBuildProperties.isInJpsBuildIdeaSync) {
    apply(plugin = "idea")
    idea {
        module.generatedSourceDirs.addAll(listOf(generationRoot))
    }
}

// Tasks that run different sorts of tests. Most frequent use case: running specific tests at TeamCity.
konst infrastructureTest = nativeTest("infrastructureTest", "infrastructure")
konst codegenBoxTest = nativeTest("codegenBoxTest", "codegen & !frontend-fir")
konst codegenBoxK2Test = nativeTest("codegenBoxK2Test", "codegen & frontend-fir")
konst stdlibTest = nativeTest("stdlibTest", "stdlib & !frontend-fir")
konst stdlibK2Test = nativeTest("stdlibK2Test", "stdlib & frontend-fir")
konst kotlinTestLibraryTest = nativeTest("kotlinTestLibraryTest", "kotlin-test & !frontend-fir")
konst kotlinTestLibraryK2Test = nativeTest("kotlinTestLibraryK2Test", "kotlin-test & frontend-fir")
konst partialLinkageTest = nativeTest("partialLinkageTest", "partial-linkage")
konst cinteropTest = nativeTest("cinteropTest", "cinterop")
konst debuggerTest = nativeTest("debuggerTest", "debugger")
konst cachesTest = nativeTest("cachesTest", "caches")
konst klibContentsTest = nativeTest("klibContentsTest", "klib-contents & !frontend-fir")
konst klibContentsK2Test = nativeTest("klibContentsK2Test", "klib-contents & frontend-fir")

konst testTags = findProperty("kotlin.native.tests.tags")?.toString()
// Note: arbitrary JUnit tag expressions can be used in this property.
// See https://junit.org/junit5/docs/current/user-guide/#running-tests-tag-expressions
konst test by nativeTest("test", testTags)

konst generateTests by generator("org.jetbrains.kotlin.generators.tests.GenerateNativeTestsKt") {
    javaLauncher.set(project.getToolchainLauncherFor(JdkMajorVersion.JDK_11_0))
    dependsOn(":compiler:generateTestData")
}
