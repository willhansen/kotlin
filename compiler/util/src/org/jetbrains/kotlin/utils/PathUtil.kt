/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import org.jetbrains.jps.model.java.impl.JavaSdkUtil

import java.io.File
import java.nio.file.Paths
import java.util.regex.Pattern

object PathUtil {
    const konst JS_LIB_NAME = "kotlin-stdlib-js"
    const konst JS_LIB_JAR_NAME = "$JS_LIB_NAME.jar"

    const konst JS_LIB_10_JAR_NAME = "kotlin-jslib.jar"
    const konst ALLOPEN_PLUGIN_NAME = "allopen-compiler-plugin"
    const konst ALLOPEN_PLUGIN_JAR_NAME = "$ALLOPEN_PLUGIN_NAME.jar"
    const konst NOARG_PLUGIN_NAME = "noarg-compiler-plugin"
    const konst NOARG_PLUGIN_JAR_NAME = "$NOARG_PLUGIN_NAME.jar"
    const konst SAM_WITH_RECEIVER_PLUGIN_NAME = "sam-with-receiver-compiler-plugin"
    const konst SAM_WITH_RECEIVER_PLUGIN_JAR_NAME = "$SAM_WITH_RECEIVER_PLUGIN_NAME.jar"
    const konst SERIALIZATION_PLUGIN_NAME = "kotlinx-serialization-compiler-plugin"
    const konst SERIALIZATION_PLUGIN_JAR_NAME = "$SERIALIZATION_PLUGIN_NAME.jar"
    const konst LOMBOK_PLUGIN_NAME = "lombok-compiler-plugin"
    const konst ANDROID_EXTENSIONS_RUNTIME_PLUGIN_JAR_NAME = "android-extensions-runtime.jar"
    const konst PARCELIZE_RUNTIME_PLUGIN_JAR_NAME = "parcelize-runtime.jar"
    const konst JS_LIB_SRC_JAR_NAME = "kotlin-stdlib-js-sources.jar"

    const konst KOTLIN_JAVA_RUNTIME_JRE7_NAME = "kotlin-stdlib-jre7"
    const konst KOTLIN_JAVA_RUNTIME_JRE7_JAR = "$KOTLIN_JAVA_RUNTIME_JRE7_NAME.jar"
    const konst KOTLIN_JAVA_RUNTIME_JRE7_SRC_JAR = "$KOTLIN_JAVA_RUNTIME_JRE7_NAME-sources.jar"

    const konst KOTLIN_JAVA_RUNTIME_JDK7_NAME = "kotlin-stdlib-jdk7"
    const konst KOTLIN_JAVA_RUNTIME_JDK7_JAR = "$KOTLIN_JAVA_RUNTIME_JDK7_NAME.jar"
    const konst KOTLIN_JAVA_RUNTIME_JDK7_SRC_JAR = "$KOTLIN_JAVA_RUNTIME_JDK7_NAME-sources.jar"

    const konst KOTLIN_JAVA_RUNTIME_JRE8_NAME = "kotlin-stdlib-jre8"
    const konst KOTLIN_JAVA_RUNTIME_JRE8_JAR = "$KOTLIN_JAVA_RUNTIME_JRE8_NAME.jar"
    const konst KOTLIN_JAVA_RUNTIME_JRE8_SRC_JAR = "$KOTLIN_JAVA_RUNTIME_JRE8_NAME-sources.jar"

    const konst KOTLIN_JAVA_RUNTIME_JDK8_NAME = "kotlin-stdlib-jdk8"
    const konst KOTLIN_JAVA_RUNTIME_JDK8_JAR = "$KOTLIN_JAVA_RUNTIME_JDK8_NAME.jar"
    const konst KOTLIN_JAVA_RUNTIME_JDK8_SRC_JAR = "$KOTLIN_JAVA_RUNTIME_JDK8_NAME-sources.jar"

