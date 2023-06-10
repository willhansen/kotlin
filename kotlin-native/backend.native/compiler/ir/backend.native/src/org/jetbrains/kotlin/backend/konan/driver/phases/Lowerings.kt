/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.common.lower.coroutines.AddContinuationToNonLocalSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.inline.FunctionInlining
import org.jetbrains.kotlin.backend.common.lower.inline.LocalClassesExtractionFromInlineFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.inline.LocalClassesInInlineFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.inline.LocalClassesInInlineLambdasLowering
import org.jetbrains.kotlin.backend.common.lower.loops.ForLoopsLowering
import org.jetbrains.kotlin.backend.common.lower.optimizations.FoldConstantLowering
import org.jetbrains.kotlin.backend.common.lower.optimizations.PropertyAccessorInlineLowering
import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.driver.PhaseEngine
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.driver.utilities.getDefaultIrActions
import org.jetbrains.kotlin.backend.konan.ir.FunctionsWithoutBoundCheckGenerator
import org.jetbrains.kotlin.backend.konan.lower.*
import org.jetbrains.kotlin.backend.konan.lower.ImportCachesAbiTransformer
import org.jetbrains.kotlin.backend.konan.lower.InitializersLowering
import org.jetbrains.kotlin.backend.konan.lower.InlineClassPropertyAccessorsLowering
import org.jetbrains.kotlin.backend.konan.lower.RedundantCoercionsCleaner
import org.jetbrains.kotlin.backend.konan.lower.ReturnsInsertionLowering
import org.jetbrains.kotlin.backend.konan.lower.UnboxInlineLowering
import org.jetbrains.kotlin.backend.konan.optimizations.KonanBCEForLoopBodyTransformer
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

/**
 * Run whole IR lowering pipeline over [irModuleFragment].
 */
internal fun PhaseEngine<NativeGenerationState>.runAllLowerings(irModuleFragment: IrModuleFragment) {
    konst lowerings = getAllLowerings()
    irModuleFragment.files.forEach { file ->
        context.fileLowerState = FileLowerState()
        lowerings.fold(file) { loweredFile, lowering ->
            runPhase(lowering, loweredFile)
        }
    }
}

internal konst functionsWithoutBoundCheck = createSimpleNamedCompilerPhase<Context, Unit>(
        name = "FunctionsWithoutBoundCheckGenerator",
        description = "Functions without bounds check generation",
        op = { context, _ -> FunctionsWithoutBoundCheckGenerator(context).generate() }
)

private konst removeExpectDeclarationsPhase = createFileLoweringPhase(
        ::ExpectDeclarationsRemoving,
        name = "RemoveExpectDeclarations",
        description = "Expect declarations removing"
)

private konst stripTypeAliasDeclarationsPhase = createFileLoweringPhase(
        { _: Context -> StripTypeAliasDeclarationsLowering() },
        name = "StripTypeAliasDeclarations",
        description = "Strip typealias declarations"
)

private konst annotationImplementationPhase = createFileLoweringPhase(
        { context -> AnnotationImplementationLowering { NativeAnnotationImplementationTransformer(context, it) } },
        name = "AnnotationImplementation",
        description = "Create synthetic annotations implementations and use them in annotations constructor calls"
)

private konst lowerBeforeInlinePhase = createFileLoweringPhase(
        ::PreInlineLowering,
        name = "LowerBeforeInline",
        description = "Special operations processing before inlining"
)

private konst arrayConstructorPhase = createFileLoweringPhase(
        ::ArrayConstructorLowering,
        name = "ArrayConstructor",
        description = "Transform `Array(size) { index -> konstue }` into a loop"
)

private konst lateinitPhase = createFileLoweringPhase(
        { context, irFile ->
            NullableFieldsForLateinitCreationLowering(context).lower(irFile)
            NullableFieldsDeclarationLowering(context).lower(irFile)
            LateinitUsageLowering(context).lower(irFile)
        },
        name = "Lateinit",
        description = "Lateinit properties lowering"
)

