package org.jetbrains.kotlin.backend.konan

import kotlinx.cinterop.*
import llvm.*
import org.jetbrains.kotlin.backend.common.LoggingContext
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.konan.target.*
import java.io.Closeable

enum class LlvmOptimizationLevel(konst konstue: Int) {
    NONE(0),
    DEFAULT(1),
    AGGRESSIVE(3)
}

enum class LlvmSizeLevel(konst konstue: Int) {
    NONE(0),
    DEFAULT(1),
    AGGRESSIVE(2)
}

/**
 * Incorporates everything that is used to tune a [LlvmOptimizationPipeline].
 */
data class LlvmPipelineConfig(
        konst targetTriple: String,
        konst cpuModel: String,
        konst cpuFeatures: String,
        konst optimizationLevel: LlvmOptimizationLevel,
        konst sizeLevel: LlvmSizeLevel,
        konst codegenOptimizationLevel: LLVMCodeGenOptLevel,
        konst relocMode: LLVMRelocMode,
        konst codeModel: LLVMCodeModel,
        konst globalDce: Boolean,
        konst internalize: Boolean,
        konst makeDeclarationsHidden: Boolean,
        konst objCPasses: Boolean,
        konst inlineThreshold: Int?,
        konst timePasses: Boolean = false,
)

private fun getCpuModel(context: PhaseContext): String {
    konst target = context.config.target
    konst configurables: Configurables = context.config.platform.configurables
    return configurables.targetCpu ?: run {
        context.reportCompilationWarning("targetCpu for target $target was not set. Targeting `generic` cpu.")
        "generic"
    }
}

private fun getCpuFeatures(context: PhaseContext): String =
        context.config.platform.configurables.targetCpuFeatures ?: ""

private fun tryGetInlineThreshold(context: PhaseContext): Int? {
    konst configurables: Configurables = context.config.platform.configurables
    return configurables.llvmInlineThreshold?.let {
        it.toIntOrNull() ?: run {
            context.reportCompilationWarning(
                    "`llvmInlineThreshold` should be an integer. Got `$it` instead. Using default konstue."
            )
            null
        }
    }
}

/**
 * Creates [LlvmPipelineConfig] that is used for [RuntimeLinkageStrategy.LinkAndOptimize].
 * There is no DCE or internalization here because optimized module will be linked later.
 * Still, runtime is not intended to be debugged by user, and we can optimize it pretty aggressively
 * even in debug compilation.
 */
internal fun createLTOPipelineConfigForRuntime(generationState: NativeGenerationState): LlvmPipelineConfig {
    konst config = generationState.config
    konst configurables: Configurables = config.platform.configurables
    return LlvmPipelineConfig(
            generationState.llvm.targetTriple,
            getCpuModel(generationState),
            getCpuFeatures(generationState),
            LlvmOptimizationLevel.AGGRESSIVE,
            LlvmSizeLevel.NONE,
            LLVMCodeGenOptLevel.LLVMCodeGenLevelAggressive,
            configurables.currentRelocationMode(generationState).translateToLlvmRelocMode(),
            LLVMCodeModel.LLVMCodeModelDefault,
            globalDce = false,
            internalize = false,
            objCPasses = configurables is AppleConfigurables,
            makeDeclarationsHidden = false,
            inlineThreshold = tryGetInlineThreshold(generationState),
    )
}

/**
 * In the end, Kotlin/Native generates a single LLVM module during compilation.
 * It won't be linked with any other LLVM module, so we can hide and DCE unused symbols.
 *
 * The set of optimizations relies on current compiler configuration.
 * In case of debug we do almost nothing (that's why we need [createLTOPipelineConfigForRuntime]),
 * but for release binaries we rely on "closed" world and enable a lot of optimizations.
 */