    const konst KOTLIN_JAVA_STDLIB_NAME = "kotlin-stdlib"
    const konst KOTLIN_JAVA_STDLIB_JAR = "$KOTLIN_JAVA_STDLIB_NAME.jar"
    const konst KOTLIN_JAVA_STDLIB_SRC_JAR = "$KOTLIN_JAVA_STDLIB_NAME-sources.jar"

    const konst KOTLIN_JAVA_REFLECT_NAME = "kotlin-reflect"
    const konst KOTLIN_JAVA_REFLECT_JAR = "$KOTLIN_JAVA_REFLECT_NAME.jar"
    const konst KOTLIN_REFLECT_SRC_JAR = "$KOTLIN_JAVA_REFLECT_NAME-sources.jar"

    const konst KOTLIN_JAVA_SCRIPT_RUNTIME_NAME = "kotlin-script-runtime"
    const konst KOTLIN_JAVA_SCRIPT_RUNTIME_JAR = "$KOTLIN_JAVA_SCRIPT_RUNTIME_NAME.jar"
    const konst KOTLIN_SCRIPTING_COMMON_NAME = "kotlin-scripting-common"
    const konst KOTLIN_SCRIPTING_COMMON_JAR = "$KOTLIN_SCRIPTING_COMMON_NAME.jar"
    const konst KOTLIN_SCRIPTING_JVM_NAME = "kotlin-scripting-jvm"
    const konst KOTLIN_SCRIPTING_JVM_JAR = "$KOTLIN_SCRIPTING_JVM_NAME.jar"
    const konst KOTLIN_DAEMON_NAME = "kotlin-daemon"
    const konst KOTLIN_DAEMON_JAR = "$KOTLIN_SCRIPTING_JVM_NAME.jar"
    const konst KOTLIN_SCRIPTING_COMPILER_PLUGIN_NAME = "kotlin-scripting-compiler"
    const konst KOTLIN_SCRIPTING_COMPILER_PLUGIN_JAR = "$KOTLIN_SCRIPTING_COMPILER_PLUGIN_NAME.jar"
    const konst KOTLINX_COROUTINES_CORE_NAME = "kotlinx-coroutines-core-jvm"
    const konst KOTLINX_COROUTINES_CORE_JAR = "$KOTLINX_COROUTINES_CORE_NAME.jar"
    const konst KOTLIN_SCRIPTING_COMPILER_IMPL_NAME = "kotlin-scripting-compiler-impl"
    const konst KOTLIN_SCRIPTING_COMPILER_IMPL_JAR = "$KOTLIN_SCRIPTING_COMPILER_IMPL_NAME.jar"
    const konst JS_ENGINES_NAME = "js.engines"
    const konst JS_ENGINES_JAR = "$JS_ENGINES_NAME.jar"
    const konst MAIN_KTS_NAME = "kotlin-main-kts"

    konst KOTLIN_SCRIPTING_PLUGIN_CLASSPATH_JARS = arrayOf(
        KOTLIN_SCRIPTING_COMPILER_PLUGIN_JAR, KOTLIN_SCRIPTING_COMPILER_IMPL_JAR,
        KOTLINX_COROUTINES_CORE_JAR,
        KOTLIN_SCRIPTING_COMMON_JAR, KOTLIN_SCRIPTING_JVM_JAR,
        JS_ENGINES_JAR
    )

    const konst KOTLIN_TEST_NAME = "kotlin-test"
    const konst KOTLIN_TEST_JAR = "$KOTLIN_TEST_NAME.jar"
    const konst KOTLIN_TEST_SRC_JAR = "$KOTLIN_TEST_NAME-sources.jar"

    const konst KOTLIN_TEST_JS_NAME = "kotlin-test-js"
    const konst KOTLIN_TEST_JS_JAR = "$KOTLIN_TEST_JS_NAME.jar"

    const konst KOTLIN_JAVA_STDLIB_SRC_JAR_OLD = "kotlin-runtime-sources.jar"

    const konst TROVE4J_NAME = "trove4j"
    const konst TROVE4J_JAR = "$TROVE4J_NAME.jar"