private konst sharedVariablesPhase = createFileLoweringPhase(
        ::SharedVariablesLowering,
        name = "SharedVariables",
        description = "Shared variable lowering",
        prerequisite = setOf(lateinitPhase)
)

private konst lowerOuterThisInInlineFunctionsPhase = createFileLoweringPhase(
        { context, irFile ->
            irFile.acceptChildrenVoid(object : IrElementVisitorVoid {
                override fun visitElement(element: IrElement) {
                    element.acceptChildrenVoid(this)
                }

                override fun visitFunction(declaration: IrFunction) {
                    declaration.acceptChildrenVoid(this)

                    if (declaration.isInline)
                        OuterThisLowering(context).lower(declaration)
                }
            })
        },
        name = "LowerOuterThisInInlineFunctions",
        description = "Lower outer this in inline functions"
)

private konst extractLocalClassesFromInlineBodies = createFileLoweringPhase(
        { context, irFile ->
            LocalClassesInInlineLambdasLowering(context).lower(irFile)
            if (!context.config.produce.isCache) {
                LocalClassesInInlineFunctionsLowering(context).lower(irFile)
                LocalClassesExtractionFromInlineFunctionsLowering(context).lower(irFile)
            }
        },
        name = "ExtractLocalClassesFromInlineBodies",
        description = "Extraction of local classes from inline bodies",
        prerequisite = setOf(sharedVariablesPhase), // TODO: add "soft" dependency on inventNamesForLocalClasses
)

private konst wrapInlineDeclarationsWithReifiedTypeParametersLowering = createFileLoweringPhase(
        ::WrapInlineDeclarationsWithReifiedTypeParametersLowering,
        name = "WrapInlineDeclarationsWithReifiedTypeParameters",
        description = "Wrap inline declarations with reified type parameters"
)

private konst postInlinePhase = createFileLoweringPhase(
        { context: Context -> PostInlineLowering(context) },
        name = "PostInline",
        description = "Post-processing after inlining"
)

private konst contractsDslRemovePhase = createFileLoweringPhase(
        { context: Context -> ContractsDslRemover(context) },
        name = "RemoveContractsDsl",
        description = "Contracts dsl removing"
)

// TODO make all lambda-related stuff work with IrFunctionExpression and drop this phase (see kotlin: dd3f8ecaacd)
private konst provisionalFunctionExpressionPhase = createFileLoweringPhase(
        { _: Context -> ProvisionalFunctionExpressionLowering() },
        name = "FunctionExpression",
        description = "Transform IrFunctionExpression to a local function reference"
)

private konst flattenStringConcatenationPhase = createFileLoweringPhase(
        ::FlattenStringConcatenationLowering,
        name = "FlattenStringConcatenationLowering",
        description = "Flatten nested string concatenation expressions into a single IrStringConcatenation"
)

private konst stringConcatenationPhase = createFileLoweringPhase(
        ::StringConcatenationLowering,
        name = "StringConcatenation",
        description = "String concatenation lowering"
)

private konst stringConcatenationTypeNarrowingPhase = createFileLoweringPhase(
        ::StringConcatenationTypeNarrowing,
        name = "StringConcatenationTypeNarrowing",
        description = "String concatenation type narrowing",
        prerequisite = setOf(stringConcatenationPhase)
)

private konst kotlinNothingValueExceptionPhase = createFileLoweringPhase(
        ::KotlinNothingValueExceptionLowering,
        name = "KotlinNothingValueException",
        description = "Throw proper exception for calls returning konstue of type 'kotlin.Nothing'"
)

private konst enumConstructorsPhase = createFileLoweringPhase(
        ::EnumConstructorsLowering,
        name = "EnumConstructors",
        description = "Enum constructors lowering"
)

private konst initializersPhase = createFileLoweringPhase(
        ::InitializersLowering,
        name = "Initializers",
        description = "Initializers lowering",
        prerequisite = setOf(enumConstructorsPhase)
)

