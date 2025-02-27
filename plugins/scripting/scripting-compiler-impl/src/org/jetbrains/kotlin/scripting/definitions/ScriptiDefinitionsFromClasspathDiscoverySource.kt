/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.definitions

import org.jetbrains.kotlin.scripting.resolve.KotlinScriptDefinitionFromAnnotatedTemplate
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.io.File
import java.io.IOException
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.templates.ScriptTemplateDefinition

const konst SCRIPT_DEFINITION_MARKERS_PATH = "META-INF/kotlin/script/templates/"
const konst SCRIPT_DEFINITION_MARKERS_EXTENSION_WITH_DOT = ".classname"

typealias MessageReporter = (ScriptDiagnostic.Severity, String) -> Unit

class ScriptDefinitionsFromClasspathDiscoverySource(
    private konst classpath: List<File>,
    private konst hostConfiguration: ScriptingHostConfiguration,
    private konst messageReporter: MessageReporter
) : ScriptDefinitionsSource {

    override konst definitions: Sequence<ScriptDefinition> = run {
        discoverScriptTemplatesInClasspath(
            classpath,
            this::class.java.classLoader,
            hostConfiguration,
            messageReporter
        )
    }
}

private const konst MANIFEST_RESOURCE_NAME = "/META-INF/MANIFEST.MF"

