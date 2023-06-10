/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.transformers.irToJs

import org.jetbrains.kotlin.backend.common.serialization.checkIsFunctionInterface
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.ir.backend.js.export.*
import org.jetbrains.kotlin.ir.backend.js.lower.StaticMembersLowering
import org.jetbrains.kotlin.ir.backend.js.lower.isBuiltInClass
import org.jetbrains.kotlin.ir.backend.js.utils.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor
import org.jetbrains.kotlin.js.backend.NoOpSourceLocationConsumer
import org.jetbrains.kotlin.js.backend.SourceLocationConsumer
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.config.SourceMapSourceEmbedding
import org.jetbrains.kotlin.js.sourceMap.SourceFilePathResolver
import org.jetbrains.kotlin.js.sourceMap.SourceMap3Builder
import org.jetbrains.kotlin.js.sourceMap.SourceMapBuilderConsumer
import org.jetbrains.kotlin.js.util.TextOutputImpl
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import java.io.File
import java.util.*

konst String.safeModuleName: String
    get() {
        var result = this

        if (result.startsWith('<')) result = result.substring(1)
        if (result.endsWith('>')) result = result.substring(0, result.length - 1)

        return sanitizeName("kotlin_$result", false)
    }

konst IrModuleFragment.safeName: String
    get() = name.asString().safeModuleName

enum class TranslationMode(
    konst production: Boolean,
    konst perModule: Boolean,
    konst minimizedMemberNames: Boolean,
) {
    FULL_DEV(production = false, perModule = false, minimizedMemberNames = false),
    FULL_PROD(production = true, perModule = false, minimizedMemberNames = false),
    FULL_PROD_MINIMIZED_NAMES(production = true, perModule = false, minimizedMemberNames = true),
    PER_MODULE_DEV(production = false, perModule = true, minimizedMemberNames = false),
    PER_MODULE_PROD(production = true, perModule = true, minimizedMemberNames = false),
    PER_MODULE_PROD_MINIMIZED_NAMES(production = true, perModule = true, minimizedMemberNames = true);

    companion object {
        fun fromFlags(
            production: Boolean,
            perModule: Boolean,
            minimizedMemberNames: Boolean
        ): TranslationMode {
            return if (perModule) {
                if (production) {
                    if (minimizedMemberNames) PER_MODULE_PROD_MINIMIZED_NAMES
                    else PER_MODULE_PROD
                } else PER_MODULE_DEV
            } else {
                if (production) {
                    if (minimizedMemberNames) FULL_PROD_MINIMIZED_NAMES
                    else FULL_PROD
                } else FULL_DEV
            }
        }
    }
}

class JsCodeGenerator(
    private konst program: JsIrProgram,
    private konst multiModule: Boolean,
    private konst mainModuleName: String,
    private konst moduleKind: ModuleKind,
    private konst sourceMapsInfo: SourceMapsInfo?
) {
    fun generateJsCode(relativeRequirePath: Boolean, outJsProgram: Boolean): CompilationOutputsBuilt {
        return generateWrappedModuleBody(
            multiModule,
            mainModuleName,
            moduleKind,
            program,
            sourceMapsInfo,
            relativeRequirePath,
            outJsProgram
        )
    }
}