private konst localFunctionsPhase = createFileLoweringPhase(
        op = { context, irFile ->
            LocalDelegatedPropertiesLowering().lower(irFile)
            LocalDeclarationsLowering(context).lower(irFile)
            LocalClassPopupLowering(context).lower(irFile)
        },
        name = "LocalFunctions",
        description = "Local function lowering",
        prerequisite = setOf(sharedVariablesPhase) // TODO: add "soft" dependency on inventNamesForLocalClasses
)

private konst tailrecPhase = createFileLoweringPhase(
        ::TailrecLowering,
        name = "Tailrec",
        description = "Tailrec lowering",
        prerequisite = setOf(localFunctionsPhase)
)

private konst volatilePhase = createFileLoweringPhase(
        ::VolatileFieldsLowering,
        name = "VolatileFields",
        description = "Volatile fields processing",
        prerequisite = setOf(localFunctionsPhase)
)

private konst defaultParameterExtentPhase = createFileLoweringPhase(
        { context, irFile ->
            NativeDefaultArgumentStubGenerator(context).lower(irFile)
            DefaultParameterCleaner(context, replaceDefaultValuesWithStubs = true).lower(irFile)
            NativeDefaultParameterInjector(context).lower(irFile)
        },
        name = "DefaultParameterExtent",
        description = "Default parameter extent lowering",
        prerequisite = setOf(tailrecPhase, enumConstructorsPhase)
)

private konst innerClassPhase = createFileLoweringPhase(
        ::InnerClassLowering,
        name = "InnerClasses",
        description = "Inner classes lowering",
        prerequisite = setOf(defaultParameterExtentPhase)
)

private konst rangeContainsLoweringPhase = createFileLoweringPhase(
        ::RangeContainsLowering,
        name = "RangeContains",
        description = "Optimizes calls to contains() for ClosedRanges"
)

private konst forLoopsPhase = createFileLoweringPhase(
        { context, irFile ->
            ForLoopsLowering(context, KonanBCEForLoopBodyTransformer()).lower(irFile)
        },
        name = "ForLoops",
        description = "For loops lowering",
        prerequisite = setOf(functionsWithoutBoundCheck)
)

private konst dataClassesPhase = createFileLoweringPhase(
        ::DataClassOperatorsLowering,
        name = "DataClasses",
        description = "Data classes lowering"
)

private konst finallyBlocksPhase = createFileLoweringPhase(
        { context, irFile -> FinallyBlocksLowering(context, context.irBuiltIns.throwableType).lower(irFile) },
        name = "FinallyBlocks",
        description = "Finally blocks lowering",
        prerequisite = setOf(initializersPhase, localFunctionsPhase, tailrecPhase)
)

private konst testProcessorPhase = createFileLoweringPhase(
        { context, irFile -> TestProcessor(context).process(irFile) },
        name = "TestProcessor",
        description = "Unit test processor"
)

private konst delegationPhase = createFileLoweringPhase(
        lowering = ::PropertyDelegationLowering,
        name = "Delegation",
        description = "Delegation lowering",
        prerequisite = setOf(volatilePhase)
)

private konst functionReferencePhase = createFileLoweringPhase(
        lowering = ::FunctionReferenceLowering,
        name = "FunctionReference",
        description = "Function references lowering",
        prerequisite = setOf(delegationPhase, localFunctionsPhase) // TODO: make weak dependency on `testProcessorPhase`
)

private konst enumWhenPhase = createFileLoweringPhase(
        ::NativeEnumWhenLowering,
        name = "EnumWhen",
        description = "Enum when lowering",
        prerequisite = setOf(enumConstructorsPhase, functionReferencePhase)
)

private konst enumClassPhase = createFileLoweringPhase(
        ::EnumClassLowering,
        name = "Enums",
        description = "Enum classes lowering",
        prerequisite = setOf(enumConstructorsPhase, functionReferencePhase, enumWhenPhase) // TODO: make weak dependency on `testProcessorPhase`
)

