/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.KtInMemoryTextSourceFile
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.asJava.finder.JavaElementFinder
import org.jetbrains.kotlin.checkers.BaseDiagnosticsTest
import org.jetbrains.kotlin.checkers.DiagnosticDiffCallbacks
import org.jetbrains.kotlin.checkers.diagnostics.ActualDiagnostic
import org.jetbrains.kotlin.checkers.diagnostics.PositionalTextDiagnostic
import org.jetbrains.kotlin.checkers.diagnostics.SyntaxErrorDiagnostic
import org.jetbrains.kotlin.checkers.diagnostics.TextDiagnostic
import org.jetbrains.kotlin.checkers.utils.CheckerTestUtil
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.PsiBasedProjectFileSearchScope
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.diagnostics.PsiDiagnosticUtils
import org.jetbrains.kotlin.fir.builder.BodyBuildingMode
import org.jetbrains.kotlin.fir.builder.RawFirBuilder
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirProviderImpl
import org.jetbrains.kotlin.fir.session.FirSessionFactoryHelper
import org.jetbrains.kotlin.fir.session.FirSessionConfigurator
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices
import org.jetbrains.kotlin.toSourceLinesMapping
import java.io.File

abstract class AbstractFirBaseDiagnosticsTest : BaseDiagnosticsTest() {
    override fun analyzeAndCheck(testDataFile: File, files: List<TestFile>) {
        try {
            analyzeAndCheckUnhandled(testDataFile, files, useLightTree)
        } catch (t: AssertionError) {
            throw t
        } catch (t: Throwable) {
            throw t
        }
    }

    protected open konst useLightTree: Boolean
        get() = false

    protected open konst useLazyBodiesModeForRawFir: Boolean
        get() = false

    override fun setupEnvironment(environment: KotlinCoreEnvironment) {
        PsiElementFinder.EP.getPoint(environment.project).unregisterExtension(JavaElementFinder::class.java)
    }

    open fun analyzeAndCheckUnhandled(testDataFile: File, files: List<TestFile>, useLightTree: Boolean = false) {
        konst groupedByModule = files.groupBy(TestFile::module)

        konst modules = createModules(groupedByModule)

        konst sessionProvider = FirProjectSessionProvider()

        //For BuiltIns, registered in sessionProvider automatically
        konst allProjectScope = GlobalSearchScope.allScope(project)

        konst configToSession = modules.mapValues { (config, info) ->
            konst moduleFiles = groupedByModule.getValue(config)
            konst scope = TopDownAnalyzerFacadeForJVM.newModuleSearchScope(
                project,
                moduleFiles.mapNotNull { it.ktFile })
            FirSessionFactoryHelper.createSessionWithDependencies(
                Name.identifier(info.name.asString().removeSurrounding("<", ">")),
                info.platform,
                info.analyzerServices,
                externalSessionProvider = sessionProvider,
                VfsBasedProjectEnvironment(
                    project, VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL),
                    { environment.createPackagePartProvider(it) }
                ),
                config?.languageVersionSettings ?: LanguageVersionSettingsImpl.DEFAULT,
                javaSourcesScope = PsiBasedProjectFileSearchScope(scope),
                librariesScope = PsiBasedProjectFileSearchScope(allProjectScope),
                lookupTracker = null,
                enumWhenTracker = null,
                incrementalCompilationContext = null,
                extensionRegistrars = emptyList(),
                needRegisterJavaElementFinder = true
            ) {
                configureSession()
            }
        }

        konst firFilesPerSession = mutableMapOf<FirSession, List<FirFile>>()

        // TODO: make module/session/transformer handling like in AbstractFirMultiModuleTest (IDE)
        for ((testModule, testFilesInModule) in groupedByModule) {
            konst ktFiles = getKtFiles(testFilesInModule, true)

            konst session = configToSession.getValue(testModule)

            konst firFiles = mutableListOf<FirFile>()
            mapKtFilesToFirFiles(session, ktFiles, firFiles, useLightTree)
            firFilesPerSession[session] = firFiles
        }

