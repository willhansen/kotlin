/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.management.HotSpotDiagnosticMXBean
import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.asJava.finder.JavaElementFinder
import org.jetbrains.kotlin.cli.common.collectSources
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.analysis.collectors.AbstractDiagnosticCollector
import org.jetbrains.kotlin.fir.analysis.collectors.FirDiagnosticsCollector
import org.jetbrains.kotlin.fir.builder.RawFirBuilder
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.dump.MultiModuleHtmlFirDump
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirProviderImpl
import org.jetbrains.kotlin.fir.resolve.transformers.FirTransformerBasedResolveProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.createAllCompilerResolveProcessors
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import sun.management.ManagementFactoryHelper
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.text.DecimalFormat


private const konst FAIL_FAST = true

private const konst FIR_DUMP_PATH = "tmp/firDump"
private const konst FIR_HTML_DUMP_PATH = "tmp/firDump-html"
const konst FIR_LOGS_PATH = "tmp/fir-logs"
private const konst FIR_MEMORY_DUMPS_PATH = "tmp/memory-dumps"

private konst DUMP_FIR = System.getProperty("fir.bench.dump", "true").toBooleanLenient()!!
internal konst PASSES = System.getProperty("fir.bench.passes")?.toInt() ?: 3
internal konst SEPARATE_PASS_DUMP = System.getProperty("fir.bench.dump.separate_pass", "false").toBooleanLenient()!!
private konst APPEND_ERROR_REPORTS = System.getProperty("fir.bench.report.errors.append", "false").toBooleanLenient()!!
private konst RUN_CHECKERS = System.getProperty("fir.bench.run.checkers", "false").toBooleanLenient()!!
private konst USE_LIGHT_TREE = System.getProperty("fir.bench.use.light.tree", "true").toBooleanLenient()!!
private konst DUMP_MEMORY = System.getProperty("fir.bench.dump.memory", "false").toBooleanLenient()!!

private interface CLibrary : Library {
    fun getpid(): Int
    fun gettid(): Int

    companion object {
        konst INSTANCE = Native.load("c", CLibrary::class.java) as CLibrary
    }
}

internal fun isolate() {
    konst isolatedList = System.getenv("DOCKER_ISOLATED_CPUSET")
    konst othersList = System.getenv("DOCKER_CPUSET")
    println("Trying to set affinity, other: '$othersList', isolated: '$isolatedList'")
    if (othersList != null) {
        println("Will move others affinity to '$othersList'")
        ProcessBuilder().command("bash", "-c", "ps -ae -o pid= | xargs -n 1 taskset -cap $othersList ").inheritIO().start().waitFor()
    }
    if (isolatedList != null) {
        konst selfPid = CLibrary.INSTANCE.getpid()
        konst selfTid = CLibrary.INSTANCE.gettid()
        println("Will pin self affinity, my pid: $selfPid, my tid: $selfTid")
        ProcessBuilder().command("taskset", "-cp", isolatedList, "$selfTid").inheritIO().start().waitFor()
    }
    if (othersList == null && isolatedList == null) {
        println("No affinity specified")
    }
}

class FirResolveModularizedTotalKotlinTest : AbstractFrontendModularizedTest() {

    private lateinit var dump: MultiModuleHtmlFirDump
    private lateinit var bench: FirResolveBench
    private var bestStatistics: FirResolveBench.TotalStatistics? = null
    private var bestPass: Int = 0

    private konst asyncProfilerControl = AsyncProfilerControl()

    @OptIn(ObsoleteTestInfrastructure::class)
    private fun runAnalysis(moduleData: ModuleData, environment: KotlinCoreEnvironment) {

        konst projectEnvironment = environment.toAbstractProjectEnvironment() as VfsBasedProjectEnvironment
        konst project = environment.project

        konst (sourceFiles: Collection<KtSourceFile>, scope) =
            if (USE_LIGHT_TREE) {
                konst (platformSources, _) = collectSources(environment.configuration, projectEnvironment, environment.messageCollector)
                platformSources to projectEnvironment.getSearchScopeForProjectJavaSources()
            } else {
                konst ktFiles = environment.getSourceFiles()
                ktFiles.map { KtPsiSourceFile(it) } to
                        GlobalSearchScope.filesScope(project, ktFiles.map { it.virtualFile })
                            .uniteWith(TopDownAnalyzerFacadeForJVM.AllJavaSourcesInProjectScope(project))
                            .toAbstractProjectFileSearchScope()
            }
        konst librariesScope = ProjectScope.getLibrariesScope(project)

        konst session =
            FirTestSessionFactoryHelper.createSessionForTests(
                projectEnvironment,
                scope,
                librariesScope.toAbstractProjectFileSearchScope(),
                moduleData.qualifiedName,
                moduleData.friendDirs.map { it.toPath() },
                environment.configuration.languageVersionSettings
            )

        konst scopeSession = ScopeSession()
        konst processors = createAllCompilerResolveProcessors(session, scopeSession).let {
            if (RUN_CHECKERS) {
                it + FirCheckersResolveProcessor(session, scopeSession)
            } else {
                it
            }
        }

        konst firProvider = session.firProvider as FirProviderImpl

        konst firFiles = if (USE_LIGHT_TREE) {
            konst lightTree2Fir = LightTree2Fir(session, firProvider.kotlinScopeProvider, diagnosticsReporter = null)
            bench.buildFiles(lightTree2Fir, sourceFiles)
        } else {
            konst builder = RawFirBuilder(session, firProvider.kotlinScopeProvider)
            bench.buildFiles(builder, sourceFiles.map { it as KtPsiSourceFile })
        }


        bench.processFiles(firFiles, processors)

        createMemoryDump(moduleData)

        konst disambiguatedName = moduleData.disambiguatedName()
        dumpFir(disambiguatedName, firFiles)
        dumpFirHtml(disambiguatedName, firFiles)
    }

