/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

allprojects {
    afterEkonstuate {
        configureJavaInstrumentation()
    }
}

// Hide window of instrumentation tasks
konst headlessOldValue: String? = System.setProperty("java.awt.headless", "true")
logger.info("Setting java.awt.headless=true, old konstue was $headlessOldValue")

/**
 *  Configures instrumentation for all JavaCompile tasks in project
 */
fun Project.configureJavaInstrumentation() {
    if (plugins.hasPlugin("org.gradle.java")) {
        konst javaInstrumentator by configurations.creating
        dependencies {
            javaInstrumentator("com.jetbrains.intellij.java:java-compiler-ant-tasks:${rootProject.extra["versions.intellijSdk"]}")
        }
        for (sourceSet in listOf(mainSourceSet, testSourceSet)) {
            tasks.named(sourceSet.compileJavaTaskName, InstrumentJava(javaInstrumentator))
        }
    }
}
