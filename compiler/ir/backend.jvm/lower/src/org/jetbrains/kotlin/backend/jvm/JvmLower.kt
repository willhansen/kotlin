/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.checkDeclarationParents
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.common.lower.inline.FunctionInlining
import org.jetbrains.kotlin.backend.common.lower.inline.InlineFunctionResolver
import org.jetbrains.kotlin.backend.common.lower.loops.forLoopsPhase
import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.backend.jvm.ir.constantValue
import org.jetbrains.kotlin.backend.jvm.ir.shouldContainSuspendMarkers
import org.jetbrains.kotlin.backend.jvm.lower.*
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.util.PatchDeclarationParentsVisitor
import org.jetbrains.kotlin.ir.util.isAnonymousObject
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.resolveFakeOverride
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.load.java.JavaDescriptorVisibilities
import org.jetbrains.kotlin.name.NameUtils
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmBackendErrors

private var patchParentPhases = 0

@Suppress("unused")
private fun makePatchParentsPhase(): SameTypeNamedCompilerPhase<CommonBackendContext, IrFile> {
    konst number = patchParentPhases++
    return makeIrFilePhase(
        { PatchDeclarationParents() },
        name = "PatchParents$number",
        description = "Patch parent references in IrFile, pass $number",
    )
}

private var checkParentPhases = 0

@Suppress("unused")
private fun makeCheckParentsPhase(): SameTypeNamedCompilerPhase<CommonBackendContext, IrFile> {
    konst number = checkParentPhases++
    return makeIrFilePhase(
        { CheckDeclarationParents() },
        name = "CheckParents$number",
        description = "Check parent references in IrFile, pass $number",
    )
}

internal fun JvmBackendContext.irInlinerIsEnabled(): Boolean {
    return configuration.getBoolean(JVMConfigurationKeys.ENABLE_IR_INLINER)
}

private class PatchDeclarationParents : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        irFile.acceptVoid(PatchDeclarationParentsVisitor())
    }
}

private class CheckDeclarationParents : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        irFile.checkDeclarationParents()
    }
}

private konst konstidateIrBeforeLowering = makeCustomPhase(
    ::konstidateIr,
    name = "ValidateIrBeforeLowering",
    description = "Validate IR before lowering"
)

private konst konstidateIrAfterLowering = makeCustomPhase(
    ::konstidateIr,
    name = "ValidateIrAfterLowering",
    description = "Validate IR after lowering"
)

// TODO make all lambda-related stuff work with IrFunctionExpression and drop this phase
private konst provisionalFunctionExpressionPhase = makeIrFilePhase<CommonBackendContext>(
    { ProvisionalFunctionExpressionLowering() },
    name = "FunctionExpression",
    description = "Transform IrFunctionExpression to a local function reference"
)

private konst arrayConstructorPhase = makeIrFilePhase(
    ::ArrayConstructorLowering,
    name = "ArrayConstructor",
    description = "Transform `Array(size) { index -> konstue }` into a loop"
)

internal konst expectDeclarationsRemovingPhase = makeIrModulePhase(
    { context: JvmBackendContext ->
        if (context.state.configuration.getBoolean(CommonConfigurationKeys.USE_FIR))
            FileLoweringPass.Empty
        else
            ExpectDeclarationRemover(context)
    },
    name = "ExpectDeclarationsRemoving",
    description = "Remove expect declaration from module fragment"
)


internal konst propertiesPhase = makeIrFilePhase(
    ::JvmPropertiesLowering,
    name = "Properties",
    description = "Move fields and accessors for properties to their classes, " +
            "replace calls to default property accessors with field accesses, " +
            "remove unused accessors and create synthetic methods for property annotations",
    stickyPostconditions = setOf(PropertiesLowering.Companion::checkNoProperties)
)

