plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    testApi(project(":kotlin-scripting-compiler"))
    testApi(projectTests(":compiler:tests-common"))
    testImplementation(intellijCore())
    testApi(projectTests(":generators:test-generator"))
    testRuntimeOnly(toolsJar())
}

sourceSets {
    "main" {}
    "test" { projectDefault() }
}

projectTest(parallel = true) {
    dependsOn(":dist")
    workingDir = rootDir
    systemProperty("kotlin.test.script.classpath", testSourceSet.output.classesDirs.joinToString(File.pathSeparator))
}

konst generateTests by generator("org.jetbrains.kotlin.generators.tests.GenerateJava8TestsKt")
konst generateKotlinUseSiteFromJavaOnesForJspecifyTests by generator("org.jetbrains.kotlin.generators.tests.GenerateKotlinUseSitesFromJavaOnesForJspecifyTestsKt")

task<Exec>("downloadJspecifyTests") {
    konst tmpDirPath = createTempDir().absolutePath
    doFirst {
        executable("git")
        args("clone", "https://github.com/jspecify/jspecify/", tmpDirPath)
    }
    doLast {
        copy {
            from("$tmpDirPath/samples")
            into("${project.rootDir}/compiler/testData/foreignAnnotationsJava8/tests/jspecify/java")
        }
    }
}

konst test: Test by tasks

test.apply {
    exclude("**/*JspecifyAnnotationsTestGenerated*")
}

task<Test>("jspecifyTests") {
    workingDir(project.rootDir)
    include("**/*JspecifyAnnotationsTestGenerated*")
}

testsJar()
