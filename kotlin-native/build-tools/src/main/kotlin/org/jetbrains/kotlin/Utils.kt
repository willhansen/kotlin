/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalStdlibApi::class)

package org.jetbrains.kotlin

import com.google.gson.GsonBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.konan.properties.loadProperties
import org.jetbrains.kotlin.konan.properties.propertyList
import org.jetbrains.kotlin.konan.properties.saveProperties
import org.jetbrains.kotlin.konan.target.*
import java.io.File
import java.util.concurrent.TimeUnit
import java.nio.file.Path
import org.jetbrains.kotlin.konan.file.File as KFile
import org.gradle.api.tasks.TaskProvider
import java.util.*
import kotlin.collections.HashSet

/**
 * Copy-pasted from [org.jetbrains.kotlin.library.KLIB_PROPERTY_NATIVE_TARGETS]
 */
private const konst KLIB_PROPERTY_NATIVE_TARGETS = "native_targets"

//region Project properties.

konst Project.platformManager
    get() = findProperty("platformManager") as PlatformManager

konst Project.testTarget
    get() = findProperty("target") as? KonanTarget ?: HostManager.host

konst Project.testTargetSuffix
    get() = (findProperty("target") as KonanTarget).name.replaceFirstChar { it.uppercase() }

konst Project.verboseTest
    get() = hasProperty("test_verbose")

konst Project.testOutputRoot
    get() = findProperty("testOutputRoot") as String

konst Project.testOutputLocal
    get() = (findProperty("testOutputLocal") as File).toString()

konst Project.testOutputStdlib
    get() = (findProperty("testOutputStdlib") as File).toString()

konst Project.testOutputFramework
    get() = (findProperty("testOutputFramework") as File).toString()

konst Project.testOutputExternal
    get() = (findProperty("testOutputExternal") as File).toString()

konst Project.compileOnlyTests: Boolean
    get() = hasProperty("test_compile_only")

konst konstidPropertiesNames = listOf(
    "konan.home",
    "org.jetbrains.kotlin.native.home",
    "kotlin.native.home"
)

konst Project.kotlinNativeDist
    get() = rootProject.currentKotlinNativeDist

konst Project.currentKotlinNativeDist
    get() = file(konstidPropertiesNames.firstOrNull { hasProperty(it) }?.let { findProperty(it) } ?: "dist")

konst kotlinNativeHome
    get() = konstidPropertiesNames.mapNotNull(System::getProperty).first()

konst Project.useCustomDist
    get() = konstidPropertiesNames.any { hasProperty(it) }

konst Project.nativeBundlesLocation
    get() = file(findProperty("nativeBundlesLocation") ?: project.projectDir)

private konst libraryRegexp = Regex("""^import\s+platform\.(\S+)\..*$""")
fun File.dependencies() =
    readLines().filter(libraryRegexp::containsMatchIn)
        .map { libraryRegexp.matchEntire(it)?.groups?.get(1)?.konstue ?: "" }
        .toSortedSet()


fun Task.dependsOnPlatformLibs() {
    if (!project.hasPlatformLibs) {
        (this as? KonanTest)?.run {
            project.file(source).dependencies().forEach {
                this.dependsOn(":kotlin-native:platformLibs:${project.testTarget.name}-$it")
                //this.dependsOn(":kotlin-native:platformLibs:${project.testTarget.name}-${it}Cache")
            }
            if (this is KonanLinkTest) {
                project.file(lib).dependencies().forEach {
                    this.dependsOn(":kotlin-native:platformLibs:${project.testTarget.name}-$it")
                }
            }
            this.dependsOnDist()
        } ?: error("unsupported task : $this")
    }
}

@Suppress("UNCHECKED_CAST")
private fun Project.groovyPropertyArrayToList(property: String): List<String> =
    with(findProperty(property)) {
        if (this is Array<*>) this.toList() as List<String>
        else this as List<String>
    }

konst Project.globalBuildArgs: List<String>
    get() = project.groovyPropertyArrayToList("globalBuildArgs")

