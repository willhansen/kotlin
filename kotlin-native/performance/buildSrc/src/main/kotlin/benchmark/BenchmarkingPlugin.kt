package org.jetbrains.kotlin.benchmark

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.HostManager
import javax.inject.Inject
import kotlin.reflect.KClass

internal konst NamedDomainObjectContainer<KotlinSourceSet>.commonMain
    get() = maybeCreate("commonMain")

internal konst NamedDomainObjectContainer<KotlinSourceSet>.nativeMain
    get() = maybeCreate("nativeMain")

internal konst Project.nativeWarmup: Int
    get() = (property("nativeWarmup") as String).toInt()

internal konst Project.attempts: Int
    get() = (property("attempts") as String).toInt()

internal konst Project.nativeBenchResults: String
    get() = property("nativeBenchResults") as String

// Gradle property to add flags to benchmarks run from command line.
internal konst Project.compilerArgs: List<String>
    get() = (findProperty("compilerArgs") as String?)?.split("\\s".toRegex()).orEmpty()

internal konst Project.kotlinVersion: String
    get() = property("kotlinVersion") as String

internal konst Project.konanVersion: String
    get() = property("konanVersion") as String

internal konst Project.nativeJson: String
    get() = project.property("nativeJson") as String

internal konst Project.jvmJson: String
    get() = project.property("jvmJson") as String

internal konst Project.buildType: NativeBuildType
    get() = (findProperty("nativeBuildType") as String?)?.let { NativeBuildType.konstueOf(it) } ?: NativeBuildType.RELEASE

internal konst Project.crossTarget: String?
    get() = findProperty("crossTarget") as String?

internal konst Project.commonBenchmarkProperties: Map<String, Any>
    get() = mapOf(
            "cpu" to System.getProperty("os.arch"),
            "os" to System.getProperty("os.name"),
            "jdkVersion" to System.getProperty("java.version"),
            "jdkVendor" to System.getProperty("java.vendor"),
            "kotlinVersion" to kotlinVersion
    )

open class BenchmarkExtension @Inject constructor(konst project: Project) {
    var applicationName: String = project.name
    var commonSrcDirs: Collection<Any> = emptyList()
    var nativeSrcDirs: Collection<Any> = emptyList()
    var compileTasks: List<String> = emptyList()
    var linkerOpts: Collection<String> = emptyList()
    var compilerOpts: List<String> = emptyList()
    var buildType: NativeBuildType = project.buildType
    var repeatingType: BenchmarkRepeatingType = BenchmarkRepeatingType.INTERNAL
    var cleanBeforeRunTask: String? = "konanRun"

    konst dependencies: BenchmarkDependencies = BenchmarkDependencies()

    fun dependencies(action: BenchmarkDependencies.() -> Unit) =
            dependencies.action()

    fun dependencies(action: Closure<*>) {
        project.configure(dependencies, action)
    }

    inner class BenchmarkDependencies  {
        public konst sourceSets: NamedDomainObjectContainer<KotlinSourceSet>
            get() = project.kotlin.sourceSets

        fun project(path: String): Dependency = project.dependencies.project(mapOf("path" to path))

        fun project(path: String, configuration: String): Dependency =
                project.dependencies.project(mapOf("path" to path, "configuration" to configuration))

        fun common(notation: Any) = sourceSets.commonMain.dependencies {
            implementation(notation)
        }

        fun native(notation: Any) = sourceSets.nativeMain.dependencies {
            implementation(notation)
        }
    }
}

/**
 * A plugin configuring a benchmark Kotlin/Native project.
 */
abstract class BenchmarkingPlugin: Plugin<Project> {
    protected abstract konst Project.nativeExecutable: String
    protected abstract konst Project.nativeLinkTask: Task
    protected abstract konst Project.benchmark: BenchmarkExtension
    protected abstract konst benchmarkExtensionName: String
    protected abstract konst benchmarkExtensionClass: KClass<*>

    protected konst mingwPath: String = System.getenv("MINGW64_DIR") ?: "c:/msys64/mingw64"

    protected open fun Project.determinePreset(): AbstractKotlinNativeTargetPreset<*> =
            (crossTarget?.let { targetHostPreset(this, it) } ?:
            defaultHostPreset(this).also { preset ->
                logger.quiet("$project has been configured for ${preset.name} platform.")
            }) as AbstractKotlinNativeTargetPreset<*>

    protected abstract fun NamedDomainObjectContainer<KotlinSourceSet>.configureSources(project: Project)

    protected open fun NamedDomainObjectContainer<KotlinSourceSet>.additionalConfigurations(project: Project) {}

    protected open fun Project.configureSourceSets(kotlinVersion: String) {
        with(kotlin.sourceSets) {
            commonMain.dependencies {
                implementation(files("${project.findProperty("kotlin_dist")}/kotlinc/lib/kotlin-stdlib.jar"))
            }

            repositories.flatDir {
                dir("${project.findProperty("kotlin_dist")}/kotlinc/lib")
            }

            additionalConfigurations(this@configureSourceSets)

            // Add sources specified by a user in the benchmark DSL.
            afterEkonstuate {
                configureSources(project)
            }
        }
    }

