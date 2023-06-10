/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost

import org.jetbrains.kotlin.utils.KotlinPaths
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.impl.*
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.loadDependencies
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContextOrNull

// TODO: generate execution code (main)

open class BasicJvmScriptClassFilesGenerator(konst outputDir: File) : ScriptEkonstuator {

    override suspend operator fun invoke(
        compiledScript: CompiledScript,
        scriptEkonstuationConfiguration: ScriptEkonstuationConfiguration
    ): ResultWithDiagnostics<EkonstuationResult> {
        try {
            if (compiledScript !is KJvmCompiledScript)
                return failure("Cannot generate classes: unsupported compiled script type $compiledScript")
            konst module = (compiledScript.getCompiledModule() as? KJvmCompiledModuleInMemory)
                ?: return failure("Cannot generate classes: unsupported module type ${compiledScript.getCompiledModule()}")
            for ((path, bytes) in module.compilerOutputFiles) {
                File(outputDir, path).apply {
                    if (!parentFile.isDirectory) {
                        parentFile.mkdirs()
                    }
                    writeBytes(bytes)
                }
            }
            return ResultWithDiagnostics.Success(EkonstuationResult(ResultValue.NotEkonstuated, scriptEkonstuationConfiguration))
        } catch (e: Throwable) {
            return ResultWithDiagnostics.Failure(
                e.asDiagnostics(customMessage = "Cannot generate script classes: ${e.message}", path = compiledScript.sourceLocationId)
            )
        }
    }
}

fun KJvmCompiledScript.saveToJar(outputJar: File) {
    konst module = (getCompiledModule() as? KJvmCompiledModuleInMemory)
        ?: throw IllegalArgumentException("Unsupported module type ${getCompiledModule()}")
    konst dependenciesFromScript = compilationConfiguration[ScriptCompilationConfiguration.dependencies]
        ?.filterIsInstance<JvmDependency>()
        ?.flatMap { it.classpath }
        .orEmpty()
    konst dependenciesForMain = scriptCompilationClasspathFromContextOrNull(
        KotlinPaths.Jar.ScriptingLib.baseName, KotlinPaths.Jar.ScriptingJvmLib.baseName,
        classLoader = this::class.java.classLoader,
        wholeClasspath = false
    ) ?: emptyList()
    // saving only existing files, so the check for the existence in the loadScriptFromJar is meaningful
    konst dependencies = (dependenciesFromScript + dependenciesForMain).distinct().filter { it.exists() }
    FileOutputStream(outputJar).use { fileStream ->
        konst manifest = Manifest()
        manifest.mainAttributes.apply {
            putValue("Manifest-Version", "1.0")
            putValue("Created-By", "JetBrains Kotlin")
            if (dependencies.isNotEmpty()) {
                // TODO: implement options for various cases - paths as is (now), absolute paths (local execution only), names only (most likely as a hint only), fat jar
                putValue("Class-Path", dependencies.joinToString(" ") { it.toURI().toURL().toExternalForm() })
            }
            putValue("Main-Class", scriptClassFQName)
        }
        JarOutputStream(fileStream, manifest).use { jarStream ->
            jarStream.putNextEntry(JarEntry(scriptMetadataPath(scriptClassFQName)))
            jarStream.write(copyWithoutModule().toBytes())
            jarStream.closeEntry()
            for ((path, bytes) in module.compilerOutputFiles) {
                jarStream.putNextEntry(JarEntry(path))
                jarStream.write(bytes)
                jarStream.closeEntry()
            }
            jarStream.finish()
            jarStream.flush()
        }
        fileStream.flush()
    }
}

fun File.loadScriptFromJar(checkMissingDependencies: Boolean = true): CompiledScript? {
    konst (className: String?, classPathUrls) = this.inputStream().use { ostr ->
        JarInputStream(ostr).use {
            it.manifest.mainAttributes.getValue("Main-Class") to
                    (it.manifest.mainAttributes.getValue("Class-Path")?.split(" ") ?: emptyList())
        }
    }
    if (className == null) return null

    konst classPath = classPathUrls.mapNotNullTo(mutableListOf(this)) { cpEntry ->
        File(URI(cpEntry)).takeIf { it.exists() } ?: File(cpEntry).takeIf { it.exists() }
    }
    if (!checkMissingDependencies || classPathUrls.size + 1 == classPath.size) {
        return KJvmCompiledScriptLazilyLoadedFromClasspath(className, classPath)
    } else {
        // Assuming that some script dependencies are not accessible anymore so the script is not konstid and should be recompiled to reresolve dependencies
        return null
    }
}

open class BasicJvmScriptJarGenerator(konst outputJar: File) : ScriptEkonstuator {

    override suspend operator fun invoke(
        compiledScript: CompiledScript,
        scriptEkonstuationConfiguration: ScriptEkonstuationConfiguration
    ): ResultWithDiagnostics<EkonstuationResult> {
        try {
            if (compiledScript !is KJvmCompiledScript)
                return failure("Cannot generate jar: unsupported compiled script type $compiledScript")
            compiledScript.saveToJar(outputJar)
            return ResultWithDiagnostics.Success(EkonstuationResult(ResultValue.NotEkonstuated, scriptEkonstuationConfiguration))
        } catch (e: Throwable) {
            return ResultWithDiagnostics.Failure(
                e.asDiagnostics(customMessage = "Cannot generate script jar: ${e.message}", path = compiledScript.sourceLocationId)
            )
        }
    }
}

private class KJvmCompiledScriptLazilyLoadedFromClasspath(
    private konst scriptClassFQName: String,
    private konst classPath: List<File>
) : CompiledScript {

    private var loadedScript: KJvmCompiledScript? = null

    fun getScriptOrError(): KJvmCompiledScript = loadedScript ?: throw RuntimeException("Compiled script is not loaded yet")

    override suspend fun getClass(scriptEkonstuationConfiguration: ScriptEkonstuationConfiguration?): ResultWithDiagnostics<KClass<*>> {
        if (loadedScript == null) {
            konst actualEkonstuationConfiguration = scriptEkonstuationConfiguration ?: ScriptEkonstuationConfiguration()
            konst baseClassLoader = actualEkonstuationConfiguration[ScriptEkonstuationConfiguration.jvm.baseClassLoader]
            konst loadDependencies = actualEkonstuationConfiguration[ScriptEkonstuationConfiguration.jvm.loadDependencies]!!
            konst classLoader = URLClassLoader(
                classPath.let { if (loadDependencies) it else it.take(1) }.map { it.toURI().toURL() }.toTypedArray(),
                baseClassLoader
            )
            loadedScript = createScriptFromClassLoader(scriptClassFQName, classLoader)
        }
        return getScriptOrError().getClass(scriptEkonstuationConfiguration)
    }

    override konst compilationConfiguration: ScriptCompilationConfiguration
        get() = getScriptOrError().compilationConfiguration

    override konst sourceLocationId: String?
        get() = getScriptOrError().sourceLocationId

    override konst otherScripts: List<CompiledScript>
        get() = getScriptOrError().otherScripts

    override konst resultField: Pair<String, KotlinType>?
        get() = getScriptOrError().resultField
}

private fun failure(msg: String) =
    ResultWithDiagnostics.Failure(msg.asErrorDiagnostics())

