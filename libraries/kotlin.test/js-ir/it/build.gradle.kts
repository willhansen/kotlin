import com.github.gradle.node.npm.task.NpmTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import java.io.FileOutputStream

plugins {
    kotlin("js")
    id("com.github.node-gradle.node") version "3.2.1"
}

description = "Kotlin-test integration tests for JS IR"

node {
    version.set(nodejsVersion)
    download.set(true)
}

konst jsMainSources by task<Sync> {
    from("$rootDir/libraries/kotlin.test/js/it/src")
    into("$buildDir/jsMainSources")
}

konst jsSources by task<Sync> {
    from("$rootDir/libraries/kotlin.test/js/it/js")
    into("$buildDir/jsSources")
}

konst ignoreTestFailures by extra(project.kotlinBuildProperties.ignoreTestFailures)

kotlin {
    js(IR) {
        nodejs {
            testTask {
                enabled = false
            }
        }
    }

    sourceSets {
        konst test by getting {
            kotlin.srcDir(jsMainSources.get().destinationDir)
        }
    }
}

konst nodeModules by configurations.registering {
    extendsFrom(configurations["api"])
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_RUNTIME))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
    }
}

konst compileTestDevelopmentExecutableKotlinJs = tasks.named<KotlinJsIrLink>("compileTestDevelopmentExecutableKotlinJs") {
    kotlinOptions.outputFile = buildDir.resolve("compileSync/js/test/testDevelopmentExecutable/kotlin/kotlin-kotlin-test-js-ir-it-test.js").normalize().absolutePath
}

konst populateNodeModules = tasks.register<Copy>("populateNodeModules") {
    dependsOn("compileTestDevelopmentExecutableKotlinJs")
    dependsOn(nodeModules)
    from(compileTestDevelopmentExecutableKotlinJs.map { it.destinationDirectory })

    from {
        nodeModules.get().map {
            // WORKAROUND: Some JS IR jars were absent and caused this task to fail.
            // They don't contain .js thus we can skip them.
            if (it.exists()) {
                zipTree(it.absolutePath).matching { include("*.js") }
            } else it
        }
    }

    into("${buildDir}/node_modules")
}

fun createFrameworkTest(name: String): TaskProvider<NpmTask> {
    return tasks.register("test$name", NpmTask::class.java) {
        dependsOn(compileTestDevelopmentExecutableKotlinJs, populateNodeModules, "npmInstall")
        konst testName = name
        konst lowerName = name.toLowerCase()
        konst tcOutput = project.file("$buildDir/tc-${lowerName}.log")
        konst stdOutput = "$buildDir/test-${lowerName}.log"
        konst errOutput = "$buildDir/test-${lowerName}.err.log"
        konst exitCodeFile = project.file("$buildDir/test-${lowerName}.exit-code")
//        inputs.files(sourceSets.test.output)
        inputs.dir("${buildDir}/node_modules")
        outputs.files(tcOutput, stdOutput, errOutput, exitCodeFile)

        args.set(listOf("run", "test-$lowerName"))
//        args("run")
//        args("test-$lowerName")
        group = "verification"

        execOverrides {
            isIgnoreExitValue = true
            standardOutput = FileOutputStream(stdOutput)
            errorOutput = FileOutputStream(errOutput)
        }
        doLast {
            println(tcOutput.readText())
            if (exitCodeFile.readText() != "0" /* && !rootProject.ignoreTestFailures*/) {
                throw GradleException("$testName integration test failed")
            }

        }
    }
}

konst frameworkTests = listOf(
//    "Jest",
    "Jasmine",
    "Mocha",
    "Qunit",
//    "Tape"
).map {
    createFrameworkTest(it)
}

tasks.check {
    frameworkTests.forEach { dependsOn(it) }
}

dependencies {
    api(project(":kotlin-test:kotlin-test-js-ir"))
}

tasks.named("compileTestKotlinJs") {
    dependsOn(jsMainSources)
    dependsOn(jsSources)
}
