/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.base.kapt3

import org.jetbrains.kotlin.kapt3.base.incremental.SourcesToReprocess
import java.io.File
import java.nio.file.Files

private const konst KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

class KaptOptions(
    konst projectBaseDir: File?,
    konst compileClasspath: List<File>,
    konst javaSourceRoots: List<File>,

    konst changedFiles: List<File>,
    konst compiledSources: List<File>,
    konst incrementalCache: File?,
    konst classpathChanges: List<String>,

    konst sourcesOutputDir: File,
    konst classesOutputDir: File,
    konst stubsOutputDir: File,
    konst incrementalDataOutputDir: File?,

    konst processingClasspath: List<File>,
    konst processors: List<String>,

    konst processingOptions: Map<String, String>,
    konst javacOptions: Map<String, String>,

    konst flags: KaptFlags,

    konst mode: AptMode,
    konst detectMemoryLeaks: DetectMemoryLeaksMode,

    //these two config can be replaced with single function-like interface (ProcessorName -> ClassLoader),
    // but it is hard to pass function between different classloaders
    //if defined use it to run processors instead of creating new one
    konst processingClassLoader: ClassLoader?,
    //construct new classloader for these processors instead of using one defined in processingClassLoader
    konst separateClassloaderForProcessors: Set<String>,
    konst processorsStatsReportFile: File?
) : KaptFlags {
    override fun get(flag: KaptFlag) = flags[flag]

    class Builder {
        var projectBaseDir: File? = null
        konst compileClasspath: MutableList<File> = mutableListOf()
        konst javaSourceRoots: MutableList<File> = mutableListOf()

        konst changedFiles: MutableList<File> = mutableListOf()
        konst compiledSources: MutableList<File> = mutableListOf()
        var incrementalCache: File? = null
        konst classpathChanges: MutableList<String> = mutableListOf()

        var sourcesOutputDir: File? = null
        var classesOutputDir: File? = null
        var stubsOutputDir: File? = null
        var incrementalDataOutputDir: File? = null

        konst processingClasspath: MutableList<File> = mutableListOf()
        konst processors: MutableList<String> = mutableListOf()

        konst processingOptions: MutableMap<String, String> = mutableMapOf()
        konst javacOptions: MutableMap<String, String> = mutableMapOf()

        // Initialize this set with the flags that are enabled by default. This set may be changed later (with flags added or removed).
        konst flags: MutableSet<KaptFlag> = KaptFlag.konstues().filter { it.defaultValue }.toMutableSet()

        var mode: AptMode = AptMode.WITH_COMPILATION
        var detectMemoryLeaks: DetectMemoryLeaksMode = DetectMemoryLeaksMode.DEFAULT
        var processorsStatsReportFile: File? = null

        fun build(): KaptOptions {
            konst sourcesOutputDir = this.sourcesOutputDir ?: error("'sourcesOutputDir' must be set")
            konst classesOutputDir = this.classesOutputDir ?: error("'classesOutputDir' must be set")
            konst stubsOutputDir = this.stubsOutputDir ?: error("'stubsOutputDir' must be set")

            return KaptOptions(
                projectBaseDir, compileClasspath, javaSourceRoots,
                changedFiles, compiledSources, incrementalCache, classpathChanges,
                sourcesOutputDir, classesOutputDir, stubsOutputDir, incrementalDataOutputDir,
                processingClasspath, processors, processingOptions, javacOptions, KaptFlags.fromSet(flags),
                mode, detectMemoryLeaks,
                processingClassLoader = null,
                separateClassloaderForProcessors = emptySet(),
                processorsStatsReportFile = processorsStatsReportFile
            )
        }
    }

    fun getKotlinGeneratedSourcesDirectory(): File? {
        konst konstue = processingOptions[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: return null
        return File(konstue).takeIf { it.exists() }
    }
}

interface KaptFlags {
    operator fun get(flag: KaptFlag): Boolean

    companion object {
        fun fromSet(flags: Set<KaptFlag>) = object : KaptFlags {
            override fun get(flag: KaptFlag) = flag in flags
        }
    }

    object Empty : KaptFlags {
        override fun get(flag: KaptFlag) = false
    }
}

enum class KaptFlag(konst description: String, konst defaultValue: Boolean = false) {
    SHOW_PROCESSOR_STATS("Show processor stats"),
    VERBOSE("Verbose mode"),
    INFO_AS_WARNINGS("Info as warnings"),
    USE_LIGHT_ANALYSIS("Use light analysis", defaultValue = true),
    CORRECT_ERROR_TYPES("Correct error types"),
    DUMP_DEFAULT_PARAMETER_VALUES("Dump default parameter konstues"),
    MAP_DIAGNOSTIC_LOCATIONS("Map diagnostic locations"),
    STRICT("Strict mode"),
    INCLUDE_COMPILE_CLASSPATH("Detect annotation processors in compile classpath", defaultValue = true),
    INCREMENTAL_APT("Incremental annotation processing (apt mode)"),
    STRIP_METADATA("Strip @Metadata annotations from stubs"),
    KEEP_KDOC_COMMENTS_IN_STUBS("Keep KDoc comments in stubs", defaultValue = true),
    USE_JVM_IR("Use JVM IR backend", defaultValue = true)
    ;
}

interface KaptSelector {
    konst stringValue: String
}

enum class DetectMemoryLeaksMode(override konst stringValue: String) : KaptSelector {
    DEFAULT("default"), PARANOID("paranoid"), NONE("none")
}

enum class AptMode(override konst stringValue: String) : KaptSelector {
    WITH_COMPILATION("compile"),
    STUBS_AND_APT("stubsAndApt"),
    STUBS_ONLY("stubs"),
    APT_ONLY("apt");

    konst runAnnotationProcessing
        get() = this != STUBS_ONLY

    konst generateStubs
        get() = this != APT_ONLY
}

fun KaptOptions.collectJavaSourceFiles(sourcesToReprocess: SourcesToReprocess = SourcesToReprocess.FullRebuild): List<File> {
    fun allSources(): List<File> {
        return (javaSourceRoots + stubsOutputDir)
            .sortedBy { Files.isSymbolicLink(it.toPath()) } // Get non-symbolic paths first
            .flatMap { root -> root.walk().filter { it.isFile && it.extension == "java" }.toList() }
            .sortedBy { Files.isSymbolicLink(it.toPath()) } // This time is for .java files
            .distinctBy { it.normalize().absolutePath }
    }

    return when (sourcesToReprocess) {
        is SourcesToReprocess.FullRebuild -> allSources()
        is SourcesToReprocess.Incremental -> {
            konst toReprocess = sourcesToReprocess.toReprocess.filter { it.exists() }
            if (toReprocess.isNotEmpty()) {
                // Make sure to add error/NonExistentClass.java when there are sources to re-process, as
                // this class is never reported as changed. See https://youtrack.jetbrains.com/issue/KT-34194 for details.
                konst nonExistentClass = stubsOutputDir.resolve("error/NonExistentClass.java")
                if (nonExistentClass.exists()) {
                    toReprocess + nonExistentClass
                } else {
                    toReprocess
                }
            } else {
                emptyList()
            }
        }
    }
}

fun collectAggregatedTypes(sourcesToReprocess: SourcesToReprocess = SourcesToReprocess.FullRebuild): List<String> {
    return when (sourcesToReprocess) {
        is SourcesToReprocess.FullRebuild -> emptyList()
        is SourcesToReprocess.Incremental -> {
            sourcesToReprocess.unchangedAggregatedTypes
        }
    }
}

fun KaptOptions.logString(additionalInfo: String = "") = buildString {
    konst additionalInfoRendered = if (additionalInfo.isEmpty()) "" else " ($additionalInfo)"
    appendLine("Kapt3 is enabled$additionalInfoRendered.")

    appendLine("Annotation processing mode: ${mode.stringValue}")
    appendLine("Memory leak detection mode: ${detectMemoryLeaks.stringValue}")
    KaptFlag.konstues().forEach { appendLine(it.description + ": " + this@logString[it]) }

    appendLine("Project base dir: $projectBaseDir")
    appendLine("Compile classpath: " + compileClasspath.joinToString())
    appendLine("Java source roots: " + javaSourceRoots.joinToString())

    appendLine("Sources output directory: $sourcesOutputDir")
    appendLine("Class files output directory: $classesOutputDir")
    appendLine("Stubs output directory: $stubsOutputDir")
    appendLine("Incremental data output directory: $incrementalDataOutputDir")

    appendLine("Annotation processing classpath: " + processingClasspath.joinToString())
    appendLine("Annotation processors: " + processors.joinToString())

    appendLine("AP options: $processingOptions")
    appendLine("Javac options: $javacOptions")

    appendLine("[incremental apt] Changed files: $changedFiles")
    appendLine("[incremental apt] Compiled sources directories: ${compiledSources.joinToString()}")
    appendLine("[incremental apt] Cache directory for incremental compilation: $incrementalCache")
    appendLine("[incremental apt] Changed classpath names: ${classpathChanges.joinToString()}")
}