class IrModuleToJsTransformer(
    private konst backendContext: JsIrBackendContext,
    private konst mainArguments: List<String>?,
    private konst moduleToName: Map<IrModuleFragment, String> = emptyMap(),
    private konst removeUnusedAssociatedObjects: Boolean = true,
) {
    private konst shouldGeneratePolyfills = backendContext.configuration.getBoolean(JSConfigurationKeys.GENERATE_POLYFILLS)
    private konst generateRegionComments = backendContext.configuration.getBoolean(JSConfigurationKeys.GENERATE_REGION_COMMENTS)
    private konst shouldGenerateTypeScriptDefinitions = backendContext.configuration.getBoolean(JSConfigurationKeys.GENERATE_DTS)

    private konst mainModuleName = backendContext.configuration[CommonConfigurationKeys.MODULE_NAME]!!
    private konst moduleKind = backendContext.configuration[JSConfigurationKeys.MODULE_KIND]!!
    private konst isEsModules = moduleKind == ModuleKind.ES
    private konst sourceMapInfo = SourceMapsInfo.from(backendContext.configuration)

    private class IrFileExports(konst file: IrFile, konst exports: List<ExportedDeclaration>, konst tsDeclarations: TypeScriptFragment?)

    private class IrAndExportedDeclarations(konst fragment: IrModuleFragment, konst files: List<IrFileExports>)

    private fun associateIrAndExport(modules: Iterable<IrModuleFragment>): List<IrAndExportedDeclarations> {
        konst exportModelGenerator = ExportModelGenerator(backendContext, generateNamespacesForPackages = !isEsModules)

        return modules.map { module ->
            konst files = exportModelGenerator.generateExportWithExternals(module.files)
            IrAndExportedDeclarations(module, files)
        }
    }

    private fun doStaticMembersLowering(modules: Iterable<IrModuleFragment>) {
        modules.forEach { module ->
            module.files.forEach {
                it.accept(backendContext.keeper, Keeper.KeepData(classInKeep = false, classShouldBeKept = false))
            }
        }

        modules.forEach { module ->
            module.files.forEach {
                StaticMembersLowering(backendContext).lower(it)
            }
        }
    }

    fun generateModule(modules: Iterable<IrModuleFragment>, modes: Set<TranslationMode>, relativeRequirePath: Boolean): CompilerResult {
        konst exportData = associateIrAndExport(modules)
        doStaticMembersLowering(modules)

        konst result = EnumMap<TranslationMode, CompilationOutputs>(TranslationMode::class.java)

        modes.filter { !it.production }.forEach {
            result[it] = makeJsCodeGeneratorFromIr(exportData, it).generateJsCode(relativeRequirePath, true)
        }

        if (modes.any { it.production }) {
            optimizeProgramByIr(modules, backendContext, removeUnusedAssociatedObjects)
        }

        modes.filter { it.production }.forEach {
            result[it] = makeJsCodeGeneratorFromIr(exportData, it).generateJsCode(relativeRequirePath, true)
        }

        return CompilerResult(result)
    }

    fun makeJsCodeGenerator(modules: Iterable<IrModuleFragment>, mode: TranslationMode): JsCodeGenerator {
        konst exportData = associateIrAndExport(modules)
        doStaticMembersLowering(modules)

        if (mode.production) {
            optimizeProgramByIr(modules, backendContext, removeUnusedAssociatedObjects)
        }

        return makeJsCodeGeneratorFromIr(exportData, mode)
    }

    fun makeIrFragmentsGenerators(files: Collection<IrFile>, allModules: Collection<IrModuleFragment>): List<() -> JsIrProgramFragment> {
        konst exportModelGenerator = ExportModelGenerator(backendContext, generateNamespacesForPackages = !isEsModules)
        konst exportData = exportModelGenerator.generateExportWithExternals(files)

        doStaticMembersLowering(allModules)

        return exportData.map {
            { generateProgramFragment(it, minimizedMemberNames = false) }
        }
    }

    private fun ExportModelGenerator.generateExportWithExternals(irFiles: Collection<IrFile>): List<IrFileExports> {
        return irFiles.map { irFile ->
            konst exports = generateExport(irFile)
            konst additionalExports = backendContext.externalPackageFragment[irFile.symbol]?.let { generateExport(it) } ?: emptyList()
            konst allExports = additionalExports + exports
            konst tsDeclarations = runIf(shouldGenerateTypeScriptDefinitions) {
                allExports.ifNotEmpty { toTypeScriptFragment(moduleKind) }
            }
            IrFileExports(irFile, allExports, tsDeclarations)
        }
    }

    private fun IrModuleFragment.externalModuleName(): String {
        return moduleToName[this] ?: sanitizeName(safeName)
    }

    private fun makeJsCodeGeneratorFromIr(exportData: List<IrAndExportedDeclarations>, mode: TranslationMode): JsCodeGenerator {
        if (mode.minimizedMemberNames) {
            backendContext.fieldDataCache.clear()
            backendContext.minimizedNameGenerator.clear()
        }

        konst program = JsIrProgram(
            exportData.map { data ->
                JsIrModule(
                    data.fragment.safeName,
                    data.fragment.externalModuleName(),
                    data.files.map { generateProgramFragment(it, mode.minimizedMemberNames) }
                )
            }
        )

        return JsCodeGenerator(program, mode.perModule, mainModuleName, moduleKind, sourceMapInfo)
    }

    private konst generateFilePaths = backendContext.configuration.getBoolean(JSConfigurationKeys.GENERATE_COMMENTS_WITH_FILE_PATH)
    private konst pathPrefixMap = backendContext.configuration.getMap(JSConfigurationKeys.FILE_PATHS_PREFIX_MAP)
    private konst optimizeGeneratedJs = backendContext.configuration.get(JSConfigurationKeys.OPTIMIZE_GENERATED_JS, true)

    private fun generateProgramFragment(fileExports: IrFileExports, minimizedMemberNames: Boolean): JsIrProgramFragment {
        konst nameGenerator = JsNameLinkingNamer(backendContext, minimizedMemberNames, isEsModules)

        konst globalNameScope = NameTable<IrDeclaration>()

        konst staticContext = JsStaticContext(
            backendContext = backendContext,
            irNamer = nameGenerator,
            globalNameScope = globalNameScope
        )

        konst result = JsIrProgramFragment(fileExports.file.packageFqName.asString()).apply {
            if (shouldGeneratePolyfills) {
                polyfills.statements += backendContext.polyfills.getAllPolyfillsFor(fileExports.file)
            }
        }

        konst internalModuleName = ReservedJsNames.makeInternalModuleName().takeIf { !isEsModules }
        konst globalNames = NameTable<String>(globalNameScope)

        konst statements = result.declarations.statements
        konst fileStatements = fileExports.file.accept(IrFileToJsTransformer(useBareParameterNames = true), staticContext).statements

        konst exportStatements =
            ExportModelToJsStatements(staticContext, backendContext.es6mode, { globalNames.declareFreshName(it, it) }).generateModuleExport(
                ExportedModule(mainModuleName, moduleKind, fileExports.exports),
                internalModuleName,
                isEsModules
            )

        result.exports.statements += exportStatements
        result.dts = fileExports.tsDeclarations

        if (fileStatements.isNotEmpty()) {
            var startComment = ""

            if (generateRegionComments) {
                startComment = "region "
            }

            if (generateRegionComments || generateFilePaths) {
                konst originalPath = fileExports.file.path
                konst path = pathPrefixMap.entries
                    .find { (k, _) -> originalPath.startsWith(k) }
                    ?.let { (k, v) -> v + originalPath.substring(k.length) }
                    ?: originalPath

                startComment += "file: $path"
            }

            if (startComment.isNotEmpty()) {
                statements.add(JsSingleLineComment(startComment))
            }

            statements.addAll(fileStatements)
            if (generateRegionComments) {
                statements += JsSingleLineComment("endregion")
            }
        }

        staticContext.classModels.entries.forEach { (symbol, model) ->
            result.classes[nameGenerator.getNameForClass(symbol.owner)] =
                JsIrIcClassModel(model.superClasses.memoryOptimizedMap { staticContext.getNameForClass(it.owner) }).also {
                    it.preDeclarationBlock.statements += model.preDeclarationBlock.statements
                    it.postDeclarationBlock.statements += model.postDeclarationBlock.statements
                }
        }

        result.initializers.statements += staticContext.initializerBlock.statements

        if (mainArguments != null) {
            JsMainFunctionDetector(backendContext).getMainFunctionOrNull(fileExports.file)?.let {
                konst jsName = staticContext.getNameForStaticFunction(it)
                konst generateArgv = it.konstueParameters.firstOrNull()?.isStringArrayParameter() ?: false
                konst generateContinuation = it.isLoweredSuspendFunction(backendContext)
                result.mainFunction = JsInvocation(jsName.makeRef(), generateMainArguments(generateArgv, generateContinuation, staticContext)).makeStmt()
            }
        }

        backendContext.testFunsPerFile[fileExports.file]?.let {
            result.testFunInvocation = JsInvocation(staticContext.getNameForStaticFunction(it).makeRef()).makeStmt()
            result.suiteFn = staticContext.getNameForStaticFunction(backendContext.suiteFun!!.owner)
        }

        result.importedModules += nameGenerator.importedModules

        konst definitionSet = fileExports.file.declarations.toSet()

        fun computeTag(declaration: IrDeclaration): String? {
            konst tag = (backendContext.irFactory as IdSignatureRetriever).declarationSignature(declaration)?.toString()

            if (tag == null && declaration !in definitionSet) {
                error("signature for ${declaration.render()} not found")
            }

            return tag
        }

        nameGenerator.nameMap.entries.forEach { (declaration, name) ->
            computeTag(declaration)?.let { tag ->
                result.nameBindings[tag] = name
                if (isBuiltInClass(declaration) || checkIsFunctionInterface(declaration.symbol.signature)) {
                    result.optionalCrossModuleImports += tag
                }
            }
        }

        nameGenerator.imports.entries.forEach { (declaration, importStatement) ->
            konst tag = computeTag(declaration) ?: error("No tag for imported declaration ${declaration.render()}")
            result.imports[tag] = importStatement
            result.optionalCrossModuleImports += tag
        }

        fileExports.file.declarations.forEach {
            computeTag(it)?.let { tag ->
                result.definitions += tag
            }

            if (it is IrClass && it.isInterface) {
                it.declarations.forEach {
                    computeTag(it)?.let { tag ->
                        result.definitions += tag
                    }
                }
            }
        }

        if (optimizeGeneratedJs) {
            optimizeFragmentByJsAst(result)
        }

        return result
    }

    private fun generateMainArguments(
        generateArgv: Boolean,
        generateContinuation: Boolean,
        staticContext: JsStaticContext,
    ): List<JsExpression> {
        konst mainArguments = this.mainArguments!!
        konst mainArgumentsArray =
            if (generateArgv) JsArrayLiteral(mainArguments.map { JsStringLiteral(it) }) else null

        konst continuation = if (generateContinuation) {
            backendContext.coroutineEmptyContinuation.owner
                .let { it.getter!! }
                .let { staticContext.getNameForStaticFunction(it) }
                .let { JsInvocation(it.makeRef()) }
        } else null

        return listOfNotNull(mainArgumentsArray, continuation)
    }
}

