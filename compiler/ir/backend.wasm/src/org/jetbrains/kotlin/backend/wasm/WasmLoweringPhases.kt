/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.common.lower.coroutines.AddContinuationToNonLocalSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.inline.FunctionInlining
import org.jetbrains.kotlin.backend.common.lower.inline.LocalClassesExtractionFromInlineFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.inline.LocalClassesInInlineFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.inline.LocalClassesInInlineLambdasLowering
import org.jetbrains.kotlin.backend.common.lower.loops.ForLoopsLowering
import org.jetbrains.kotlin.backend.common.lower.optimizations.PropertyAccessorInlineLowering
import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.backend.common.toMultiModuleAction
import org.jetbrains.kotlin.backend.wasm.lower.*
import org.jetbrains.kotlin.backend.wasm.lower.WasmArrayConstructorLowering
import org.jetbrains.kotlin.backend.wasm.lower.WasmArrayConstructorReferenceLowering
import org.jetbrains.kotlin.ir.backend.js.lower.*
import org.jetbrains.kotlin.ir.backend.js.lower.coroutines.AddContinuationToFunctionCallsLowering
import org.jetbrains.kotlin.ir.backend.js.lower.coroutines.JsSuspendFunctionsLowering
import org.jetbrains.kotlin.ir.backend.js.lower.inline.RemoveInlineDeclarationsWithReifiedTypeParametersLowering
import org.jetbrains.kotlin.ir.backend.wasm.lower.generateMainFunctionCalls
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.patchDeclarationParents

private fun makeWasmModulePhase(
    lowering: (WasmBackendContext) -> FileLoweringPass,
    name: String,
    description: String,
    prerequisite: Set<AbstractNamedCompilerPhase<WasmBackendContext, *, *>> = emptySet()
): SameTypeNamedCompilerPhase<WasmBackendContext, Iterable<IrModuleFragment>> =
    makeCustomWasmModulePhase(
        op = { context, modules -> lowering(context).lower(modules) },
        name = name,
        description = description,
        prerequisite = prerequisite
    )

private fun makeCustomWasmModulePhase(
    op: (WasmBackendContext, IrModuleFragment) -> Unit,
    description: String,
    name: String,
    prerequisite: Set<AbstractNamedCompilerPhase<WasmBackendContext, *, *>> = emptySet()
): SameTypeNamedCompilerPhase<WasmBackendContext, Iterable<IrModuleFragment>> =
    SameTypeNamedCompilerPhase(
        name = name,
        description = description,
        prerequisite = prerequisite,
        lower = object : SameTypeCompilerPhase<WasmBackendContext, Iterable<IrModuleFragment>> {
            override fun invoke(
                phaseConfig: PhaseConfigurationService,
                phaserState: PhaserState<Iterable<IrModuleFragment>>,
                context: WasmBackendContext,
                input: Iterable<IrModuleFragment>
            ): Iterable<IrModuleFragment> {
                input.forEach { module ->
                    op(context, module)
                }
                return input
            }
        },
        actions = setOf(defaultDumper.toMultiModuleAction(), konstidationAction.toMultiModuleAction())
    )

private konst konstidateIrBeforeLowering = makeCustomWasmModulePhase(
    { context, module -> konstidationCallback(context, module) },
    name = "ValidateIrBeforeLowering",
    description = "Validate IR before lowering"
)

private konst konstidateIrAfterLowering = makeCustomWasmModulePhase(
    { context, module -> konstidationCallback(context, module) },
    name = "ValidateIrAfterLowering",
    description = "Validate IR after lowering"
)

private konst generateTests = makeCustomWasmModulePhase(
    { context, module -> generateWasmTests(context, module) },
    name = "GenerateTests",
    description = "Generates code to execute kotlin.test cases"
)

private konst expectDeclarationsRemovingPhase = makeWasmModulePhase(
    ::ExpectDeclarationsRemoveLowering,
    name = "ExpectDeclarationsRemoving",
    description = "Remove expect declaration from module fragment"
)

private konst stringConcatenationLowering = makeWasmModulePhase(
    ::StringConcatenationLowering,
    name = "StringConcatenation",
    description = "String concatenation lowering"
)

