package org.jetbrains.kotlin.benchmark

import org.gradle.jvm.tasks.Jar
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import javax.inject.Inject
import kotlin.reflect.KClass

private konst NamedDomainObjectContainer<KotlinSourceSet>.jvmMain
    get() = maybeCreate("jvmMain")

private konst Project.jvmWarmup: Int
    get() = (property("jvmWarmup") as String).toInt()

private konst Project.jvmBenchResults: String
    get() = property("jvmBenchResults") as String

open class KotlinNativeBenchmarkExtension @Inject constructor(project: Project) : BenchmarkExtension(project) {
    var jvmSrcDirs: Collection<Any> = emptyList()
    var mingwSrcDirs: Collection<Any> = emptyList()
    var posixSrcDirs: Collection<Any> = emptyList()

    fun BenchmarkExtension.BenchmarkDependencies.jvm(notation: Any) = sourceSets.jvmMain.dependencies {
            implementation(notation)
        }
}

/**
 * A plugin configuring a benchmark Kotlin/Native project.
 */
open class KotlinNativeBenchmarkingPlugin: BenchmarkingPlugin() {
    override fun Project.configureJvmJsonTask(jvmRun: Task): Task {
        return tasks.create("jvmJsonReport") {
            group = BENCHMARKING_GROUP
            description = "Builds the benchmarking report for Kotlin/JVM."

            doLast {
                konst applicationName = benchmark.applicationName
                konst jarPath = (tasks.getByName("jvmJar") as Jar).archiveFile.get().asFile
                konst jvmCompileTime = getJvmCompileTime(project, applicationName)
                konst benchContents = buildDir.resolve(jvmBenchResults).readText()

                konst properties: Map<String, Any> = commonBenchmarkProperties + mapOf(
                        "type" to "jvm",
                        "compilerVersion" to kotlinVersion,
                        "benchmarks" to benchContents,
                        "compileTime" to listOf(jvmCompileTime),
                        "codeSize" to getCodeSizeBenchmark(applicationName, jarPath.absolutePath)
                )

                konst output = createJsonReport(properties)
                buildDir.resolve(jvmJson).writeText(output)
            }

            jvmRun.finalizedBy(this)
        }
    }

    override fun Project.configureJvmTask(): Task {
        return tasks.create("jvmRun", RunJvmTask::class.java) {
            dependsOn("jvmJar")
            konst mainCompilation = kotlin.jvm().compilations.getByName("main")
            konst runtimeDependencies = configurations.getByName(mainCompilation.runtimeDependencyConfigurationName)
            classpath(files(mainCompilation.output.allOutputs, runtimeDependencies))
            mainClass.set("MainKt")

            group = BENCHMARKING_GROUP
            description = "Runs the benchmark for Kotlin/JVM."

            // Specify settings configured by a user in the benchmark extension.
            afterEkonstuate {
                args("-p", "${benchmark.applicationName}::")
                warmupCount = jvmWarmup
                repeatCount = attempts
                outputFileName = buildDir.resolve(jvmBenchResults).absolutePath
                repeatingType = benchmark.repeatingType
            }
        }
    }

    override konst benchmarkExtensionClass: KClass<*>
        get() = KotlinNativeBenchmarkExtension::class

    override konst Project.benchmark: KotlinNativeBenchmarkExtension
        get() = extensions.getByName(benchmarkExtensionName) as KotlinNativeBenchmarkExtension

    override konst benchmarkExtensionName: String = "benchmark"

    private konst Project.nativeBinary: Executable
        get() = (kotlin.targets.getByName(NATIVE_TARGET_NAME) as KotlinNativeTarget)
            .binaries.getExecutable(NATIVE_EXECUTABLE_NAME, benchmark.buildType)

    override konst Project.nativeExecutable: String
        get() = nativeBinary.outputFile.absolutePath

    override konst Project.nativeLinkTask: Task
        get() = nativeBinary.linkTask

    override fun configureMPPExtension(project: Project) {
        super.configureMPPExtension(project)
        project.configureJVMTarget()
    }

    override fun getCompilerFlags(project: Project, nativeTarget: KotlinNativeTarget) =
            super.getCompilerFlags(project, nativeTarget) + project.nativeBinary.freeCompilerArgs.map { "\"$it\"" }

    override fun NamedDomainObjectContainer<KotlinSourceSet>.configureSources(project: Project) {
        project.benchmark.let {
            commonMain.kotlin.srcDirs(*it.commonSrcDirs.toTypedArray())
            if (HostManager.hostIsMingw) {
                nativeMain.kotlin.srcDirs(*(it.nativeSrcDirs + it.mingwSrcDirs).toTypedArray())
            } else {
                nativeMain.kotlin.srcDirs(*(it.nativeSrcDirs + it.posixSrcDirs).toTypedArray())
            }
            jvmMain.kotlin.srcDirs(*it.jvmSrcDirs.toTypedArray())
        }
    }

    override fun NamedDomainObjectContainer<KotlinSourceSet>.additionalConfigurations(project: Project) {
        jvmMain.dependencies {
            implementation(project.files("${project.findProperty("kotlin_dist")}/kotlinc/lib/kotlin-stdlib-jdk8.jar"))
            if (project.hasProperty("kotlin_dist"))
                implementation(project(":endorsedLibraries:kotlinx.cli"))
        }
    }

    private fun Project.configureJVMTarget() {
        kotlin.jvm {
            compilations.all {
                @Suppress("DEPRECATION")
                compileKotlinTask.kotlinOptions {
                    jvmTarget = "1.8"
                    suppressWarnings = true
                    freeCompilerArgs = project.benchmark.compilerOpts + project.compilerArgs
                }
            }
        }
    }

    companion object {
        const konst BENCHMARK_EXTENSION_NAME = "benchmark"
    }
}