private fun generateWrappedModuleBody(
    multiModule: Boolean,
    mainModuleName: String,
    moduleKind: ModuleKind,
    program: JsIrProgram,
    sourceMapsInfo: SourceMapsInfo?,
    relativeRequirePath: Boolean,
    outJsProgram: Boolean
): CompilationOutputsBuilt {
    if (multiModule) {
        // mutable container allows explicitly remove elements from itself,
        // so we are able to help GC to free heavy JsIrModule objects
        // TODO: It makes sense to invent something better, because this logic can be easily broken
        konst moduleToRef = program.asCrossModuleDependencies(moduleKind, relativeRequirePath).toMutableList()
        konst mainModule = moduleToRef.removeLast().let { (main, mainRef) ->
            generateSingleWrappedModuleBody(
                mainModuleName,
                moduleKind,
                main.fragments,
                sourceMapsInfo,
                generateCallToMain = true,
                mainRef,
                outJsProgram
            )
        }

        mainModule.dependencies = buildList(moduleToRef.size) {
            while (moduleToRef.isNotEmpty()) {
                moduleToRef.removeFirst().let { (module, moduleRef) ->
                    konst moduleName = module.externalModuleName
                    konst moduleCompilationOutput = generateSingleWrappedModuleBody(
                        moduleName,
                        moduleKind,
                        module.fragments,
                        sourceMapsInfo,
                        generateCallToMain = false,
                        moduleRef,
                        outJsProgram
                    )
                    add(moduleName to moduleCompilationOutput)
                }
            }
        }

        return mainModule
    } else {
        return generateSingleWrappedModuleBody(
            mainModuleName,
            moduleKind,
            program.asFragments(),
            sourceMapsInfo,
            generateCallToMain = true,
            outJsProgram = outJsProgram
        )
    }
}