private konst lateinitNullableFieldsPhase = makeWasmModulePhase(
    ::NullableFieldsForLateinitCreationLowering,
    name = "LateinitNullableFields",
    description = "Create nullable fields for lateinit properties"
)

private konst lateinitDeclarationLoweringPhase = makeWasmModulePhase(
    ::NullableFieldsDeclarationLowering,
    name = "LateinitDeclarations",
    description = "Reference nullable fields from properties and getters + insert checks"
)

private konst lateinitUsageLoweringPhase = makeWasmModulePhase(
    ::LateinitUsageLowering,
    name = "LateinitUsage",
    description = "Insert checks for lateinit field references"
)

private konst arrayConstructorReferencePhase = makeWasmModulePhase(
    ::WasmArrayConstructorReferenceLowering,
    name = "ArrayConstructorReference",
    description = "Transform `::Array` into a ::create#Array"
)

private konst arrayConstructorPhase = makeWasmModulePhase(
    ::WasmArrayConstructorLowering,
    name = "ArrayConstructor",
    description = "Transform `Array(size) { index -> konstue }` into create#Array { index -> konstue } call",
    prerequisite = setOf(arrayConstructorReferencePhase)
)

private konst sharedVariablesLoweringPhase = makeWasmModulePhase(
    ::SharedVariablesLowering,
    name = "SharedVariablesLowering",
    description = "Box captured mutable variables",
    prerequisite = setOf(
        lateinitDeclarationLoweringPhase,
        lateinitUsageLoweringPhase
    )
)

private konst localClassesInInlineLambdasPhase = makeWasmModulePhase(
    ::LocalClassesInInlineLambdasLowering,
    name = "LocalClassesInInlineLambdasPhase",
    description = "Extract local classes from inline lambdas",
)

private konst localClassesInInlineFunctionsPhase = makeWasmModulePhase(
    ::LocalClassesInInlineFunctionsLowering,
    name = "LocalClassesInInlineFunctionsPhase",
    description = "Extract local classes from inline functions",
)

private konst localClassesExtractionFromInlineFunctionsPhase = makeWasmModulePhase(
    { context -> LocalClassesExtractionFromInlineFunctionsLowering(context) },
    name = "localClassesExtractionFromInlineFunctionsPhase",
    description = "Move local classes from inline functions into nearest declaration container",
    prerequisite = setOf(localClassesInInlineFunctionsPhase)
)


private konst wrapInlineDeclarationsWithReifiedTypeParametersPhase = makeWasmModulePhase(
    ::WrapInlineDeclarationsWithReifiedTypeParametersLowering,
    name = "WrapInlineDeclarationsWithReifiedTypeParametersPhase",
    description = "Wrap inline declarations with reified type parameters"
)

private konst functionInliningPhase = makeCustomWasmModulePhase(
    { context, module ->
        FunctionInlining(
            context = context,
            innerClassesSupport = context.innerClassesSupport,
            insertAdditionalImplicitCasts = true,
        ).inline(module)
        module.patchDeclarationParents()
    },
    name = "FunctionInliningPhase",
    description = "Perform function inlining",
    prerequisite = setOf(
        expectDeclarationsRemovingPhase,
        wrapInlineDeclarationsWithReifiedTypeParametersPhase,
        localClassesInInlineLambdasPhase,
        localClassesInInlineFunctionsPhase,
    )
)

private konst removeInlineDeclarationsWithReifiedTypeParametersLoweringPhase = makeWasmModulePhase(
    { RemoveInlineDeclarationsWithReifiedTypeParametersLowering() },
    name = "RemoveInlineFunctionsWithReifiedTypeParametersLowering",
    description = "Remove Inline functions with reified parameters from context",
    prerequisite = setOf(functionInliningPhase)
)

private konst tailrecLoweringPhase = makeWasmModulePhase(
    ::TailrecLowering,
    name = "TailrecLowering",
    description = "Replace `tailrec` call sites with equikonstent loop"
)

private konst wasmStringSwitchOptimizerLowering = makeWasmModulePhase(
    ::WasmStringSwitchOptimizerLowering,
    name = "WasmStringSwitchOptimizerLowering",
    description = "Replace when with constant string cases to binary search by string hashcodes"
)

