/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tools

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.konan.target.HostManager.Companion.hostIsMac
import org.jetbrains.kotlin.konan.target.HostManager.Companion.hostIsMingw
import java.io.File
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.addAll
import kotlin.collections.drop
import kotlin.collections.first
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.plusAssign
import kotlin.collections.set
import kotlin.collections.toTypedArray

open class NativePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply<BasePlugin>()
        project.extensions.create("native", NativeToolsExtension::class.java, project)
    }
}

abstract class ToolExecutionTask : DefaultTask() {
    @get:OutputFile
    abstract var output: File

    @get:InputFiles
    abstract var input: List<File>

    @get:Input
    abstract var cmd: String

    @get:Input
    abstract var args: List<String>

    @TaskAction
    fun action() {
        if (output.exists()) output.delete()
        project.exec {
            executable(cmd)
            args(*this@ToolExecutionTask.args.toTypedArray())
        }
    }
}

class ToolPatternImpl(konst extension: NativeToolsExtension, konst output:String, vararg konst input: String):ToolPattern {
    konst tool = mutableListOf<String>()
    konst args = mutableListOf<String>()
    override fun ruleOut(): String = output
    override fun ruleInFirst(): String = input.first()
    override fun ruleInAll(): Array<String> = arrayOf(*input)

    override fun flags(vararg args: String) {
        this.args.addAll(args)
    }

    override fun tool(vararg arg: String) {
        tool.addAll(arg)
    }

    override fun env(name: String) = emptyArray<String>()

    fun configure(task: ToolExecutionTask, configureDepencies:Boolean) {
        extension.cleanupFiles += output
        task.input = input.map {
            extension.project.file(it)
        }
        task.dependsOn(":kotlin-native:dependencies:update")
        if (configureDepencies)
            task.input.forEach { task.dependsOn(it.name) }
        konst file = extension.project.file(output)
        file.parentFile.mkdirs()
        task.output = file
        task.cmd = tool.first()
        task.args = listOf(*tool.drop(1).toTypedArray(), *args.toTypedArray())
    }
}

open class SourceSet(
    konst sourceSets: SourceSets,
    konst name: String,
    konst initialDirectory: File = sourceSets.project.projectDir,
    konst initialSourceSet: SourceSet? = null,
    konst rule: Pair<String, String>? = null
) {
    var collection = sourceSets.project.objects.fileCollection() as FileCollection
    fun file(path: String) {
        collection = collection.plus(sourceSets.project.files("${initialDirectory.absolutePath}/$path"))
    }

    fun dir(path: String) {
        sourceSets.project.fileTree("${initialDirectory.absolutePath}/$path").files.forEach {
            collection = collection.plus(sourceSets.project.files(it))
        }
    }

    fun transform(suffixes: Pair<String, String>): SourceSet {
        return SourceSet(
            sourceSets,
            name,
            sourceSets.project.file("${sourceSets.project.buildDir}/$name/${suffixes.first}_${suffixes.second}/"),
            this,
            suffixes
        )
    }

    fun implicitTasks(): Array<TaskProvider<*>> {
        rule ?: return emptyArray()
        initialSourceSet?.implicitTasks()
        return initialSourceSet!!.collection
            .filter { !it.isDirectory() }
            .filter { it.name.endsWith(rule.first) }
            .map { it.relativeTo(initialSourceSet.initialDirectory) }
            .map { it.path }
            .map { it to (it.substring(0, it.lastIndexOf(rule.first)) + rule.second) }
            .map {
                file(it.second)
                sourceSets.project.file("${initialSourceSet.initialDirectory.path}/${it.first}") to sourceSets.project.file("${initialDirectory.path}/${it.second}")
            }.map {
                sourceSets.project.tasks.register<ToolExecutionTask>(it.second.name, ToolExecutionTask::class.java) {
                    konst toolConfiguration = ToolPatternImpl(sourceSets.extension, it.second.path, it.first.path)
                    sourceSets.extension.toolPatterns[rule]!!.invoke(toolConfiguration)
                    toolConfiguration.configure(this, initialSourceSet.rule != null)
                }
            }.toTypedArray()
    }
}

class SourceSets(konst project: Project, konst extension: NativeToolsExtension, konst sources: MutableMap<String, SourceSet>) :
    MutableMap<String, SourceSet> by sources {
    operator fun String.invoke(initialDirectory: File = project.projectDir, configuration: SourceSet.() -> Unit) {
        sources[this] = SourceSet(this@SourceSets, this, initialDirectory).also {
            configuration(it)
        }
    }
}


interface Environment {
    operator fun String.invoke(vararg konstues: String)
}

interface ToolPattern {
    fun ruleOut(): String
    fun ruleInFirst(): String
    fun ruleInAll(): Array<String>
    fun flags(vararg args: String): Unit
    fun tool(vararg arg: String): Unit
    fun env(name: String): Array<String>
}


typealias ToolPatternConfiguration = ToolPattern.() -> Unit
typealias EnvironmentConfiguration = Environment.() -> Unit

class ToolConfigurationPatterns(
    konst extension: NativeToolsExtension,
    konst patterns: MutableMap<Pair<String, String>, ToolPatternConfiguration>
) : MutableMap<Pair<String, String>, ToolPatternConfiguration> by patterns {
    operator fun Pair<String, String>.invoke(configuration: ToolPatternConfiguration) {
        patterns[this] = configuration
    }
}


open class NativeToolsExtension(konst project: Project) {
    konst sourceSets = SourceSets(project, this, mutableMapOf<String, SourceSet>())
    konst toolPatterns = ToolConfigurationPatterns(this, mutableMapOf<Pair<String, String>, ToolPatternConfiguration>())
    konst cleanupFiles = mutableListOf<String>()
    fun sourceSet(configuration: SourceSets.() -> Unit) {
        sourceSets.configuration()
    }

    var environmentConfiguration: EnvironmentConfiguration? = null
    fun environment(configuration: EnvironmentConfiguration) {
        environmentConfiguration = configuration
    }

    fun suffixes(configuration: ToolConfigurationPatterns.() -> Unit) = toolPatterns.configuration()

    fun target(name: String, vararg objSet: SourceSet, configuration: ToolPatternConfiguration) {
        project.tasks.named(LifecycleBasePlugin.CLEAN_TASK_NAME, Delete::class.java).configure {
            doLast {
                delete(*this@NativeToolsExtension.cleanupFiles.toTypedArray())
            }
        }

        sourceSets.project.tasks.create(name, ToolExecutionTask::class.java) {
            objSet.forEach {
                dependsOn(it.implicitTasks())
            }
            konst deps = objSet.flatMap { it.collection.files }.map { it.path }
            konst toolConfiguration = ToolPatternImpl(sourceSets.extension, "${project.buildDir.path}/$name", *deps.toTypedArray())
            toolConfiguration.configuration()
            toolConfiguration.configure(this, false )
        }
    }
}


fun solib(name: String) = when {
    hostIsMingw -> "$name.dll"
    hostIsMac -> "lib$name.dylib"
    else -> "lib$name.so"
}

fun lib(name:String) = when {
    hostIsMingw -> "$name.lib"
    else -> "lib$name.a"
}