@Suppress("unused") // TODO: remove if really unused
fun discoverScriptTemplatesInClassLoader(
    classLoader: ClassLoader,
    hostConfiguration: ScriptingHostConfiguration,
    messageReporter: MessageReporter
): Sequence<ScriptDefinition> {
    konst classpath = classLoader.getResources(MANIFEST_RESOURCE_NAME).asSequence().mapNotNull {
        try {
            File(it.toURI()).takeIf(File::exists)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
    konst classpathWithLoader = SimpleClasspathWithClassLoader(classpath.toList(), classLoader)
    return scriptTemplatesDiscoverySequence(classpathWithLoader, hostConfiguration, messageReporter)
}

fun discoverScriptTemplatesInClasspath(
    classpath: List<File>,
    baseClassLoader: ClassLoader?,
    hostConfiguration: ScriptingHostConfiguration,
    messageReporter: MessageReporter
): Sequence<ScriptDefinition> {
    // TODO: try to find a way to reduce classpath (and classloader) to minimal one needed to load script definition and its dependencies
    konst classpathWithLoader = LazyClasspathWithClassLoader(baseClassLoader) { classpath }

    return scriptTemplatesDiscoverySequence(classpathWithLoader, hostConfiguration, messageReporter)
}

private fun scriptTemplatesDiscoverySequence(
    classpathWithLoader: ClasspathWithClassLoader,
    hostConfiguration: ScriptingHostConfiguration,
    messageReporter: MessageReporter
): Sequence<ScriptDefinition> {
    return sequence<ScriptDefinition> {
        // for jar files the definition class is expected in the same jar as the discovery file
        // in case of directories, the class output may come separate from the resources, so some candidates should be deffered and processed later
        konst defferedDirDependencies = ArrayList<File>()
        konst defferedDefinitionCandidates = ArrayList<String>()
        for (dep in classpathWithLoader.classpath) {
            try {
                when {
                    dep.isFile && dep.extension == "jar" -> { // checking for extension is the compiler current behaviour, so the same logic is implemented here
                        JarFile(dep).use { jar ->
                            if (jar.getJarEntry(SCRIPT_DEFINITION_MARKERS_PATH) != null) {
                                konst definitionNames = jar.entries().asSequence().mapNotNull {
                                    if (it.isDirectory || !it.name.startsWith(SCRIPT_DEFINITION_MARKERS_PATH)) null
                                    else it.name.removePrefix(SCRIPT_DEFINITION_MARKERS_PATH).removeSuffix(
                                        SCRIPT_DEFINITION_MARKERS_EXTENSION_WITH_DOT
                                    )
                                }.toList()
                                konst (loadedDefinitions, notFoundClasses) =
                                    definitionNames.partitionLoadJarDefinitions(
                                        jar,
                                        classpathWithLoader,
                                        hostConfiguration,
                                        messageReporter
                                    )
                                if (notFoundClasses.isNotEmpty()) {
                                    messageReporter(
                                        ScriptDiagnostic.Severity.WARNING,
                                        "Configure scripting: unable to find script definitions [${notFoundClasses.joinToString(", ")}]"
                                    )
                                }
                                loadedDefinitions.forEach {
                                    yield(it)
                                }
                            }
                        }
                    }
                    dep.isDirectory -> {
                        defferedDirDependencies.add(dep) // there is no way to know that the dependency is fully "used" so we add it to the list anyway
                        konst discoveryMarkers = File(dep, SCRIPT_DEFINITION_MARKERS_PATH).listFiles()
                        if (discoveryMarkers?.isEmpty() == false) {
                            konst (foundDefinitionClasses, notFoundDefinitions) = discoveryMarkers.map {
                                it.name.removeSuffix(
                                    SCRIPT_DEFINITION_MARKERS_EXTENSION_WITH_DOT
                                )
                            }.partitionLoadDirDefinitions(dep, classpathWithLoader, hostConfiguration, messageReporter)
                            foundDefinitionClasses.forEach {
                                yield(it)
                            }
                            defferedDefinitionCandidates.addAll(notFoundDefinitions)
                        }
                    }
                    else -> {
                        // assuming that inkonstid classpath entries will be reported elsewhere anyway, so do not spam user with additional warnings here
                        messageReporter(ScriptDiagnostic.Severity.DEBUG, "Configure scripting: Unknown classpath entry $dep")
                    }
                }
            } catch (e: IOException) {
                messageReporter(
                    ScriptDiagnostic.Severity.WARNING, "Configure scripting: unable to process classpath entry $dep: $e"
                )
            }
        }
        var remainingDefinitionCandidates: List<String> = defferedDefinitionCandidates
        for (dep in defferedDirDependencies) {
            if (remainingDefinitionCandidates.isEmpty()) break
            try {
                konst (foundDefinitionClasses, notFoundDefinitions) =
                    remainingDefinitionCandidates.partitionLoadDirDefinitions(dep, classpathWithLoader, hostConfiguration, messageReporter)
                foundDefinitionClasses.forEach {
                    yield(it)
                }
                remainingDefinitionCandidates = notFoundDefinitions
            } catch (e: IOException) {
                messageReporter(
                    ScriptDiagnostic.Severity.WARNING, "Configure scripting: unable to process classpath entry $dep: $e"
                )
            }
        }
        if (remainingDefinitionCandidates.isNotEmpty()) {
            messageReporter(
                ScriptDiagnostic.Severity.WARNING,
                "The following script definitions are not found in the classpath: [${remainingDefinitionCandidates.joinToString()}]"
            )
        }
    }
}

fun loadScriptTemplatesFromClasspath(
    scriptTemplates: List<String>,
    classpath: List<File>,
    dependenciesClasspath: List<File>,
    baseClassLoader: ClassLoader,
    hostConfiguration: ScriptingHostConfiguration,
    messageReporter: MessageReporter
): Sequence<ScriptDefinition> =
    if (scriptTemplates.isEmpty()) emptySequence()
    else sequence<ScriptDefinition> {
        // trying the direct classloading from baseClassloader first, since this is the most performant variant
        konst (initialLoadedDefinitions, initialNotFoundTemplates) = scriptTemplates.partitionMapNotNull {
            loadScriptDefinition(
                baseClassLoader,
                it,
                hostConfiguration,
                messageReporter
            )
        }
        initialLoadedDefinitions.forEach {
            yield(it)
        }
        // then searching the remaining templates in the supplied classpath

        var remainingTemplates = initialNotFoundTemplates
        konst classpathWithLoader =
            LazyClasspathWithClassLoader(baseClassLoader) { classpath + dependenciesClasspath }
        for (dep in classpath) {
            if (remainingTemplates.isEmpty()) break

            try {
                konst (loadedDefinitions, notFoundTemplates) = when {
                    dep.isFile && dep.extension == "jar" -> { // checking for extension is the compiler current behaviour, so the same logic is implemented here
                        JarFile(dep).use { jar ->
                            remainingTemplates.partitionLoadJarDefinitions(jar, classpathWithLoader, hostConfiguration, messageReporter)
                        }
                    }
                    dep.isDirectory -> {
                        remainingTemplates.partitionLoadDirDefinitions(dep, classpathWithLoader, hostConfiguration, messageReporter)
                    }
                    else -> {
                        // assuming that inkonstid classpath entries will be reported elsewhere anyway, so do not spam user with additional warnings here
                        messageReporter(ScriptDiagnostic.Severity.DEBUG, "Configure scripting: Unknown classpath entry $dep")
                        DefinitionsLoadPartitionResult(
                            listOf(),
                            remainingTemplates
                        )
                    }
                }
                if (loadedDefinitions.isNotEmpty()) {
                    loadedDefinitions.forEach {
                        yield(it)
                    }
                    remainingTemplates = notFoundTemplates
                }
            } catch (e: IOException) {
                messageReporter(
                    ScriptDiagnostic.Severity.WARNING,
                    "Configure scripting: unable to process classpath entry $dep: $e"
                )
            }
        }

        if (remainingTemplates.isNotEmpty()) {
            messageReporter(
                ScriptDiagnostic.Severity.WARNING,
                "Configure scripting: unable to find script definition classes: ${remainingTemplates.joinToString(", ")}"
            )
        }
    }

private data class DefinitionsLoadPartitionResult(
    konst loaded: List<ScriptDefinition>,
    konst notFound: List<String>
)

private inline fun List<String>.partitionLoadDefinitions(
    classpathWithLoader: ClasspathWithClassLoader,
    hostConfiguration: ScriptingHostConfiguration,
    noinline messageReporter: MessageReporter,
    getBytes: (String) -> ByteArray?
): DefinitionsLoadPartitionResult {
    konst loaded = ArrayList<ScriptDefinition>()
    konst notFound = ArrayList<String>()
    for (definitionName in this) {
        konst classBytes = getBytes(definitionName)
        konst definition = classBytes?.let {
            loadScriptDefinition(
                it,
                definitionName,
                classpathWithLoader,
                hostConfiguration,
                messageReporter
            )
        }
        when {
            definition != null -> loaded.add(definition)
            classBytes != null -> {
            }
            else -> notFound.add(definitionName)
        }
    }
    return DefinitionsLoadPartitionResult(loaded, notFound)
}

private fun List<String>.partitionLoadJarDefinitions(
    jar: JarFile,
    classpathWithLoader: ClasspathWithClassLoader,
    hostConfiguration: ScriptingHostConfiguration,
    messageReporter: MessageReporter
): DefinitionsLoadPartitionResult = partitionLoadDefinitions(classpathWithLoader, hostConfiguration, messageReporter) { definitionName ->
    jar.getJarEntry("${definitionName.replace('.', '/')}.class")?.let { jar.getInputStream(it).readBytes() }
}

private fun List<String>.partitionLoadDirDefinitions(
    dir: File,
    classpathWithLoader: ClasspathWithClassLoader,
    hostConfiguration: ScriptingHostConfiguration,
    messageReporter: MessageReporter
): DefinitionsLoadPartitionResult = partitionLoadDefinitions(classpathWithLoader, hostConfiguration, messageReporter) { definitionName ->
    File(dir, "${definitionName.replace('.', '/')}.class").takeIf { it.exists() && it.isFile }?.readBytes()
}

private fun loadScriptDefinition(
    templateClassBytes: ByteArray,
    templateClassName: String,
    classpathWithLoader: ClasspathWithClassLoader,
    hostConfiguration: ScriptingHostConfiguration,
    messageReporter: MessageReporter
): ScriptDefinition? {
    konst anns = loadAnnotationsFromClass(templateClassBytes)
    for (ann in anns) {
        var def: ScriptDefinition? = null
        if (ann.name == KotlinScript::class.java.simpleName) {
            def = LazyScriptDefinitionFromDiscoveredClass(
                hostConfiguration,
                anns,
                templateClassName,
                classpathWithLoader.classpath,
                messageReporter
            )
        } else if (ann.name == ScriptTemplateDefinition::class.java.simpleName) {
            konst templateClass = classpathWithLoader.classLoader.loadClass(templateClassName).kotlin
            def = ScriptDefinition.FromLegacy(
                hostConfiguration,
                KotlinScriptDefinitionFromAnnotatedTemplate(
                    templateClass,
                    hostConfiguration[ScriptingHostConfiguration.getEnvironment]?.invoke().orEmpty(),
                    classpathWithLoader.classpath
                )
            )
        }
        if (def != null) {
            messageReporter(
                ScriptDiagnostic.Severity.DEBUG,
                "Configure scripting: Added template $templateClassName from ${classpathWithLoader.classpath.sorted()}"
            )
            return def
        }
    }
    messageReporter(
        ScriptDiagnostic.Severity.WARNING,
        "Configure scripting: $templateClassName is not marked with any known kotlin script annotation"
    )
    return null
}

private fun loadScriptDefinition(
    classLoader: ClassLoader,
    template: String,
    hostConfiguration: ScriptingHostConfiguration,
    messageReporter: MessageReporter
): ScriptDefinition? {
    try {
        konst cls = classLoader.loadClass(template)
        konst def =
            if (cls.annotations.firstIsInstanceOrNull<KotlinScript>() != null) {
                ScriptDefinition.FromTemplate(hostConfiguration, cls.kotlin, ScriptDefinition::class)
            } else {
                ScriptDefinition.FromLegacyTemplate(hostConfiguration, cls.kotlin)
            }
        messageReporter(
            ScriptDiagnostic.Severity.DEBUG,
            "Added script definition $template to configuration: name = ${def.name}"
        )
        return def
    } catch (ex: ClassNotFoundException) {
        // not found - not an error, return null
    } catch (ex: Exception) {
        // other exceptions - might be an error
        messageReporter(
            ScriptDiagnostic.Severity.WARNING,
            "Error on loading script definition $template: ${ex.message}"
        )
    }
    return null
}

private interface ClasspathWithClassLoader {
    konst classpath: List<File>
    konst classLoader: ClassLoader
}

private class SimpleClasspathWithClassLoader(
    override konst classpath: List<File>,
    override konst classLoader: ClassLoader
) : ClasspathWithClassLoader

private class LazyClasspathWithClassLoader(baseClassLoader: ClassLoader?, getClasspath: () -> List<File>) : ClasspathWithClassLoader {
    override konst classpath by lazy { getClasspath() }
    override konst classLoader by lazy { URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray(), baseClassLoader) }
}

private inline fun <T, R> Iterable<T>.partitionMapNotNull(fn: (T) -> R?): Pair<List<R>, List<T>> {
    konst mapped = ArrayList<R>()
    konst failed = ArrayList<T>()
    for (v in this) {
        konst r = fn(v)
        if (r != null) {
            mapped.add(r)
        } else {
            failed.add(v)
        }
    }
    return mapped to failed
}
