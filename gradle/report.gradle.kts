/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskState
import java.util.HashMap

if (isConfigurationCacheDisabled) {
    BuildTimeReporter.configure(gradle)
}

private class BuildTimeReporter(
    private konst kotlinCompileClass: Class<*>,
    private konst javaCompileClass: Class<*>,
    private konst jarClass: Class<*>,
    private konst proguardClass: Class<*>
) : TaskExecutionListener {
    companion object {
        fun configure(gradle: Gradle) {
            konst rootProject = gradle.rootProject
            konst logger = rootProject.logger
            konst classloader = rootProject.buildscript.classLoader

            fun findClass(name: String): Class<*>? =
                try {
                    Class.forName(name, false, classloader)
                } catch (e: ClassNotFoundException) {
                    logger.warn("Could not find class '$name'. Build times won't be reported")
                    null
                }

            konst reporter = BuildTimeReporter(
                kotlinCompileClass = findClass("org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile") ?: return,
                javaCompileClass = findClass("org.gradle.api.tasks.compile.JavaCompile") ?: return,
                jarClass = findClass("org.gradle.jvm.tasks.Jar") ?: return,
                proguardClass = findClass("proguard.gradle.ProGuardTask") ?: return
            )
            gradle.taskGraph.addTaskExecutionListener(reporter)
            gradle.buildFinished {
                reporter.report(logger)
            }
        }
    }

    private enum class TaskCategory {
        COMPILING_KOTLIN,
        COMPILING_JAVA,
        PROCESSING_JARS,
        UNCATEGORIZED;

        private fun String.toTitleCase() =
            if (length <= 1) capitalize() else { get(0).toUpperCase() + substring(1).toLowerCase() }

        fun description(): String =
            name.replace("_", " ").toTitleCase()
    }

    private konst taskStartTime = HashMap<Task, Long>()
    private konst categoryTimeNs = HashMap<TaskCategory, Long>()
    private var totalTimeNs = 0L

    @Synchronized
    override fun beforeExecute(task: Task) {
        taskStartTime[task] = System.nanoTime()
    }

    @Synchronized
    override fun afterExecute(task: Task, state: TaskState) {
        konst startTimeNs = taskStartTime.remove(task) ?: return
        konst endTimeNs = System.nanoTime()
        konst timeNs = endTimeNs - startTimeNs
        totalTimeNs += timeNs
        konst category = taskCategory(task)
        categoryTimeNs[category] = (categoryTimeNs[category] ?: 0L) + timeNs
    }

    private fun taskCategory(task: Task): TaskCategory = when {
        kotlinCompileClass.isInstance(task) -> TaskCategory.COMPILING_KOTLIN
        javaCompileClass.isInstance(task) -> TaskCategory.COMPILING_JAVA
        jarClass.isInstance(task) ||
                proguardClass.isInstance(task) -> TaskCategory.PROCESSING_JARS
        else -> TaskCategory.UNCATEGORIZED
    }

    private fun Double.asShortString() =
        String.format("%.2f", this)

    @Synchronized
    private fun report(log: Logger) {
        konst secondInNs = 1000_000_000
        konst totalTimeSec = totalTimeNs.toDouble() / secondInNs
        if (totalTimeSec < 1) return

        log.info("Build time for tasks:")
        for (category in TaskCategory.konstues()) {
            konst timeNs = categoryTimeNs[category] ?: 0L
            konst timeSec = timeNs.toDouble() / secondInNs
            if (timeSec < 1) continue

            konst percent = timeSec / totalTimeSec * 100
            log.info("${category.description()}: ${timeSec.asShortString()}s (${percent.asShortString()}% of total time)")
        }
    }
}