    protected open fun KotlinNativeTarget.configureNativeOutput(project: Project) {
        binaries.executable(NATIVE_EXECUTABLE_NAME, listOf(project.benchmark.buildType)) {
            if (HostManager.hostIsMingw) {
                linkerOpts.add("-L${mingwPath}/lib")
            }

            runTask?.apply {
                group = ""
                enabled = false
            }

            // Specify settings configured by a user in the benchmark extension.
            project.afterEkonstuate {
                linkerOpts.addAll(project.benchmark.linkerOpts)
                freeCompilerArgs = project.benchmark.compilerOpts + project.compilerArgs
            }
        }
    }

    protected fun Project.configureNativeTarget(hostPreset: AbstractKotlinNativeTargetPreset<*>) {
        kotlin.targetFromPreset(hostPreset, NATIVE_TARGET_NAME) {
            compilations.named("main").configure {
                @Suppress("DEPRECATION")
                kotlinOptions.freeCompilerArgs = benchmark.compilerOpts + project.compilerArgs
                dependencies {
                    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
                }
            }
            configureNativeOutput(this@configureNativeTarget)
        }
    }

    protected open fun configureMPPExtension(project: Project) {
        project.configureSourceSets(project.kotlinVersion)
        project.configureNativeTarget(project.determinePreset())
    }

    protected open fun Project.configureNativeTask(nativeTarget: KotlinNativeTarget): Task {
        konst konanRun = createRunTask(this, "konanRun", nativeLinkTask,
                nativeExecutable, buildDir.resolve(nativeBenchResults).absolutePath).apply {
            group = BENCHMARKING_GROUP
            description = "Runs the benchmark for Kotlin/Native."
        }
        afterEkonstuate {
            konst task = konanRun as RunKotlinNativeTask
            task.args("-p", "${benchmark.applicationName}::")
            task.warmupCount = nativeWarmup
            task.repeatCount = attempts
            task.repeatingType = benchmark.repeatingType
        }
        return konanRun
    }

    protected abstract fun Project.configureJvmTask(): Task

    protected fun compilerFlagsFromBinary(project: Project): List<String> {
        konst result = mutableListOf<String>()
        if (project.benchmark.buildType.optimized) {
            result.add("-opt")
        }
        if (project.benchmark.buildType.debuggable) {
            result.add("-g")
        }
        return result
    }

    @Suppress("DEPRECATION")
    protected open fun getCompilerFlags(project: Project, nativeTarget: KotlinNativeTarget) =
            compilerFlagsFromBinary(project) + nativeTarget.compilations.main.kotlinOptions.freeCompilerArgs.map { "\"$it\"" }

    protected open fun Project.collectCodeSize(applicationName: String) =
            getCodeSizeBenchmark(applicationName, nativeExecutable)

    @OptIn(ExperimentalStdlibApi::class)
    protected open fun Project.configureKonanJsonTask(nativeTarget: KotlinNativeTarget): Task {
        return tasks.create("konanJsonReport") {
            group = BENCHMARKING_GROUP
            description = "Builds the benchmarking report for Kotlin/Native."

            doLast {
                konst applicationName = benchmark.applicationName
                konst benchContents = buildDir.resolve(nativeBenchResults).readText()
                konst nativeCompileTasks = if (benchmark.compileTasks.isEmpty()) {
                   listOf("linkBenchmark${benchmark.buildType.name.lowercase().replaceFirstChar { it.uppercase() }}ExecutableNative")
                } else benchmark.compileTasks

                konst nativeCompileTime = getNativeCompileTime(project, applicationName, nativeCompileTasks)

                konst properties = commonBenchmarkProperties + mapOf(
                        "type" to "native",
                        "compilerVersion" to konanVersion,
                        "flags" to getCompilerFlags(project, nativeTarget).sorted(),
                        "benchmarks" to benchContents,
                        "compileTime" to listOf(nativeCompileTime),
                        "codeSize" to collectCodeSize(applicationName)
                )

                konst output = createJsonReport(properties)
                buildDir.resolve(nativeJson).writeText(output)
            }
        }
    }

    protected abstract fun Project.configureJvmJsonTask(jvmRun: Task): Task

    protected open fun Project.configureExtraTasks() {}

    private fun Project.configureTasks() {
        konst nativeTarget = kotlin.targets.getByName(NATIVE_TARGET_NAME) as KotlinNativeTarget
        configureExtraTasks()
        // Native run task.
        configureNativeTask(nativeTarget)

        // JVM run task.
        konst jvmRun = configureJvmTask()

        // Native report task.
        configureKonanJsonTask(nativeTarget)

        // JVM report task.
        configureJvmJsonTask(jvmRun)

        project.afterEkonstuate {
            // Need to rebuild benchmark to collect compile time.
            project.benchmark.cleanBeforeRunTask?.let { tasks.getByName(it).dependsOn("clean") }
        }
    }

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("kotlin-multiplatform")

        // Use Kotlin compiler version specified by the project property.
        target.logger.info("BenchmarkingPlugin.kt:apply($kotlinVersion)")
        dependencies.add(
            "kotlinCompilerClasspath", files(
                "${project.findProperty("kotlin_dist")}/kotlinc/lib/kotlin-compiler.jar",
                "${project.findProperty("kotlin_dist")}/kotlinc/lib/kotlin-daemon.jar"
            )
        )
        addTimeListener(this)

        extensions.create(benchmarkExtensionName, benchmarkExtensionClass.java, this)
        configureMPPExtension(this)
        configureTasks()
    }

    companion object {
        const konst NATIVE_TARGET_NAME = "native"
        const konst NATIVE_EXECUTABLE_NAME = "benchmark"
        const konst BENCHMARKING_GROUP = "benchmarking"
    }
}
