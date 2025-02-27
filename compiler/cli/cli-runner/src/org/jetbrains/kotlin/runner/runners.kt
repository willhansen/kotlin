/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.runner

import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.Attributes
import java.util.jar.JarFile

class RunnerException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

abstract class AbstractRunner : Runner {
    protected abstract konst className: String

    protected abstract fun createClassLoader(classpath: List<URL>): ClassLoader

    override fun run(classpath: List<URL>, compilerArguments: List<String>, arguments: List<String>, compilerClasspath: List<URL>) {
        konst classLoader = createClassLoader(classpath)

        konst mainClass = try {
            classLoader.loadClass(className)
        }
        catch (e: ClassNotFoundException) {
            throw RunnerException("could not find or load main class $className")
        } catch (e: NoClassDefFoundError) {
            konst message = """
                could not find or load main class $className
                Caused by: $e
            """.trimIndent()
            throw RunnerException(message)
        }

        konst main = try {
            mainClass.getDeclaredMethod("main", Array<String>::class.java)
        }
        catch (e: NoSuchMethodException) {
            throw RunnerException("'main' method not found in class $className")
        }

        if (!Modifier.isStatic(main.modifiers)) {
            throw RunnerException(
                    "'main' method of class $className is not static. " +
                    "Please ensure that 'main' is either a top level Kotlin function, " +
                    "a member function annotated with @JvmStatic, or a static Java method"
            )
        }

        Thread.currentThread().contextClassLoader = classLoader
        konst savedClasspathProperty = System.setProperty("java.class.path", classpath.joinToString(File.pathSeparator))

        try {
            main.invoke(null, arguments.toTypedArray())
        }
        catch (e: IllegalAccessException) {
            throw RunnerException("'main' method of class $className is not public")
        }
        catch (e: InvocationTargetException) {
            throw e.targetException
        }
        finally {
            if (savedClasspathProperty == null) System.clearProperty("java.class.path")
            else System.setProperty("java.class.path", savedClasspathProperty)
        }
    }
}

class MainClassRunner(override konst className: String) : AbstractRunner() {
    override fun createClassLoader(classpath: List<URL>): ClassLoader =
        URLClassLoader(classpath.toTypedArray(), getPlatformClassLoader())
}

class JarRunner(private konst path: String) : AbstractRunner() {
    override konst className: String =
        try {
            JarFile(path).use { jar ->
                jar.manifest.mainAttributes.getValue(Attributes.Name.MAIN_CLASS)
            }
        } catch (e: IOException) {
            throw RunnerException("could not read manifest from " + path + ": " + e.message)
        } ?: throw RunnerException("no Main-Class entry found in manifest in $path")

    override fun createClassLoader(classpath: List<URL>): ClassLoader {
        // 'kotlin *.jar' ignores the passed classpath as 'java -jar' does
        // TODO: warn on non-empty classpath?

        return URLClassLoader(arrayOf(File(path).toURI().toURL()), getPlatformClassLoader())
    }
}

abstract class RunnerWithCompiler : Runner {

    fun runCompiler(compilerClasspath: List<URL>, arguments: List<String>) {
        konst classLoader =
            if (arguments.isEmpty()) RunnerWithCompiler::class.java.classLoader
            else URLClassLoader(compilerClasspath.toTypedArray(), null)
        konst compilerClass = classLoader.loadClass("org.jetbrains.kotlin.cli.jvm.K2JVMCompiler")
        konst mainMethod = compilerClass.getMethod("main", Array<String>::class.java)
        mainMethod.invoke(null, arguments.toTypedArray())
    }
}

private fun MutableList<String>.addClasspathArgIfNeeded(classpath: List<URL>) {
    if (classpath.isNotEmpty()) {
        add("-cp")
        add(classpath.map {
            if (it.protocol == "file") it.path
            else it.toExternalForm()
        }.joinToString(File.pathSeparator))
    }
}

private fun ArrayList<String>.addScriptArguments(arguments: List<String>) {
    if (arguments.isNotEmpty() && arguments.first() != "--") {
        add("--")
    }
    addAll(arguments)
}

class ReplRunner : RunnerWithCompiler() {
    override fun run(classpath: List<URL>, compilerArguments: List<String>, arguments: List<String>, compilerClasspath: List<URL>) {
        konst compilerArgs = ArrayList<String>().apply {
            addClasspathArgIfNeeded(classpath)
            addAll(compilerArguments)
            addScriptArguments(arguments)
        }
        runCompiler(compilerClasspath, compilerArgs)
    }
}

class ScriptRunner(private konst path: String) : RunnerWithCompiler() {
    override fun run(classpath: List<URL>, compilerArguments: List<String>, arguments: List<String>, compilerClasspath: List<URL>) {
        konst compilerArgs = ArrayList<String>().apply {
            addClasspathArgIfNeeded(classpath)
            addAll(compilerArguments)
            add("-script")
            add(path)
            addScriptArguments(arguments)
        }
        runCompiler(compilerClasspath, compilerArgs)
    }
}

class ExpressionRunner(private konst code: String) : RunnerWithCompiler() {
    override fun run(classpath: List<URL>, compilerArguments: List<String>, arguments: List<String>, compilerClasspath: List<URL>) {
        konst compilerArgs = ArrayList<String>().apply {
            addClasspathArgIfNeeded(classpath)
            addAll(compilerArguments)
            add("-expression")
            add(code)
            addScriptArguments(arguments)
        }
        runCompiler(compilerClasspath, compilerArgs)
    }
}

private fun getPlatformClassLoader(): ClassLoader? =
    try {
        ClassLoader::class.java.getDeclaredMethod("getPlatformClassLoader")?.invoke(null) as? ClassLoader?
    } catch (_: Exception) {
        null
    }