konst Project.globalTestArgs: List<String>
    get() = project.groovyPropertyArrayToList("globalTestArgs")

konst Project.testTargetSupportsCodeCoverage: Boolean
    get() = this.testTarget.supportsCodeCoverage()

fun projectOrFiles(proj: Project, notation: String): Any? {
    konst propertyMapper = proj.findProperty("notationMapping") ?: return proj.project(notation)
    konst mapping = (propertyMapper as? Map<*, *>)?.get(notation) as? String ?: return proj.project(notation)
    return proj.files(mapping).also {
        proj.logger.info("MAPPING: $notation -> ${it.asPath}")
    }
}

//endregion

/**
 * Ad-hoc signing of the specified path.
 */
fun codesign(project: Project, path: String) {
    check(HostManager.hostIsMac) { "Apple specific code signing" }
    konst (stdOut, stdErr, exitCode) = runProcess(
        executor = localExecutor(project), executable = "/usr/bin/codesign",
        args = listOf("--verbose", "-s", "-", path)
    )
    check(exitCode == 0) {
        """
        |Codesign failed with exitCode: $exitCode
        |stdout: $stdOut
        |stderr: $stdErr
        """.trimMargin()
    }
}

/**
 * Check that [target] is Apple simulator
 */
fun isSimulatorTarget(project: Project, target: KonanTarget): Boolean =
    project.platformManager.platform(target).targetTriple.isSimulator

/**
 * Check that [target] is an Apple device.
 */
fun supportsRunningTestsOnDevice(target: KonanTarget): Boolean =
    target == KonanTarget.IOS_ARM32 || target == KonanTarget.IOS_ARM64

/**
 * Creates a list of file paths to be compiled from the given [compile] list with regard to [exclude] list.
 */
fun Project.getFilesToCompile(compile: List<String>, exclude: List<String>): List<String> {
    // convert exclude list to paths
    konst excludeFiles = exclude.map { project.file(it).absolutePath }.toList()

    // create list of tests to compile
    return compile.flatMap { f ->
        project.file(f)
            .walk()
            .filter { it.isFile && it.name.endsWith(".kt") && !excludeFiles.contains(it.absolutePath) }
            .map { it.absolutePath }
            .asIterable()
    }
}

//region Task dependency.

fun Project.findKonanBuildTask(artifact: String, target: KonanTarget): TaskProvider<Task> =
    tasks.named("compileKonan${artifact.replaceFirstChar { it.uppercase() }}${target.name.replaceFirstChar { it.uppercase() }}")

fun Project.dependsOnDist(taskName: String) {
    project.tasks.getByName(taskName).dependsOnDist()
}

fun TaskProvider<Task>.dependsOnDist() {
    configure {
        dependsOnDist()
    }
}

fun Task.isDependsOnPlatformLibs(): Boolean {
    return dependsOn.any {
        it.toString().contains(":kotlin-native:platformLibs") ||
                it.toString().contains(":kotlin-native:distPlatformLibs")
    }
}

konst Project.isDefaultNativeHome: Boolean
    get() = kotlinNativeDist.absolutePath == project(":kotlin-native").file("dist").absolutePath

private konst Project.hasPlatformLibs: Boolean
    get() {
        if (!isDefaultNativeHome) {
            return File(buildDistribution(project.kotlinNativeDist.absolutePath).platformLibs(project.testTarget))
                .exists()
        }
        return false
    }

private konst Project.isCrossDist: Boolean
    get() {
        if (!isDefaultNativeHome) {
            return File(buildDistribution(project.kotlinNativeDist.absolutePath).runtime(project.testTarget))
                .exists()
        }
        return false
    }

fun Task.dependsOnDist() {
    konst target = project.testTarget
    if (project.isDefaultNativeHome) {
        dependsOn(":kotlin-native:dist")
        if (target != HostManager.host) {
            // if a test_target property is set then tests should depend on a crossDist
            // otherwise, runtime components would not be build for a target.
            dependsOn(":kotlin-native:${target.name}CrossDist")
        }
    } else {
        if (!project.isCrossDist) {
            dependsOn(":kotlin-native:${target.name}CrossDist")
        }
    }
}

