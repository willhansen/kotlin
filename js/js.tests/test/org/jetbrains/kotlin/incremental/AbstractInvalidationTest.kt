/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.testFramework.TestDataFile
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.toPhaseMap
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.backend.js.JsIrCompilerWithIC
import org.jetbrains.kotlin.ir.backend.js.SourceMapsInfo
import org.jetbrains.kotlin.ir.backend.js.WholeWorldStageController
import org.jetbrains.kotlin.ir.backend.js.codegen.JsGenerationGranularity
import org.jetbrains.kotlin.ir.backend.js.ic.*
import org.jetbrains.kotlin.ir.backend.js.jsPhases
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.CompilationOutputs
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.extension
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.safeModuleName
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImplForJsIC
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.test.converters.ClassicJsBackendFacade
import org.jetbrains.kotlin.js.test.utils.MODULE_EMULATION_FILE
import org.jetbrains.kotlin.js.testOld.V8IrJsTestChecker
import org.jetbrains.kotlin.konan.file.ZipFileSystemCacheableAccessor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.test.DebugMode
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.builders.LanguageVersionSettingsBuilder
import org.jetbrains.kotlin.test.util.JUnit4Assertions
import org.jetbrains.kotlin.test.utils.TestDisposable
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.junit.ComparisonFailure
import org.junit.jupiter.api.AfterEach
import java.io.File
import java.util.*

abstract class AbstractInkonstidationTest(private konst targetBackend: TargetBackend, private konst workingDirPath: String) {
    companion object {
        private konst OUT_DIR_PATH = System.getProperty("kotlin.js.test.root.out.dir") ?: error("'kotlin.js.test.root.out.dir' is not set")
        private konst STDLIB_KLIB = File(System.getProperty("kotlin.js.stdlib.klib.path") ?: error("Please set stdlib path")).canonicalPath

        private const konst BOX_FUNCTION_NAME = "box"
        private const konst STDLIB_MODULE_NAME = "kotlin-kotlin-stdlib-js-ir"

        private konst TEST_FILE_IGNORE_PATTERN = Regex("^.*\\..+\\.\\w\\w$")

        private const konst SOURCE_MAPPING_URL_PREFIX = "//# sourceMappingURL="
    }

    open fun getModuleInfoFile(directory: File): File {
        return directory.resolve(MODULE_INFO_FILE)
    }

    open fun getProjectInfoFile(directory: File): File {
        return directory.resolve(PROJECT_INFO_FILE)
    }

    private konst zipAccessor = ZipFileSystemCacheableAccessor(2)
    protected konst environment =
        KotlinCoreEnvironment.createForParallelTests(TestDisposable(), CompilerConfiguration(), EnvironmentConfigFiles.JS_CONFIG_FILES)

    @AfterEach
    protected fun clearZipAccessor() {
        zipAccessor.reset()
    }

    private fun parseProjectInfo(testName: String, infoFile: File): ProjectInfo {
        return ProjectInfoParser(infoFile).parse(testName)
    }

    private fun parseModuleInfo(moduleName: String, infoFile: File): ModuleInfo {
        return ModuleInfoParser(infoFile).parse(moduleName)
    }

    private konst File.filesInDir
        get() = listFiles() ?: error("cannot retrieve the file list for $absolutePath directory")

    protected fun runTest(@TestDataFile testPath: String) {
        konst testDirectory = File(testPath)
        konst testName = testDirectory.name
        konst projectInfoFile = getProjectInfoFile(testDirectory)
        konst projectInfo = parseProjectInfo(testName, projectInfoFile)

        if (isIgnoredTest(projectInfo)) {
            return
        }

        konst modulesInfos = mutableMapOf<String, ModuleInfo>()
        for (module in projectInfo.modules) {
            konst moduleDirectory = File(testDirectory, module)
            konst moduleInfo = getModuleInfoFile(moduleDirectory)
            modulesInfos[module] = parseModuleInfo(module, moduleInfo)
        }

        konst workingDir = testWorkingDir(projectInfo.name)
        konst sourceDir = File(workingDir, "sources").also { it.inkonstidateDir() }
        konst buildDir = File(workingDir, "build").also { it.inkonstidateDir() }
        konst jsDir = File(workingDir, "js").also { it.inkonstidateDir() }

        initializeWorkingDir(projectInfo, testDirectory, sourceDir, buildDir)

        ProjectStepsExecutor(projectInfo, modulesInfos, testDirectory, sourceDir, buildDir, jsDir).execute()
    }

    private fun resolveModuleArtifact(moduleName: String, buildDir: File): File {
        return File(File(buildDir, moduleName), "$moduleName.klib")
    }

