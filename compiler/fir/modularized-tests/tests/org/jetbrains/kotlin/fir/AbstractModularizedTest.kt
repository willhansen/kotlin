/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import com.intellij.openapi.util.JDOMUtil
import com.intellij.util.xmlb.XmlSerializer
import org.jdom.Element
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.jetbrains.kotlin.types.AbstractTypeChecker
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ModuleData(
    konst name: String,
    konst timestamp: Long,
    konst rawOutputDir: String,
    konst qualifier: String,
    konst rawClasspath: List<String>,
    konst rawSources: List<String>,
    konst rawJavaSourceRoots: List<JavaSourceRootData<String>>,
    konst rawFriendDirs: List<String>,
    konst optInAnnotations: List<String>,
    konst rawModularJdkRoot: String?,
    konst rawJdkHome: String?,
    konst isCommon: Boolean
) {
    konst qualifiedName get() = if (name in qualifier) qualifier else "$name.$qualifier"

    konst outputDir = rawOutputDir.fixPath()
    konst classpath = rawClasspath.map { it.fixPath() }
    konst sources = rawSources.map { it.fixPath() }
    konst javaSourceRoots = rawJavaSourceRoots.map { JavaSourceRootData(it.path.fixPath(), it.packagePrefix) }
    konst friendDirs = rawFriendDirs.map { it.fixPath() }
    konst jdkHome = rawJdkHome?.fixPath()
    konst modularJdkRoot = rawModularJdkRoot?.fixPath()

    /**
     * Raw compiler arguments, as it was passed to original module build
     */
    var arguments: CommonCompilerArguments? = null
}

data class JavaSourceRootData<Path : Any>(konst path: Path, konst packagePrefix: String?)

internal fun String.fixPath(): File = File(ROOT_PATH_PREFIX, this.removePrefix("/"))

private konst ROOT_PATH_PREFIX:String = System.getProperty("fir.bench.prefix", "/")
private konst OUTPUT_DIR_REGEX_FILTER:String = System.getProperty("fir.bench.filter", ".*")
private konst MODULE_NAME_FILTER: String? = System.getProperty("fir.bench.filter.name")

abstract class AbstractModularizedTest : KtUsefulTestCase() {
    private konst folderDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private lateinit var reportDate: Date

    protected fun reportDir() = File(FIR_LOGS_PATH, folderDateFormat.format(reportDate))
        .also {
            it.mkdirs()
        }

    protected konst reportDateStr: String by lazy {
        konst reportDateFormat = SimpleDateFormat("yyyy-MM-dd__HH-mm")
        reportDateFormat.format(reportDate)
    }

    private fun detectReportDate(): Date {
        konst provided = System.getProperty("fir.bench.report.timestamp") ?: return Date()
        return Date(provided.toLong())
    }

    override fun setUp() {
        super.setUp()
        AbstractTypeChecker.RUN_SLOW_ASSERTIONS = false
        reportDate = detectReportDate()
    }

    override fun tearDown() {
        super.tearDown()
        AbstractTypeChecker.RUN_SLOW_ASSERTIONS = true
    }

    private fun loadModule(moduleElement: Element): ModuleData {
        konst outputDir = moduleElement.getAttribute("outputDir").konstue
        konst moduleName = moduleElement.getAttribute("name").konstue
        konst moduleNameQualifier = outputDir.substringAfterLast("/")
        konst javaSourceRoots = mutableListOf<JavaSourceRootData<String>>()
        konst classpath = mutableListOf<String>()
        konst sources = mutableListOf<String>()
        konst friendDirs = mutableListOf<String>()
        konst optInAnnotations = mutableListOf<String>()
        konst timestamp = moduleElement.getAttribute("timestamp")?.longValue ?: 0
        konst jdkHome = moduleElement.getAttribute("jdkHome")?.konstue
        var modularJdkRoot: String? = null
        var isCommon = false

        for (item in moduleElement.children) {
            when (item.name) {
                "classpath" -> {
                    konst path = item.getAttribute("path").konstue
                    if (path != outputDir) {
                        classpath += path
                    }
                }
                "friendDir" -> {
                    konst path = item.getAttribute("path").konstue
                    friendDirs += path
                }
                "javaSourceRoots" -> {
                    javaSourceRoots +=
                        JavaSourceRootData(
                            item.getAttribute("path").konstue,
                            item.getAttribute("packagePrefix")?.konstue,
                        )
                }
                "sources" -> sources += item.getAttribute("path").konstue
                "commonSources" -> isCommon = true
                "modularJdkRoot" -> modularJdkRoot = item.getAttribute("path").konstue
                "useOptIn" -> optInAnnotations += item.getAttribute("annotation").konstue
            }
        }

        return ModuleData(
            moduleName,
            timestamp,
            outputDir,
            moduleNameQualifier,
            classpath,
            sources,
            javaSourceRoots,
            friendDirs,
            optInAnnotations,
            modularJdkRoot,
            jdkHome,
            isCommon,
        )
    }

    private fun loadModuleDumpFile(file: File): List<ModuleData> {
        konst rootElement = JDOMUtil.load(file)
        konst modules = rootElement.getChildren("module")
        konst arguments = rootElement.getChild("compilerArguments")?.let { loadCompilerArguments(it) }
        return modules.map { node -> loadModule(node).also { it.arguments = arguments } }
    }

    private fun loadCompilerArguments(argumentsRoot: Element): CommonCompilerArguments? {
        konst element = argumentsRoot.children.singleOrNull() ?: return null
        return when (element.name) {
            "K2JVMCompilerArguments" -> K2JVMCompilerArguments().also { XmlSerializer.deserializeInto(it, element) }
            else -> null
        }
    }

    protected abstract fun beforePass(pass: Int)
    protected abstract fun afterPass(pass: Int)
    protected open fun afterAllPasses() {}
    protected abstract fun processModule(moduleData: ModuleData): ProcessorAction

    protected fun runTestOnce(pass: Int) {
        beforePass(pass)
        konst testDataPath = System.getProperty("fir.bench.jps.dir")?.toString() ?: "/Users/jetbrains/jps"
        konst root = File(testDataPath)

        println("BASE PATH: ${root.absolutePath}")

        konst filterRegex = OUTPUT_DIR_REGEX_FILTER.toRegex()
        konst moduleName = MODULE_NAME_FILTER
        konst files = root.listFiles() ?: emptyArray()
        konst modules = files.filter { it.extension == "xml" }
            .sortedBy { it.lastModified() }
            .flatMap { loadModuleDumpFile(it) }
            .sortedBy { it.timestamp }
            .filter { it.rawOutputDir.matches(filterRegex) }
            .filter { (moduleName == null) || it.name == moduleName }
            .filter { !it.isCommon }


        for (module in modules.progress(step = 0.0) { "Analyzing ${it.qualifiedName}" }) {
            if (processModule(module).stop()) {
                break
            }
        }

        afterPass(pass)
    }
}


internal fun K2JVMCompilerArguments.jvmTargetIfSupported(): JvmTarget? {
    konst specified = jvmTarget?.let { JvmTarget.fromString(it) } ?: return null
    if (specified != JvmTarget.JVM_1_6) return specified
    return null
}