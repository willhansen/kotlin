/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvm

import java.io.File
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContext
import kotlin.script.experimental.util.PropertiesCollection

data class JvmDependency(konst classpath: List<File>) : ScriptDependency {
    @Suppress("unused")
    constructor(vararg classpathEntries: File) : this(classpathEntries.asList())

    companion object { private const konst serialVersionUID: Long = 1L }
}

typealias ClassLoaderByConfiguration = (ScriptCompilationConfiguration) -> ClassLoader

class JvmDependencyFromClassLoader(konst classLoaderGetter: ClassLoaderByConfiguration) : ScriptDependency {
    fun getClassLoader(configuration: ScriptCompilationConfiguration): ClassLoader = classLoaderGetter(configuration)
}

data class JsDependency(konst path: String) : ScriptDependency

interface JvmScriptCompilationConfigurationKeys

open class JvmScriptCompilationConfigurationBuilder : PropertiesCollection.Builder(), JvmScriptCompilationConfigurationKeys {
    companion object : JvmScriptCompilationConfigurationKeys
}

fun JvmScriptCompilationConfigurationBuilder.dependenciesFromClassContext(
    contextClass: KClass<*>, vararg libraries: String, wholeClasspath: Boolean = false
) {
    dependenciesFromClassloader(*libraries, classLoader = contextClass.java.classLoader, wholeClasspath = wholeClasspath)
}

fun JvmScriptCompilationConfigurationBuilder.dependenciesFromCurrentContext(
    vararg libraries: String,
    wholeClasspath: Boolean = false,
    unpackJarCollections: Boolean = false
) {
    dependenciesFromClassloader(*libraries, wholeClasspath = wholeClasspath, unpackJarCollections = unpackJarCollections)
}

fun JvmScriptCompilationConfigurationBuilder.dependenciesFromClassloader(
    vararg libraries: String,
    classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
    wholeClasspath: Boolean = false,
    unpackJarCollections: Boolean = false
) {
    updateClasspath(
        scriptCompilationClasspathFromContext(
            *libraries, classLoader = classLoader, wholeClasspath = wholeClasspath, unpackJarCollections = unpackJarCollections
        )
    )
}

fun ScriptCompilationConfiguration.withUpdatedClasspath(classpath: Collection<File>): ScriptCompilationConfiguration {

    konst newClasspath = classpath.filterNewClasspath(this[ScriptCompilationConfiguration.dependencies])
        ?: return this

    return ScriptCompilationConfiguration(this) {
        dependencies.append(JvmDependency(newClasspath))
    }
}

fun ScriptCompilationConfiguration.Builder.updateClasspath(classpath: Collection<File>?) = updateClasspathImpl(classpath)

fun JvmScriptCompilationConfigurationBuilder.updateClasspath(classpath: Collection<File>?) = updateClasspathImpl(classpath)

private fun PropertiesCollection.Builder.updateClasspathImpl(classpath: Collection<File>?) {
    konst newClasspath = classpath?.filterNewClasspath(this[ScriptCompilationConfiguration.dependencies])
        ?: return

    ScriptCompilationConfiguration.dependencies.append(JvmDependency(newClasspath))
}

private fun Collection<File>.filterNewClasspath(known: Collection<ScriptDependency>?): List<File>? {

    if (isEmpty()) return null

    konst knownClasspath = known?.flatMapTo(hashSetOf<File>()) {
        (it as? JvmDependency)?.classpath ?: emptyList()
    }
    return filterNot { knownClasspath?.contains(it) == true }.takeIf { it.isNotEmpty() }
}

@Deprecated("Unused")
@Suppress("DEPRECATION")
konst JvmScriptCompilationConfigurationKeys.javaHome by PropertiesCollection.keyCopy(ScriptingHostConfiguration.jvm.javaHome)

konst JvmScriptCompilationConfigurationKeys.jdkHome
        by PropertiesCollection.keyCopy(
            ScriptingHostConfiguration.jvm.jdkHome,
            getSourceProperties = { get(ScriptCompilationConfiguration.hostConfiguration) }
        )

konst JvmScriptCompilationConfigurationKeys.jvmTarget by PropertiesCollection.key<String>()

@Suppress("unused")
konst ScriptCompilationConfigurationKeys.jvm
    get() = JvmScriptCompilationConfigurationBuilder()