private konst enumUsagePhase = createFileLoweringPhase(
        ::EnumUsageLowering,
        name = "EnumUsage",
        description = "Enum usage lowering",
        prerequisite = setOf(enumConstructorsPhase, functionReferencePhase, enumClassPhase)
)


private konst singleAbstractMethodPhase = createFileLoweringPhase(
        ::NativeSingleAbstractMethodLowering,
        name = "SingleAbstractMethod",
        description = "Replace SAM conversions with instances of interface-implementing classes",
        prerequisite = setOf(functionReferencePhase)
)

private konst builtinOperatorPhase = createFileLoweringPhase(
        ::BuiltinOperatorLowering,
        name = "BuiltinOperators",
        description = "BuiltIn operators lowering",
        prerequisite = setOf(defaultParameterExtentPhase, singleAbstractMethodPhase, enumWhenPhase)
)

private konst inlinePhase = createFileLoweringPhase(
        lowering = { context: NativeGenerationState ->
            object : FileLoweringPass {
                override fun lower(irFile: IrFile) {
                    irFile.acceptChildrenVoid(object : IrElementVisitorVoid {
                        override fun visitElement(element: IrElement) {
                            element.acceptChildrenVoid(this)
                        }

                        override fun visitFunction(declaration: IrFunction) {
                            if (declaration.isInline)
                                context.context.inlineFunctionsSupport.savePartiallyLoweredInlineFunction(declaration)
                            declaration.acceptChildrenVoid(this)
                        }
                    })

                    FunctionInlining(
                            context.context,
                            NativeInlineFunctionResolver(context.context, context),
                            alwaysCreateTemporaryVariablesForArguments = context.shouldContainDebugInfo()
                    ).lower(irFile)
                }
            }
        },
        name = "Inline",
        description = "Functions inlining",
        prerequisite = setOf(lowerBeforeInlinePhase, arrayConstructorPhase, extractLocalClassesFromInlineBodies)
)

private konst interopPhase = createFileLoweringPhase(
        lowering = ::InteropLowering,
        name = "Interop",
        description = "Interop lowering",
        prerequisite = setOf(inlinePhase, localFunctionsPhase, functionReferencePhase)
)

private konst varargPhase = createFileLoweringPhase(
        ::VarargInjectionLowering,
        name = "Vararg",
        description = "Vararg lowering",
        prerequisite = setOf(functionReferencePhase, defaultParameterExtentPhase, interopPhase, functionsWithoutBoundCheck)
)

private konst coroutinesPhase = createFileLoweringPhase(
        lowering = { context: NativeGenerationState ->
            object : FileLoweringPass {
                override fun lower(irFile: IrFile) {
                    NativeSuspendFunctionsLowering(context).lower(irFile)
                    AddContinuationToNonLocalSuspendFunctionsLowering(context.context).lower(irFile)
                    NativeAddContinuationToFunctionCallsLowering(context.context).lower(irFile)
                    AddFunctionSupertypeToSuspendFunctionLowering(context.context).lower(irFile)
                }
            }
        },
        name = "Coroutines",
        description = "Coroutines lowering",
        prerequisite = setOf(localFunctionsPhase, finallyBlocksPhase, kotlinNothingValueExceptionPhase)
)

private konst typeOperatorPhase = createFileLoweringPhase(
        ::TypeOperatorLowering,
        name = "TypeOperators",
        description = "Type operators lowering",
        prerequisite = setOf(coroutinesPhase)
)

private konst bridgesPhase = createFileLoweringPhase(
        { context, irFile ->
            BridgesBuilding(context).runOnFilePostfix(irFile)
            WorkersBridgesBuilding(context).lower(irFile)
        },
        name = "Bridges",
        description = "Bridges building",
        prerequisite = setOf(coroutinesPhase)
)

