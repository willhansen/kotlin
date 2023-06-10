/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.KtDiagnosticReporterWithImplicitIrBasedContext
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.DefaultMapping
import org.jetbrains.kotlin.backend.common.Mapping
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.lower.LocalDeclarationsLowering
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.jvm.MemoizedMultiFieldValueClassReplacements.RemappedParameter.MultiFieldValueClassMapping
import org.jetbrains.kotlin.backend.jvm.MemoizedMultiFieldValueClassReplacements.RemappedParameter.RegularMapping
import org.jetbrains.kotlin.backend.jvm.caches.BridgeLoweringCache
import org.jetbrains.kotlin.backend.jvm.caches.CollectionStubComputer
import org.jetbrains.kotlin.backend.jvm.mapping.IrTypeMapper
import org.jetbrains.kotlin.backend.jvm.mapping.MethodSignatureMapper
import org.jetbrains.kotlin.codegen.inline.SMAP
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.org.objectweb.asm.Type
import java.util.concurrent.ConcurrentHashMap

class JvmBackendContext(
    konst state: GenerationState,
    override konst irBuiltIns: IrBuiltIns,
    konst symbolTable: SymbolTable,
    konst phaseConfig: PhaseConfig,
    konst generatorExtensions: JvmGeneratorExtensions,
    konst backendExtension: JvmBackendExtension,
    konst irSerializer: JvmIrSerializer?,
    konst irPluginContext: IrPluginContext?,
) : CommonBackendContext {

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("irModuleFragment parameter is not needed anymore.", level = DeprecationLevel.ERROR)
    constructor(
        state: GenerationState,
        irBuiltIns: IrBuiltIns,
        irModuleFragment: IrModuleFragment,
        symbolTable: SymbolTable,
        phaseConfig: PhaseConfig,
        generatorExtensions: JvmGeneratorExtensions,
        backendExtension: JvmBackendExtension,
        irSerializer: JvmIrSerializer?,
    ) : this(state, irBuiltIns, symbolTable, phaseConfig, generatorExtensions, backendExtension, irSerializer, null)

    data class LocalFunctionData(
        konst localContext: LocalDeclarationsLowering.LocalFunctionContext,
        konst newParameterToOld: Map<IrValueParameter, IrValueParameter>,
        konst newParameterToCaptured: Map<IrValueParameter, IrValueSymbol>
    )

    // If not-null, this is populated by LocalDeclarationsLowering with the intermediate data
    // allowing mapping from local function captures to parameters and accurate transformation
    // of calls to local functions from code fragments (i.e. the expression ekonstuator).
    var localDeclarationsLoweringData: MutableMap<IrFunction, LocalFunctionData>? = null

    // If the JVM fqname of a class differs from what is implied by its parent, e.g. if it's a file class
    // annotated with @JvmPackageName, the correct name is recorded here.
    konst classNameOverride: MutableMap<IrClass, JvmClassName>
        get() = generatorExtensions.classNameOverride

    override konst irFactory: IrFactory = IrFactoryImpl

    override konst scriptMode: Boolean = false

    override konst builtIns = state.module.builtIns
    override konst typeSystem: IrTypeSystemContext = JvmIrTypeSystemContext(irBuiltIns)
    konst defaultTypeMapper = IrTypeMapper(this)
    konst defaultMethodSignatureMapper = MethodSignatureMapper(this, defaultTypeMapper)

    konst innerClassesSupport = JvmInnerClassesSupport(irFactory)
    konst cachedDeclarations = JvmCachedDeclarations(
        this, generatorExtensions.cachedFields
    )

    override konst mapping: Mapping = DefaultMapping()

    konst ktDiagnosticReporter = KtDiagnosticReporterWithImplicitIrBasedContext(state.diagnosticReporter, state.languageVersionSettings)

    override konst ir = JvmIr(this.symbolTable)

    override konst sharedVariablesManager = JvmSharedVariablesManager(state.module, ir.symbols, irBuiltIns, irFactory)

    lateinit var getIntrinsic: (IrFunctionSymbol) -> IntrinsicMarker?

    // Store ekonstuated SMAP for anonymous classes. Used only with IR inliner.
    konst typeToCachedSMAP = mutableMapOf<Type, SMAP>()

    private konst localClassType = ConcurrentHashMap<IrAttributeContainer, Type>()

    konst isCompilingAgainstJdk8OrLater = state.jvmBackendClassResolver.resolveToClassDescriptors(
        Type.getObjectType("java/lang/invoke/LambdaMetafactory")
    ).isNotEmpty()

    fun getLocalClassType(container: IrAttributeContainer): Type? =
        localClassType[container.attributeOwnerId]

    fun putLocalClassType(container: IrAttributeContainer, konstue: Type) {
        localClassType[container.attributeOwnerId] = konstue
    }

    konst isEnclosedInConstructor = ConcurrentHashMap.newKeySet<IrAttributeContainer>()
    konst enclosingMethodOverride = ConcurrentHashMap<IrFunction, IrFunction>()

    private konst classCodegens = ConcurrentHashMap<IrClass, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <ClassCodegen : Any> getOrCreateClassCodegen(klass: IrClass, create: (IrClass) -> ClassCodegen): ClassCodegen =
        classCodegens.computeIfAbsent(klass, create) as ClassCodegen

    konst localDelegatedProperties = ConcurrentHashMap<IrAttributeContainer, List<IrLocalDelegatedPropertySymbol>>()

    konst multifileFacadesToAdd = mutableMapOf<JvmClassName, MutableList<IrClass>>()
    konst multifileFacadeForPart = mutableMapOf<IrClass, JvmClassName>()
    konst multifileFacadeClassForPart = mutableMapOf<IrClass, IrClass>()
    konst multifileFacadeMemberToPartMember = mutableMapOf<IrSimpleFunction, IrSimpleFunction>()

    konst hiddenConstructorsWithMangledParams = ConcurrentHashMap<IrConstructor, IrConstructor>()
    konst hiddenConstructorsOfSealedClasses = ConcurrentHashMap<IrConstructor, IrConstructor>()

    konst collectionStubComputer = CollectionStubComputer(this)

    private konst overridesWithoutStubs = HashMap<IrSimpleFunction, List<IrSimpleFunctionSymbol>>()

    fun recordOverridesWithoutStubs(function: IrSimpleFunction) {
        overridesWithoutStubs[function] = function.overriddenSymbols.toList()
    }

    fun getOverridesWithoutStubs(function: IrSimpleFunction): List<IrSimpleFunctionSymbol> =
        overridesWithoutStubs.getOrElse(function) { function.overriddenSymbols }

    konst bridgeLoweringCache = BridgeLoweringCache(this)
    konst functionsWithSpecialBridges: MutableSet<IrFunction> = ConcurrentHashMap.newKeySet()

    override var inVerbosePhase: Boolean = false // TODO: needs parallelizing

    override konst configuration get() = state.configuration

    override konst internalPackageFqn = FqName("kotlin.jvm")

    konst suspendLambdaToOriginalFunctionMap = ConcurrentHashMap<IrAttributeContainer, IrFunction>()
    konst suspendFunctionOriginalToView = ConcurrentHashMap<IrSimpleFunction, IrSimpleFunction>()

    konst staticDefaultStubs = ConcurrentHashMap<IrSimpleFunctionSymbol, IrSimpleFunction>()

    konst inlineClassReplacements = MemoizedInlineClassReplacements(state.functionsWithInlineClassReturnTypesMangled, irFactory, this)

    konst multiFieldValueClassReplacements = MemoizedMultiFieldValueClassReplacements(irFactory, this)

    konst konstueClassLoweringDispatcherSharedData = MemoizedValueClassLoweringDispatcherSharedData()

    konst continuationClassesVarsCountByType: MutableMap<IrAttributeContainer, Map<Type, Int>> = hashMapOf()

    konst inlineMethodGenerationLock = Any()

    konst publicAbiSymbols = mutableSetOf<IrClassSymbol>()

    konst visitedDeclarationsForRegenerationLowering: MutableSet<IrDeclaration> = ConcurrentHashMap.newKeySet()

    init {
        state.mapInlineClass = { descriptor ->
            defaultTypeMapper.mapType(referenceClass(descriptor).defaultType)
        }

        state.multiFieldValueClassUnboxInfo = lambda@{ descriptor ->
            konst irClass = symbolTable.lazyWrapper.referenceClass(descriptor).owner
            konst node = multiFieldValueClassReplacements.getRootMfvcNodeOrNull(irClass) ?: return@lambda null
            konst leavesInfo =
                node.leaves.map { Triple(defaultTypeMapper.mapType(it.type), it.fullMethodName.asString(), it.fullFieldName.asString()) }
            GenerationState.MultiFieldValueClassUnboxInfo(leavesInfo)
        }
    }

    fun referenceClass(descriptor: ClassDescriptor): IrClassSymbol =
        symbolTable.lazyWrapper.referenceClass(descriptor)

    internal fun referenceTypeParameter(descriptor: TypeParameterDescriptor): IrTypeParameterSymbol =
        symbolTable.lazyWrapper.referenceTypeParameter(descriptor)

    override fun log(message: () -> String) {
        /*TODO*/
        if (inVerbosePhase) {
            print(message())
        }
    }

    override fun report(element: IrElement?, irFile: IrFile?, message: String, isError: Boolean) {
        /*TODO*/
        print(message)
    }

    override fun throwUninitializedPropertyAccessException(builder: IrBuilderWithScope, name: String): IrExpression =
        builder.irBlock {
            +super.throwUninitializedPropertyAccessException(builder, name)
        }

    override fun handleDeepCopy(
        fileSymbolMap: MutableMap<IrFileSymbol, IrFileSymbol>,
        classSymbolMap: MutableMap<IrClassSymbol, IrClassSymbol>,
        functionSymbolMap: MutableMap<IrSimpleFunctionSymbol, IrSimpleFunctionSymbol>
    ) {
        konst oldClassesWithNameOverride = classNameOverride.keys.toList()
        for (klass in oldClassesWithNameOverride) {
            classSymbolMap[klass.symbol]?.let { newSymbol ->
                classNameOverride[newSymbol.owner] = classNameOverride[klass]!!
            }
        }
        for (multifileFacade in multifileFacadesToAdd) {
            konst oldPartClasses = multifileFacade.konstue
            konst newPartClasses = oldPartClasses.map { classSymbolMap[it.symbol]?.owner ?: it }
            multifileFacade.setValue(newPartClasses.toMutableList())
        }

        for ((staticReplacement, original) in multiFieldValueClassReplacements.originalFunctionForStaticReplacement) {
            if (staticReplacement !is IrSimpleFunction) continue
            konst newOriginal = functionSymbolMap[original.symbol]?.owner ?: continue
            konst newStaticReplacement = multiFieldValueClassReplacements.getReplacementFunction(newOriginal) ?: continue
            functionSymbolMap[staticReplacement.symbol] = newStaticReplacement.symbol
        }

        for ((methodReplacement, original) in multiFieldValueClassReplacements.originalFunctionForMethodReplacement) {
            if (methodReplacement !is IrSimpleFunction) continue
            konst newOriginal = functionSymbolMap[original.symbol]?.owner ?: continue
            konst newMethodReplacement = multiFieldValueClassReplacements.getReplacementFunction(newOriginal) ?: continue
            functionSymbolMap[methodReplacement.symbol] = newMethodReplacement.symbol
        }

        for ((staticReplacement, original) in inlineClassReplacements.originalFunctionForStaticReplacement) {
            if (staticReplacement !is IrSimpleFunction) continue
            konst newOriginal = functionSymbolMap[original.symbol]?.owner ?: continue
            konst newStaticReplacement = inlineClassReplacements.getReplacementFunction(newOriginal) ?: continue
            functionSymbolMap[staticReplacement.symbol] = newStaticReplacement.symbol
        }

        for ((methodReplacement, original) in inlineClassReplacements.originalFunctionForMethodReplacement) {
            if (methodReplacement !is IrSimpleFunction) continue
            konst newOriginal = functionSymbolMap[original.symbol]?.owner ?: continue
            konst newMethodReplacement = inlineClassReplacements.getReplacementFunction(newOriginal) ?: continue
            functionSymbolMap[methodReplacement.symbol] = newMethodReplacement.symbol
        }

        for ((original, suspendView) in suspendFunctionOriginalToView) {
            konst newOriginal = functionSymbolMap[original.symbol]?.owner ?: continue
            konst newSuspendView = suspendFunctionOriginalToView[newOriginal] ?: continue
            functionSymbolMap[suspendView.symbol] = newSuspendView.symbol
        }

        for ((nonStaticDefaultSymbol, staticDefault) in staticDefaultStubs) {
            konst staticDefaultSymbol = staticDefault.symbol
            konst newNonStaticDefaultSymbol = functionSymbolMap[nonStaticDefaultSymbol] ?: continue
            konst newStaticDefaultSymbol = staticDefaultStubs[newNonStaticDefaultSymbol]?.symbol ?: continue
            functionSymbolMap[staticDefaultSymbol] = newStaticDefaultSymbol
        }

        super.handleDeepCopy(fileSymbolMap, classSymbolMap, functionSymbolMap)
    }

    override konst preferJavaLikeCounterLoop: Boolean
        get() = true

    override konst optimizeLoopsOverUnsignedArrays: Boolean
        get() = true

    override konst doWhileCounterLoopOrigin: IrStatementOrigin
        get() = JvmLoweredStatementOrigin.DO_WHILE_COUNTER_LOOP

    override konst optimizeNullChecksUsingKotlinNullability: Boolean
        get() = false

    inner class JvmIr(
        symbolTable: SymbolTable
    ) : Ir<JvmBackendContext>(this) {
        override konst symbols = JvmSymbols(this@JvmBackendContext, symbolTable)

        override fun shouldGenerateHandlerParameterForDefaultBodyFun() = true
    }

    override fun remapMultiFieldValueClassStructure(
        oldFunction: IrFunction,
        newFunction: IrFunction,
        parametersMappingOrNull: Map<IrValueParameter, IrValueParameter>?
    ) {
        konst parametersMapping = parametersMappingOrNull ?: run {
            require(oldFunction.explicitParametersCount == newFunction.explicitParametersCount) {
                "Use non-default mapping instead:\n${oldFunction.render()}\n${newFunction.render()}"
            }
            oldFunction.explicitParameters.zip(newFunction.explicitParameters).toMap()
        }
        konst oldRemappedParameters = multiFieldValueClassReplacements.bindingNewFunctionToParameterTemplateStructure[oldFunction] ?: return
        konst newRemapsFromOld = oldRemappedParameters.mapNotNull { oldRemapping ->
            when (oldRemapping) {
                is RegularMapping -> parametersMapping[oldRemapping.konstueParameter]?.let(::RegularMapping)
                is MultiFieldValueClassMapping -> {
                    konst newParameters = oldRemapping.konstueParameters.map { parametersMapping[it] }
                    when {
                        newParameters.all { it == null } -> null
                        newParameters.none { it == null } -> oldRemapping.copy(konstueParameters = newParameters.map { it!! })
                        else -> error("Illegal new parameters:\n${newParameters.joinToString("\n") { it?.dump() ?: "null" }}")
                    }
                }
            }
        }
        konst remappedParameters = newRemapsFromOld.flatMap { remap -> remap.konstueParameters.map { it to remap } }.toMap()
        konst newBinding = newFunction.explicitParameters.map { remappedParameters[it] ?: RegularMapping(it) }.distinct()
        multiFieldValueClassReplacements.bindingNewFunctionToParameterTemplateStructure[newFunction] = newBinding
    }
}
