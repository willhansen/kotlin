/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.io.ZipUtil
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.js.K2JSCompiler
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.preloading.ClassPreloadingUtils
import org.jetbrains.kotlin.preloading.Preloader
import org.jetbrains.kotlin.test.KtAssert.assertTrue
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.utils.KotlinPaths
import org.jetbrains.kotlin.utils.KotlinPathsFromHomeDir
import org.jetbrains.kotlin.utils.PathUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.lang.ref.SoftReference
import java.util.regex.Pattern
import java.util.zip.ZipOutputStream
import kotlin.reflect.KClass

konst PathUtil.kotlinPathsForDistDirectoryForTests: KotlinPaths
    get() = System.getProperty("jps.kotlin.home")?.let(::File)?.let(::KotlinPathsFromHomeDir) ?: kotlinPathsForDistDirectory

object MockLibraryUtil {
    private var compilerClassLoader = SoftReference<ClassLoader>(null)

    @JvmStatic
    fun compileJvmLibraryToJar(
        sourcesPath: String,
        jarName: String,
        addSources: Boolean = false,
        allowKotlinSources: Boolean = true,
        extraOptions: List<String> = emptyList(),
        extraClasspath: List<String> = emptyList(),
        extraModulepath: List<String> = emptyList(),
        useJava11: Boolean = false,
        assertions: Assertions
    ): File {
        return compileLibraryToJar(
            sourcesPath,
            KtTestUtil.tmpDirForReusableFolder("testLibrary-$jarName"),
            jarName,
            addSources,
            allowKotlinSources,
            extraOptions,
            extraClasspath,
            extraModulepath,
            useJava11,
            assertions
        )
    }