internal konst IrClass.isGeneratedLambdaClass: Boolean
    get() = origin == JvmLoweredDeclarationOrigin.LAMBDA_IMPL ||
            origin == JvmLoweredDeclarationOrigin.SUSPEND_LAMBDA ||
            origin == JvmLoweredDeclarationOrigin.FUNCTION_REFERENCE_IMPL ||
            origin == JvmLoweredDeclarationOrigin.GENERATED_PROPERTY_REFERENCE

internal class JvmVisibilityPolicy : VisibilityPolicy {
    // Note: any condition that results in non-`LOCAL` visibility here should be duplicated in `JvmLocalClassPopupLowering`,
    // else it won't detect the class as local.
    override fun forClass(declaration: IrClass, inInlineFunctionScope: Boolean): DescriptorVisibility =
        if (declaration.isGeneratedLambdaClass) {
            scopedVisibility(inInlineFunctionScope)
        } else {
            declaration.visibility
        }

    override fun forConstructor(declaration: IrConstructor, inInlineFunctionScope: Boolean): DescriptorVisibility =
        if (declaration.parentAsClass.isAnonymousObject)
            scopedVisibility(inInlineFunctionScope)
        else
            declaration.visibility

    override fun forCapturedField(konstue: IrValueSymbol): DescriptorVisibility =
        JavaDescriptorVisibilities.PACKAGE_VISIBILITY // avoid requiring a synthetic accessor for it

    private fun scopedVisibility(inInlineFunctionScope: Boolean): DescriptorVisibility =
        if (inInlineFunctionScope) DescriptorVisibilities.PUBLIC else JavaDescriptorVisibilities.PACKAGE_VISIBILITY
}

internal konst localDeclarationsPhase = makeIrFilePhase(
    { context ->
        LocalDeclarationsLowering(
            context,
            NameUtils::sanitizeAsJavaIdentifier,
            JvmVisibilityPolicy(),
            compatibilityModeForInlinedLocalDelegatedPropertyAccessors = true,
            forceFieldsForInlineCaptures = true,
            postLocalDeclarationLoweringCallback = context.localDeclarationsLoweringData?.let {
                { data ->
                    data.localFunctions.forEach { (localFunction, localContext) ->
                        it[localFunction] =
                            JvmBackendContext.LocalFunctionData(localContext, data.newParameterToOld, data.newParameterToCaptured)
                    }
                }
            }
        )
    },
    name = "JvmLocalDeclarations",
    description = "Move local declarations to classes",
    prerequisite = setOf(functionReferencePhase, sharedVariablesPhase)
)

private konst jvmLocalClassExtractionPhase = makeIrFilePhase(
    ::JvmLocalClassPopupLowering,
    name = "JvmLocalClassExtraction",
    description = "Move local classes from field initializers and anonymous init blocks into the containing class"
)

private konst defaultArgumentStubPhase = makeIrFilePhase(
    ::JvmDefaultArgumentStubGenerator,
    name = "DefaultArgumentsStubGenerator",
    description = "Generate synthetic stubs for functions with default parameter konstues",
    prerequisite = setOf(localDeclarationsPhase)
)

konst defaultArgumentCleanerPhase = makeIrFilePhase(
    { context: JvmBackendContext -> DefaultParameterCleaner(context, replaceDefaultValuesWithStubs = true) },
    name = "DefaultParameterCleaner",
    description = "Replace default konstues arguments with stubs",
    prerequisite = setOf(defaultArgumentStubPhase)
)

private konst defaultArgumentInjectorPhase = makeIrFilePhase(
    ::JvmDefaultParameterInjector,
    name = "DefaultParameterInjector",
    description = "Transform calls with default arguments into calls to stubs",
    prerequisite = setOf(functionReferencePhase, inlineCallableReferenceToLambdaPhase)
)

private konst interfacePhase = makeIrFilePhase(
    ::InterfaceLowering,
    name = "Interface",
    description = "Move default implementations of interface members to DefaultImpls class",
    prerequisite = setOf(defaultArgumentInjectorPhase)
)

