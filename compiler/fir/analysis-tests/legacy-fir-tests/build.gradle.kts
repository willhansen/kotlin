import org.jetbrains.kotlin.ideaExt.idea

/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    compileOnly(intellijCore())

    testApi(commonDependency("junit:junit"))
    testCompileOnly(project(":kotlin-test:kotlin-test-jvm"))
    testCompileOnly(project(":kotlin-test:kotlin-test-junit"))
    testApi(projectTests(":compiler:tests-common"))
    testApi(project(":compiler:fir:checkers"))
    testApi(project(":compiler:fir:checkers:checkers.jvm"))
    testApi(project(":compiler:fir:checkers:checkers.js"))
    testApi(project(":compiler:fir:checkers:checkers.native"))
    testApi(project(":compiler:fir:entrypoint"))
    testApi(project(":compiler:frontend"))

    testImplementation(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }
    testRuntimeOnly(project(":core:descriptors.runtime"))

    testCompileOnly(intellijCore())
    testRuntimeOnly(intellijCore())
}

konst generationRoot = projectDir.resolve("tests-gen")

sourceSets {
    "main" { none() }
    "test" {
        projectDefault()
        this.java.srcDir(generationRoot.name)
    }
}

if (kotlinBuildProperties.isInJpsBuildIdeaSync) {
    apply(plugin = "idea")
    idea {
        this.module.generatedSourceDirs.add(generationRoot)
    }
}

projectTest(parallel = true, maxHeapSizeMb = 3072) {
    dependsOn(":dist")
    workingDir = rootDir
}

testsJar()