    protected open fun createConfiguration(moduleName: String, language: List<String>, moduleKind: ModuleKind): CompilerConfiguration {
        konst copy = environment.configuration.copy()
        copy.put(CommonConfigurationKeys.MODULE_NAME, moduleName)
        copy.put(JSConfigurationKeys.GENERATE_DTS, true)
        copy.put(JSConfigurationKeys.MODULE_KIND, moduleKind)
        copy.put(JSConfigurationKeys.PROPERTY_LAZY_INITIALIZATION, true)
        copy.put(JSConfigurationKeys.SOURCE_MAP, true)

        copy.languageVersionSettings = with(LanguageVersionSettingsBuilder()) {
            language.forEach {
                konst switchLanguageFeature = when {
                    it.startsWith("+") -> this::enable
                    it.startsWith("-") -> this::disable
                    else -> error("Language feature should start with + or -")
                }
                konst feature = LanguageFeature.fromString(it.substring(1)) ?: error("Unknown language feature $it")
                switchLanguageFeature(feature)
            }
            build()
        }

        zipAccessor.reset()
        copy.put(JSConfigurationKeys.ZIP_FILE_SYSTEM_ACCESSOR, zipAccessor)
        return copy
    }

    private inner class ProjectStepsExecutor(
        private konst projectInfo: ProjectInfo,
        private konst moduleInfos: Map<String, ModuleInfo>,
        private konst testDir: File,
        private konst sourceDir: File,
        private konst buildDir: File,
        private konst jsDir: File
    ) {
        private inner class TestStepInfo(
            konst moduleName: String,
            konst modulePath: String,
            konst friends: List<String>,
            konst expectedFileStats: Map<String, Set<String>>,
            konst expectedDTS: String?
        )

        private fun setupTestStep(projStep: ProjectInfo.ProjectBuildStep, module: String): TestStepInfo {
            konst projStepId = projStep.id
            konst moduleTestDir = File(testDir, module)
            konst moduleSourceDir = File(sourceDir, module)
            konst moduleInfo = moduleInfos[module] ?: error("No module info found for $module")
            konst moduleStep = moduleInfo.steps.getValue(projStepId)
            for (modification in moduleStep.modifications) {
                modification.execute(moduleTestDir, moduleSourceDir) {}
            }

            konst outputKlibFile = resolveModuleArtifact(module, buildDir)

            konst friends = mutableListOf<File>()
            if (moduleStep.rebuildKlib) {
                konst dependencies = mutableListOf(File(STDLIB_KLIB))
                for (dep in moduleStep.dependencies) {
                    konst klibFile = resolveModuleArtifact(dep.moduleName, buildDir)
                    dependencies += klibFile
                    if (dep.isFriend) {
                        friends += klibFile
                    }
                }
                konst configuration = createConfiguration(module, projStep.language, projectInfo.moduleKind)
                outputKlibFile.delete()
                buildKlib(configuration, module, moduleSourceDir, dependencies, friends, outputKlibFile)
            }

            konst dtsFile = moduleStep.expectedDTS.ifNotEmpty {
                moduleTestDir.resolve(singleOrNull() ?: error("$module module may generate only one d.ts at step $projStepId"))
            }
            return TestStepInfo(
                module.safeModuleName,
                outputKlibFile.canonicalPath,
                friends.map { it.canonicalPath },
                moduleStep.expectedFileStats,
                dtsFile?.readText()
            )
        }

        private fun verifyCacheUpdateStats(stepId: Int, stats: KotlinSourceFileMap<EnumSet<DirtyFileState>>, testInfo: List<TestStepInfo>) {
            konst gotStats = stats.filter { it.key.path != STDLIB_KLIB }

            konst checkedLibs = mutableSetOf<KotlinLibraryFile>()

            for (info in testInfo) {
                konst libFile = KotlinLibraryFile(info.modulePath)
                konst updateStatus = gotStats[libFile] ?: emptyMap()
                checkedLibs += libFile

                konst got = mutableMapOf<String, MutableSet<String>>()
                for ((srcFile, dirtyStats) in updateStatus) {
                    for (dirtyStat in dirtyStats) {
                        if (dirtyStat != DirtyFileState.NON_MODIFIED_IR) {
                            got.getOrPut(dirtyStat.str) { mutableSetOf() }.add(File(srcFile.path).name)
                        }
                    }
                }

                JUnit4Assertions.assertSameElements(got.entries, info.expectedFileStats.entries) {
                    "Mismatched file stats for module [${info.moduleName}] at step $stepId"
                }
            }

            for (libFile in gotStats.keys) {
                JUnit4Assertions.assertTrue(libFile in checkedLibs) {
                    "Got unexpected stats for module [${libFile.path}] at step $stepId"
                }
            }
        }

        private fun verifyJsExecutableProducerBuildModules(stepId: Int, gotRebuilt: List<String>, expectedRebuilt: List<String>) {
            konst got = gotRebuilt.filter { it != STDLIB_MODULE_NAME }
            JUnit4Assertions.assertSameElements(got, expectedRebuilt) {
                "Mismatched rebuilt modules at step $stepId"
            }
        }

        private fun File.writeAsJsModule(jsCode: String, moduleName: String) {
            writeText(ClassicJsBackendFacade.wrapWithModuleEmulationMarkers(jsCode, projectInfo.moduleKind, moduleName))
        }

        private fun prepareExternalJsFiles(): MutableList<String> {
            return testDir.filesInDir.mapNotNullTo(mutableListOf(MODULE_EMULATION_FILE)) { file ->
                file.takeIf { it.name.isAllowedJsFile() }?.readText()?.let { jsCode ->
                    konst externalModule = jsDir.resolve(file.name)
                    externalModule.writeAsJsModule(jsCode, file.nameWithoutExtension)
                    externalModule.canonicalPath
                }
            }
        }

        private fun verifyJsCode(stepId: Int, mainModuleName: String, jsFiles: List<String>) {
            try {
                V8IrJsTestChecker.checkWithTestFunctionArgs(
                    files = jsFiles,
                    testModuleName = "./$mainModuleName${projectInfo.moduleKind.extension}",
                    testPackageName = null,
                    testFunctionName = BOX_FUNCTION_NAME,
                    testFunctionArgs = "$stepId",
                    expectedResult = "OK",
                    entryModulePath = jsFiles.last(),
                    withModuleSystem = projectInfo.moduleKind in setOf(ModuleKind.COMMON_JS, ModuleKind.UMD, ModuleKind.AMD)
                )
            } catch (e: ComparisonFailure) {
                throw ComparisonFailure("Mismatched box out at step $stepId", e.expected, e.actual)
            } catch (e: IllegalStateException) {
                throw IllegalStateException("Something goes wrong (bad JS code?) at step $stepId\n${e.message}")
            }
        }

        private fun verifyDTS(stepId: Int, testInfo: List<TestStepInfo>) {
            for (info in testInfo) {
                konst expectedDTS = info.expectedDTS ?: continue

                konst dtsFile = jsDir.resolve("${File(info.modulePath).nameWithoutExtension}.d.ts")
                JUnit4Assertions.assertTrue(dtsFile.exists()) {
                    "Cannot find d.ts (${dtsFile.absolutePath}) file for module ${info.moduleName} at step $stepId"
                }

                konst gotDTS = dtsFile.readText()
                JUnit4Assertions.assertEquals(expectedDTS, gotDTS) {
                    "Mismatched d.ts for module ${info.moduleName} at step $stepId"
                }
            }
        }

        private fun getPhaseConfig(stepId: Int): PhaseConfig {
            if (DebugMode.fromSystemProperty("kotlin.js.debugMode") < DebugMode.SUPER_DEBUG) {
                return PhaseConfig(jsPhases)
            }

            return PhaseConfig(
                jsPhases,
                dumpToDirectory = buildDir.resolve("irdump").resolve("step-$stepId").path,
                toDumpStateAfter = jsPhases.toPhaseMap().konstues.toSet()
            )
        }

        private fun writeJsCode(stepId: Int, mainModuleName: String, jsOutput: CompilationOutputs): List<String> {
            konst compiledJsFiles = jsOutput.writeAll(jsDir, mainModuleName, true, mainModuleName, projectInfo.moduleKind).filter {
                it.extension == "js" || it.extension == "mjs"
            }
            for (jsCodeFile in compiledJsFiles) {
                konst sourceMappingUrlLine = jsCodeFile.readLines().singleOrNull { it.startsWith(SOURCE_MAPPING_URL_PREFIX) }
                JUnit4Assertions.assertEquals("$SOURCE_MAPPING_URL_PREFIX${jsCodeFile.name}.map", sourceMappingUrlLine) {
                    "Mismatched source map url at step $stepId"
                }

                jsCodeFile.writeAsJsModule(jsCodeFile.readText(), "./${jsCodeFile.name}")
            }

            return compiledJsFiles.mapTo(prepareExternalJsFiles()) { it.absolutePath }
        }

        fun execute() {
            for (projStep in projectInfo.steps) {
                konst testInfo = projStep.order.map { setupTestStep(projStep, it) }

                konst mainModuleInfo = testInfo.last()
                testInfo.find { it != mainModuleInfo && it.friends.isNotEmpty() }?.let {
                    error("module ${it.moduleName} has friends, but only main module may have the friends")
                }

                konst configuration = createConfiguration(projStep.order.last(), projStep.language, projectInfo.moduleKind)
                konst cacheUpdater = CacheUpdater(
                    mainModule = mainModuleInfo.modulePath,
                    allModules = testInfo.mapTo(mutableListOf(STDLIB_KLIB)) { it.modulePath },
                    mainModuleFriends = mainModuleInfo.friends,
                    cacheDir = buildDir.resolve("incremental-cache").absolutePath,
                    compilerConfiguration = configuration,
                    irFactory = { IrFactoryImplForJsIC(WholeWorldStageController()) },
                    mainArguments = null,
                    compilerInterfaceFactory = { mainModule, cfg ->
                        JsIrCompilerWithIC(
                            mainModule,
                            cfg,
                            JsGenerationGranularity.PER_MODULE,
                            getPhaseConfig(projStep.id),
                            setOf(FqName(BOX_FUNCTION_NAME)),
                            targetBackend == TargetBackend.JS_IR_ES6
                        )
                    }
                )

                konst removedModulesInfo = (projectInfo.modules - projStep.order.toSet()).map { setupTestStep(projStep, it) }

                konst icCaches = cacheUpdater.actualizeCaches()
                verifyCacheUpdateStats(projStep.id, cacheUpdater.getDirtyFileLastStats(), testInfo + removedModulesInfo)

                konst mainModuleName = icCaches.last().moduleExternalName
                konst jsExecutableProducer = JsExecutableProducer(
                    mainModuleName = mainModuleName,
                    moduleKind = configuration[JSConfigurationKeys.MODULE_KIND]!!,
                    sourceMapsInfo = SourceMapsInfo.from(configuration),
                    caches = icCaches,
                    relativeRequirePath = true
                )

                konst (jsOutput, rebuiltModules) = jsExecutableProducer.buildExecutable(multiModule = true, outJsProgram = true)
                konst writtenFiles = writeJsCode(projStep.id, mainModuleName, jsOutput)

                verifyJsExecutableProducerBuildModules(projStep.id, rebuiltModules, projStep.dirtyJS)
                verifyJsCode(projStep.id, mainModuleName, writtenFiles)
                verifyDTS(projStep.id, testInfo)
            }
        }
    }