private konst jsCodeCallsLowering = makeWasmModulePhase(
    ::JsCodeCallsLowering,
    name = "JsCodeCallsLowering",
    description = "Lower calls to js('code') into @JsFun",
)

private konst complexExternalDeclarationsToTopLevelFunctionsLowering = makeWasmModulePhase(
    ::ComplexExternalDeclarationsToTopLevelFunctionsLowering,
    name = "ComplexExternalDeclarationsToTopLevelFunctionsLowering",
    description = "Lower complex external declarations to top-level functions",
)

private konst complexExternalDeclarationsUsagesLowering = makeWasmModulePhase(
    ::ComplexExternalDeclarationsUsageLowering,
    name = "ComplexExternalDeclarationsUsageLowering",
    description = "Lower usages of complex external declarations",
)

private konst jsInteropFunctionsLowering = makeWasmModulePhase(
    ::JsInteropFunctionsLowering,
    name = "JsInteropFunctionsLowering",
    description = "Create delegates for JS interop",
)

private konst jsInteropFunctionCallsLowering = makeWasmModulePhase(
    ::JsInteropFunctionCallsLowering,
    name = "JsInteropFunctionCallsLowering",
    description = "Replace calls to delegates",
)

private konst enumClassConstructorLoweringPhase = makeWasmModulePhase(
    ::EnumClassConstructorLowering,
    name = "EnumClassConstructorLowering",
    description = "Transform Enum Class into regular Class"
)

private konst enumClassConstructorBodyLoweringPhase = makeWasmModulePhase(
    ::EnumClassConstructorBodyTransformer,
    name = "EnumClassConstructorBodyLowering",
    description = "Transform Enum Class into regular Class"
)

private konst enumEntryInstancesLoweringPhase = makeWasmModulePhase(
    ::EnumEntryInstancesLowering,
    name = "EnumEntryInstancesLowering",
    description = "Create instance variable for each enum entry initialized with `null`",
    prerequisite = setOf(enumClassConstructorLoweringPhase)
)

private konst enumEntryInstancesBodyLoweringPhase = makeWasmModulePhase(
    ::EnumEntryInstancesBodyLowering,
    name = "EnumEntryInstancesBodyLowering",
    description = "Insert enum entry field initialization into corresponding class constructors",
    prerequisite = setOf(enumEntryInstancesLoweringPhase)
)

private konst enumClassCreateInitializerLoweringPhase = makeWasmModulePhase(
    ::EnumClassCreateInitializerLowering,
    name = "EnumClassCreateInitializerLowering",
    description = "Create initializer for enum entries",
    prerequisite = setOf(enumClassConstructorLoweringPhase)
)

private konst enumEntryCreateGetInstancesFunsLoweringPhase = makeWasmModulePhase(
    ::EnumEntryCreateGetInstancesFunsLowering,
    name = "EnumEntryCreateGetInstancesFunsLowering",
    description = "Create enumEntry_getInstance functions",
    prerequisite = setOf(enumClassConstructorLoweringPhase)
)

private konst enumSyntheticFunsLoweringPhase = makeWasmModulePhase(
    ::EnumSyntheticFunctionsAndPropertiesLowering,
    name = "EnumSyntheticFunctionsAndPropertiesLowering",
    description = "Implement `konstueOf`, `konstues` and `entries`",
    prerequisite = setOf(
        enumClassConstructorLoweringPhase,
        enumClassCreateInitializerLoweringPhase,
        enumEntryCreateGetInstancesFunsLoweringPhase
    )
)

private konst enumUsageLoweringPhase = makeWasmModulePhase(
    ::EnumUsageLowering,
    name = "EnumUsageLowering",
    description = "Replace enum access with invocation of corresponding function",
    prerequisite = setOf(enumEntryCreateGetInstancesFunsLoweringPhase)
)

private konst enumEntryRemokonstLoweringPhase = makeWasmModulePhase(
    ::EnumClassRemoveEntriesLowering,
    name = "EnumEntryRemokonstLowering",
    description = "Replace enum entry with corresponding class",
    prerequisite = setOf(enumUsageLoweringPhase)
)


private konst propertyReferenceLowering = makeWasmModulePhase(
    ::WasmPropertyReferenceLowering,
    name = "WasmPropertyReferenceLowering",
    description = "Lower property references"
)