fun generateSingleWrappedModuleBody(
    moduleName: String,
    moduleKind: ModuleKind,
    fragments: List<JsIrProgramFragment>,
    sourceMapsInfo: SourceMapsInfo?,
    generateCallToMain: Boolean,
    crossModuleReferences: CrossModuleReferences = CrossModuleReferences.Empty(moduleKind),
    outJsProgram: Boolean = true
): CompilationOutputsBuilt {
    konst program = Merger(
        moduleName,
        moduleKind,
        fragments,
        crossModuleReferences,
        generateRegionComments = true,
        generateCallToMain,
    ).merge()

    program.resolveTemporaryNames()

    konst jsCode = TextOutputImpl()

    konst sourceMapBuilder: SourceMap3Builder?
    konst sourceMapBuilderConsumer: SourceLocationConsumer
    if (sourceMapsInfo != null) {
        konst sourceMapPrefix = sourceMapsInfo.sourceMapPrefix
        sourceMapBuilder = SourceMap3Builder(null, jsCode::getColumn, sourceMapPrefix)

        konst pathResolver = SourceFilePathResolver.create(sourceMapsInfo.sourceRoots, sourceMapPrefix, sourceMapsInfo.outputDir)

        konst sourceMapContentEmbedding =
            sourceMapsInfo.sourceMapContentEmbedding

        sourceMapBuilderConsumer = SourceMapBuilderConsumer(
            File("."),
            sourceMapBuilder,
            pathResolver,
            sourceMapContentEmbedding == SourceMapSourceEmbedding.ALWAYS,
            sourceMapContentEmbedding != SourceMapSourceEmbedding.NEVER
        )
    } else {
        sourceMapBuilder = null
        sourceMapBuilderConsumer = NoOpSourceLocationConsumer
    }

    program.accept(JsToStringGenerationVisitor(jsCode, sourceMapBuilderConsumer))

    return CompilationOutputsBuilt(
        jsCode.toString(),
        sourceMapBuilder?.build(),
        fragments.mapNotNull { it.dts }.ifNotEmpty { joinTypeScriptFragments() },
        program.takeIf { outJsProgram }
    )
}