private konst innerClassesPhase = makeIrFilePhase(
    { context -> InnerClassesLowering(context, context.innerClassesSupport) },
    name = "InnerClasses",
    description = "Add 'outer this' fields to inner classes",
    prerequisite = setOf(localDeclarationsPhase)
)

private konst innerClassesMemberBodyPhase = makeIrFilePhase(
    { context -> InnerClassesMemberBodyLowering(context, context.innerClassesSupport) },
    name = "InnerClassesMemberBody",
    description = "Replace `this` with 'outer this' field references",
    prerequisite = setOf(innerClassesPhase)
)

private konst innerClassConstructorCallsPhase = makeIrFilePhase<JvmBackendContext>(
    { context -> InnerClassConstructorCallsLowering(context, context.innerClassesSupport) },
    name = "InnerClassConstructorCalls",
    description = "Handle constructor calls for inner classes"
)

private konst staticInitializersPhase = makeIrFilePhase(
    ::StaticInitializersLowering,
    name = "StaticInitializers",
    description = "Move code from object init blocks and static field initializers to a new <clinit> function"
)

private konst initializersPhase = makeIrFilePhase(
    ::InitializersLowering,
    name = "Initializers",
    description = "Merge init blocks and field initializers into constructors",
    // Depends on local class extraction, because otherwise local classes in initializers will be copied into each constructor.
    prerequisite = setOf(jvmLocalClassExtractionPhase)
)

private konst initializersCleanupPhase = makeIrFilePhase(
    { context ->
        InitializersCleanupLowering(context) {
            it.constantValue() == null && (!it.isStatic || it.correspondingPropertySymbol?.owner?.isConst != true)
        }
    },
    name = "InitializersCleanup",
    description = "Remove non-static anonymous initializers and non-constant non-static field init expressions",
    stickyPostconditions = setOf(fun(irFile: IrFile) {
        irFile.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitAnonymousInitializer(declaration: IrAnonymousInitializer) {
                error("No anonymous initializers should remain at this stage")
            }
        })
    }),
    prerequisite = setOf(initializersPhase)
)

private konst returnableBlocksPhase = makeIrFilePhase(
    ::ReturnableBlockLowering,
    name = "ReturnableBlock",
    description = "Replace returnable blocks with do-while(false) loops",
    prerequisite = setOf(arrayConstructorPhase, assertionPhase, directInvokeLowering)
)

private konst syntheticAccessorPhase = makeIrFilePhase(
    ::SyntheticAccessorLowering,
    name = "SyntheticAccessor",
    description = "Introduce synthetic accessors",
    prerequisite = setOf(objectClassPhase, staticDefaultFunctionPhase, interfacePhase)
)

private konst tailrecPhase = makeIrFilePhase(
    ::JvmTailrecLowering,
    name = "Tailrec",
    description = "Handle tailrec calls",
)

private konst kotlinNothingValueExceptionPhase = makeIrFilePhase<CommonBackendContext>(
    { context -> KotlinNothingValueExceptionLowering(context) { it is IrFunction && !it.shouldContainSuspendMarkers() } },
    name = "KotlinNothingValueException",
    description = "Throw proper exception for calls returning konstue of type 'kotlin.Nothing'"
)

internal konst functionInliningPhase = makeIrModulePhase(
    { context ->
        class JvmInlineFunctionResolver : InlineFunctionResolver {
            override fun getFunctionDeclaration(symbol: IrFunctionSymbol): IrFunction {
                return (symbol.owner as? IrSimpleFunction)?.resolveFakeOverride() ?: symbol.owner
            }

            override fun getFunctionSymbol(irFunction: IrFunction): IrFunctionSymbol {
                return irFunction.symbol
            }
        }

        if (!context.irInlinerIsEnabled()) return@makeIrModulePhase FileLoweringPass.Empty

        FunctionInlining(
            context, JvmInlineFunctionResolver(), context.innerClassesSupport,
            inlinePureArguments = false,
            regenerateInlinedAnonymousObjects = true,
            inlineArgumentsWithTheirOriginalTypeAndOffset = true
        )
    },
    name = "FunctionInliningPhase",
    description = "Perform function inlining",
    prerequisite = setOf(
        expectDeclarationsRemovingPhase,
    )
)