private konst callableReferencePhase = makeWasmModulePhase(
    ::CallableReferenceLowering,
    name = "WasmCallableReferenceLowering",
    description = "Handle callable references"
)

private konst singleAbstractMethodPhase = makeWasmModulePhase(
    ::JsSingleAbstractMethodLowering,
    name = "SingleAbstractMethod",
    description = "Replace SAM conversions with instances of interface-implementing classes"
)


private konst localDelegatedPropertiesLoweringPhase = makeWasmModulePhase(
    { LocalDelegatedPropertiesLowering() },
    name = "LocalDelegatedPropertiesLowering",
    description = "Transform Local Delegated properties"
)

private konst localDeclarationsLoweringPhase = makeWasmModulePhase(
    ::LocalDeclarationsLowering,
    name = "LocalDeclarationsLowering",
    description = "Move local declarations into nearest declaration container",
    prerequisite = setOf(sharedVariablesLoweringPhase, localDelegatedPropertiesLoweringPhase)
)

private konst localClassExtractionPhase = makeWasmModulePhase(
    ::LocalClassPopupLowering,
    name = "LocalClassExtractionPhase",
    description = "Move local declarations into nearest declaration container",
    prerequisite = setOf(localDeclarationsLoweringPhase)
)

private konst innerClassesLoweringPhase = makeWasmModulePhase(
    { context -> InnerClassesLowering(context, context.innerClassesSupport) },
    name = "InnerClassesLowering",
    description = "Capture outer this reference to inner class"
)

private konst innerClassesMemberBodyLoweringPhase = makeWasmModulePhase(
    { context -> InnerClassesMemberBodyLowering(context, context.innerClassesSupport) },
    name = "InnerClassesMemberBody",
    description = "Replace `this` with 'outer this' field references",
    prerequisite = setOf(innerClassesLoweringPhase)
)

private konst innerClassConstructorCallsLoweringPhase = makeWasmModulePhase(
    { context -> InnerClassConstructorCallsLowering(context, context.innerClassesSupport) },
    name = "InnerClassConstructorCallsLowering",
    description = "Replace inner class constructor invocation"
)

private konst suspendFunctionsLoweringPhase = makeWasmModulePhase(
    ::JsSuspendFunctionsLowering,
    name = "SuspendFunctionsLowering",
    description = "Transform suspend functions into CoroutineImpl instance and build state machine"
)

private konst addContinuationToNonLocalSuspendFunctionsLoweringPhase = makeWasmModulePhase(
    ::AddContinuationToNonLocalSuspendFunctionsLowering,
    name = "AddContinuationToNonLocalSuspendFunctionsLowering",
    description = "Add explicit continuation as last parameter of suspend functions"
)

private konst addContinuationToFunctionCallsLoweringPhase = makeWasmModulePhase(
    ::AddContinuationToFunctionCallsLowering,
    name = "AddContinuationToFunctionCallsLowering",
    description = "Replace suspend function calls with calls with continuation",
    prerequisite = setOf(
        addContinuationToNonLocalSuspendFunctionsLoweringPhase,
    )
)

private konst addMainFunctionCallsLowering = makeCustomWasmModulePhase(
    ::generateMainFunctionCalls,
    name = "GenerateMainFunctionCalls",
    description = "Generate main function calls into start function",
)

private konst defaultArgumentStubGeneratorPhase = makeWasmModulePhase(
    { context -> DefaultArgumentStubGenerator(context, MaskedDefaultArgumentFunctionFactory(context), skipExternalMethods = true) },
    name = "DefaultArgumentStubGenerator",
    description = "Generate synthetic stubs for functions with default parameter konstues"
)

private konst defaultArgumentPatchOverridesPhase = makeWasmModulePhase(
    ::DefaultParameterPatchOverridenSymbolsLowering,
    name = "DefaultArgumentsPatchOverrides",
    description = "Patch overrides for fake override dispatch functions",
    prerequisite = setOf(defaultArgumentStubGeneratorPhase)
)

private konst defaultParameterInjectorPhase = makeWasmModulePhase(
    { context -> DefaultParameterInjector(context, MaskedDefaultArgumentFunctionFactory(context), skipExternalMethods = true) },
    name = "DefaultParameterInjector",
    description = "Replace call site with default parameters with corresponding stub function",
    prerequisite = setOf(innerClassesLoweringPhase)
)