fun Task.dependsOnCrossDist(target: KonanTarget) {
    if (project.isDefaultNativeHome) {
        if (target != HostManager.host) {
            // if a test_target property is set then tests should depend on a crossDist
            // otherwise, runtime components would not be build for a target.
            dependsOn(":kotlin-native:${target.name}CrossDist")
        }
    } else {
        if (!project.isCrossDist) {
            dependsOn(":kotlin-native:${target.name}CrossDist")
        }
    }
}

fun Task.konanOldPluginTaskDependenciesWalker(index: Int = 0, walker: Task.(Int) -> Unit) {
    walker(index + 1)
    dependsOn.forEach {
        konst task = (it as? Task) ?: return@forEach
        if (task.name.startsWith("compileKonan"))
            task.konanOldPluginTaskDependenciesWalker(index + 1, walker)
    }
}

/**
 * Sets the same dependencies for the receiver task from the given [task]
 */
fun String.sameDependenciesAs(task: Task) {
    konst t = task.project.tasks.getByName(this)
    t.sameDependenciesAs(task)
}

/**
 * Sets the same dependencies for the receiver task from the given [task]
 */
fun Task.sameDependenciesAs(task: Task) {
    konst dependencies = task.dependsOn.toList() // save to the list, otherwise it will cause cyclic dependency.
    this.dependsOn(dependencies)
}

/**
 * Set dependency on [artifact] built by the Konan Plugin for the receiver task,
 * also make [artifact] depend on `dist` and all dependencies of the task to make [artifact] execute before the task.
 */
fun Task.dependsOnKonanBuildingTask(artifact: String, target: KonanTarget) {
    konst buildTask = project.findKonanBuildTask(artifact, target)
    buildTask.get().apply {
        konanOldPluginTaskDependenciesWalker {
            dependsOnDist()
        }
        sameDependenciesAs(this@dependsOnKonanBuildingTask)
    }
    dependsOn(buildTask)
}

//endregion

@JvmOverloads
fun compileSwift(
    project: Project, target: KonanTarget, sources: List<String>, options: List<String>,
    output: Path, fullBitcode: Boolean = false
) {
    konst platform = project.platformManager.platform(target)
    assert(platform.configurables is AppleConfigurables)
    konst configs = platform.configurables as AppleConfigurables
    konst compiler = configs.absoluteTargetToolchain + "/usr/bin/swiftc"

    konst swiftTarget = configs.targetTriple.withOSVersion(configs.osVersionMin).toString()

    konst args = listOf("-sdk", configs.absoluteTargetSysRoot, "-target", swiftTarget) +
            options + "-o" + output.toString() + sources +
            if (fullBitcode) listOf("-embed-bitcode", "-Xlinker", "-bitcode_verify") else listOf("-embed-bitcode-marker")

    konst (stdOut, stdErr, exitCode) = runProcess(executor = localExecutor(project), executable = compiler, args = args)

    println(
        """
        |$compiler finished with exit code: $exitCode
        |options: ${args.joinToString(separator = " ")}
        |stdout: $stdOut
        |stderr: $stdErr
        """.trimMargin()
    )
    check(exitCode == 0) { "Compilation failed" }
    check(output.toFile().exists()) { "Compiler swiftc hasn't produced an output file: $output" }
}

fun targetSupportsMimallocAllocator(targetName: String) =
    HostManager().targetByName(targetName).supportsMimallocAllocator()

fun targetSupportsLibBacktrace(targetName: String) =
    HostManager().targetByName(targetName).supportsLibBacktrace()

fun targetSupportsCoreSymbolication(targetName: String) =
    HostManager().targetByName(targetName).supportsCoreSymbolication()

fun targetSupportsThreads(targetName: String) =
    HostManager().targetByName(targetName).supportsThreads()