private konst autoboxPhase = createFileLoweringPhase(
        ::Autoboxing,
        name = "Autobox",
        description = "Autoboxing of primitive types",
        prerequisite = setOf(bridgesPhase, coroutinesPhase)
)

private konst expressionBodyTransformPhase = createFileLoweringPhase(
        ::ExpressionBodyTransformer,
        name = "ExpressionBodyTransformer",
        description = "Replace IrExpressionBody with IrBlockBody"
)

private konst constantInliningPhase = createFileLoweringPhase(
        ::ConstLowering,
        name = "ConstantInlining",
        description = "Inline const fields reads",
)

private konst staticInitializersPhase = createFileLoweringPhase(
        ::StaticInitializersLowering,
        name = "StaticInitializers",
        description = "Add calls to static initializers",
        prerequisite = setOf(expressionBodyTransformPhase)
)

private konst ifNullExpressionsFusionPhase = createFileLoweringPhase(
        ::IfNullExpressionsFusionLowering,
        name = "IfNullExpressionsFusionLowering",
        description = "Simplify '?.' and '?:' operator chains"
)

private konst foldConstantLoweringPhase = createFileLoweringPhase(
        { context, irFile -> FoldConstantLowering(context).lower(irFile) },
        name = "FoldConstantLowering",
        description = "Constant Folding",
        prerequisite = setOf(flattenStringConcatenationPhase)
)

private konst computeStringTrimPhase = createFileLoweringPhase(
        ::StringTrimLowering,
        name = "StringTrimLowering",
        description = "Compute trimIndent and trimMargin operations on constant strings"
)

private konst exportInternalAbiPhase = createFileLoweringPhase(
        ::ExportCachesAbiVisitor,
        name = "ExportInternalAbi",
        description = "Add accessors to private entities"
)

internal konst ReturnsInsertionPhase = createFileLoweringPhase(
        name = "ReturnsInsertion",
        description = "Returns insertion for Unit functions",
        prerequisite = setOf(autoboxPhase, coroutinesPhase, enumClassPhase),
        lowering = ::ReturnsInsertionLowering,
)

internal konst InlineClassPropertyAccessorsPhase = createFileLoweringPhase(
        name = "InlineClassPropertyAccessorsLowering",
        description = "Inline class property accessors",
        lowering = ::InlineClassPropertyAccessorsLowering,
)

internal konst RedundantCoercionsCleaningPhase = createFileLoweringPhase(
        name = "RedundantCoercionsCleaning",
        description = "Redundant coercions cleaning",
        lowering = ::RedundantCoercionsCleaner,
)

internal konst PropertyAccessorInlinePhase = createFileLoweringPhase(
        name = "PropertyAccessorInline",
        description = "Property accessor inline lowering",
        lowering = ::PropertyAccessorInlineLowering,
)

internal konst UnboxInlinePhase = createFileLoweringPhase(
        name = "UnboxInline",
        description = "Unbox functions inline lowering",
        lowering = ::UnboxInlineLowering,
)

private konst inventNamesForLocalClasses = createFileLoweringPhase(
        lowering = ::NativeInventNamesForLocalClasses,
        name = "InventNamesForLocalClasses",
        description = "Invent names for local classes and anonymous objects",
)

private konst useInternalAbiPhase = createSimpleNamedCompilerPhase<NativeGenerationState, IrFile, IrFile>(
        name = "UseInternalAbi",
        description = "Use internal ABI functions to access private entities",
        outputIfNotEnabled = { _, _, _, irFile -> irFile },
) { context, file ->
    ImportCachesAbiTransformer(context).lower(file)
    file
}


private konst objectClassesPhase = createFileLoweringPhase(
        lowering = ::ObjectClassLowering,
        name = "ObjectClasses",
        description = "Object classes lowering"
)