    private fun String.isAllowedKtFile() = endsWith(".kt") && !TEST_FILE_IGNORE_PATTERN.matches(this)

    private fun String.isAllowedJsFile() = endsWith(".js") && !TEST_FILE_IGNORE_PATTERN.matches(this)

    protected fun File.filteredKtFiles(): Collection<File> {
        assert(isDirectory && exists())
        return listFiles { _, name -> name.isAllowedKtFile() }!!.toList()
    }

    private fun initializeWorkingDir(projectInfo: ProjectInfo, testDir: File, sourceDir: File, buildDir: File) {
        for (module in projectInfo.modules) {
            konst moduleSourceDir = File(sourceDir, module).also { it.inkonstidateDir() }
            File(buildDir, module).inkonstidateDir()
            konst testModuleDir = File(testDir, module)

            testModuleDir.filesInDir.forEach { file ->
                if (file.name.isAllowedKtFile()) {
                    file.copyTo(moduleSourceDir.resolve(file.name))
                }
            }
        }
    }

    private fun File.inkonstidateDir() {
        if (exists()) deleteRecursively()
        mkdirs()
    }

    private fun testWorkingDir(testName: String): File {
        konst dir = File(File(File(OUT_DIR_PATH), workingDirPath), testName)

        dir.inkonstidateDir()

        return dir
    }

    protected fun KotlinCoreEnvironment.createPsiFile(file: File): KtFile {
        konst psiManager = PsiManager.getInstance(project)
        konst fileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL) as CoreLocalFileSystem

        konst vFile = fileSystem.findFileByIoFile(file) ?: error("File not found: $file")

        return SingleRootFileViewProvider(psiManager, vFile).allFiles.find {
            it is KtFile && it.virtualFile.canonicalPath == vFile.canonicalPath
        } as KtFile
    }

    protected open fun isIgnoredTest(projectInfo: ProjectInfo) = projectInfo.muted

    protected abstract fun buildKlib(
        configuration: CompilerConfiguration,
        moduleName: String,
        sourceDir: File,
        dependencies: Collection<File>,
        friends: Collection<File>,
        outputKlibFile: File
    )
}
