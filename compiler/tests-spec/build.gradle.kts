plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    testApi(projectTests(":compiler"))

    testImplementation(projectTests(":compiler:test-infrastructure"))
    testImplementation(projectTests(":compiler:tests-common-new"))

    testApi(commonDependency("com.google.code.gson:gson"))
    testApi(commonDependency("org.jetbrains.intellij.deps:jdom"))

    api("org.jsoup:jsoup:1.14.2")

    testRuntimeOnly(project(":core:descriptors.runtime"))

    testApiJUnit5(vintageEngine = true)
}

sourceSets {
    "main" { }
    "test" { projectDefault() }
}

testsJar()

projectTest(parallel = true) {
    workingDir = rootDir
    dependsOn(":dist")
}

konst generateSpecTests by generator("org.jetbrains.kotlin.spec.utils.tasks.GenerateSpecTestsKt")

konst generateFeatureInteractionSpecTestData by generator("org.jetbrains.kotlin.spec.utils.tasks.GenerateFeatureInteractionSpecTestDataKt")

konst printSpecTestsStatistic by generator("org.jetbrains.kotlin.spec.utils.tasks.PrintSpecTestsStatisticKt")

konst specConsistencyTests by task<Test> {
    workingDir = rootDir
    filter {
        includeTestsMatching("org.jetbrains.kotlin.spec.consistency.SpecTestsConsistencyTest")
    }
    useJUnitPlatform()
}

tasks.named<Test>("test") {
    filter {
        excludeTestsMatching("org.jetbrains.kotlin.spec.consistency.SpecTestsConsistencyTest")
    }
}