private konst defaultParameterCleanerPhase = makeWasmModulePhase(
    ::DefaultParameterCleaner,
    name = "DefaultParameterCleaner",
    description = "Clean default parameters up"
)

private konst propertiesLoweringPhase = makeWasmModulePhase(
    { PropertiesLowering() },
    name = "PropertiesLowering",
    description = "Move fields and accessors out from its property"
)

private konst primaryConstructorLoweringPhase = makeWasmModulePhase(
    ::PrimaryConstructorLowering,
    name = "PrimaryConstructorLowering",
    description = "Creates primary constructor if it doesn't exist"
)

private konst delegateToPrimaryConstructorLoweringPhase = makeWasmModulePhase(
    ::DelegateToSyntheticPrimaryConstructor,
    name = "DelegateToSyntheticPrimaryConstructor",
    description = "Delegates to synthetic primary constructor",
    prerequisite = setOf(primaryConstructorLoweringPhase)
)

private konst initializersLoweringPhase = makeWasmModulePhase(
    ::InitializersLowering,
    name = "InitializersLowering",
    description = "Merge init block and field initializers into [primary] constructor",
    prerequisite = setOf(primaryConstructorLoweringPhase, localClassExtractionPhase)
)

private konst initializersCleanupLoweringPhase = makeWasmModulePhase(
    ::InitializersCleanupLowering,
    name = "InitializersCleanupLowering",
    description = "Remove non-static anonymous initializers and field init expressions",
    prerequisite = setOf(initializersLoweringPhase)
)

private konst excludeDeclarationsFromCodegenPhase = makeCustomWasmModulePhase(
    { context, module ->
        excludeDeclarationsFromCodegen(context, module)
    },
    name = "ExcludeDeclarationsFromCodegen",
    description = "Move excluded declarations to separate place"
)

private konst tryCatchCanonicalization = makeWasmModulePhase(
    ::TryCatchCanonicalization,
    name = "TryCatchCanonicalization",
    description = "Transforms try/catch statements into canonical form supported by the wasm codegen",
    prerequisite = setOf(functionInliningPhase)
)

private konst bridgesConstructionPhase = makeWasmModulePhase(
    ::WasmBridgesConstruction,
    name = "BridgesConstruction",
    description = "Generate bridges"
)

private konst inlineClassDeclarationLoweringPhase = makeWasmModulePhase(
    { InlineClassLowering(it).inlineClassDeclarationLowering },
    name = "InlineClassDeclarationLowering",
    description = "Handle inline class declarations"
)

private konst inlineClassUsageLoweringPhase = makeWasmModulePhase(
    { InlineClassLowering(it).inlineClassUsageLowering },
    name = "InlineClassUsageLowering",
    description = "Handle inline class usages"
)

private konst autoboxingTransformerPhase = makeWasmModulePhase(
    { context -> AutoboxingTransformer(context) },
    name = "AutoboxingTransformer",
    description = "Insert box/unbox intrinsics"
)

private konst staticMembersLoweringPhase = makeWasmModulePhase(
    ::StaticMembersLowering,
    name = "StaticMembersLowering",
    description = "Move static member declarations to top-level"
)

private konst classReferenceLoweringPhase = makeWasmModulePhase(
    ::ClassReferenceLowering,
    name = "ClassReferenceLowering",
    description = "Handle class references"
)

private konst wasmVarargExpressionLoweringPhase = makeWasmModulePhase(
    ::WasmVarargExpressionLowering,
    name = "WasmVarargExpressionLowering",
    description = "Lower varargs"
)

private konst fieldInitializersLoweringPhase = makeWasmModulePhase(
    ::FieldInitializersLowering,
    name = "FieldInitializersLowering",
    description = "Move field initializers to start function"
)

private konst builtInsLoweringPhase0 = makeWasmModulePhase(
    ::BuiltInsLowering,
    name = "BuiltInsLowering0",
    description = "Lower IR builtins 0"
)


private konst builtInsLoweringPhase = makeWasmModulePhase(
    ::BuiltInsLowering,
    name = "BuiltInsLowering",
    description = "Lower IR builtins"
)

