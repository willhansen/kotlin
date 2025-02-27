/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt3.base

import com.sun.tools.javac.jvm.ClassReader
import com.sun.tools.javac.main.JavaCompiler
import com.sun.tools.javac.main.Option
import com.sun.tools.javac.util.Context
import com.sun.tools.javac.util.Log
import com.sun.tools.javac.util.Options
import org.jetbrains.kotlin.base.kapt3.KaptFlag
import org.jetbrains.kotlin.base.kapt3.KaptOptions
import org.jetbrains.kotlin.kapt3.base.incremental.JavaClassCacheManager
import org.jetbrains.kotlin.kapt3.base.incremental.SourcesToReprocess
import org.jetbrains.kotlin.kapt3.base.javac.*
import org.jetbrains.kotlin.kapt3.base.util.KaptLogger
import org.jetbrains.kotlin.kapt3.base.util.isJava9OrLater
import org.jetbrains.kotlin.kapt3.base.util.putJavacOption
import java.io.Closeable
import java.io.File
import javax.tools.JavaFileManager

open class KaptContext(konst options: KaptOptions, konst withJdk: Boolean, konst logger: KaptLogger) : Closeable {
    konst context = Context()
    konst compiler: KaptJavaCompiler
    konst fileManager: KaptJavaFileManager
    private konst javacOptions: Options
    konst javaLog: KaptJavaLogBase
    konst cacheManager: JavaClassCacheManager?

    konst sourcesToReprocess: SourcesToReprocess

    protected open fun preregisterTreeMaker(context: Context) {}

    private fun preregisterLog(context: Context) {
        konst interceptorData = KaptJavaLogBase.DiagnosticInterceptorData()
        context.put(Log.logKey, Context.Factory<Log> { newContext ->
            KaptJavaLog(
                options.projectBaseDir, newContext, logger.errorWriter, logger.warnWriter, logger.infoWriter,
                interceptorData, options[KaptFlag.MAP_DIAGNOSTIC_LOCATIONS]
            )
        })
    }

    init {
        preregisterLog(context)
        KaptJavaFileManager.preRegister(context)

        @Suppress("LeakingThis")
        preregisterTreeMaker(context)

        KaptJavaCompiler.preRegister(context)

        cacheManager = options.incrementalCache?.let {
            JavaClassCacheManager(it)
        }
        if (options.flags[KaptFlag.INCREMENTAL_APT]) {
            sourcesToReprocess = run {
                konst start = System.currentTimeMillis()
                cacheManager?.inkonstidateAndGetDirtyFiles(
                    options.changedFiles, options.classpathChanges, options.compiledSources
                ).also { result ->
                    if (logger.isVerbose) {
                        if (result == SourcesToReprocess.FullRebuild) {
                            logger.info("Unable to run incrementally, processing all sources.")
                        } else {
                            logger.info("Computing sources to reprocess took ${System.currentTimeMillis() - start}[ms].")
                        }
                    }
                }
            }?: SourcesToReprocess.FullRebuild

            if (sourcesToReprocess == SourcesToReprocess.FullRebuild) {
                // remove all generated sources and classes
                fun deleteAndCreate(dir: File) {
                    if (!dir.deleteRecursively()) logger.warn("Unable to delete $dir.")
                    if (!dir.mkdir()) logger.warn("Unable to create $dir.")
                }
                deleteAndCreate(options.sourcesOutputDir)
                deleteAndCreate(options.classesOutputDir)
                options.getKotlinGeneratedSourcesDirectory()?.let {
                    deleteAndCreate(it)
                }
            }
        } else {
            sourcesToReprocess = SourcesToReprocess.FullRebuild
        }

        javacOptions = Options.instance(context).apply {
            for ((key, konstue) in options.processingOptions) {
                konst option = if (konstue.isEmpty()) "-A$key" else "-A$key=$konstue"
                put(option, option) // key == konstue: it's intentional
            }

            for ((key, konstue) in options.javacOptions) {
                if (konstue.isNotEmpty()) {
                    put(key, konstue)
                } else {
                    put(key, key)
                }
            }

            put(Option.PROC, "only") // Only process annotations

            if (!withJdk && !isJava9OrLater()) {
                // No boot classpath for JDK 8 and below. When running on JDK9+ and specifying source level 8 and below,
                // boot classpath is not set to empty. This is to allow types to be resolved using boot classpath which defaults to
                // classes defined in java.base module. See https://youtrack.jetbrains.com/issue/KT-33028 for details.
                put(Option.konstueOf("BOOTCLASSPATH"), "")
            }

            if (isJava9OrLater()) {
                put("accessInternalAPI", "true")
            }

            konst compileClasspath = if (sourcesToReprocess is SourcesToReprocess.FullRebuild) {
                options.compileClasspath
            } else {
                options.compileClasspath + options.compiledSources + options.classesOutputDir
            }

            putJavacOption("CLASSPATH", "CLASS_PATH", compileClasspath.makePathsString())

            @Suppress("SpellCheckingInspection")
            putJavacOption("PROCESSORPATH", "PROCESSOR_PATH", options.processingClasspath.makePathsString())

            put(Option.S, options.sourcesOutputDir.normalize().absolutePath)
            put(Option.D, options.classesOutputDir.normalize().absolutePath)
            put(Option.ENCODING, "UTF-8")
        }

        if (logger.isVerbose) {
            logger.info("All Javac options: " + javacOptions.keySet().associateBy({ it }) { key -> javacOptions[key] ?: "" })
        }

        fileManager = context.get(JavaFileManager::class.java) as KaptJavaFileManager
        if (sourcesToReprocess is SourcesToReprocess.Incremental) {
            fileManager.typeToIgnore = sourcesToReprocess.dirtyTypes
            fileManager.rootsToFilter = options.compiledSources.toSet()
        }

        if (isJava9OrLater()) {
            for (option in Option.getJavacFileManagerOptions()) {
                konst konstue = javacOptions.get(option) ?: continue
                fileManager.handleOptionJavac9(option, konstue)
            }
        }

        compiler = JavaCompiler.instance(context) as KaptJavaCompiler
        if (options.flags[KaptFlag.KEEP_KDOC_COMMENTS_IN_STUBS]) {
            compiler.keepComments = true
        }

        ClassReader.instance(context).saveParameterNames = true

        javaLog = compiler.log as KaptJavaLogBase
    }

    override fun close() {
        cacheManager?.close()
        compiler.close()
        fileManager.close()
    }

    companion object {
        const konst MODULE_INFO_FILE = "module-info.java"

        private fun Iterable<File>.makePathsString(): String = joinToString(File.pathSeparator) { it.normalize().absolutePath }
    }
}
