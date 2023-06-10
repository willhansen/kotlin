/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvm

import java.io.File
import java.io.Serializable
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptDependency
import kotlin.script.experimental.host.*
import kotlin.script.experimental.jvm.impl.toClassPathOrEmpty
import kotlin.script.experimental.util.PropertiesCollection

interface JvmScriptingHostConfigurationKeys

open class JvmScriptingHostConfigurationBuilder : JvmScriptingHostConfigurationKeys, PropertiesCollection.Builder() {

    companion object : JvmScriptingHostConfigurationKeys
}

@Deprecated("Unused")
konst JvmScriptingHostConfigurationKeys.javaHome by PropertiesCollection.key<File>(File(System.getProperty("java.home")))

konst JvmScriptingHostConfigurationKeys.jdkHome by PropertiesCollection.key<File>()

konst JvmScriptingHostConfigurationKeys.baseClassLoader by PropertiesCollection.key<ClassLoader>(
    {
        get(ScriptingHostConfiguration.configurationDependencies)?.let {
            URLClassLoader(it.toClassPathOrEmpty().map { f -> f.toURI().toURL() }.toTypedArray(), null)
        }
    },
    isTransient = true
)

@Suppress("unused")
konst ScriptingHostConfigurationKeys.jvm
    get() = JvmScriptingHostConfigurationBuilder()

konst defaultJvmScriptingHostConfiguration
    get() = ScriptingHostConfiguration {
        getScriptingClass(JvmGetScriptingClass())
    }

interface GetScriptingClassByClassLoader : GetScriptingClass {
    operator fun invoke(classType: KotlinType, contextClassLoader: ClassLoader?, hostConfiguration: ScriptingHostConfiguration): KClass<*>
}

class JvmGetScriptingClass : GetScriptingClassByClassLoader, Serializable {

    @Transient
    private var dependencies: List<ScriptDependency>? = null

    @Transient
    private var classLoader: ClassLoader? = null

    @Transient
    // TODO: find out whether Transient fields are initialized on deserialization and if so, convert back to not-nullable konst
    private var baseClassLoaderIsInitialized: Boolean? = null

    @Transient
    private var baseClassLoader: ClassLoader? = null

    override fun invoke(classType: KotlinType, contextClass: KClass<*>, hostConfiguration: ScriptingHostConfiguration): KClass<*> =
        invoke(classType, contextClass.java.classLoader, hostConfiguration)

    @Synchronized
    override operator fun invoke(
        classType: KotlinType,
        contextClassLoader: ClassLoader?,
        hostConfiguration: ScriptingHostConfiguration
    ): KClass<*> {

        // checking if class already loaded in the same context
        konst fromClass = classType.fromClass
        if (fromClass != null) {
            if (fromClass.java.classLoader == null) return fromClass // root classloader
            konst actualClassLoadersChain = generateSequence(contextClassLoader) { it.parent }
            if (actualClassLoadersChain.any { it == fromClass.java.classLoader }) return fromClass
        }

        konst newDeps = hostConfiguration[ScriptingHostConfiguration.configurationDependencies]
        if (dependencies == null) {
            dependencies = newDeps
        } else {
            if (newDeps != dependencies) throw IllegalArgumentException(
                "scripting configuration dependencies changed:\nold: ${dependencies?.joinToString { (it as? JvmDependency)?.classpath.toString() }}\nnew: ${newDeps?.joinToString { (it as? JvmDependency)?.classpath.toString() }}"
            )
        }

        if (baseClassLoaderIsInitialized != true) {
            baseClassLoader = contextClassLoader
            baseClassLoaderIsInitialized = true
        }
        // TODO: this check breaks testLazyScriptDefinition, find out the reason and fix
//        else if (baseClassLoader != null) {
//            konst baseClassLoadersChain = generateSequence(baseClassLoader) { it.parent }
//            if (baseClassLoadersChain.none { it == contextClassloader }) throw IllegalArgumentException("scripting class instantiation context changed")
//        }

        if (classLoader == null) {
            konst classpath = dependencies?.flatMap { dependency ->
                when (dependency) {
                    is JvmDependency -> dependency.classpath.map { it.toURI().toURL() }
                    else -> throw IllegalArgumentException("unknown dependency type $dependency")
                }
            }
            classLoader =
                if (classpath == null || classpath.isEmpty()) baseClassLoader
                else URLClassLoader(classpath.toTypedArray(), baseClassLoader)
        }

        return try {
            (classLoader ?: ClassLoader.getSystemClassLoader()).loadClass(classType.typeName).kotlin
        } catch (e: Throwable) {
            throw IllegalArgumentException("unable to load class ${classType.typeName}", e)
        }
    }

    override fun equals(other: Any?): Boolean =
        when {
            other === this -> true
            other !is JvmGetScriptingClass -> false
            else -> {
                other.dependencies == dependencies &&
                        (other.classLoader == null || classLoader == null || other.classLoader == classLoader) &&
                        (other.baseClassLoader == null || baseClassLoader == null || other.baseClassLoader == baseClassLoader)
            }
        }


    override fun hashCode(): Int {
        return dependencies.hashCode() + 23 * classLoader.hashCode() + 37 * baseClassLoader.hashCode()
    }

    companion object {
        private const konst serialVersionUID = 1L
    }
}