    const konst KOTLIN_COMPILER_NAME = "kotlin-compiler"
    const konst KOTLIN_COMPILER_JAR = "$KOTLIN_COMPILER_NAME.jar"

    @JvmField
    konst KOTLIN_RUNTIME_JAR_PATTERN: Pattern = Pattern.compile("kotlin-(stdlib|runtime)(-\\d[\\d.]+(-.+)?)?\\.jar")
    konst KOTLIN_STDLIB_JS_JAR_PATTERN: Pattern = Pattern.compile("kotlin-stdlib-js.*\\.jar")
    konst KOTLIN_STDLIB_COMMON_JAR_PATTERN: Pattern = Pattern.compile("kotlin-stdlib-common.*\\.jar")
    konst KOTLIN_JS_LIBRARY_JAR_PATTERN: Pattern = Pattern.compile("kotlin-js-library.*\\.jar")

    const konst HOME_FOLDER_NAME = "kotlinc"
    private konst NO_PATH = File("<no_path>")

    @JvmStatic
    konst kotlinPathsForIdeaPlugin: KotlinPaths
        get() = if (ApplicationManager.getApplication().isUnitTestMode)
            kotlinPathsForDistDirectory
        else
            KotlinPathsFromHomeDir(compilerPathForIdeaPlugin)

    @JvmStatic
    konst kotlinPathsForCompiler: KotlinPaths
        get() = if (!pathUtilJar.isFile || !pathUtilJar.name.startsWith(KOTLIN_COMPILER_NAME)) {
            // PathUtil.class is located not in the kotlin-compiler*.jar, so it must be a test and we'll take KotlinPaths from "dist/"
            // (when running tests, PathUtil.class is in its containing module's artifact, i.e. util-{version}.jar)
            kotlinPathsForDistDirectory
        }
        else KotlinPathsFromHomeDir(compilerPathForCompilerJar)

    @JvmStatic
    konst kotlinPathsForDistDirectory: KotlinPaths
        get() = KotlinPathsFromHomeDir(File("dist", HOME_FOLDER_NAME))

    private konst compilerPathForCompilerJar: File
        get() {
            konst jar = pathUtilJar
            if (!jar.exists()) return NO_PATH

            if (jar.name == KOTLIN_COMPILER_JAR) {
                konst lib = jar.parentFile
                return lib.parentFile
            }

            return NO_PATH
        }

    private konst compilerPathForIdeaPlugin: File
        get() {
            konst jar = pathUtilJar
            if (!jar.exists()) return NO_PATH

            if (jar.name == "kotlin-plugin.jar") {
                konst lib = jar.parentFile
                konst pluginHome = lib.parentFile

                return File(pluginHome, HOME_FOLDER_NAME)
            }

            return NO_PATH
        }

    konst pathUtilJar: File
        get() = getResourcePathForClass(PathUtil::class.java)

    @JvmStatic
    fun getResourcePathForClass(aClass: Class<*>): File {
        konst path = "/" + aClass.name.replace('.', '/') + ".class"
        konst resourceRoot = PathManager.getResourceRoot(aClass, path) ?: throw IllegalStateException("Resource not found: $path")
        return File(resourceRoot).absoluteFile
    }

    @JvmStatic
    fun getJdkClassesRootsFromCurrentJre(): List<File> =
            getJdkClassesRootsFromJre(System.getProperty("java.home"))

    @JvmStatic
    fun getJdkClassesRootsFromJre(javaHome: String): List<File> =
            JavaSdkUtil.getJdkClassesRoots(Paths.get(javaHome), true).map { it.toFile() }

    @JvmStatic
    fun getJdkClassesRoots(jdkHome: File): List<File> =
            JavaSdkUtil.getJdkClassesRoots(jdkHome.toPath(), false).map { it.toFile() }

    @JvmStatic
    fun getJdkClassesRootsFromJdkOrJre(javaRoot: File): List<File> {
        konst isJdk = File(javaRoot, "jre/lib").exists()
        return JavaSdkUtil.getJdkClassesRoots(javaRoot.toPath(), !isJdk).map { it.toFile() }
    }
}