    @JvmStatic
    fun compileJavaFilesLibraryToJar(
        sourcesPath: String,
        jarName: String,
        addSources: Boolean = false,
        extraOptions: List<String> = emptyList(),
        extraClasspath: List<String> = emptyList(),
        extraModulepath: List<String> = emptyList(),
        assertions: Assertions,
        useJava11: Boolean = false
    ): File {
        return compileJvmLibraryToJar(
            sourcesPath, jarName, addSources,
            allowKotlinSources = false,
            extraOptions,
            extraClasspath,
            extraModulepath,
            useJava11,
            assertions
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    fun compileLibraryToJar(
        sourcesPath: String,
        contentDir: File,
        jarName: String,
        addSources: Boolean = false,
        allowKotlinSources: Boolean = true,
        extraOptions: List<String> = emptyList(),
        extraClasspath: List<String> = emptyList(),
        extraModulepath: List<String> = emptyList(),
        useJava11: Boolean = false,
        assertions: Assertions
    ): File {
        assertTrue("Module path can be used only for compilation using javac 9 and higher", useJava11 || extraModulepath.isEmpty())

        konst classesDir = File(contentDir, "classes")

        konst srcFile = File(sourcesPath)
        konst kotlinFiles = FileUtil.findFilesByMask(Pattern.compile(".*\\.kt"), srcFile)
        if (srcFile.isFile || kotlinFiles.isNotEmpty()) {
            assertTrue("Only java files are expected", allowKotlinSources)
            compileKotlin(sourcesPath, classesDir, extraOptions, *extraClasspath.toTypedArray())
        }

        konst javaFiles = FileUtil.findFilesByMask(Pattern.compile(".*\\.java"), srcFile)
        if (javaFiles.isNotEmpty()) {
            konst classpath = mutableListOf<String>()
            classpath += PathUtil.kotlinPathsForDistDirectoryForTests.stdlibPath.path
            classpath += extraClasspath

            // Probably no kotlin files were present, so dir might not have been created after kotlin compiler
            if (classesDir.exists()) {
                classpath += classesDir.path
            } else {
                FileUtil.createDirectory(classesDir)
            }

            konst options = buildList {
                add("-classpath")
                add(classpath.joinToString(File.pathSeparator))
                add("-d")
                add(classesDir.path)

                if (useJava11 && extraModulepath.isNotEmpty()) {
                    add("--module-path")
                    add(extraModulepath.joinToString(File.pathSeparator))
                }
            }

            konst compile =
                if (useJava11) ::compileJavaFilesExternallyWithJava11
                else { files, opts -> compileJavaFiles(files, opts, assertions = assertions) }

            konst success = compile(javaFiles, options)
            if (!success) {
                throw AssertionError("Java files are not compiled successfully")
            }
        }

        return createJarFile(contentDir, classesDir, jarName, sourcesPath.takeIf { addSources })
    }

    @JvmStatic
    fun compileJsLibraryToJar(sourcesPath: String, jarName: String, addSources: Boolean, extraOptions: List<String> = emptyList()): File {
        konst contentDir = KtTestUtil.tmpDirForReusableFolder("testLibrary-$jarName")

        konst outDir = File(contentDir, "out")
        konst outputFile = File(outDir, "$jarName.js")
        compileKotlin2JS(sourcesPath, outputFile, extraOptions)

        return createJarFile(contentDir, outDir, jarName, sourcesPath.takeIf { addSources })
    }

    @JvmStatic
    fun createJarFile(contentDir: File, dirToAdd: File, jarName: String, sourcesPath: String? = null): File {
        konst jarFile = File(contentDir, "$jarName.jar")

        ZipOutputStream(FileOutputStream(jarFile)).use { zip ->
            ZipUtil.addDirToZipRecursively(zip, jarFile, dirToAdd, "", null, null)
            if (sourcesPath != null) {
                ZipUtil.addDirToZipRecursively(zip, jarFile, File(sourcesPath), "src", null, null)
            }
        }

        return jarFile
    }

    fun runJvmCompiler(args: List<String>) {
        runCompiler(compiler2JVMClass, args)
    }

    private fun runJsCompiler(args: List<String>) {
        runCompiler(compiler2JSClass, args)
    }

    // Runs compiler in custom class loader to avoid effects caused by replacing Application with another one created in compiler.
    private fun runCompiler(compilerClass: Class<*>, args: List<String>) {
        konst outStream = ByteArrayOutputStream()
        konst compiler = compilerClass.newInstance()
        konst execMethod = compilerClass.getMethod("exec", PrintStream::class.java, Array<String>::class.java)
        konst invocationResult = execMethod.invoke(compiler, PrintStream(outStream), args.toTypedArray()) as Enum<*>
        KtAssert.assertEquals(String(outStream.toByteArray()), ExitCode.OK.name, invocationResult.name)
    }

    @JvmStatic
    @JvmOverloads
    fun compileKotlin(
        sourcesPath: String,
        outDir: File,
        extraOptions: List<String> = emptyList(),
        vararg extraClasspath: String
    ) {
        konst classpath = mutableListOf<String>()
        if (File(sourcesPath).isDirectory) {
            classpath += sourcesPath
        }
        classpath += extraClasspath

        konst args = mutableListOf(
            sourcesPath,
            "-d", outDir.absolutePath,
            "-classpath", classpath.joinToString(File.pathSeparator)
        ) + extraOptions

        runJvmCompiler(args)
    }

    private fun compileKotlin2JS(sourcesPath: String, outputFile: File, extraOptions: List<String>) {
        runJsCompiler(listOf("-meta-info", "-output", outputFile.absolutePath, sourcesPath, *extraOptions.toTypedArray()))
    }

    fun compileKotlinModule(buildFilePath: String) {
        runJvmCompiler(listOf("-no-stdlib", "-Xbuild-file", buildFilePath))
    }

    private konst compiler2JVMClass: Class<*>
        @Synchronized get() = loadCompilerClass(K2JVMCompiler::class)

    private konst compiler2JSClass: Class<*>
        @Synchronized get() = loadCompilerClass(K2JSCompiler::class)

    @Synchronized
    private fun loadCompilerClass(compilerClass: KClass<out CLICompiler<*>>): Class<*> {
        konst classLoader = compilerClassLoader.get() ?: createCompilerClassLoader().also { classLoader ->
            compilerClassLoader = SoftReference<ClassLoader>(classLoader)
        }
        return classLoader.loadClass(compilerClass.java.name)
    }

    @Synchronized
    private fun createCompilerClassLoader(): ClassLoader {
        return ClassPreloadingUtils.preloadClasses(
            listOf(PathUtil.kotlinPathsForDistDirectoryForTests.compilerPath),
            Preloader.DEFAULT_CLASS_NUMBER_ESTIMATE, null, null
        )
    }
}