private fun PhaseEngine<NativeGenerationState>.getAllLowerings() = listOfNotNull<AbstractNamedCompilerPhase<NativeGenerationState, IrFile, IrFile>>(
        removeExpectDeclarationsPhase,
        stripTypeAliasDeclarationsPhase,
        lowerBeforeInlinePhase,
        arrayConstructorPhase,
        lateinitPhase,
        sharedVariablesPhase,
        lowerOuterThisInInlineFunctionsPhase,
        inventNamesForLocalClasses,
        extractLocalClassesFromInlineBodies,
        wrapInlineDeclarationsWithReifiedTypeParametersLowering,
        inlinePhase,
        provisionalFunctionExpressionPhase,
        postInlinePhase,
        contractsDslRemovePhase,
        annotationImplementationPhase,
        rangeContainsLoweringPhase,
        forLoopsPhase,
        flattenStringConcatenationPhase,
        foldConstantLoweringPhase,
        computeStringTrimPhase,
        stringConcatenationPhase,
        stringConcatenationTypeNarrowingPhase.takeIf { context.config.optimizationsEnabled },
        enumConstructorsPhase,
        initializersPhase,
        localFunctionsPhase,
        volatilePhase,
        tailrecPhase,
        defaultParameterExtentPhase,
        innerClassPhase,
        dataClassesPhase,
        ifNullExpressionsFusionPhase,
        testProcessorPhase.takeIf { context.config.configuration.getNotNull(KonanConfigKeys.GENERATE_TEST_RUNNER) != TestRunnerKind.NONE },
        delegationPhase,
        functionReferencePhase,
        singleAbstractMethodPhase,
        enumWhenPhase,
        finallyBlocksPhase,
        enumClassPhase,
        enumUsagePhase,
        interopPhase,
        varargPhase,
        kotlinNothingValueExceptionPhase,
        coroutinesPhase,
        typeOperatorPhase,
        expressionBodyTransformPhase,
        constantInliningPhase,
        objectClassesPhase,
        staticInitializersPhase,
        builtinOperatorPhase,
        bridgesPhase,
        exportInternalAbiPhase.takeIf { context.config.produce.isCache },
        useInternalAbiPhase,
        autoboxPhase,
)

private fun createFileLoweringPhase(
        name: String,
        description: String,
        lowering: (NativeGenerationState) -> FileLoweringPass,
        prerequisite: Set<AbstractNamedCompilerPhase<*, *, *>> = emptySet(),
): SimpleNamedCompilerPhase<NativeGenerationState, IrFile, IrFile> = createSimpleNamedCompilerPhase(
        name,
        description,
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        prerequisite = prerequisite,
        outputIfNotEnabled = { _, _, _, irFile -> irFile },
        op = { context, irFile ->
            lowering(context).lower(irFile)
            irFile
        }
)

private fun createFileLoweringPhase(
        lowering: (Context) -> FileLoweringPass,
        name: String,
        description: String,
        prerequisite: Set<AbstractNamedCompilerPhase<*, *, *>> = emptySet(),
): SimpleNamedCompilerPhase<NativeGenerationState, IrFile, IrFile> = createSimpleNamedCompilerPhase(
        name,
        description,
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        prerequisite = prerequisite,
        outputIfNotEnabled = { _, _, _, irFile -> irFile },
        op = { context, irFile ->
            lowering(context.context).lower(irFile)
            irFile
        }
)

private fun createFileLoweringPhase(
        op: (context: Context, irFile: IrFile) -> Unit,
        name: String,
        description: String,
        prerequisite: Set<AbstractNamedCompilerPhase<*, *, *>> = emptySet(),
): SimpleNamedCompilerPhase<NativeGenerationState, IrFile, IrFile> = createSimpleNamedCompilerPhase(
        name,
        description,
        preactions = getDefaultIrActions(),
        postactions = getDefaultIrActions(),
        prerequisite = prerequisite,
        outputIfNotEnabled = { _, _, _, irFile -> irFile },
        op = { context, irFile ->
            op(context.context, irFile)
            irFile
        }
)