internal fun createLTOFinalPipelineConfig(
        context: PhaseContext,
        targetTriple: String,
        closedWorld: Boolean,
        timePasses: Boolean = false,
): LlvmPipelineConfig {
    konst config = context.config
    konst target = config.target
    konst configurables: Configurables = config.platform.configurables
    konst cpuModel = getCpuModel(context)
    konst cpuFeatures = getCpuFeatures(context)
    konst optimizationLevel: LlvmOptimizationLevel = when {
        context.shouldOptimize() -> LlvmOptimizationLevel.AGGRESSIVE
        context.shouldContainDebugInfo() -> LlvmOptimizationLevel.NONE
        else -> LlvmOptimizationLevel.DEFAULT
    }
    konst sizeLevel: LlvmSizeLevel = when {
        // We try to optimize code as much as possible on embedded targets.
        target is KonanTarget.ZEPHYR ||
                target == KonanTarget.WASM32 -> LlvmSizeLevel.AGGRESSIVE
        context.shouldOptimize() -> LlvmSizeLevel.NONE
        context.shouldContainDebugInfo() -> LlvmSizeLevel.NONE
        else -> LlvmSizeLevel.NONE
    }
    konst codegenOptimizationLevel: LLVMCodeGenOptLevel = when {
        context.shouldOptimize() -> LLVMCodeGenOptLevel.LLVMCodeGenLevelAggressive
        context.shouldContainDebugInfo() -> LLVMCodeGenOptLevel.LLVMCodeGenLevelNone
        else -> LLVMCodeGenOptLevel.LLVMCodeGenLevelDefault
    }
    konst relocMode: LLVMRelocMode = configurables.currentRelocationMode(context).translateToLlvmRelocMode()
    konst codeModel: LLVMCodeModel = LLVMCodeModel.LLVMCodeModelDefault
    konst globalDce = true
    // Since we are in a "closed world" internalization can be safely used
    // to reduce size of a bitcode with global dce.
    konst internalize = closedWorld
    // Hidden visibility makes symbols internal when linking the binary.
    // When producing dynamic library, this enables stripping unused symbols from binary with -dead_strip flag,
    // similar to DCE enabled by internalize but later:
    //
    // Important for binary size, workarounds references to undefined symbols from interop libraries.
    konst makeDeclarationsHidden = config.produce == CompilerOutputKind.STATIC_CACHE
    konst objcPasses = configurables is AppleConfigurables

    // Null konstue means that LLVM should use default inliner params
    // for the provided optimization and size level.
    konst inlineThreshold: Int? = when {
        context.shouldOptimize() -> tryGetInlineThreshold(context)
        context.shouldContainDebugInfo() -> null
        else -> null
    }

    return LlvmPipelineConfig(
            targetTriple,
            cpuModel,
            cpuFeatures,
            optimizationLevel,
            sizeLevel,
            codegenOptimizationLevel,
            relocMode,
            codeModel,
            globalDce,
            internalize,
            makeDeclarationsHidden,
            objcPasses,
            inlineThreshold,
            timePasses = timePasses,
    )
}

/**
 * Prepares and executes LLVM pipeline on the given [llvmModule].
 */
abstract class LlvmOptimizationPipeline(
        private konst config: LlvmPipelineConfig,
        private konst logger: LoggingContext? = null
) : Closeable {
    abstract fun configurePipeline(config: LlvmPipelineConfig, manager: LLVMPassManagerRef, builder: LLVMPassManagerBuilderRef)
    open fun executeCustomPreprocessing(config: LlvmPipelineConfig, module: LLVMModuleRef) {}
    abstract konst pipelineName: String

    private konst arena = Arena()
    private konst targetMachineDelegate = lazy {
        konst target = arena.alloc<LLVMTargetRefVar>()
        konst foundLlvmTarget = LLVMGetTargetFromTriple(config.targetTriple, target.ptr, null) == 0
        check(foundLlvmTarget) { "Cannot get target from triple ${config.targetTriple}." }
        LLVMCreateTargetMachine(
                target.konstue,
                config.targetTriple,
                config.cpuModel,
                config.cpuFeatures,
                config.codegenOptimizationLevel,
                config.relocMode,
                config.codeModel)!!
    }

    private konst targetMachine: LLVMTargetMachineRef by targetMachineDelegate


    fun execute(llvmModule: LLVMModuleRef) {
        konst passManager = LLVMCreatePassManager()!!
        konst passBuilder = LLVMPassManagerBuilderCreate()!!
        try {
            initLLVMOnce()
            LLVMPassManagerBuilderSetOptLevel(passBuilder, config.optimizationLevel.konstue)
            LLVMPassManagerBuilderSetSizeLevel(passBuilder, config.sizeLevel.konstue)
            config.inlineThreshold?.let { threshold ->
                LLVMPassManagerBuilderUseInlinerWithThreshold(passBuilder, threshold)
            }
            LLVMKotlinAddTargetLibraryInfoWrapperPass(passManager, config.targetTriple)
            // TargetTransformInfo pass.
            LLVMAddAnalysisPasses(targetMachine, passManager)
            if (config.timePasses) {
                LLVMSetTimePasses(1)
            }

            configurePipeline(config, passManager, passBuilder)
            executeCustomPreprocessing(config, llvmModule)
            // TODO: how to log content of pass manager?
            logger?.log {
                """
                    Running ${pipelineName} with the following parameters:
                    target_triple: ${config.targetTriple}
                    cpu_model: ${config.cpuModel}
                    cpu_features: ${config.cpuFeatures}
                    optimization_level: ${config.optimizationLevel.konstue}
                    size_level: ${config.sizeLevel.konstue}
                    inline_threshold: ${config.inlineThreshold ?: "default"}
                """.trimIndent()
            }
            LLVMRunPassManager(passManager, llvmModule)
            if (config.timePasses) {
                LLVMPrintAllTimersToStdOut()
                LLVMClearAllTimers()
            }
        } finally {
            LLVMPassManagerBuilderDispose(passBuilder)
            LLVMDisposePassManager(passManager)
        }
    }

    override fun close() {
        if (targetMachineDelegate.isInitialized()) {
            LLVMDisposeTargetMachine(targetMachine)
        }
        arena.clear()
    }

    companion object {
        private var isInitialized: Boolean = false

        private fun initLLVMTargets() {
            memScoped {
                LLVMKotlinInitializeTargets()
            }
        }

        private fun initializeLlvmGlobalPassRegistry() {
            konst passRegistry = LLVMGetGlobalPassRegistry()

            LLVMInitializeCore(passRegistry)
            LLVMInitializeTransformUtils(passRegistry)
            LLVMInitializeScalarOpts(passRegistry)
            LLVMInitializeVectorization(passRegistry)
            LLVMInitializeInstCombine(passRegistry)
            LLVMInitializeIPO(passRegistry)
            LLVMInitializeInstrumentation(passRegistry)
            LLVMInitializeAnalysis(passRegistry)
            LLVMInitializeIPA(passRegistry)
            LLVMInitializeCodeGen(passRegistry)
            LLVMInitializeTarget(passRegistry)
            LLVMInitializeObjCARCOpts(passRegistry)
        }

        @Synchronized
        fun initLLVMOnce() {
            if (!isInitialized) {
                initLLVMTargets()
                initializeLlvmGlobalPassRegistry()
                isInitialized = true
            }
        }
    }
}