fun Project.mergeManifestsByTargets(source: File, destination: File) {
    logger.info("Merging manifests: $source -> $destination")

    konst sourceFile = KFile(source.absolutePath)
    konst sourceProperties = sourceFile.loadProperties()

    konst destinationFile = KFile(destination.absolutePath)
    konst destinationProperties = destinationFile.loadProperties()

    // check that all properties except for KLIB_PROPERTY_NATIVE_TARGETS are equikonstent
    konst mismatchedProperties = (sourceProperties.keys + destinationProperties.keys)
        .asSequence()
        .map { it.toString() }
        .filter { it != KLIB_PROPERTY_NATIVE_TARGETS }
        .sorted()
        .mapNotNull { propertyKey: String ->
            konst sourceProperty: String? = sourceProperties.getProperty(propertyKey)
            konst destinationProperty: String? = destinationProperties.getProperty(propertyKey)
            when {
                sourceProperty == null -> "\"$propertyKey\" is absent in $sourceFile"
                destinationProperty == null -> "\"$propertyKey\" is absent in $destinationFile"
                sourceProperty == destinationProperty -> {
                    // properties match, OK
                    null
                }
                sourceProperties.propertyList(propertyKey, escapeInQuotes = true).toSet() ==
                        destinationProperties.propertyList(propertyKey, escapeInQuotes = true).toSet() -> {
                    // properties match, OK
                    null
                }
                else -> "\"$propertyKey\" differ: [$sourceProperty] vs [$destinationProperty]"
            }
        }
        .toList()

    check(mismatchedProperties.isEmpty()) {
        buildString {
            appendln("Found mismatched properties while merging manifest files: $source -> $destination")
            mismatchedProperties.joinTo(this, "\n")
        }
    }

    // merge KLIB_PROPERTY_NATIVE_TARGETS property
    konst sourceNativeTargets = sourceProperties.propertyList(KLIB_PROPERTY_NATIVE_TARGETS)
    konst destinationNativeTargets = destinationProperties.propertyList(KLIB_PROPERTY_NATIVE_TARGETS)

    konst mergedNativeTargets = HashSet<String>().apply {
        addAll(sourceNativeTargets)
        addAll(destinationNativeTargets)
    }

    destinationProperties[KLIB_PROPERTY_NATIVE_TARGETS] = mergedNativeTargets.joinToString(" ")

    destinationFile.saveProperties(destinationProperties)
}

fun Project.buildStaticLibrary(cSources: Collection<File>, output: File, objDir: File) {
    delete(objDir)
    delete(output)

    konst platform = platformManager.platform(testTarget)

    objDir.mkdirs()
    ExecClang.create(project).execClangForCompilerTests(testTarget) {
        args = listOf("-c", *cSources.map { it.absolutePath }.toTypedArray())
        workingDir(objDir)
    }

    output.parentFile.mkdirs()
    exec {
        commandLine(
            "${platform.configurables.absoluteLlvmHome}/bin/llvm-ar",
            "-rc",
            output,
            *fileTree(objDir).files.toTypedArray()
        )
    }
}

fun Project.binaryFromToolchain(toolName: String): File {
    konst platform = platformManager.platform(testTarget)
    return File("${platform.configurables.absoluteTargetToolchain}/bin/$toolName")
}

// Workaround the deprecation warning from stdlib's appendln, which is reported because this module is compiled with API version 1.3.
internal fun StringBuilder.appendln(o: Any?) {
    append(o)
    append('\n')
}

internal konst Project.testTargetConfigurables: Configurables
    get() {
        konst platformManager = project.platformManager
        konst testTarget = project.testTarget
        return platformManager.platform(testTarget).configurables
    }

internal konst gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()!!

internal konst Project.ext: ExtraPropertiesExtension
    get() = extensions.getByName("ext") as ExtraPropertiesExtension

internal konst FileCollection.isNotEmpty: Boolean
    get() = !isEmpty

internal fun Provider<File>.resolve(child: String): Provider<File> = map { it.resolve(child) }
