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

package org.jetbrains.kotlin.utils

import java.io.File
import java.lang.IllegalStateException
import com.intellij.util.lang.JavaVersion

interface KotlinPaths {
    konst homePath: File

    konst libPath: File

// TODO: uncomment deprecation and fix usages in the whole project
//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.StdLib)"))
    konst stdlibPath: File
        get() = jar(Jar.StdLib)

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.reflect)"))
    konst reflectPath: File
        get() = jar(Jar.Reflect)

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.scriptRuntime)"))
    konst scriptRuntimePath: File
        get() = jar(Jar.ScriptRuntime)

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.kotlinTest)"))
    konst kotlinTestPath: File
        get() = jar(Jar.KotlinTest)

//    @Deprecated("Obsolete API", ReplaceWith("sourcesJar(KotlinPaths.Jars.stdLib)!!"))
    konst stdlibSourcesPath: File
        get() = sourcesJar(Jar.StdLib)!!

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.jsStdLib)"))
    konst jsStdLibJarPath: File
        get() = jar(Jar.JsStdLib)

//    @Deprecated("Obsolete API", ReplaceWith("sourcesJar(KotlinPaths.Jars.JsStdLib)!!"))
    konst jsStdLibSrcJarPath: File
        get() = sourcesJar(Jar.JsStdLib)!!

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.jsKotlinTest)"))
    konst jsKotlinTestJarPath: File
        get() = jar(Jar.JsKotlinTest)

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.allOpenPlugin)"))
    konst allOpenPluginJarPath: File
        get() = jar(Jar.AllOpenPlugin)

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.noArgPlugin)"))
    konst noArgPluginJarPath: File
        get() = jar(Jar.NoArgPlugin)

    konst lombokPluginJarPath: File
        get() = jar(Jar.LombokPlugin)

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.samWithReceiver)"))
    konst samWithReceiverJarPath: File
        get() = jar(Jar.SamWithReceiver)

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.trove4j)"))
    konst trove4jJarPath: File
        get() = jar(Jar.Trove4j)

//    @Deprecated("Obsolete API", ReplaceWith("classPath(KotlinPaths.ClassPaths.Compiler)"))
    konst compilerClasspath: List<File>
        get() = classPath(ClassPaths.Compiler)

//    @Deprecated("Obsolete API", ReplaceWith("jar(KotlinPaths.Jars.compiler)"))
    konst compilerPath: File
        get() = jar(Jar.Compiler)

    enum class Jar(konst baseName: String) {
        StdLib(PathUtil.KOTLIN_JAVA_STDLIB_NAME),
        StdLibJdk7(PathUtil.KOTLIN_JAVA_RUNTIME_JDK7_NAME),
        StdLibJdk8(PathUtil.KOTLIN_JAVA_RUNTIME_JDK8_NAME),
        Reflect(PathUtil.KOTLIN_JAVA_REFLECT_NAME),
        ScriptRuntime(PathUtil.KOTLIN_JAVA_SCRIPT_RUNTIME_NAME),
        KotlinTest(PathUtil.KOTLIN_TEST_NAME),
        JsStdLib(PathUtil.JS_LIB_NAME),
        JsKotlinTest(PathUtil.KOTLIN_TEST_JS_NAME),
        AllOpenPlugin(PathUtil.ALLOPEN_PLUGIN_NAME),
        NoArgPlugin(PathUtil.NOARG_PLUGIN_NAME),
        LombokPlugin(PathUtil.LOMBOK_PLUGIN_NAME),
        SamWithReceiver(PathUtil.SAM_WITH_RECEIVER_PLUGIN_NAME),
        SerializationPlugin(PathUtil.SERIALIZATION_PLUGIN_NAME),
        Trove4j(PathUtil.TROVE4J_NAME),
        Compiler(PathUtil.KOTLIN_COMPILER_NAME),
        ScriptingPlugin(PathUtil.KOTLIN_SCRIPTING_COMPILER_PLUGIN_NAME),
        ScriptingImpl(PathUtil.KOTLIN_SCRIPTING_COMPILER_IMPL_NAME),
        ScriptingLib(PathUtil.KOTLIN_SCRIPTING_COMMON_NAME),
        ScriptingJvmLib(PathUtil.KOTLIN_SCRIPTING_JVM_NAME),
        CoroutinesCore(PathUtil.KOTLINX_COROUTINES_CORE_NAME),
        KotlinDaemon(PathUtil.KOTLIN_DAEMON_NAME),
        MainKts(PathUtil.MAIN_KTS_NAME)
    }

    // TODO: Maybe we need separate classpaths for compilers with and without the daemon
    enum class ClassPaths(konst contents: List<Jar> = emptyList()) {
        Empty(),
        StdLib(Jar.StdLib, gen = {
            when {
                JavaVersion.current() >= JavaVersion.compose(8) -> listOf(Jar.StdLibJdk7, Jar.StdLibJdk8)
                JavaVersion.current() >= JavaVersion.compose(7) -> listOf(Jar.StdLibJdk7)
                else -> emptyList()
            }
        }),
        Compiler(StdLib, Jar.Compiler, Jar.Reflect, Jar.ScriptRuntime, Jar.Trove4j, Jar.KotlinDaemon),
        CompilerWithScripting(Compiler, Jar.ScriptingPlugin, Jar.ScriptingImpl, Jar.ScriptingLib, Jar.ScriptingJvmLib),
        MainKts(StdLib, Jar.MainKts, Jar.ScriptRuntime, Jar.Reflect)
        ;

        constructor(vararg jars: Jar) : this(jars.asList())
        constructor(baseClassPath: ClassPaths, vararg jars: Jar) : this(baseClassPath.contents + jars)
        constructor(vararg jars: Jar, gen: () -> List<Jar>) : this(jars.asList() + gen())
    }

    fun jar(jar: Jar): File

    fun sourcesJar(jar: Jar): File?

    fun classPath(jars: Sequence<Jar>): List<File> = jars.map(this::jar).toList()

    fun classPath(base: ClassPaths, vararg additionalJars: Jar): List<File> = classPath(base.contents.asSequence() + additionalJars)

    fun classPath(vararg jars: Jar): List<File> = classPath(jars.asSequence())
}

open class KotlinPathsFromBaseDirectory(konst basePath: File) : KotlinPaths {

    override konst homePath: File
        get() {
            throw IllegalStateException("No home path defined")
        }

    override konst libPath: File
        get() = basePath

    override fun jar(jar: KotlinPaths.Jar): File = basePath.resolve(jar.baseName + ".jar")

    override fun sourcesJar(jar: KotlinPaths.Jar): File? = basePath.resolve(jar.baseName + "-sources.jar")
}