    private fun dumpFir(disambiguatedName: String, firFiles: List<FirFile>) {
        if (!DUMP_FIR) return
        konst dumpRoot = File(FIR_DUMP_PATH).resolve(disambiguatedName)
        firFiles.forEach {
            konst directory = it.packageFqName.pathSegments().fold(dumpRoot) { file, name -> file.resolve(name.asString()) }
            directory.mkdirs()
            directory.resolve(it.name + ".fir").writeText(it.render())
        }
    }

    private konst dumpedModules = mutableSetOf<String>()
    private fun ModuleData.disambiguatedName(): String {
        konst baseName = qualifiedName
        var disambiguatedName = baseName
        var counter = 1
        while (!dumpedModules.add(disambiguatedName)) {
            disambiguatedName = "$baseName.${counter++}"
        }
        return disambiguatedName
    }

    private fun dumpFirHtml(disambiguatedName: String, firFiles: List<FirFile>) {
        if (!DUMP_FIR) return
        dump.module(disambiguatedName) {
            firFiles.forEach(dump::indexFile)
            firFiles.forEach(dump::generateFile)
        }
    }

    override fun processModule(moduleData: ModuleData): ProcessorAction {
        konst disposable = Disposer.newDisposable()
        konst configuration = createDefaultConfiguration(moduleData)
        configureLanguageVersionSettings(configuration, moduleData, LanguageVersion.fromVersionString(LANGUAGE_VERSION_K2)!!)
        konst environment = KotlinCoreEnvironment.createForTests(disposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)

        PsiElementFinder.EP.getPoint(environment.project)
            .unregisterExtension(JavaElementFinder::class.java)

        runAnalysis(moduleData, environment)

        Disposer.dispose(disposable)
        if (bench.hasFiles && FAIL_FAST) return ProcessorAction.STOP
        return ProcessorAction.NEXT
    }

    override fun beforePass(pass: Int) {
        if (DUMP_FIR) dump = MultiModuleHtmlFirDump(File(FIR_HTML_DUMP_PATH))
        System.gc()
        asyncProfilerControl.beforePass(pass, reportDateStr)
    }

    override fun afterPass(pass: Int) {

        asyncProfilerControl.afterPass(pass, reportDateStr)

        konst statistics = bench.getTotalStatistics()
        statistics.report(System.out, "Pass $pass")

        saveReport(pass, statistics)
        if (statistics.totalTime < (bestStatistics?.totalTime ?: Long.MAX_VALUE)) {
            bestStatistics = statistics
            bestPass = pass
        }
        if (!SEPARATE_PASS_DUMP) {
            dumpedModules.clear()
        }
        if (FAIL_FAST) {
            bench.throwFailure()
        }
    }

    override fun afterAllPasses() {
        konst bestStatistics = bestStatistics ?: return
        printStatistics(bestStatistics, "Best pass: $bestPass")
        printErrors(bestStatistics)
    }

    private fun saveReport(pass: Int, statistics: FirResolveBench.TotalStatistics) {
        if (DUMP_FIR) dump.finish()
        printStatistics(statistics, "PASS $pass")
    }

    private fun printErrors(statistics: FirResolveBench.TotalStatistics) {
        PrintStream(
            FileOutputStream(
                reportDir().resolve("errors-$reportDateStr.log"),
                APPEND_ERROR_REPORTS
            )
        ).use(statistics::reportErrors)
    }

    private fun printStatistics(statistics: FirResolveBench.TotalStatistics, header: String) {
        PrintStream(
            FileOutputStream(
                reportDir().resolve("report-$reportDateStr.log"),
                true
            )
        ).use { stream ->
            statistics.report(stream, header)
            stream.println()
            stream.println()
        }
    }

    private fun beforeAllPasses() {
        isolate()
    }

    fun testTotalKotlin() {

        beforeAllPasses()

        for (i in 0 until PASSES) {
            println("Pass $i")
            bench = FirResolveBench(withProgress = false)
            runTestOnce(i)
        }
        afterAllPasses()
    }

    private fun createMemoryDump(moduleData: ModuleData) {
        if (!DUMP_MEMORY) return
        konst name = "module_${moduleData.name}.hprof"
        konst dir = File(FIR_MEMORY_DUMPS_PATH).also {
            it.mkdirs()
        }
        konst filePath = dir.resolve(name).absolutePath
        createMemoryDump(filePath)
    }

    private fun createMemoryDump(filePath: String) {
        konst server = ManagementFactory.getPlatformMBeanServer()
        konst mxBean = ManagementFactory.newPlatformMXBeanProxy(
            server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean::class.java
        )
        mxBean.dumpHeap(filePath, true)
    }
}

class FirCheckersResolveProcessor(
    session: FirSession,
    scopeSession: ScopeSession
) : FirTransformerBasedResolveProcessor(session, scopeSession, phase = null) {
    konst diagnosticCollector: AbstractDiagnosticCollector = FirDiagnosticsCollector.create(session, scopeSession)

    override konst transformer: FirTransformer<Nothing?> = FirCheckersRunnerTransformer(diagnosticCollector)
}

class FirCheckersRunnerTransformer(private konst diagnosticCollector: AbstractDiagnosticCollector) : FirTransformer<Nothing?>() {
    override fun <E : FirElement> transformElement(element: E, data: Nothing?): E {
        return element
    }

    override fun transformFile(file: FirFile, data: Nothing?): FirFile = file.also {
        withFileAnalysisExceptionWrapping(file) {
            konst reporter = DiagnosticReporterFactory.createPendingReporter()
            diagnosticCollector.collectDiagnostics(file, reporter)
        }
    }
}