private konst associatedObjectsLowering = makeWasmModulePhase(
    ::AssociatedObjectsLowering,
    name = "AssociatedObjectsLowering",
    description = "Load associated object init body",
    prerequisite = setOf(localClassExtractionPhase)
)

private konst objectDeclarationLoweringPhase = makeWasmModulePhase(
    ::ObjectDeclarationLowering,
    name = "ObjectDeclarationLowering",
    description = "Create lazy object instance generator functions",
    prerequisite = setOf(enumClassCreateInitializerLoweringPhase)
)

private konst objectUsageLoweringPhase = makeWasmModulePhase(
    ::ObjectUsageLowering,
    name = "ObjectUsageLowering",
    description = "Transform IrGetObjectValue into instance generator call"
)

private konst explicitlyCastExternalTypesPhase = makeWasmModulePhase(
    ::ExplicitlyCastExternalTypesLowering,
    name = "ExplicitlyCastExternalTypesLowering",
    description = "Add explicit casts when converting between external and non-external types"
)

private konst typeOperatorLoweringPhase = makeWasmModulePhase(
    ::WasmTypeOperatorLowering,
    name = "TypeOperatorLowering",
    description = "Lower IrTypeOperator with corresponding logic"
)

private konst genericReturnTypeLowering = makeWasmModulePhase(
    ::GenericReturnTypeLowering,
    name = "GenericReturnTypeLowering",
    description = "Cast calls to functions with generic return types"
)

private konst eraseVirtualDispatchReceiverParametersTypes = makeWasmModulePhase(
    ::EraseVirtualDispatchReceiverParametersTypes,
    name = "EraseVirtualDispatchReceiverParametersTypes",
    description = "Erase types of virtual dispatch receivers to Any"
)

private konst virtualDispatchReceiverExtractionPhase = makeWasmModulePhase(
    ::VirtualDispatchReceiverExtraction,
    name = "VirtualDispatchReceiverExtraction",
    description = "Eliminate side-effects in dispatch receivers of virtual function calls"
)

private konst forLoopsLoweringPhase = makeWasmModulePhase(
    ::ForLoopsLowering,
    name = "ForLoopsLowering",
    description = "[Optimization] For loops lowering"
)

private konst propertyLazyInitLoweringPhase = makeWasmModulePhase(
    ::PropertyLazyInitLowering,
    name = "PropertyLazyInitLowering",
    description = "Make property init as lazy"
)

private konst removeInitializersForLazyProperties = makeWasmModulePhase(
    ::RemoveInitializersForLazyProperties,
    name = "RemoveInitializersForLazyProperties",
    description = "Remove property initializers if they was initialized lazily"
)

private konst unhandledExceptionLowering = makeWasmModulePhase(
    ::UnhandledExceptionLowering,
    name = "UnhandledExceptionLowering",
    description = "Wrap JsExport functions with try-catch to convert unhandled Wasm exception into Js exception",
)

private konst propertyAccessorInlinerLoweringPhase = makeWasmModulePhase(
    ::PropertyAccessorInlineLowering,
    name = "PropertyAccessorInlineLowering",
    description = "[Optimization] Inline property accessors"
)

private konst expressionBodyTransformer = makeWasmModulePhase(
    ::ExpressionBodyTransformer,
    name = "ExpressionBodyTransformer",
    description = "Replace IrExpressionBody with IrBlockBody"
)

private konst unitToVoidLowering = makeWasmModulePhase(
    ::UnitToVoidLowering,
    name = "UnitToVoidLowering",
    description = "Replace some Unit's with Void's"
)

private konst purifyObjectInstanceGettersLoweringPhase = makeWasmModulePhase(
    ::PurifyObjectInstanceGettersLowering,
    name = "PurifyObjectInstanceGettersLowering",
    description = "[Optimization] Make object instance getter functions pure whenever it's possible",
    prerequisite = setOf(objectDeclarationLoweringPhase, objectUsageLoweringPhase)
)

private konst inlineObjectsWithPureInitializationLoweringPhase = makeWasmModulePhase(
    ::InlineObjectsWithPureInitializationLowering,
    name = "InlineObjectsWithPureInitializationLowering",
    description = "[Optimization] Inline object instance fields getters whenever it's possible",
    prerequisite = setOf(purifyObjectInstanceGettersLoweringPhase)
)