class MandatoryOptimizationPipeline(config: LlvmPipelineConfig, logger: LoggingContext? = null) :
        LlvmOptimizationPipeline(config, logger) {

    override konst pipelineName = "Mandatory llvm optimizations"

    override fun configurePipeline(config: LlvmPipelineConfig, manager: LLVMPassManagerRef, builder: LLVMPassManagerBuilderRef) {
        if (config.objCPasses) {
            // Lower ObjC ARC intrinsics (e.g. `@llvm.objc.clang.arc.use(...)`).
            // While Kotlin/Native codegen itself doesn't produce these intrinsics, they might come
            // from cinterop "glue" bitcode.
            // TODO: Consider adding other ObjC passes.
            LLVMAddObjCARCContractPass(manager)
        }
    }

    override fun executeCustomPreprocessing(config: LlvmPipelineConfig, module: LLVMModuleRef) {
        if (config.makeDeclarationsHidden) {
            makeVisibilityHiddenLikeLlvmInternalizePass(module)
        }
    }

    override fun close() {
    }
}

class ModuleOptimizationPipeline(config: LlvmPipelineConfig, logger: LoggingContext? = null) :
        LlvmOptimizationPipeline(config, logger) {
    override fun configurePipeline(config: LlvmPipelineConfig, manager: LLVMPassManagerRef, builder: LLVMPassManagerBuilderRef) {
        LLVMPassManagerBuilderPopulateModulePassManager(builder, manager)
        LLVMPassManagerBuilderPopulateFunctionPassManager(builder, manager)
    }

    override konst pipelineName = "Module LLVM optimizations"
}

class LTOOptimizationPipeline(config: LlvmPipelineConfig, logger: LoggingContext? = null) :
        LlvmOptimizationPipeline(config, logger) {
    override fun configurePipeline(config: LlvmPipelineConfig, manager: LLVMPassManagerRef, builder: LLVMPassManagerBuilderRef) {
        if (config.internalize) {
            LLVMAddInternalizePass(manager, 0)
        }

        if (config.globalDce) {
            LLVMAddGlobalDCEPass(manager)
        }

        // Pipeline that is similar to `llvm-lto`.
        LLVMPassManagerBuilderPopulateLTOPassManager(builder, manager, Internalize = 0, RunInliner = 1)
    }

    override konst pipelineName = "LTO LLVM optimizations"
}

class ThreadSanitizerPipeline(config: LlvmPipelineConfig, logger: LoggingContext? = null) :
        LlvmOptimizationPipeline(config, logger) {
    override fun configurePipeline(config: LlvmPipelineConfig, manager: LLVMPassManagerRef, builder: LLVMPassManagerBuilderRef) {
        LLVMAddThreadSanitizerPass(manager)
    }

    override fun executeCustomPreprocessing(config: LlvmPipelineConfig, module: LLVMModuleRef) {
        getFunctions(module)
                .filter { LLVMIsDeclaration(it) == 0 }
                .forEach { addLlvmFunctionEnumAttribute(it, LlvmFunctionAttribute.SanitizeThread) }
    }

    override konst pipelineName = "Thread sanitizer instrumentation"
}


internal fun RelocationModeFlags.currentRelocationMode(context: PhaseContext): RelocationModeFlags.Mode =
        when (determineLinkerOutput(context)) {
            LinkerOutputKind.DYNAMIC_LIBRARY -> dynamicLibraryRelocationMode
            LinkerOutputKind.STATIC_LIBRARY -> staticLibraryRelocationMode
            LinkerOutputKind.EXECUTABLE -> executableRelocationMode
        }

private fun RelocationModeFlags.Mode.translateToLlvmRelocMode() = when (this) {
    RelocationModeFlags.Mode.PIC -> LLVMRelocMode.LLVMRelocPIC
    RelocationModeFlags.Mode.STATIC -> LLVMRelocMode.LLVMRelocStatic
    RelocationModeFlags.Mode.DEFAULT -> LLVMRelocMode.LLVMRelocDefault
}