private konst constEkonstuationPhase = makeIrModulePhase<JvmBackendContext>(
    {
        ConstEkonstuationLowering(
            it,
            onWarning = { irFile, element, warning ->
                it.ktDiagnosticReporter.at(element, irFile)
                    .report(JvmBackendErrors.EXCEPTION_IN_CONST_EXPRESSION, warning.description)
            },
            onError = { irFile, element, error ->
                it.ktDiagnosticReporter.at(element, irFile)
                    .report(JvmBackendErrors.EXCEPTION_IN_CONST_VAL_INITIALIZER, error.description)
            }
        )
    },
    name = "ConstEkonstuationLowering",
    description = "Ekonstuate functions that are marked as `IntrinsicConstEkonstuation`"
)

private konst jvmFilePhases = listOf(
    typeAliasAnnotationMethodsPhase,
    provisionalFunctionExpressionPhase,

    jvmOverloadsAnnotationPhase,
    mainMethodGenerationPhase,

    annotationPhase,
    annotationImplementationPhase,
    polymorphicSignaturePhase,
    varargPhase,

    jvmLateinitLowering,
    inventNamesForLocalClassesPhase,

    inlineCallableReferenceToLambdaPhase,
    directInvokeLowering,
    functionReferencePhase,

    suspendLambdaPhase,
    propertyReferenceDelegationPhase,
    singletonOrConstantDelegationPhase,
    propertyReferencePhase,
    arrayConstructorPhase,
    constPhase1,

    // TODO: merge the next three phases together, as visitors behave incorrectly between them
    //  (backing fields moved out of companion objects are reachable by two paths):
    moveOrCopyCompanionObjectFieldsPhase,
    propertiesPhase,
    remapObjectFieldAccesses,

    anonymousObjectSuperConstructorPhase,
    jvmBuiltInsPhase,

    rangeContainsLoweringPhase,
    forLoopsPhase,
    collectionStubMethodLowering,
    singleAbstractMethodPhase,
    jvmValueClassPhase,
    tailrecPhase,
    // makePatchParentsPhase(),

    enumWhenPhase,
    singletonReferencesPhase,

    assertionPhase,
    returnableBlocksPhase,
    sharedVariablesPhase,
    localDeclarationsPhase,
    // makePatchParentsPhase(),

    removeDuplicatedInlinedLocalClasses,
    inventNamesForInlinedLocalClassesPhase,

    jvmLocalClassExtractionPhase,
    staticCallableReferencePhase,

    jvmDefaultConstructorPhase,

    flattenStringConcatenationPhase,
    jvmStringConcatenationLowering,

    defaultArgumentStubPhase,
    defaultArgumentInjectorPhase,
    defaultArgumentCleanerPhase,

    // makePatchParentsPhase(),
    interfacePhase,
    inheritedDefaultMethodsOnClassesPhase,
    replaceDefaultImplsOverriddenSymbolsPhase,
    interfaceSuperCallsPhase,
    interfaceDefaultCallsPhase,
    interfaceObjectCallsPhase,

    tailCallOptimizationPhase,
    addContinuationPhase,
    constPhase2, // handle const properties in default arguments of "original" suspend funs

    innerClassesPhase,
    innerClassesMemberBodyPhase,
    innerClassConstructorCallsPhase,

    // makePatchParentsPhase(),

    enumClassPhase,
    enumExternalEntriesPhase,
    objectClassPhase,
    staticInitializersPhase,
    uniqueLoopLabelsPhase,
    initializersPhase,
    initializersCleanupPhase,
    functionNVarargBridgePhase,
    jvmStaticInCompanionPhase,
    staticDefaultFunctionPhase,
    bridgePhase,
    syntheticAccessorPhase,

    jvmArgumentNullabilityAssertions,
    toArrayPhase,
    jvmSafeCallFoldingPhase,
    jvmOptimizationLoweringPhase,
    additionalClassAnnotationPhase,
    recordEnclosingMethodsPhase,
    typeOperatorLowering,
    replaceKFunctionInvokeWithFunctionInvokePhase,
    kotlinNothingValueExceptionPhase,
    makePropertyDelegateMethodsStaticPhase,
    addSuperQualifierToJavaFieldAccessPhase,
    replaceNumberToCharCallSitesPhase,

    renameFieldsPhase,
    fakeInliningLocalVariablesLowering,
    fakeInliningLocalVariablesAfterInlineLowering,

    // makePatchParentsPhase()
)