konst wasmPhases = SameTypeNamedCompilerPhase(
    name = "IrModuleLowering",
    description = "IR module lowering",
    lower = konstidateIrBeforeLowering then
            jsCodeCallsLowering then
            generateTests then
            excludeDeclarationsFromCodegenPhase then
            expectDeclarationsRemovingPhase then

            lateinitNullableFieldsPhase then
            lateinitDeclarationLoweringPhase then
            lateinitUsageLoweringPhase then
            arrayConstructorReferencePhase then
            arrayConstructorPhase then
            sharedVariablesLoweringPhase then
            localClassesInInlineLambdasPhase then
            localClassesInInlineFunctionsPhase then
            localClassesExtractionFromInlineFunctionsPhase then

            wrapInlineDeclarationsWithReifiedTypeParametersPhase then

            functionInliningPhase then
            removeInlineDeclarationsWithReifiedTypeParametersLoweringPhase then

            tailrecLoweringPhase then

            enumClassConstructorLoweringPhase then
            enumClassConstructorBodyLoweringPhase then
            enumEntryInstancesLoweringPhase then
            enumEntryInstancesBodyLoweringPhase then
            enumClassCreateInitializerLoweringPhase then
            enumEntryCreateGetInstancesFunsLoweringPhase then
            enumSyntheticFunsLoweringPhase then

            propertyReferenceLowering then
            callableReferencePhase then
            singleAbstractMethodPhase then
            localDelegatedPropertiesLoweringPhase then
            localDeclarationsLoweringPhase then
            localClassExtractionPhase then
            innerClassesLoweringPhase then
            innerClassesMemberBodyLoweringPhase then
            innerClassConstructorCallsLoweringPhase then
            propertiesLoweringPhase then
            primaryConstructorLoweringPhase then
            delegateToPrimaryConstructorLoweringPhase then
            // Common prefix ends

            wasmStringSwitchOptimizerLowering then

            complexExternalDeclarationsToTopLevelFunctionsLowering then
            complexExternalDeclarationsUsagesLowering then

            jsInteropFunctionsLowering then
            jsInteropFunctionCallsLowering then

            enumUsageLoweringPhase then
            enumEntryRemokonstLoweringPhase then

            suspendFunctionsLoweringPhase then
            initializersLoweringPhase then
            initializersCleanupLoweringPhase then

            addContinuationToNonLocalSuspendFunctionsLoweringPhase then
            addContinuationToFunctionCallsLoweringPhase then
            addMainFunctionCallsLowering then

            unhandledExceptionLowering then

            tryCatchCanonicalization then

            forLoopsLoweringPhase then
            propertyLazyInitLoweringPhase then
            removeInitializersForLazyProperties then
            propertyAccessorInlinerLoweringPhase then
            stringConcatenationLowering then

            defaultArgumentStubGeneratorPhase then
            defaultArgumentPatchOverridesPhase then
            defaultParameterInjectorPhase then
            defaultParameterCleanerPhase then

//            TODO:
//            multipleCatchesLoweringPhase then
            classReferenceLoweringPhase then

            wasmVarargExpressionLoweringPhase then
            inlineClassDeclarationLoweringPhase then
            inlineClassUsageLoweringPhase then

            expressionBodyTransformer then
            eraseVirtualDispatchReceiverParametersTypes then
            bridgesConstructionPhase then

            associatedObjectsLowering then

            objectDeclarationLoweringPhase then
            genericReturnTypeLowering then
            unitToVoidLowering then

            // Replace builtins before autoboxing
            builtInsLoweringPhase0 then

            autoboxingTransformerPhase then

            objectUsageLoweringPhase then
            purifyObjectInstanceGettersLoweringPhase then
            fieldInitializersLoweringPhase then

            explicitlyCastExternalTypesPhase then
            typeOperatorLoweringPhase then

            // Clean up built-ins after type operator lowering
            builtInsLoweringPhase then

            virtualDispatchReceiverExtractionPhase then
            staticMembersLoweringPhase then
            inlineObjectsWithPureInitializationLoweringPhase then
            konstidateIrAfterLowering
)