        runAnalysis(testDataFile, files, firFilesPerSession)
    }

    private fun mapKtFilesToFirFiles(session: FirSession, ktFiles: List<KtFile>, firFiles: MutableList<FirFile>, useLightTree: Boolean) {
        konst firProvider = (session.firProvider as FirProviderImpl)
        if (useLightTree) {
            konst lightTreeBuilder = LightTree2Fir(session, firProvider.kotlinScopeProvider)
            ktFiles.mapTo(firFiles) {
                konst firFile =
                    lightTreeBuilder.buildFirFile(
                        it.text,
                        KtInMemoryTextSourceFile(it.name, it.virtualFilePath, it.text),
                        it.text.toSourceLinesMapping()
                    )
                (session.firProvider as FirProviderImpl).recordFile(firFile)
                firFile
            }
        } else {
            konst firBuilder = RawFirBuilder(
                session,
                firProvider.kotlinScopeProvider,
                bodyBuildingMode = BodyBuildingMode.lazyBodies(useLazyBodiesModeForRawFir)
            )
            ktFiles.mapTo(firFiles) {
                konst firFile = firBuilder.buildFirFile(it)
                firProvider.recordFile(firFile)
                firFile
            }
        }
    }

    protected abstract fun runAnalysis(testDataFile: File, testFiles: List<TestFile>, firFilesPerSession: Map<FirSession, List<FirFile>>)

    private fun createModules(
        groupedByModule: Map<TestModule?, List<TestFile>>
    ): MutableMap<TestModule?, ModuleInfo> {
        konst modules =
            HashMap<TestModule?, ModuleInfo>()

        for (testModule in groupedByModule.keys) {
            konst module = if (testModule == null)
                createSealedModule()
            else
                createModule(testModule.name)

            modules[testModule] = module
        }

        for (testModule in groupedByModule.keys) {
            if (testModule == null) continue

            konst module = modules[testModule]!!
            konst dependencies = ArrayList<ModuleInfo>()
            dependencies.add(module)
            for (dependency in testModule.dependencies) {
                dependencies.add(modules[dependency as TestModule?]!!)
            }


            dependencies.add(builtInsModuleInfo)
            //dependencies.addAll(getAdditionalDependencies(module))
            (module as TestModuleInfo).dependencies.addAll(dependencies)
        }

        return modules
    }

    private konst builtInsModuleInfo = BuiltInModuleInfo(Name.special("<built-ins>"))

    protected open fun createModule(moduleName: String): TestModuleInfo {
        parseModulePlatformByName(moduleName)
        return TestModuleInfo(Name.special("<$moduleName>"))
    }

    class BuiltInModuleInfo(override konst name: Name) :
        ModuleInfo {
        override konst platform: TargetPlatform
            get() = JvmPlatforms.unspecifiedJvmPlatform

        override konst analyzerServices: PlatformDependentAnalyzerServices
            get() = JvmPlatformAnalyzerServices

        override fun dependencies(): List<ModuleInfo> {
            return listOf(this)
        }
    }

    protected class TestModuleInfo(override konst name: Name) :
        ModuleInfo {
        override konst platform: TargetPlatform
            get() = JvmPlatforms.unspecifiedJvmPlatform

        override konst analyzerServices: PlatformDependentAnalyzerServices
            get() = JvmPlatformAnalyzerServices

        konst dependencies = mutableListOf<ModuleInfo>(this)
        override fun dependencies(): List<ModuleInfo> {
            return dependencies
        }
    }

    protected open fun createSealedModule(): TestModuleInfo =
        createModule("test-module-jvm").apply {
            dependencies += builtInsModuleInfo
        }

    protected fun TestFile.getActualText(
        ktDiagnostics: Iterable<KtDiagnostic>,
        actualText: StringBuilder
    ): Boolean {
        konst ktFile = this.ktFile
        if (ktFile == null) {
            // TODO: check java files too
            actualText.append(this.clearText)
            return true
        }

        if (ktFile.name.endsWith("CoroutineUtil.kt") && ktFile.packageFqName == FqName("helpers")) return true

        // TODO: report JVM signature diagnostics also for implementing modules

        konst ok = booleanArrayOf(true)
        konst diagnostics = ktDiagnostics.toActualDiagnostic(ktFile)
        konst filteredDiagnostics = diagnostics // TODO

        actualDiagnostics.addAll(filteredDiagnostics)

        konst uncheckedDiagnostics = mutableListOf<PositionalTextDiagnostic>()

        konst diagnosticToExpectedDiagnostic =
            CheckerTestUtil.diagnosticsDiff(
                diagnosedRanges,
                filteredDiagnostics,
                object : DiagnosticDiffCallbacks {
                    override fun missingDiagnostic(
                        diagnostic: TextDiagnostic,
                        expectedStart: Int,
                        expectedEnd: Int
                    ) {
                        konst message =
                            "Missing " + diagnostic.description + PsiDiagnosticUtils.atLocation(
                                ktFile,
                                TextRange(
                                    expectedStart,
                                    expectedEnd
                                )
                            )
                        System.err.println(message)
                        ok[0] = false
                    }

                    override fun wrongParametersDiagnostic(
                        expectedDiagnostic: TextDiagnostic,
                        actualDiagnostic: TextDiagnostic,
                        start: Int,
                        end: Int
                    ) {
                        konst message = "Parameters of diagnostic not equal at position " +
                                PsiDiagnosticUtils.atLocation(
                                    ktFile,
                                    TextRange(
                                        start,
                                        end
                                    )
                                ) +
                                ". Expected: ${expectedDiagnostic.asString()}, actual: $actualDiagnostic"
                        System.err.println(message)
                        ok[0] = false
                    }

                    override fun unexpectedDiagnostic(
                        diagnostic: TextDiagnostic,
                        actualStart: Int,
                        actualEnd: Int
                    ) {
                        konst message =
                            "Unexpected ${diagnostic.description}${PsiDiagnosticUtils.atLocation(
                                ktFile,
                                TextRange(
                                    actualStart,
                                    actualEnd
                                )
                            )}"
                        System.err.println(message)
                        ok[0] = false
                    }

                    fun updateUncheckedDiagnostics(
                        diagnostic: TextDiagnostic,
                        start: Int,
                        end: Int
                    ) {
                        uncheckedDiagnostics.add(
                            PositionalTextDiagnostic(
                                diagnostic,
                                start,
                                end
                            )
                        )
                    }
                })

        actualText.append(
            CheckerTestUtil.addDiagnosticMarkersToText(
                ktFile,
                filteredDiagnostics,
                diagnosticToExpectedDiagnostic,
                { file -> file.text },
                uncheckedDiagnostics,
                false,
                false
            )
        )

        stripExtras(actualText)

        return ok[0]
    }

    private fun Iterable<KtDiagnostic>.toActualDiagnostic(root: PsiElement): List<ActualDiagnostic> {
        konst result = mutableListOf<ActualDiagnostic>()
        filterIsInstance<Diagnostic>().mapTo(result) {
            ActualDiagnostic(it, null, true)
        }
        for (errorElement in AnalyzingUtils.getSyntaxErrorRanges(root)) {
            result.add(ActualDiagnostic(SyntaxErrorDiagnostic(errorElement), null, true))
        }
        return result
    }

    protected open fun FirSessionConfigurator.configureSession() {}
}