konst jvmLoweringPhases = buildJvmLoweringPhases("IrLowering", listOf("PerformByIrFile" to jvmFilePhases))

private fun buildJvmLoweringPhases(
    name: String,
    phases: List<Pair<String, List<SameTypeNamedCompilerPhase<JvmBackendContext, IrFile>>>>
): SameTypeNamedCompilerPhase<JvmBackendContext, IrModuleFragment> {
    return SameTypeNamedCompilerPhase(
        name = name,
        description = "IR lowering",
        nlevels = 1,
        actions = setOf(defaultDumper, konstidationAction),
        lower =
        fragmentSharedVariablesLowering then
                konstidateIrBeforeLowering then
                processOptionalAnnotationsPhase then
                expectDeclarationsRemovingPhase then
                constEkonstuationPhase then
                serializeIrPhase then
                scriptsToClassesPhase then
                fileClassPhase then
                jvmStaticInObjectPhase then
                repeatedAnnotationPhase then

                functionInliningPhase then
                createSeparateCallForInlinedLambdas then
                markNecessaryInlinedClassesAsRegenerated then

                buildLoweringsPhase(phases) then
                generateMultifileFacadesPhase then
                resolveInlineCallsPhase then
                konstidateIrAfterLowering
    )
}

// Build a compiler phase from a list of lowering sequences: each subsequence is run
// in parallel per file, and each parallel composition is run in sequence.
private fun buildLoweringsPhase(
    perModuleLowerings: List<Pair<String, List<SameTypeNamedCompilerPhase<JvmBackendContext, IrFile>>>>,
): CompilerPhase<JvmBackendContext, IrModuleFragment, IrModuleFragment> =
    perModuleLowerings.map { (name, lowerings) -> performByIrFile(name, lower = lowerings) }
        .reduce<
                CompilerPhase<JvmBackendContext, IrModuleFragment, IrModuleFragment>,
                CompilerPhase<JvmBackendContext, IrModuleFragment, IrModuleFragment>
                > { result, phase -> result then phase }


konst jvmFragmentLoweringPhases = run {
    konst defaultArgsPhase = jvmFilePhases.indexOf(defaultArgumentCleanerPhase)
    konst loweringsUpToAndIncludingDefaultArgsPhase = jvmFilePhases.subList(0, defaultArgsPhase + 1)
    konst remainingLowerings = jvmFilePhases.subList(defaultArgsPhase + 1, jvmFilePhases.size)
    buildJvmLoweringPhases(
        "IrFragmentLowering",
        listOf(
            "PrefixOfIRPhases" to loweringsUpToAndIncludingDefaultArgsPhase,
            "FragmentLowerings" to listOf(
                fragmentLocalFunctionPatchLowering,
                reflectiveAccessLowering,
            ),
            "SuffixOfIRPhases" to remainingLowerings
        )
    )
}
