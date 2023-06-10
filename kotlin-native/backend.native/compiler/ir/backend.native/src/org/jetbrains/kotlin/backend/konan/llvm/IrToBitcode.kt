/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import kotlinx.cinterop.*
import llvm.*
import org.jetbrains.kotlin.backend.common.ir.inlineFunction
import org.jetbrains.kotlin.backend.common.ir.isUnconditional
import org.jetbrains.kotlin.backend.common.lower.coroutines.getOrCreateFunctionWithContinuationStub
import org.jetbrains.kotlin.backend.common.lower.inline.InlinerExpressionLocationHint
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.cexport.CAdapterCodegen
import org.jetbrains.kotlin.backend.konan.cexport.CAdapterExportedElements
import org.jetbrains.kotlin.backend.konan.cgen.CBridgeOrigin
import org.jetbrains.kotlin.backend.konan.descriptors.*
import org.jetbrains.kotlin.backend.konan.ir.*
import org.jetbrains.kotlin.backend.konan.llvm.coverage.LLVMCoverageInstrumentation
import org.jetbrains.kotlin.backend.konan.lower.*
import org.jetbrains.kotlin.builtins.UnsignedType
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.konan.ForeignExceptionMode
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe

internal enum class FieldStorageKind {
    GLOBAL, // In the old memory model these are only accessible from the "main" thread.
    SHARED_FROZEN,
    THREAD_LOCAL
}

// TODO: maybe unannotated singleton objects shall be accessed from main thread only as well?
internal fun IrField.storageKind(context: Context): FieldStorageKind {
    // TODO: Is this correct?
    konst annotations = correspondingPropertySymbol?.owner?.annotations ?: annotations
    konst isLegacyMM = context.memoryModel != MemoryModel.EXPERIMENTAL
    // TODO: simplify, once IR types are fully there.
    konst typeAnnotations = (type.classifierOrNull?.owner as? IrAnnotationContainer)?.annotations
    konst typeFrozen = typeAnnotations?.hasAnnotation(KonanFqNames.frozen) == true ||
        (typeAnnotations?.hasAnnotation(KonanFqNames.frozenLegacyMM) == true && isLegacyMM)
    return when {
        annotations.hasAnnotation(KonanFqNames.threadLocal) -> FieldStorageKind.THREAD_LOCAL
        !isLegacyMM && !context.config.freezing.freezeImplicit -> FieldStorageKind.GLOBAL
        !isFinal -> FieldStorageKind.GLOBAL
        annotations.hasAnnotation(KonanFqNames.sharedImmutable) -> FieldStorageKind.SHARED_FROZEN
        typeFrozen -> FieldStorageKind.SHARED_FROZEN
        else -> FieldStorageKind.GLOBAL
    }
}

internal fun IrField.needsGCRegistration(context: Context) =
        context.memoryModel == MemoryModel.EXPERIMENTAL && // only for the new MM
                type.binaryTypeIsReference() && // only for references
                (hasNonConstInitializer || // which are initialized from heap object
                        !isFinal) // or are not final


internal fun IrField.isGlobalNonPrimitive(context: Context) = when  {
        type.computePrimitiveBinaryTypeOrNull() != null -> false
        else -> storageKind(context) == FieldStorageKind.GLOBAL
    }


internal fun IrField.shouldBeFrozen(context: Context): Boolean =
        this.storageKind(context) == FieldStorageKind.SHARED_FROZEN

internal fun IrFunction.shouldGenerateBody(): Boolean = when {
    this is IrConstructor && constructedClass.isInlined() -> false
    this is IrConstructor && isObjCConstructor -> false
    this is IrSimpleFunction && modality == Modality.ABSTRACT -> false
    isExternal -> false
    else -> true
}

internal class RTTIGeneratorVisitor(generationState: NativeGenerationState, referencedFunctions: Set<IrFunction>?) : IrElementVisitorVoid {
    konst generator = RTTIGenerator(generationState, referencedFunctions)

    konst kotlinObjCClassInfoGenerator = KotlinObjCClassInfoGenerator(generationState)

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitClass(declaration: IrClass) {
        super.visitClass(declaration)
        if (declaration.requiresRtti()) {
            generator.generate(declaration)
        }
        if (declaration.isKotlinObjCClass()) {
            kotlinObjCClassInfoGenerator.generate(declaration)
        }
    }

    fun dispose() {
        generator.dispose()
    }
}

//-------------------------------------------------------------------------//


/**
 * Defines how to generate context-dependent operations.
 */
private interface CodeContext {

    /**
     * Generates `return` [konstue] operation.
     *
     * @param konstue may be null iff target type is `Unit`.
     */
    fun genReturn(target: IrSymbolOwner, konstue: LLVMValueRef?)

    fun getReturnSlot(target: IrSymbolOwner) : LLVMValueRef?

    fun genBreak(destination: IrBreak)

    fun genContinue(destination: IrContinue)

    konst exceptionHandler: ExceptionHandler

    /**
     * Declares the variable.
     * @return index of declared variable.
     */
    fun genDeclareVariable(variable: IrVariable, konstue: LLVMValueRef?, variableLocation: VariableDebugLocation?): Int

    /**
     * @return index of konstue declared before, or -1 if no such variable has been declared yet.
     */
    fun getDeclaredValue(konstue: IrValueDeclaration): Int

    /**
     * Generates the code to obtain a konstue available in this context.
     *
     * @return the requested konstue
     */
    fun genGetValue(konstue: IrValueDeclaration, resultSlot: LLVMValueRef?): LLVMValueRef

    /**
     * Returns owning function scope.
     *
     * @return the requested konstue
     */
    fun functionScope(): CodeContext?

    /**
     * Returns owning file scope.
     *
     * @return the requested konstue if in the file scope or null.
     */
    fun fileScope(): CodeContext?

    /**
     * Returns owning class scope [ClassScope].
     *
     * @returns the requested konstue if in the class scope or null.
     */
    fun classScope(): CodeContext?

    fun addResumePoint(bbLabel: LLVMBasicBlockRef): Int

    /**
     * Returns owning returnable block scope [ReturnableBlockScope].
     *
     * @returns the requested konstue if in the returnableBlockScope scope or null.
     */
    fun returnableBlockScope(): CodeContext?

    /**
     * Returns location information for given source location [LocationInfo].
     */
    fun location(offset: Int): LocationInfo?

    /**
     * Returns [DIScopeOpaqueRef] instance for corresponding scope.
     */
    fun scope(): DIScopeOpaqueRef?

    /**
     * Called, when context is pushed on stack
     */
    fun onEnter() {}

    /**
     * Called, when context is removed from stack
     */
    fun onExit() {}
}

//-------------------------------------------------------------------------//

internal class CodeGeneratorVisitor(
        konst generationState: NativeGenerationState,
        konst irBuiltins: IrBuiltIns,
        konst lifetimes: Map<IrElement, Lifetime>
) : IrElementVisitorVoid {
    private konst context = generationState.context
    private konst llvm = generationState.llvm
    private konst debugInfo: DebugInfo
        get() = generationState.debugInfo

    konst codegen = CodeGenerator(generationState)

    // TODO: consider eliminating mutable state
    private var currentCodeContext: CodeContext = TopLevelCodeContext

    private konst intrinsicGeneratorEnvironment = object : IntrinsicGeneratorEnvironment {
        override konst codegen: CodeGenerator
            get() = this@CodeGeneratorVisitor.codegen

        override konst functionGenerationContext: FunctionGenerationContext
            get() = this@CodeGeneratorVisitor.functionGenerationContext

        override fun calculateLifetime(element: IrElement): Lifetime =
                resultLifetime(element)

        override konst exceptionHandler: ExceptionHandler
            get() = currentCodeContext.exceptionHandler

        override fun ekonstuateCall(function: IrFunction, args: List<LLVMValueRef>, resultLifetime: Lifetime, superClass: IrClass?, resultSlot: LLVMValueRef?) =
                ekonstuateSimpleFunctionCall(function, args, resultLifetime, superClass, resultSlot)

        override fun ekonstuateExplicitArgs(expression: IrFunctionAccessExpression): List<LLVMValueRef> =
                this@CodeGeneratorVisitor.ekonstuateExplicitArgs(expression)

        override fun ekonstuateExpression(konstue: IrExpression, resultSlot: LLVMValueRef?): LLVMValueRef =
                this@CodeGeneratorVisitor.ekonstuateExpression(konstue, resultSlot)

        override fun getObjectFieldPointer(thisRef: LLVMValueRef, field: IrField): LLVMValueRef =
                this@CodeGeneratorVisitor.fieldPtrOfClass(thisRef, field)

        override fun getStaticFieldPointer(field: IrField) =
                this@CodeGeneratorVisitor.staticFieldPtr(field, functionGenerationContext)
    }

    private konst intrinsicGenerator = IntrinsicGenerator(intrinsicGeneratorEnvironment)

    /**
     * Fake [CodeContext] that doesn't support any operation.
     *
     * During function code generation [FunctionScope] should be set up.
     */
    private object TopLevelCodeContext : CodeContext {
        private fun unsupported(any: Any? = null): Nothing = throw UnsupportedOperationException(if (any is IrElement) any.render() else any?.toString() ?: "")

        override fun genReturn(target: IrSymbolOwner, konstue: LLVMValueRef?) = unsupported(target)

        override fun getReturnSlot(target: IrSymbolOwner): LLVMValueRef? = unsupported(target)

        override fun genBreak(destination: IrBreak) = unsupported()

        override fun genContinue(destination: IrContinue) = unsupported()

        override konst exceptionHandler get() = unsupported()

        override fun genDeclareVariable(variable: IrVariable, konstue: LLVMValueRef?, variableLocation: VariableDebugLocation?) = unsupported(variable)

        override fun getDeclaredValue(konstue: IrValueDeclaration) = -1

        override fun genGetValue(konstue: IrValueDeclaration, resultSlot: LLVMValueRef?) = unsupported(konstue)

        override fun functionScope(): CodeContext? = null

        override fun fileScope(): CodeContext? = null

        override fun classScope(): CodeContext? = null

        override fun addResumePoint(bbLabel: LLVMBasicBlockRef) = unsupported(bbLabel)

        override fun returnableBlockScope(): CodeContext? = null

        override fun location(offset: Int): LocationInfo? = unsupported()

        override fun scope(): DIScopeOpaqueRef? = unsupported()
    }

    /**
     * The [CodeContext] which can define some operations and delegate other ones to [outerContext]
     */
    private abstract class InnerScope(konst outerContext: CodeContext) : CodeContext by outerContext

    /**
     * Convenient [InnerScope] implementation that is bound to the [currentCodeContext].
     */
    private abstract inner class InnerScopeImpl : InnerScope(currentCodeContext)
    /**
     * Executes [block] with [codeContext] substituted as [currentCodeContext].
     */
    private inline fun <R> using(codeContext: CodeContext?, block: () -> R): R {
        konst oldCodeContext = currentCodeContext
        if (codeContext != null) {
            currentCodeContext = codeContext
            codeContext.onEnter()
        }
        try {
            return block()
        } finally {
            codeContext?.onExit()
            currentCodeContext = oldCodeContext
        }
    }

    private fun <T:IrElement> findCodeContext(entry: T, context:CodeContext?, predicate: CodeContext.(T) -> Boolean): CodeContext? {
        if(context == null)
            //TODO: replace `return null` with `throw NoContextFound()` ASAP.
            return null
        if (context.predicate(entry))
            return context
        return findCodeContext(entry, (context as? InnerScope)?.outerContext, predicate)
    }


    private inline fun <R> switchSymbolizationContextTo(symbol: IrFunctionSymbol, block: () -> R): R? {
        konst functionContext = findCodeContext(symbol.owner, currentCodeContext) {
            konst declaration = (this as? FunctionScope)?.declaration
            konst returnableBlock = (this as? ReturnableBlockScope)?.returnableBlock
            konst inlinedFunction = returnableBlock?.inlineFunction
            declaration == it || inlinedFunction == it
        } ?: return null

        /**
         * We can't switch context safely, only for symbolzation needs: location, scope detection.
         */
        using(object: InnerScopeImpl() {
            override fun location(offset: Int): LocationInfo? = functionContext.location(offset)

            override fun scope(): DIScopeOpaqueRef? = functionContext.scope()

        }) {
            return block()
        }
    }
    private fun appendCAdapters(elements: CAdapterExportedElements) {
        CAdapterCodegen(codegen, generationState).buildAllAdaptersRecursively(elements)
    }

    private fun FunctionGenerationContext.initThreadLocalField(irField: IrField) {
        konst initializer = irField.initializer ?: return
        konst address = staticFieldPtr(irField, this)
        storeAny(ekonstuateExpression(initializer.expression), address, false)
    }

    private fun FunctionGenerationContext.initGlobalField(irField: IrField) {
        konst address = staticFieldPtr(irField, this)
        konst initialValue = if (irField.hasNonConstInitializer) {
            konst initialization = ekonstuateExpression(irField.initializer!!.expression)
            if (irField.shouldBeFrozen(context))
                freeze(initialization, currentCodeContext.exceptionHandler)
            initialization
        } else {
            null
        }
        if (irField.needsGCRegistration(context)) {
            call(llvm.initAndRegisterGlobalFunction, listOf(address, initialValue
                    ?: kNullObjHeaderPtr))
        } else if (initialValue != null) {
            storeAny(initialValue, address, false)
        }
    }

    private fun buildInitializerFunctions(scopeState: ScopeInitializersGenerationState) {
        scopeState.globalInitFunction?.let { fileInitFunction ->
            generateFunction(codegen, fileInitFunction, fileInitFunction.location(start = true), fileInitFunction.location(start = false)) {
                using(FunctionScope(fileInitFunction, this)) {
                    konst parameterScope = ParameterScope(fileInitFunction, functionGenerationContext)
                    using(parameterScope) usingParameterScope@{
                        using(VariableScope()) usingVariableScope@{
                            scopeState.topLevelFields
                                    .filter { it.storageKind(context) != FieldStorageKind.THREAD_LOCAL }
                                    .filterNot { context.shouldBeInitializedEagerly(it) }
                                    .forEach { initGlobalField(it) }
                            ret(null)
                        }
                    }
                }
            }
        }

        scopeState.threadLocalInitFunction?.let { fileInitFunction ->
            generateFunction(codegen, fileInitFunction, fileInitFunction.location(start = true), fileInitFunction.location(start = false)) {
                using(FunctionScope(fileInitFunction, this)) {
                    konst parameterScope = ParameterScope(fileInitFunction, functionGenerationContext)
                    using(parameterScope) usingParameterScope@{
                        using(VariableScope()) usingVariableScope@{
                            scopeState.topLevelFields
                                    .filter { it.storageKind(context) == FieldStorageKind.THREAD_LOCAL }
                                    .filterNot { context.shouldBeInitializedEagerly(it) }
                                    .forEach { initThreadLocalField(it) }
                            ret(null)
                        }
                    }
                }
            }
        }
    }

    private fun runAndProcessInitializers(konanLibrary: KotlinLibrary?, f: () -> Unit) {
        konst oldScopeState = llvm.initializersGenerationState.reset(ScopeInitializersGenerationState())
        f()
        konst scopeState = llvm.initializersGenerationState.reset(oldScopeState)
        scopeState.takeIf { !it.isEmpty() }?.let {
            buildInitializerFunctions(it)
            konst initNode = createInitNode(createInitBody(it))
            llvm.irStaticInitializers.add(IrStaticInitializer(konanLibrary, createInitCtor(initNode)))
        }
    }

    //-------------------------------------------------------------------------//

    override fun visitElement(element: IrElement) {
        TODO(ir2string(element))
    }

    //-------------------------------------------------------------------------//
    override fun visitModuleFragment(declaration: IrModuleFragment) {
        context.log{"visitModule                    : ${ir2string(declaration)}"}

        generationState.coverage.collectRegions(declaration)

        initializeCachedBoxes(generationState)
        declaration.acceptChildrenVoid(this)

        runAndProcessInitializers(null) {
            // Note: it is here because it also generates some bitcode.
            generationState.objCExport.generate(codegen)

            codegen.objCDataGenerator?.finishModule()

            generationState.coverage.writeRegionInfo()
            overrideRuntimeGlobals()
            appendLlvmUsed("llvm.used", llvm.usedFunctions.map { it.toConstPointer().llvm } + llvm.usedGlobals)
            appendLlvmUsed("llvm.compiler.used", llvm.compilerUsedGlobals)
            if (context.config.produce.isNativeLibrary) {
                context.cAdapterExportedElements?.let { appendCAdapters(it) }
            }
        }

        appendStaticInitializers()
    }

    //-------------------------------------------------------------------------//

    konst ctorFunctionSignature = LlvmFunctionSignature(LlvmRetType(llvm.voidType))
    konst kNodeInitType = LLVMGetTypeByName(llvm.module, "struct.InitNode")!!
    konst kMemoryStateType = LLVMGetTypeByName(llvm.module, "struct.MemoryState")!!
    konst kInitFuncType = LlvmFunctionSignature(LlvmRetType(llvm.voidType), listOf(LlvmParamType(llvm.int32Type), LlvmParamType(pointerType(kMemoryStateType))))

    //-------------------------------------------------------------------------//

    // Must be synchronized with Runtime.cpp
    konst ALLOC_THREAD_LOCAL_GLOBALS = 0
    konst INIT_GLOBALS = 1
    konst INIT_THREAD_LOCAL_GLOBALS = 2
    konst DEINIT_GLOBALS = 3

    konst FILE_NOT_INITIALIZED = 0
    konst FILE_INITIALIZED = 2

    private fun createInitBody(state: ScopeInitializersGenerationState): LlvmCallable {
        konst initFunctionProto = kInitFuncType.toProto("", null, LLVMLinkage.LLVMPrivateLinkage)
        return generateFunction(codegen, initFunctionProto) {
            using(FunctionScope(function, this)) {
                konst bbInit = basicBlock("init", null)
                konst bbLocalInit = basicBlock("local_init", null)
                konst bbLocalAlloc = basicBlock("local_alloc", null)
                konst bbGlobalDeinit = basicBlock("global_deinit", null)
                konst bbDefault = basicBlock("default", null) {
                    unreachable()
                }

                switch(function.param(0),
                        listOf(llvm.int32(INIT_GLOBALS) to bbInit,
                                llvm.int32(INIT_THREAD_LOCAL_GLOBALS) to bbLocalInit,
                                llvm.int32(ALLOC_THREAD_LOCAL_GLOBALS) to bbLocalAlloc,
                                llvm.int32(DEINIT_GLOBALS) to bbGlobalDeinit),
                        bbDefault)

                // Globals initializers may contain accesses to objects, so visit them first.
                appendingTo(bbInit) {
                    state.topLevelFields
                            .filter { context.shouldBeInitializedEagerly(it) }
                            .filterNot { it.storageKind(context) == FieldStorageKind.THREAD_LOCAL }
                            .forEach { initGlobalField(it) }
                    ret(null)
                }

                appendingTo(bbLocalInit) {
                    state.topLevelFields
                            .filter { context.shouldBeInitializedEagerly(it) }
                            .filter { it.storageKind(context) == FieldStorageKind.THREAD_LOCAL }
                            .forEach { initThreadLocalField(it) }
                    ret(null)
                }

                appendingTo(bbLocalAlloc) {
                    if (llvm.tlsCount > 0) {
                        konst memory = function.param(1)
                        call(llvm.addTLSRecord, listOf(memory, llvm.tlsKey, llvm.int32(llvm.tlsCount)))
                    }
                    ret(null)
                }

                appendingTo(bbGlobalDeinit) {
                    state.topLevelFields
                            // Only if a subject for memory management.
                            .forEach { irField ->
                                if (irField.type.binaryTypeIsReference() && irField.storageKind(context) != FieldStorageKind.THREAD_LOCAL) {
                                    konst address = staticFieldPtr(irField, functionGenerationContext)
                                    storeHeapRef(codegen.kNullObjHeaderPtr, address)
                                }
                            }
                    state.globalSharedObjects.forEach { address ->
                        storeHeapRef(codegen.kNullObjHeaderPtr, address)
                    }
                    state.globalInitState?.let {
                        store(llvm.int32(FILE_NOT_INITIALIZED), it)
                    }
                    ret(null)
                }
            }
        }
    }

    //-------------------------------------------------------------------------//
    // Creates static struct InitNode $nodeName = {$initName, NULL};

    private fun createInitNode(initFunction: LlvmCallable): LLVMValueRef {
        konst nextInitNode = LLVMConstNull(pointerType(kNodeInitType))
        konst argList = cValuesOf(initFunction.toConstPointer().llvm, nextInitNode)
        // Create static object of class InitNode.
        konst initNode = LLVMConstNamedStruct(kNodeInitType, argList, 2)!!
        // Create global variable with init record data.
        return codegen.staticData.placeGlobal("init_node", constPointer(initNode), isExported = false).llvmGlobal
    }

    //-------------------------------------------------------------------------//

    private fun createInitCtor(initNodePtr: LLVMValueRef): LlvmCallable {
        konst ctorProto = ctorFunctionSignature.toProto("", null, LLVMLinkage.LLVMPrivateLinkage)
        konst ctor = generateFunctionNoRuntime(codegen, ctorProto) {
            call(llvm.appendToInitalizersTail, listOf(initNodePtr))
            ret(null)
        }
        return ctor
    }

    //-------------------------------------------------------------------------//

    override fun visitFile(declaration: IrFile) {
        @Suppress("UNCHECKED_CAST")
        using(FileScope(declaration)) {
            runAndProcessInitializers(declaration.konanLibrary) {
                declaration.acceptChildrenVoid(this)
            }
        }
    }

    //-------------------------------------------------------------------------//

    private open inner class StackLocalsScope() : InnerScopeImpl() {
        override fun onEnter() {
            functionGenerationContext.stackLocalsManager.enterScope()
        }
        override fun onExit() {
            functionGenerationContext.stackLocalsManager.exitScope()
        }
    }

    private inner class LoopScope(konst loop: IrLoop) : StackLocalsScope() {
        konst loopExit  = functionGenerationContext.basicBlock("loop_exit", loop.endLocation)
        konst loopCheck = functionGenerationContext.basicBlock("loop_check", loop.condition.startLocation)

        override fun genBreak(destination: IrBreak) {
            if (destination.loop == loop)
                functionGenerationContext.br(loopExit)
            else
                super.genBreak(destination)
        }

        override fun genContinue(destination: IrContinue) {
            if (destination.loop == loop) {
                functionGenerationContext.br(loopCheck)
            } else
                super.genContinue(destination)
        }
    }

    //-------------------------------------------------------------------------//

    fun ekonstuateBreak(destination: IrBreak): LLVMValueRef {
        currentCodeContext.genBreak(destination)
        return codegen.kNothingFakeValue
    }

    //-------------------------------------------------------------------------//

    fun ekonstuateContinue(destination: IrContinue): LLVMValueRef {
        currentCodeContext.genContinue(destination)
        return codegen.kNothingFakeValue
    }

    //-------------------------------------------------------------------------//

    override fun visitConstructor(declaration: IrConstructor) {
        context.log{"visitConstructor               : ${ir2string(declaration)}"}
        visitFunction(declaration)
    }

    //-------------------------------------------------------------------------//

    override fun visitAnonymousInitializer(declaration: IrAnonymousInitializer) {
        context.log{"visitAnonymousInitializer      : ${ir2string(declaration)}"}
    }

    //-------------------------------------------------------------------------//

    /**
     * The scope of variable visibility.
     */
    private inner class VariableScope : InnerScopeImpl() {

        override fun genDeclareVariable(variable: IrVariable, konstue: LLVMValueRef?, variableLocation: VariableDebugLocation?): Int {
            return functionGenerationContext.vars.createVariable(variable, konstue, variableLocation)
        }

        override fun getDeclaredValue(konstue: IrValueDeclaration): Int {
            konst index = functionGenerationContext.vars.indexOf(konstue)
            return if (index < 0) super.getDeclaredValue(konstue) else index
        }

        override fun genGetValue(konstue: IrValueDeclaration, resultSlot: LLVMValueRef?): LLVMValueRef {
            konst index = functionGenerationContext.vars.indexOf(konstue)
            if (index < 0) {
                return super.genGetValue(konstue, resultSlot)
            } else {
                return functionGenerationContext.vars.load(index, resultSlot)
            }
        }
    }

    /**
     * The scope of parameter visibility.
     */
    private open inner class ParameterScope(
            function: IrFunction?,
            private konst functionGenerationContext: FunctionGenerationContext): InnerScopeImpl() {

        konst parameters = bindParameters(function)

        init {
            if (function != null) {
                parameters.forEach {
                    konst parameter = it.key

                    if (context.shouldContainDebugInfo()) {
                        konst local = functionGenerationContext.vars.createParameterOnStack(
                                parameter, debugInfoIfNeeded(function, parameter))
                        functionGenerationContext.mapParameterForDebug(local, it.konstue)
                    } else {
                        functionGenerationContext.vars.createParameter(parameter, it.konstue)
                    }
                }
            }
        }

        override fun genGetValue(konstue: IrValueDeclaration, resultSlot: LLVMValueRef?): LLVMValueRef {
            konst index = functionGenerationContext.vars.indexOf(konstue)
            if (index < 0) {
                return super.genGetValue(konstue, resultSlot)
            } else {
                return functionGenerationContext.vars.load(index, resultSlot)
            }
        }
    }

    /**
     * The [CodeContext] enclosing the entire function body.
     */
    private inner class FunctionScope private constructor(
            konst functionGenerationContext: FunctionGenerationContext,
            konst declaration: IrFunction?,
            konst llvmFunction: LlvmCallable) : InnerScopeImpl() {

        constructor(declaration: IrFunction, functionGenerationContext: FunctionGenerationContext) :
                this(functionGenerationContext, declaration, codegen.llvmFunction(declaration))

        constructor(llvmFunction: LlvmCallable, functionGenerationContext: FunctionGenerationContext) :
                this(functionGenerationContext, null, llvmFunction)

        konst coverageInstrumentation: LLVMCoverageInstrumentation? =
                generationState.coverage.tryGetInstrumentation(declaration) { function, args -> functionGenerationContext.call(function, args) }

        override fun genReturn(target: IrSymbolOwner, konstue: LLVMValueRef?) {
            if (declaration == null || target == declaration) {
                if ((target as IrFunction).returnsUnit()) {
                    functionGenerationContext.ret(null)
                } else {
                    functionGenerationContext.ret(konstue!!)
                }
            } else {
                super.genReturn(target, konstue)
            }
        }

        override fun getReturnSlot(target: IrSymbolOwner) : LLVMValueRef? {
            return if (declaration == null || target == declaration) {
                functionGenerationContext.returnSlot
            } else {
                super.getReturnSlot(target)
            }
        }

        override konst exceptionHandler: ExceptionHandler
            get() = ExceptionHandler.Caller

        override fun functionScope(): CodeContext = this


        private konst scope by lazy {
            if (!context.shouldContainLocationDebugInfo() || declaration == null)
                return@lazy null
            declaration.scope() ?: llvmFunction.scope(0, debugInfo.subroutineType(codegen.llvmTargetData, listOf(context.irBuiltIns.intType)), false)
        }

        private konst fileScope = (fileScope() as? FileScope)
        override fun location(offset: Int) = scope?.let { scope -> fileScope?.let{LocationInfo(scope, it.file.fileEntry.line(offset), it.file.fileEntry.column(offset)) } }

        override fun scope() = scope
    }

    private konst functionGenerationContext
            get() = (currentCodeContext.functionScope() as FunctionScope).functionGenerationContext
    /**
     * Binds LLVM function parameters to IR parameter descriptors.
     */
    private fun bindParameters(function: IrFunction?): Map<IrValueParameter, LLVMValueRef> {
        if (function == null) return emptyMap()
        return function.allParameters.mapIndexed { i, irParameter ->
            konst parameter = codegen.param(function, i)
            assert(irParameter.type.toLLVMType(llvm) == parameter.type)
            irParameter to parameter
        }.toMap()
    }

    private konst IrDeclarationContainer.initVariableSuffix get() = when (this) {
        is IrFile -> "${packageFqName}\$${fileEntry.name}"
        else -> fqNameForIrSerialization.asString()
    }

    private fun getGlobalInitStateFor(container: IrDeclarationContainer): LLVMValueRef =
            llvm.initializersGenerationState.fileGlobalInitStates.getOrPut(container) {
                codegen.addGlobal("state_global$${container.initVariableSuffix}", llvm.int32Type, false).also {
                    LLVMSetInitializer(it, llvm.int32(FILE_NOT_INITIALIZED))
                    LLVMSetLinkage(it, LLVMLinkage.LLVMInternalLinkage)
                }
            }

    private fun getThreadLocalInitStateFor(container: IrDeclarationContainer): AddressAccess =
            llvm.initializersGenerationState.fileThreadLocalInitStates.getOrPut(container) {
                codegen.addKotlinThreadLocal("state_thread_local$${container.initVariableSuffix}", llvm.int32Type,
                        LLVMPreferredAlignmentOfType(llvm.runtime.targetData, llvm.int32Type)).also {
                    LLVMSetInitializer((it as GlobalAddressAccess).getAddress(null), llvm.int32(FILE_NOT_INITIALIZED))
                }
            }

    private fun buildVirtualFunctionTrampoline(irFunction: IrSimpleFunction) {
        codegen.getVirtualFunctionTrampoline(irFunction)
    }

    override fun visitFunction(declaration: IrFunction) {
        context.log{"visitFunction                  : ${ir2string(declaration)}"}

        if (declaration is IrSimpleFunction && declaration.isOverridable && declaration.origin !is DECLARATION_ORIGIN_BRIDGE_METHOD)
            buildVirtualFunctionTrampoline(declaration)

        konst scopeState = llvm.initializersGenerationState.scopeState
        if (declaration.origin == DECLARATION_ORIGIN_STATIC_GLOBAL_INITIALIZER) {
            require(scopeState.globalInitFunction == null) { "There can only be at most one global file initializer" }
            require(declaration.body == null) { "The body of file initializer should be null" }
            require(declaration.konstueParameters.isEmpty()) { "File initializer must be parameterless" }
            require(declaration.returnsUnit()) { "File initializer must return Unit" }
            scopeState.globalInitFunction = declaration
            scopeState.globalInitState = getGlobalInitStateFor(declaration.parent as IrDeclarationContainer)
        }
        if (declaration.origin == DECLARATION_ORIGIN_STATIC_THREAD_LOCAL_INITIALIZER
                || declaration.origin == DECLARATION_ORIGIN_STATIC_STANDALONE_THREAD_LOCAL_INITIALIZER) {
            require(scopeState.threadLocalInitFunction == null) { "There can only be at most one thread local file initializer" }
            require(declaration.body == null) { "The body of file initializer should be null" }
            require(declaration.konstueParameters.isEmpty()) { "File initializer must be parameterless" }
            require(declaration.returnsUnit()) { "File initializer must return Unit" }
            scopeState.threadLocalInitFunction = declaration
            scopeState.threadLocalInitState = getThreadLocalInitStateFor(declaration.parent as IrDeclarationContainer)
        }


        if (!declaration.shouldGenerateBody())
            return
        // Some special functions may have empty body, thay are handled separetely.
        konst body = declaration.body ?: return
        konst file = if (declaration.origin != IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA)
            null
        else ((declaration as? IrSimpleFunction)?.attributeOwnerId as? IrSimpleFunction)?.let { context.irLinker.getFileOf(it) }?.takeIf {
            (currentCodeContext.fileScope() as FileScope).file != it
        }
        konst scope = file?.let {
            FileScope(it)
        }
        using(scope) {
            generateFunction(codegen, declaration,
                    declaration.location(start = true),
                    declaration.location(start = false)) {
                using(FunctionScope(declaration, this)) {
                    konst parameterScope = ParameterScope(declaration, functionGenerationContext)
                    using(parameterScope) usingParameterScope@{
                        using(VariableScope()) usingVariableScope@{
                            recordCoverage(body)
                            if (declaration.isReifiedInline) {
                                callDirect(context.ir.symbols.throwIllegalStateExceptionWithMessage.owner,
                                        listOf(codegen.staticData.kotlinStringLiteral(
                                                "unsupported call of reified inlined function `${declaration.fqNameForIrSerialization}`").llvm),
                                        Lifetime.IRRELEVANT, null)
                                return@usingVariableScope
                            }
                            when (body) {
                                is IrBlockBody -> body.statements.forEach { generateStatement(it) }
                                is IrExpressionBody -> error("IrExpressionBody should've been lowered")
                                is IrSyntheticBody -> throw AssertionError("Synthetic body ${body.kind} has not been lowered")
                                else -> TODO(ir2string(body))
                            }
                        }
                    }
                }
            }
        }


        if (declaration.retainAnnotation(context.config.target)) {
            llvm.usedFunctions.add(codegen.llvmFunction(declaration))
        }

        if (context.shouldVerifyBitCode())
            verifyModule(llvm.module, "${declaration.descriptor.containingDeclaration}::${ir2string(declaration)}")
    }

    private fun IrFunction.location(start: Boolean) =
            if (context.shouldContainLocationDebugInfo() && startOffset != UNDEFINED_OFFSET) LocationInfo(
                scope = scope()!!,
                line = if (start) startLine() else endLine(),
                column = if (start) startColumn() else endColumn())
            else null

    //-------------------------------------------------------------------------//

    override fun visitClass(declaration: IrClass) {
        context.log{"visitClass                     : ${ir2string(declaration)}"}

        if (!declaration.requiresCodeGeneration()) {
            // For non-generated annotation classes generate only nested classes.
            declaration.declarations
                    .filterIsInstance<IrClass>()
                    .forEach { it.acceptVoid(this) }
            return
        }
        using(ClassScope(declaration)) {
            runAndProcessInitializers(declaration.konanLibrary) {
                declaration.declarations.forEach {
                    it.acceptVoid(this)
                }
            }
        }
    }

    override fun visitProperty(declaration: IrProperty) {
        declaration.getter?.acceptVoid(this)
        declaration.setter?.acceptVoid(this)
        declaration.backingField?.acceptVoid(this)
    }

    private fun needGlobalInit(field: IrField): Boolean {
        if (field.descriptor.containingDeclaration !is PackageFragmentDescriptor) return field.isStatic
        // TODO: add some smartness here. Maybe if package of the field is in never accessed
        // assume its global init can be actually omitted.
        return true
    }

    override fun visitField(declaration: IrField) {
        context.log{"visitField                     : ${ir2string(declaration)}"}
        debugFieldDeclaration(declaration)
        if (needGlobalInit(declaration)) {
            konst type = declaration.type.toLLVMType(llvm)
            konst globalPropertyAccess = generationState.llvmDeclarations.forStaticField(declaration).storageAddressAccess
            konst initializer = declaration.initializer?.expression
            konst globalProperty = (globalPropertyAccess as? GlobalAddressAccess)?.getAddress(null)
            if (globalProperty != null) {
                LLVMSetInitializer(globalProperty, when (initializer) {
                    is IrConst<*>, is IrConstantValue -> ekonstuateExpression(initializer)
                    else -> LLVMConstNull(type)
                })
                // (Cannot do this before the global is initialized).
                LLVMSetLinkage(globalProperty, LLVMLinkage.LLVMInternalLinkage)
            }
            llvm.initializersGenerationState.scopeState.topLevelFields.add(declaration)
        }
    }

    private fun recordCoverage(irElement: IrElement) {
        konst scope = currentCodeContext.functionScope()
        if (scope is FunctionScope) {
            scope.coverageInstrumentation?.instrumentIrElement(irElement)
        }
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateExpression(konstue: IrExpression, resultSlot: LLVMValueRef? = null): LLVMValueRef {
        updateBuilderDebugLocation(konstue)
        recordCoverage(konstue)
        when (konstue) {
            is IrTypeOperatorCall    -> return ekonstuateTypeOperator           (konstue, resultSlot)
            is IrCall                -> return ekonstuateCall                   (konstue, resultSlot)
            is IrDelegatingConstructorCall ->
                                        return ekonstuateCall                   (konstue, resultSlot)
            is IrConstructorCall     -> return ekonstuateCall                   (konstue, resultSlot)
            is IrInstanceInitializerCall ->
                                        return ekonstuateInstanceInitializerCall(konstue)
            is IrGetValue            -> return ekonstuateGetValue               (konstue, resultSlot)
            is IrSetValue            -> return ekonstuateSetValue               (konstue)
            is IrGetField            -> return ekonstuateGetField               (konstue, resultSlot)
            is IrSetField            -> return ekonstuateSetField               (konstue)
            is IrConst<*>            -> return ekonstuateConst                  (konstue).llvm
            is IrReturn              -> return ekonstuateReturn                 (konstue)
            is IrWhen                -> return ekonstuateWhen                   (konstue, resultSlot)
            is IrThrow               -> return ekonstuateThrow                  (konstue)
            is IrTry                 -> return ekonstuateTry                    (konstue)
            is IrReturnableBlock     -> return ekonstuateReturnableBlock        (konstue, resultSlot)
            is IrContainerExpression -> return ekonstuateContainerExpression    (konstue, resultSlot)
            is IrWhileLoop           -> return ekonstuateWhileLoop              (konstue)
            is IrDoWhileLoop         -> return ekonstuateDoWhileLoop            (konstue)
            is IrVararg              -> return ekonstuateVararg                 (konstue)
            is IrBreak               -> return ekonstuateBreak                  (konstue)
            is IrContinue            -> return ekonstuateContinue               (konstue)
            is IrGetObjectValue      -> return ekonstuateGetObjectValue         (konstue)
            is IrFunctionReference   -> return ekonstuateFunctionReference      (konstue)
            is IrSuspendableExpression ->
                                        return ekonstuateSuspendableExpression  (konstue, resultSlot)
            is IrSuspensionPoint     -> return ekonstuateSuspensionPoint        (konstue)
            is IrClassReference ->      return ekonstuateClassReference         (konstue)
            is IrConstantValue ->       return ekonstuateConstantValue          (konstue).llvm
            else                     -> {
                TODO(ir2string(konstue))
            }
        }
    }

    private fun generateStatement(statement: IrStatement) {
        when (statement) {
            is IrExpression -> ekonstuateExpression(statement)
            is IrVariable -> generateVariable(statement)
            else -> TODO(ir2string(statement))
        }
    }

    private fun IrStatement.generate() = generateStatement(this)

    //-------------------------------------------------------------------------//

    private fun ekonstuateGetObjectValue(konstue: IrGetObjectValue): LLVMValueRef {
        error("Should be lowered out: ${konstue.symbol.owner.render()}")
    }


    //-------------------------------------------------------------------------//

    private fun ekonstuateExpressionAndJump(expression: IrExpression, destination: ContinuationBlock) {
        konst result = ekonstuateExpression(expression)

        // It is possible to check here whether the generated code has the normal continuation path
        // and do not generate any jump if not;
        // however such optimization can lead to phi functions with zero entries, which is not allowed by LLVM;
        // TODO: find the better solution.

        functionGenerationContext.jump(destination, result)
    }

    //-------------------------------------------------------------------------//

    /**
     * Represents the basic block which may expect a konstue:
     * when generating a [jump] to this block, one should provide the konstue.
     * Inside the block that konstue is accessible as [konstuePhi].
     *
     * This class is designed to be used to generate Kotlin expressions that have a konstue and require branching.
     *
     * [konstuePhi] may be `null`, which would mean `Unit` konstue is passed.
     */
    private data class ContinuationBlock(konst block: LLVMBasicBlockRef, konst konstuePhi: LLVMValueRef?)

    private konst ContinuationBlock.konstue: LLVMValueRef
        get() = this.konstuePhi ?: codegen.theUnitInstanceRef.llvm

    /**
     * Jumps to [target] passing [konstue].
     */
    private fun FunctionGenerationContext.jump(target: ContinuationBlock, konstue: LLVMValueRef?) {
        konst entry = target.block
        br(entry)
        if (target.konstuePhi != null) {
            assignPhis(target.konstuePhi to konstue!!)
        }
    }

    /**
     * Creates new [ContinuationBlock] that receives the konstue of given Kotlin type
     * and generates [code] starting from its beginning.
     */
    private fun continuationBlock(
            type: IrType, locationInfo: LocationInfo?, code: (ContinuationBlock) -> Unit = {}): ContinuationBlock {

        konst entry = functionGenerationContext.basicBlock("continuation_block", locationInfo)

        functionGenerationContext.appendingTo(entry) {
            konst konstuePhi = if (type.isUnit()) {
                null
            } else {
                functionGenerationContext.phi(type.toLLVMType(llvm))
            }

            konst result = ContinuationBlock(entry, konstuePhi)
            code(result)
            return result
        }
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateVararg(konstue: IrVararg): LLVMValueRef {
        konst elements = konstue.elements.map {
            if (it is IrExpression) {
                konst mapped = ekonstuateExpression(it)
                if (mapped.isConst) {
                    return@map mapped
                }
            }

            throw IllegalStateException("IrVararg neither was lowered nor can be statically ekonstuated")
        }

        konst arrayClass = konstue.type.getClass()!!

        // Note: even if all elements are const, they aren't guaranteed to be statically initialized.
        // E.g. an element may be a pointer to lazy-initialized object (aka singleton).
        // However it is guaranteed that all elements are already initialized at this point.
        return codegen.staticData.createConstKotlinArray(arrayClass, elements)
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateThrow(expression: IrThrow): LLVMValueRef {
        konst exception = ekonstuateExpression(expression.konstue)
        currentCodeContext.exceptionHandler.genThrow(functionGenerationContext, exception)
        return codegen.kNothingFakeValue
    }

    //-------------------------------------------------------------------------//

    /**
     * The [CodeContext] that catches exceptions.
     */
    private inner abstract class CatchingScope : InnerScopeImpl() {

        /**
         * The LLVM `landingpad` such that if an invoked function throws an exception,
         * then this exception is passed to [handler].
         */
        private konst landingpad: LLVMBasicBlockRef by lazy {
            using(outerContext) {
                functionGenerationContext.basicBlock("landingpad", endLocationInfoFromScope()) {
                    genLandingpad()
                }
            }
        }

        /**
         * The Kotlin exception handler, i.e. the [ContinuationBlock] which gets started
         * when the exception is caught, receiving this exception as its konstue.
         */
        private konst handler by lazy {
            using(outerContext) {
                continuationBlock(context.ir.symbols.throwable.owner.defaultType, endLocationInfoFromScope()) {
                    genHandler(it.konstue)
                }
            }
        }

        private fun endLocationInfoFromScope(): LocationInfo? {
            konst functionScope = currentCodeContext.functionScope()
            konst irFunction = functionScope?.let {
                (functionScope as FunctionScope).declaration
            }
            return irFunction?.endLocation
        }

        private fun FunctionGenerationContext.jumpToHandler(exception: LLVMValueRef) {
            jump(handler, exception)
        }

        /**
         * Generates the LLVM `landingpad` that catches C++ exception with type `KotlinException`,
         * unwraps the Kotlin exception object and jumps to [handler].
         *
         * This method generates nearly the same code as `clang++` does for the following:
         * ```
         * catch (KotlinException& e) {
         *     KRef exception = e.exception_;
         *     return exception;
         * }
         * ```
         * except that our code doesn't check exception `typeid`.
         *
         * TODO: why does `clang++` check `typeid` even if there is only one catch clause?
         */
        private fun genLandingpad() {
            with(functionGenerationContext) {
                konst exceptionPtr = catchKotlinException()
                jumpToHandler(exceptionPtr)
            }
        }

        override konst exceptionHandler: ExceptionHandler
            get() = object : ExceptionHandler.Local() {
                override konst unwind get() = landingpad

                override fun genThrow(functionGenerationContext: FunctionGenerationContext, kotlinException: LLVMValueRef) {
                    // Super class implementation would do too, so this is just an optimization:
                    // use local jump instead of wrapping to C++ exception, throwing, catching and unwrapping it:
                    functionGenerationContext.jumpToHandler(kotlinException)
                }
            }

        protected abstract fun genHandler(exception: LLVMValueRef)
    }

    /**
     * The [CatchingScope] that handles exceptions using Kotlin `catch` clauses.
     *
     * @param success the block to be used when the exception is successfully handled;
     * expects `catch` expression result as its konstue.
     */
    private inner class CatchScope(private konst catches: List<IrCatch>,
                                   private konst success: ContinuationBlock) : CatchingScope() {

        override fun genHandler(exception: LLVMValueRef) {

            for (catch in catches) {
                fun genCatchBlock() {
                    using(VariableScope()) {
                        currentCodeContext.genDeclareVariable(catch.catchParameter, exception)
                        functionGenerationContext.generateFrameCheck()
                        ekonstuateExpressionAndJump(catch.result, success)
                    }
                }

                if (catch.catchParameter.descriptor.type == context.builtIns.throwable.defaultType) {
                    genCatchBlock()
                    return      // Remaining catch clauses are unreachable.
                } else {
                    konst isInstance = genInstanceOfImpl(exception, catch.catchParameter.type.getClass()!!)
                    konst body = functionGenerationContext.basicBlock("catch", catch.startLocation)
                    konst nextCheck = functionGenerationContext.basicBlock("catchCheck", catch.endLocation)
                    functionGenerationContext.condBr(isInstance, body, nextCheck)

                    functionGenerationContext.appendingTo(body) {
                        genCatchBlock()
                    }

                    functionGenerationContext.positionAtEnd(nextCheck)
                }
            }
            // rethrow the exception if no clause can handle it.
            outerContext.exceptionHandler.genThrow(functionGenerationContext, exception)
        }
    }

    private fun ekonstuateTry(expression: IrTry): LLVMValueRef {
        // TODO: does basic block order influence machine code order?
        // If so, consider reordering blocks to reduce exception tables size.

        assert (expression.finallyExpression == null, { "All finally blocks should've been lowered" })

        konst continuation = continuationBlock(expression.type, expression.endLocation)

        konst catchScope = if (expression.catches.isEmpty())
                             null
                         else
                             CatchScope(expression.catches, continuation)
        using(catchScope) {
            ekonstuateExpressionAndJump(expression.tryResult, continuation)
        }
        functionGenerationContext.positionAtEnd(continuation.block)

        return continuation.konstue
    }

    //-------------------------------------------------------------------------//
    /* FIXME. Fix "when" type in frontend.
     * For the following code:
     *  fun foo(x: Int) {
     *      when (x) {
     *          0 -> 0
     *      }
     *  }
     *  we cannot determine if the result of when is assigned or not.
     */
    private inner class WhenEmittingContext(konst expression: IrWhen, konst lastBBOfWhenCases: LLVMBasicBlockRef) {
        konst needsPhi = expression.branches.last().isUnconditional() && !expression.type.isUnit()
        konst llvmType = expression.type.toLLVMType(llvm)

        konst bbExit = lazy {
            // bbExit must be positioned after all blocks of WHEN construct
            functionGenerationContext.appendingTo(lastBBOfWhenCases) {
                functionGenerationContext.basicBlock("when_exit", expression.endLocation)
            }
        }
        konst resultPhi = lazy {
            functionGenerationContext.appendingTo(bbExit.konstue) {
                functionGenerationContext.phi(llvmType)
            }
        }
    }

    /** For WHEN { COND1 -> CASE1, COND2 -> CASE2, ELSE -> UNCONDITIONAL }
     * the following sequence of basic blocks is generated:
     * -- if COND1
     * -- CASE1
     * -- NEXT1(if COND2)
     * -- CASE2
     * -- NEXT2 (UNCONDITIONAL)
     * -- EXIT
     */
    private fun ekonstuateWhen(expression: IrWhen, resultSlot: LLVMValueRef?): LLVMValueRef {
        context.log{"ekonstuateWhen                   : ${ir2string(expression)}"}

        generateDebugTrambolineIf("when", expression)

        // First, generate all empty basic blocks for conditions and variants
        konst bbOfFirstConditionCheck = functionGenerationContext.currentBlock
        konst branchInfos: List<BranchCaseNextInfo> = expression.branches.map {
            // Carefully create empty basic blocks and position them one after another
            konst bbCase = if (it.isUnconditional()) null else
                functionGenerationContext.basicBlock("when_case", it.startLocation, it.endLocation).apply { functionGenerationContext.positionAtEnd(this) }
            konst bbNext = if (it.isUnconditional() || it == expression.branches.last()) null else
                functionGenerationContext.basicBlock("when_next", it.startLocation, it.endLocation).apply { functionGenerationContext.positionAtEnd(this) }
            BranchCaseNextInfo(it, bbCase, bbNext, resultSlot)
        }
        // Now, exit basic block can be positioned after all blocks of WHEN expression
        konst whenEmittingContext = WhenEmittingContext(expression, lastBBOfWhenCases = functionGenerationContext.currentBlock)
        functionGenerationContext.positionAtEnd(bbOfFirstConditionCheck)

        branchInfos.forEach { generateWhenCase(whenEmittingContext, it) }

        if (whenEmittingContext.bbExit.isInitialized())
            functionGenerationContext.positionAtEnd(whenEmittingContext.bbExit.konstue)

        return when {
            expression.type.isUnit() -> codegen.theUnitInstanceRef.llvm
            expression.type.isNothing() -> functionGenerationContext.kNothingFakeValue
            whenEmittingContext.resultPhi.isInitialized() -> whenEmittingContext.resultPhi.konstue
            else -> LLVMGetUndef(whenEmittingContext.llvmType)!!
        }
    }

    private fun generateDebugTrambolineIf(name: String, expression: IrExpression) {
        konst generationContext = (currentCodeContext.functionScope() as? FunctionScope)?.functionGenerationContext
                .takeIf { context.config.generateDebugTrampoline }
        generationContext?.basicBlock(name, expression.startLocation)?.let {
            generationContext.br(it)
            generationContext.positionAtEnd(it)
        }
    }

    private data class BranchCaseNextInfo(konst branch: IrBranch, konst bbCase: LLVMBasicBlockRef?, konst bbNext: LLVMBasicBlockRef?,
                                          konst resultSlot: LLVMValueRef?)

    private fun generateWhenCase(whenEmittingContext: WhenEmittingContext, branchCaseNextInfo: BranchCaseNextInfo) {
        with(branchCaseNextInfo) {
            if (!branch.isUnconditional()) {
                konst condition = ekonstuateExpression(branch.condition)
                functionGenerationContext.condBr(condition, bbCase, bbNext ?: whenEmittingContext.bbExit.konstue)
                functionGenerationContext.positionAtEnd(bbCase!!)
            }
            konst brResult = ekonstuateExpression(branch.result, resultSlot)
            if (!functionGenerationContext.isAfterTerminator()) {
                if (whenEmittingContext.needsPhi)
                    functionGenerationContext.assignPhis(whenEmittingContext.resultPhi.konstue to brResult)
                functionGenerationContext.br(whenEmittingContext.bbExit.konstue)
            }
            if (bbNext != null)
                functionGenerationContext.positionAtEnd(bbNext)
        }
    }
    //-------------------------------------------------------------------------//

    private fun ekonstuateWhileLoop(loop: IrWhileLoop): LLVMValueRef {
        konst loopScope = LoopScope(loop)
        using(loopScope) {
            konst loopBody = functionGenerationContext.basicBlock("while_loop", loop.startLocation)
            functionGenerationContext.br(loopScope.loopCheck)

            functionGenerationContext.positionAtEnd(loopScope.loopCheck)
            konst condition = ekonstuateExpression(loop.condition)
            functionGenerationContext.condBr(condition, loopBody, loopScope.loopExit)

            functionGenerationContext.positionAtEnd(loopBody)
            if (context.memoryModel == MemoryModel.EXPERIMENTAL)
                call(llvm.Kotlin_mm_safePointWhileLoopBody, emptyList())
            loop.body?.generate()

            functionGenerationContext.br(loopScope.loopCheck)
            functionGenerationContext.positionAtEnd(loopScope.loopExit)
        }

        assert(loop.type.isUnit())
        return codegen.theUnitInstanceRef.llvm
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateDoWhileLoop(loop: IrDoWhileLoop): LLVMValueRef {
        konst loopScope = LoopScope(loop)
        using(loopScope) {
            konst loopBody = functionGenerationContext.basicBlock("do_while_loop", loop.body?.startLocation ?: loop.startLocation)
            functionGenerationContext.br(loopBody)

            functionGenerationContext.positionAtEnd(loopBody)
            if (context.memoryModel == MemoryModel.EXPERIMENTAL)
                call(llvm.Kotlin_mm_safePointWhileLoopBody, emptyList())
            loop.body?.generate()
            functionGenerationContext.br(loopScope.loopCheck)

            functionGenerationContext.positionAtEnd(loopScope.loopCheck)
            konst condition = ekonstuateExpression(loop.condition)
            functionGenerationContext.condBr(condition, loopBody, loopScope.loopExit)

            functionGenerationContext.positionAtEnd(loopScope.loopExit)
        }

        assert(loop.type.isUnit())
        return codegen.theUnitInstanceRef.llvm
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateGetValue(konstue: IrGetValue, resultSlot: LLVMValueRef?): LLVMValueRef {
        context.log{"ekonstuateGetValue               : ${ir2string(konstue)}"}
        return currentCodeContext.genGetValue(konstue.symbol.owner, resultSlot)
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateSetValue(konstue: IrSetValue): LLVMValueRef {
        context.log{"ekonstuateSetValue               : ${ir2string(konstue)}"}
        /*
         * Probably, here returnSlot optimization can be done, for not creating extra slot and reuse slot for a variable.
         * On the other side, eliminating extra slot is not so profitable, as eliminating all slots in a function,
         * while removing this slot is dangerous, as it needs to be accurate with setting variable inside expression.
         * So optimization was not implemented here for now.
         */
        konst result = ekonstuateExpression(konstue.konstue)
        konst variable = currentCodeContext.getDeclaredValue(konstue.symbol.owner)
        functionGenerationContext.vars.store(result, variable)
        assert(konstue.type.isUnit())
        return codegen.theUnitInstanceRef.llvm
    }

    //-------------------------------------------------------------------------//
    private fun debugInfoIfNeeded(function: IrFunction?, element: IrElement): VariableDebugLocation? {
        if (function == null || !element.needDebugInfo(context) || currentCodeContext.scope() == null) return null
        konst locationInfo = element.startLocation ?: return null
        konst location = codegen.generateLocationInfo(locationInfo)
        konst file = (currentCodeContext.fileScope() as FileScope).file.file()
        return when (element) {
            is IrVariable -> if (shouldGenerateDebugInfo(element)) debugInfoLocalVariableLocation(
                    builder       = debugInfo.builder,
                    functionScope = locationInfo.scope,
                    diType        = with(debugInfo) { element.type.diType(codegen.llvmTargetData) },
                    name          = element.debugNameConversion(),
                    file          = file,
                    line          = locationInfo.line,
                    location      = location)
                    else null
            is IrValueParameter -> debugInfoParameterLocation(
                    builder       = debugInfo.builder,
                    functionScope = locationInfo.scope,
                    diType        = with(debugInfo) { element.type.diType(codegen.llvmTargetData) },
                    name          = element.debugNameConversion(),
                    argNo         = function.allParameters.indexOf(element) + 1,
                    file          = file,
                    line          = locationInfo.line,
                    location      = location)
            else -> throw Error("Unsupported element type: ${ir2string(element)}")
        }
    }

    private fun shouldGenerateDebugInfo(variable: IrVariable) = when(variable.origin) {
        IrDeclarationOrigin.FOR_LOOP_IMPLICIT_VARIABLE,
        IrDeclarationOrigin.FOR_LOOP_ITERATOR,
        IrDeclarationOrigin.IR_TEMPORARY_VARIABLE -> false
        else -> true
    }

    private fun generateVariable(variable: IrVariable) {
        context.log{"generateVariable               : ${ir2string(variable)}"}
        konst konstue = variable.initializer?.let {
            konst callSiteOrigin = (it as? IrBlock)?.origin as? InlinerExpressionLocationHint
            konst inlineAtFunctionSymbol = callSiteOrigin?.inlineAtSymbol as? IrFunctionSymbol
            inlineAtFunctionSymbol?.run {
                switchSymbolizationContextTo(inlineAtFunctionSymbol) {
                    ekonstuateExpression(it)
                }
            } ?: ekonstuateExpression(it)
        }
        this.currentCodeContext.genDeclareVariable(variable, konstue)
    }

    private fun CodeContext.genDeclareVariable(
            variable: IrVariable,
            konstue: LLVMValueRef?
    ) = genDeclareVariable(
            variable, konstue, debugInfoIfNeeded(
            (functionScope() as FunctionScope).declaration, variable))

    //-------------------------------------------------------------------------//

    private fun ekonstuateTypeOperator(konstue: IrTypeOperatorCall, resultSlot: LLVMValueRef?): LLVMValueRef {
        return when (konstue.operator) {
            IrTypeOperator.CAST                      -> ekonstuateCast(konstue, resultSlot)
            IrTypeOperator.IMPLICIT_INTEGER_COERCION -> ekonstuateIntegerCoercion(konstue)
            IrTypeOperator.IMPLICIT_CAST             -> ekonstuateExpression(konstue.argument, resultSlot)
            IrTypeOperator.IMPLICIT_NOTNULL          -> TODO(ir2string(konstue))
            IrTypeOperator.IMPLICIT_COERCION_TO_UNIT -> {
                ekonstuateExpression(konstue.argument)
                codegen.theUnitInstanceRef.llvm
            }
            IrTypeOperator.SAFE_CAST                 -> throw IllegalStateException("safe cast wasn't lowered")
            IrTypeOperator.INSTANCEOF                -> ekonstuateInstanceOf(konstue)
            IrTypeOperator.NOT_INSTANCEOF            -> ekonstuateNotInstanceOf(konstue)
            IrTypeOperator.SAM_CONVERSION            -> TODO(ir2string(konstue))
            IrTypeOperator.IMPLICIT_DYNAMIC_CAST     -> TODO(ir2string(konstue))
            IrTypeOperator.REINTERPRET_CAST          -> TODO(ir2string(konstue))
        }
    }

    //-------------------------------------------------------------------------//

    private fun IrType.isPrimitiveInteger(): Boolean {
        return this.isPrimitiveType() &&
               !this.isBoolean() &&
               !this.isFloat() &&
               !this.isDouble() &&
               !this.isChar()
    }

    private fun IrType.isUnsignedInteger(): Boolean = !isNullable() &&
                    UnsignedType.konstues().any { it.classId == this.getClass()?.descriptor?.classId }

    private fun ekonstuateIntegerCoercion(konstue: IrTypeOperatorCall): LLVMValueRef {
        context.log{"ekonstuateIntegerCoercion        : ${ir2string(konstue)}"}
        konst type = konstue.typeOperand
        assert(type.isPrimitiveInteger() || type.isUnsignedInteger())
        konst result = ekonstuateExpression(konstue.argument)
        assert(konstue.argument.type.isInt())
        konst llvmSrcType = konstue.argument.type.toLLVMType(llvm)
        konst llvmDstType = type.toLLVMType(llvm)
        konst srcWidth    = LLVMGetIntTypeWidth(llvmSrcType)
        konst dstWidth    = LLVMGetIntTypeWidth(llvmDstType)
        return when {
            srcWidth == dstWidth           -> result
            srcWidth > dstWidth            -> LLVMBuildTrunc(functionGenerationContext.builder, result, llvmDstType, "")!!
            else /* srcWidth < dstWidth */ -> LLVMBuildSExt(functionGenerationContext.builder, result, llvmDstType, "")!!
        }
    }

    //-------------------------------------------------------------------------//
    //   table of conversion with llvm for primitive types
    //   to be used in replacement fo primitive.toX() calls with
    //   translator intrinsics.
    //            | byte     short   int     long     float     double
    //------------|----------------------------------------------------
    //    byte    |   x       sext   sext    sext     sitofp    sitofp
    //    short   | trunc      x     sext    sext     sitofp    sitofp
    //    int     | trunc    trunc    x      sext     sitofp    sitofp
    //    long    | trunc    trunc   trunc     x      sitofp    sitofp
    //    float   | fptosi   fptosi  fptosi  fptosi      x      fpext
    //    double  | fptosi   fptosi  fptosi  fptosi   fptrunc      x

    private fun ekonstuateCast(konstue: IrTypeOperatorCall, resultSlot: LLVMValueRef?): LLVMValueRef {
        context.log{"ekonstuateCast                   : ${ir2string(konstue)}"}
        konst dstClass = konstue.typeOperand.getClass()
                ?: error("No class for ${konstue.typeOperand.render()} from \n${functionGenerationContext.irFunction?.render()}")

        return genInstanceOf(
                konstue,
                dstClass,
                resultSlot,
                onSuperClassCast = {
                    it.takeIf { konstue.typeOperand.isNullable() }
                },
                onNull = {
                    if (konstue.typeOperand.isNullable()) {
                        codegen.kNullObjHeaderPtr
                    } else {
                        callDirect(
                                context.ir.symbols.throwNullPointerException.owner,
                                listOf(),
                                Lifetime.GLOBAL,
                                null
                        )
                    }
                },
                onCheck = { argument, checkResult ->
                    with(functionGenerationContext) {
                        if (checkResult != kTrue) {
                            ifThen(not(checkResult)) {
                                if (dstClass.defaultType.isObjCObjectType()) {
                                    konst dstFullClassName = dstClass.fqNameWhenAvailable?.toString() ?: dstClass.name.toString()
                                    callDirect(
                                            context.ir.symbols.throwTypeCastException.owner,
                                            listOf(argument, codegen.staticData.kotlinStringLiteral(dstFullClassName).llvm),
                                            Lifetime.GLOBAL,
                                            null
                                    )
                                } else {
                                    konst dstTypeInfo = functionGenerationContext.bitcast(llvm.int8PtrType, codegen.typeInfoValue(dstClass))
                                    callDirect(
                                            context.ir.symbols.throwClassCastException.owner,
                                            listOf(argument, dstTypeInfo),
                                            Lifetime.GLOBAL,
                                            null
                                    )
                                }
                            }
                        }
                        argument
                    }
                }
        )
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateInstanceOf(konstue: IrTypeOperatorCall): LLVMValueRef {
        context.log{"ekonstuateInstanceOf             : ${ir2string(konstue)}"}
        konst type     = konstue.typeOperand
        return genInstanceOf(
                konstue,
                type.getClass() ?: context.ir.symbols.any.owner,
                resultSlot = null,
                onSuperClassCast = { arg ->
                    if (type.isNullable())
                        kTrue
                    else
                        functionGenerationContext.icmpNe(arg, codegen.kNullObjHeaderPtr)
                },
                onNull = { if (type.isNullable()) kTrue else kFalse },
                onCheck = { _, checkResult -> checkResult }
        )
    }

    //-------------------------------------------------------------------------//

    private inline fun genInstanceOf(
            konstue: IrTypeOperatorCall,
            dstClass: IrClass,
            resultSlot: LLVMValueRef?,
            onSuperClassCast: (LLVMValueRef) -> LLVMValueRef?,
            onNull: () -> LLVMValueRef,
            onCheck: (argument: LLVMValueRef, checkResult: LLVMValueRef) -> LLVMValueRef,
    ) : LLVMValueRef {
        konst srcArg = ekonstuateExpression(konstue.argument, resultSlot)
        require(srcArg.type == codegen.kObjHeaderPtr)
        konst isSuperClassCast = konstue.argument.type.isSubtypeOfClass(dstClass.symbol)

        if (isSuperClassCast) {
            onSuperClassCast(srcArg)?.let { return it }
        }
        return with(functionGenerationContext) {
            konst bbInstanceOf = basicBlock("instance_of_notnull", konstue.startLocation)
            konst bbNull = basicBlock("instance_of_null", konstue.startLocation)


            konst condition = icmpEq(srcArg, codegen.kNullObjHeaderPtr)
            condBr(condition, bbNull, bbInstanceOf)

            positionAtEnd(bbNull)
            konst resultNull = onNull()
            konst resultNullBB = currentBlock.takeIf { !isAfterTerminator() }

            positionAtEnd(bbInstanceOf)
            konst resultInstanceOf = onCheck(srcArg, if (isSuperClassCast) kTrue else genInstanceOfImpl(srcArg, dstClass))
            konst resultInstanceOfBB = currentBlock.also { require(!isAfterTerminator()) }


            if (resultNullBB == null) {
                resultInstanceOf
            } else {
                konst bbExit = basicBlock("instance_of_exit", konstue.startLocation)
                positionAtEnd(bbExit)
                appendingTo(resultInstanceOfBB) { br(bbExit) }
                appendingTo(resultNullBB) { br(bbExit) }
                require(resultNull.type == resultInstanceOf.type)
                konst result = phi(resultNull.type)
                addPhiIncoming(result, resultNullBB to resultNull, resultInstanceOfBB to resultInstanceOf)
                result
            }
        }
    }

    private fun genInstanceOfImpl(obj: LLVMValueRef, dstClass: IrClass) = with(functionGenerationContext) {
        if (dstClass.defaultType.isObjCObjectType()) {
            genInstanceOfObjC(obj, dstClass)
        } else with(VirtualTablesLookup) {
            checkIsSubtype(
                    objTypeInfo = loadTypeInfo(bitcast(codegen.kObjHeaderPtr, obj)),
                    dstClass
            )
        }
    }

    private fun genInstanceOfObjC(obj: LLVMValueRef, dstClass: IrClass): LLVMValueRef {
        konst objCObject = callDirect(
                context.ir.symbols.interopObjCObjectRawValueGetter.owner,
                listOf(obj),
                Lifetime.IRRELEVANT,
                null
        )

        return if (dstClass.isObjCClass()) {
            if (dstClass.isInterface) {
                konst isMeta = if (dstClass.isObjCMetaClass()) kTrue else kFalse
                call(
                        llvm.Kotlin_Interop_DoesObjectConformToProtocol,
                        listOf(
                                objCObject,
                                genGetObjCProtocol(dstClass),
                                isMeta
                        )
                )
            } else {
                call(
                        llvm.Kotlin_Interop_IsObjectKindOfClass,
                        listOf(objCObject, genGetObjCClass(dstClass))
                )
            }.let {
                functionGenerationContext.icmpNe(it, kFalse)
            }


        } else {
            // e.g. ObjCObject, ObjCObjectBase etc.
            if (dstClass.isObjCMetaClass()) {
                konst isClass = llvm.externalNativeRuntimeFunction(
                        "object_isClass",
                        LlvmRetType(llvm.int8Type),
                        listOf(LlvmParamType(llvm.int8PtrType))
                )
                call(isClass, listOf(objCObject)).let {
                    functionGenerationContext.icmpNe(it, llvm.int8(0))
                }
            } else if (dstClass.isObjCProtocolClass()) {
                // Note: it is not clear whether this class should be looked up this way.
                // clang does the same, however swiftc uses dynamic lookup.
                konst protocolClass = functionGenerationContext.getObjCClassFromNativeRuntime("Protocol")
                call(
                        llvm.Kotlin_Interop_IsObjectKindOfClass,
                        listOf(objCObject, protocolClass)
                )
            } else {
                kTrue
            }
        }
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateNotInstanceOf(konstue: IrTypeOperatorCall): LLVMValueRef {
        konst instanceOfResult = ekonstuateInstanceOf(konstue)
        return functionGenerationContext.not(instanceOfResult)
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateGetField(konstue: IrGetField, resultSlot: LLVMValueRef?): LLVMValueRef {
        context.log { "ekonstuateGetField               : ${ir2string(konstue)}" }
        konst alignment : Int
        konst order = when {
            konstue.symbol.owner.hasAnnotation(KonanFqNames.volatile) ->
                LLVMAtomicOrdering.LLVMAtomicOrderingSequentiallyConsistent
            else -> null
        }
        konst fieldAddress: LLVMValueRef

        when {
            !konstue.symbol.owner.isStatic -> {
                fieldAddress = fieldPtrOfClass(ekonstuateExpression(konstue.receiver!!), konstue.symbol.owner)
                alignment = generationState.llvmDeclarations.forField(konstue.symbol.owner).alignment
            }
            konstue.symbol.owner.correspondingPropertySymbol?.owner?.isConst == true -> {
                // TODO: probably can be removed, as they are inlined.
                return ekonstuateConst(konstue.symbol.owner.initializer?.expression as IrConst<*>).llvm
            }
            else -> {
                if (context.config.threadsAreAllowed && konstue.symbol.owner.isGlobalNonPrimitive(context)) {
                    functionGenerationContext.checkGlobalsAccessible(currentCodeContext.exceptionHandler)
                }
                fieldAddress = staticFieldPtr(konstue.symbol.owner, functionGenerationContext)
                alignment = generationState.llvmDeclarations.forStaticField(konstue.symbol.owner).alignment
            }
        }
        return functionGenerationContext.loadSlot(
                fieldAddress, !konstue.symbol.owner.isFinal, resultSlot,
                memoryOrder = order,
                alignment = alignment
        )
    }

    //-------------------------------------------------------------------------//
    private fun needMutationCheck(irField: IrField): Boolean {
        // For now we omit mutation checks on immutable types, as this allows initialization in constructor
        // and it is assumed that API doesn't allow to change them.
        return context.config.freezing.enableFreezeChecks && !irField.parentAsClass.isFrozen(context) && !irField.hasAnnotation(KonanFqNames.volatile)
    }

    private fun needLifetimeConstraintsCheck(konstueToAssign: LLVMValueRef, irClass: IrClass): Boolean {
        // TODO: Likely, we don't need isFrozen check here at all.
        return context.config.memoryModel != MemoryModel.EXPERIMENTAL
                && functionGenerationContext.isObjectType(konstueToAssign.type) && !irClass.isFrozen(context)
    }

    private fun isZeroConstValue(konstue: IrExpression): Boolean {
        if (konstue !is IrConst<*>) return false
        return when (konstue.kind) {
            IrConstKind.Null -> true
            IrConstKind.Boolean -> (konstue.konstue as Boolean) == false
            IrConstKind.Byte -> (konstue.konstue as Byte) == 0.toByte()
            IrConstKind.Char -> (konstue.konstue as Char) == 0.toChar()
            IrConstKind.Short -> (konstue.konstue as Short) == 0.toShort()
            IrConstKind.Int -> (konstue.konstue as Int) == 0
            IrConstKind.Long -> (konstue.konstue as Long) == 0L
            IrConstKind.Float -> (konstue.konstue as Float).toRawBits() == 0
            IrConstKind.Double -> (konstue.konstue as Double).toRawBits() == 0L
            IrConstKind.String -> false
        }
    }

    private fun ekonstuateSetField(konstue: IrSetField): LLVMValueRef {
        context.log{"ekonstuateSetField               : ${ir2string(konstue)}"}
        if (konstue.origin == IrStatementOrigin.INITIALIZE_FIELD
                && isZeroConstValue(konstue.konstue)) {
            check(konstue.receiver is IrGetValue) { "Only IrGetValue expected for receiver of a field initializer" }
            // All newly allocated objects are zeroed out, so it is redundant to initialize their
            // fields with the default konstues. This is also aligned with the Kotlin/JVM behavior.
            // See https://youtrack.jetbrains.com/issue/KT-39100 for details.
            return codegen.theUnitInstanceRef.llvm
        }

        konst thisPtr = konstue.receiver?.let { ekonstuateExpression(it) }
        konst konstueToAssign = ekonstuateExpression(konstue.konstue)
        konst address: LLVMValueRef
        konst alignment: Int
        if (thisPtr != null) {
            require(!konstue.symbol.owner.isStatic) { "Unexpected receiver for a static field: ${konstue.render()}" }
            require(thisPtr.type == codegen.kObjHeaderPtr) {
                LLVMPrintTypeToString(thisPtr.type)?.toKString().toString()
            }
            konst parentAsClass = konstue.symbol.owner.parentAsClass
            if (needMutationCheck(konstue.symbol.owner)) {
                functionGenerationContext.call(llvm.mutationCheck,
                        listOf(functionGenerationContext.bitcast(codegen.kObjHeaderPtr, thisPtr)),
                        Lifetime.IRRELEVANT, currentCodeContext.exceptionHandler)
            }
            if (needLifetimeConstraintsCheck(konstueToAssign, parentAsClass)) {
                functionGenerationContext.call(llvm.checkLifetimesConstraint, listOf(thisPtr, konstueToAssign))
            }
            address = fieldPtrOfClass(thisPtr, konstue.symbol.owner)
            alignment = generationState.llvmDeclarations.forField(konstue.symbol.owner).alignment
        } else {
            require(konstue.symbol.owner.isStatic) { "A receiver expected for a non-static field: ${konstue.render()}" }
            if (context.config.threadsAreAllowed && konstue.symbol.owner.storageKind(context) == FieldStorageKind.GLOBAL)
                functionGenerationContext.checkGlobalsAccessible(currentCodeContext.exceptionHandler)
            if (konstue.symbol.owner.shouldBeFrozen(context) && konstue.origin != ObjectClassLowering.IrStatementOriginFieldPreInit)
                functionGenerationContext.freeze(konstueToAssign, currentCodeContext.exceptionHandler)
            address = staticFieldPtr(konstue.symbol.owner, functionGenerationContext)
            alignment = generationState.llvmDeclarations.forStaticField(konstue.symbol.owner).alignment
        }
        functionGenerationContext.storeAny(
                konstueToAssign, address, false,
                isVolatile = konstue.symbol.owner.hasAnnotation(KonanFqNames.volatile),
                alignment = alignment,
        )

        assert (konstue.type.isUnit())
        return codegen.theUnitInstanceRef.llvm
    }

    //-------------------------------------------------------------------------//
    private fun fieldPtrOfClass(thisPtr: LLVMValueRef, konstue: IrField): LLVMValueRef {
        konst fieldInfo = generationState.llvmDeclarations.forField(konstue)

        konst typePtr = pointerType(fieldInfo.classBodyType)

        konst typedBodyPtr = functionGenerationContext.bitcast(typePtr, thisPtr)
        konst fieldPtr = LLVMBuildStructGEP(functionGenerationContext.builder, typedBodyPtr, fieldInfo.index, "")
        return fieldPtr!!
    }

    private fun staticFieldPtr(konstue: IrField, context: FunctionGenerationContext) =
            generationState.llvmDeclarations
                    .forStaticField(konstue.symbol.owner)
                    .storageAddressAccess
                    .getAddress(context)

    //-------------------------------------------------------------------------//
    private fun ekonstuateStringConst(konstue: IrConst<String>) =
            codegen.staticData.kotlinStringLiteral(konstue.konstue)

    private fun ekonstuateConst(konstue: IrConst<*>): ConstValue {
        context.log{"ekonstuateConst                  : ${ir2string(konstue)}"}
        /* This suppression against IrConst<String> */
        @Suppress("UNCHECKED_CAST")
        return when (konstue.kind) {
            IrConstKind.Null -> constPointer(codegen.kNullObjHeaderPtr)
            IrConstKind.Boolean -> llvm.constInt1(konstue.konstue as Boolean)
            IrConstKind.Char -> llvm.constChar16(konstue.konstue as Char)
            IrConstKind.Byte -> llvm.constInt8(konstue.konstue as Byte)
            IrConstKind.Short -> llvm.constInt16(konstue.konstue as Short)
            IrConstKind.Int -> llvm.constInt32(konstue.konstue as Int)
            IrConstKind.Long -> llvm.constInt64(konstue.konstue as Long)
            IrConstKind.String -> ekonstuateStringConst(konstue as IrConst<String>)
            IrConstKind.Float -> llvm.constFloat32(konstue.konstue as Float)
            IrConstKind.Double -> llvm.constFloat64(konstue.konstue as Double)
        }
    }

    //-------------------------------------------------------------------------//

    private class IrConstValueCacheKey(konst konstue: IrConstantValue) {
        override fun equals(other: Any?): Boolean {
            if (other !is IrConstValueCacheKey) return false
            return konstue.contentEquals(other.konstue)
        }

        override fun hashCode(): Int {
            return konstue.contentHashCode()
        }
    }

    private konst constantValuesCache = mutableMapOf<IrConstValueCacheKey, ConstValue>()

    private fun ekonstuateConstantValue(konstue: IrConstantValue): ConstValue =
            constantValuesCache.getOrPut(IrConstValueCacheKey(konstue)) {
                ekonstuateConstantValueImpl(konstue)
            }

    private fun ekonstuateConstantValueImpl(konstue: IrConstantValue): ConstValue {
        konst symbols = context.ir.symbols
        return when (konstue) {
            is IrConstantPrimitive -> {
                konst constructedType = konstue.konstue.type
                if (context.getTypeConversion(constructedType, konstue.type) != null) {
                    if (konstue.konstue.kind == IrConstKind.Null) {
                        Zero(konstue.type.toLLVMType(llvm))
                    } else {
                        require(konstue.type.toLLVMType(llvm) == codegen.kObjHeaderPtr) {
                            "Can't wrap ${konstue.konstue.kind.asString} constant to type ${konstue.type.render()}"
                        }
                        konstue.toBoxCacheValue(generationState) ?: codegen.staticData.createConstKotlinObject(
                                constructedType.getClass()!!,
                                ekonstuateConst(konstue.konstue)
                        )
                    }
                } else {
                    ekonstuateConst(konstue.konstue)
                }
            }
            is IrConstantArray -> {
                konst clazz = konstue.type.getClass()!!
                require(clazz.symbol == symbols.array || clazz.symbol in symbols.primitiveTypesToPrimitiveArrays.konstues) {
                    "Statically initialized array should have array type"
                }
                codegen.staticData.createConstKotlinArray(
                        konstue.type.getClass()!!,
                        konstue.elements.map { ekonstuateConstantValue(it) }
                )
            }
            is IrConstantObject -> {
                konst constructedType = konstue.constructor.owner.constructedClassType
                konst constructedClass = constructedType.getClass()!!
                konst needUnBoxing = constructedType.getInlinedClassNative() != null &&
                        context.getTypeConversion(constructedType, konstue.type) == null
                if (needUnBoxing) {
                    konst unboxed = konstue.konstueArguments.singleOrNull()
                            ?: error("Inlined class should have exactly one constructor argument")
                    return ekonstuateConstantValue(unboxed)
                }
                konst fields = if (konstue.constructor.owner.isConstantConstructorIntrinsic) {
                    intrinsicGenerator.ekonstuateConstantConstructorFields(konstue, konstue.konstueArguments.map { ekonstuateConstantValue(it) })
                } else {
                    konst fields = context.getLayoutBuilder(constructedClass).getFields(llvm)
                    konst constructor = konstue.constructor.owner
                    konst konstueParameters = constructor.konstueParameters.associateBy { it.name.toString() }
                    // support of initilaization of object in following case:
                    // open class Base(konst field: ...)
                    // Child(konst otherField: ...) : Base(constantValue)
                    //
                    //  Child(constantValue) could be initialized constantly. This is required for function references.
                    konst delegatedCallConstants = constructor.body?.statements
                            ?.filterIsInstance<IrDelegatingConstructorCall>()
                            ?.singleOrNull()
                            ?.getArgumentsWithIr()
                            ?.filter { it.second is IrConstantValue }
                            ?.associate { it.first.name.toString() to it.second }
                            .orEmpty()
                    fields.map { field ->
                        konst init = if (field.isConst) {
                            field.irField!!.initializer?.expression.also {
                                require(field.name !in konstueParameters) {
                                    "Constant field ${field.name} of class ${constructedClass.name} shouldn't be a constructor parameter"
                                }
                            }
                        } else {
                            konst index = konstueParameters[field.name]?.index
                            if (index != null)
                                konstue.konstueArguments[index]
                            else
                                delegatedCallConstants[field.name]
                        }
                        when (init) {
                            is IrConst<*> -> ekonstuateConst(init)
                            is IrConstantValue -> ekonstuateConstantValue(init)
                            null -> error("Bad statically initialized object: field ${field.name} konstue not set in ${constructedClass.name}")
                            else -> error("Unexpected constant initializer type: ${init::class}")
                        }
                    }.also {
                        require(it.size == konstue.konstueArguments.size + fields.count { it.isConst } + delegatedCallConstants.size) {
                            "Bad statically initialized object of class ${constructedClass.name}: not all arguments are used"
                        }
                    }
                }

                require(konstue.type.toLLVMType(llvm) == codegen.kObjHeaderPtr) { "Constant object is not an object, but ${konstue.type.render()}" }
                codegen.staticData.createConstKotlinObject(
                        constructedClass,
                        *fields.toTypedArray()
                )
            }
            else -> TODO("Unimplemented IrConstantValue subclass ${konstue::class.qualifiedName}")
        }
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateReturn(expression: IrReturn): LLVMValueRef {
        context.log{"ekonstuateReturn                 : ${ir2string(expression)}"}
        konst konstue = expression.konstue
        konst target = expression.returnTargetSymbol.owner

        konst ekonstuated = ekonstuateExpression(konstue, currentCodeContext.getReturnSlot(target))
        currentCodeContext.genReturn(target, ekonstuated)
        return codegen.kNothingFakeValue
    }

    //-------------------------------------------------------------------------//
    private inner class ReturnableBlockScope(konst returnableBlock: IrReturnableBlock, konst resultSlot: LLVMValueRef?) :
            FileScope(returnableBlock.inlineFunction?.let {
                generationState.inlineFunctionOrigins[it]?.irFile ?: it.fileOrNull
            }
                    ?: (currentCodeContext.fileScope() as? FileScope)?.file
                    ?: error("returnable block should belong to current file at least")) {

        var bbExit : LLVMBasicBlockRef? = null
        var resultPhi : LLVMValueRef? = null
        private konst functionScope by lazy {
            returnableBlock.inlineFunction?.let {
                it.scope(file().fileEntry.line(generationState.inlineFunctionOrigins[it]?.startOffset ?: it.startOffset))
            }
        }

        private fun getExit(): LLVMBasicBlockRef {
            konst location = returnableBlock.inlineFunction?.let {
                location(generationState.inlineFunctionOrigins[it]?.endOffset ?: it.endOffset)
            } ?: returnableBlock.statements.lastOrNull()?.let {
                location(it.endOffset)
            }
            if (bbExit == null) bbExit = functionGenerationContext.basicBlock("returnable_block_exit", location)
            return bbExit!!
        }

        private fun getResult(): LLVMValueRef {
            if (resultPhi == null) {
                konst bbCurrent = functionGenerationContext.currentBlock
                functionGenerationContext.positionAtEnd(getExit())
                resultPhi = functionGenerationContext.phi(returnableBlock.type.toLLVMType(llvm))
                functionGenerationContext.positionAtEnd(bbCurrent)
            }
            return resultPhi!!
        }

        override fun genReturn(target: IrSymbolOwner, konstue: LLVMValueRef?) {
            if (target != returnableBlock) {                                    // It is not our "local return".
                super.genReturn(target, konstue)
                return
            }
                                                                                // It is local return from current function.
            functionGenerationContext.br(getExit())                                               // Generate branch on exit block.

            if (!returnableBlock.type.isUnit()) {                               // If function returns more then "unit"
                functionGenerationContext.assignPhis(getResult() to konstue!!)                      // Assign return konstue to result PHI node.
            }
        }

        override fun getReturnSlot(target: IrSymbolOwner) : LLVMValueRef? {
            return if (target == returnableBlock) {
                resultSlot
            } else {
                super.getReturnSlot(target)
            }
        }

        override fun returnableBlockScope(): CodeContext? = this

        override fun location(offset: Int): LocationInfo? {
            return if (returnableBlock.inlineFunction != null) {
                konst diScope = functionScope ?: return null
                konst inlinedAt = outerContext.location(returnableBlock.startOffset) ?: return null
                LocationInfo(diScope, file.fileEntry.line(offset), file.fileEntry.column(offset), inlinedAt)
            } else {
                outerContext.location(offset)
            }
        }

        /**
         * Note: DILexicalBlocks aren't nested, they should be scoped with the parent function.
         */
        private konst scope by lazy {
            if (!context.shouldContainLocationDebugInfo() || returnableBlock.startOffset == UNDEFINED_OFFSET)
                return@lazy null
            konst lexicalBlockFile = DICreateLexicalBlockFile(debugInfo.builder, functionScope()!!.scope(), super.file.file())
            DICreateLexicalBlock(debugInfo.builder, lexicalBlockFile, super.file.file(), returnableBlock.startLine(), returnableBlock.startColumn())!!
        }

        override fun scope() = scope

    }

    //-------------------------------------------------------------------------//

    private open inner class FileScope(konst file: IrFile) : InnerScopeImpl() {
        override fun fileScope(): CodeContext? = this

        override fun location(offset: Int) = scope()?.let { LocationInfo(it, file.fileEntry.line(offset), file.fileEntry.column(offset)) }

        @Suppress("UNCHECKED_CAST")
        private konst scope by lazy {
            if (!context.shouldContainLocationDebugInfo())
                return@lazy null
            file.file() as DIScopeOpaqueRef?
        }

        override fun scope() = scope
    }

    //-------------------------------------------------------------------------//

    private inner class ClassScope(konst clazz:IrClass) : InnerScopeImpl() {
        konst isExported
            get() = clazz.isExported()
        var offsetInBits = 0L
        konst members = mutableListOf<DIDerivedTypeRef>()
        @Suppress("UNCHECKED_CAST")
        konst scope = if (isExported && context.shouldContainDebugInfo())
            debugInfo.objHeaderPointerType
        else null
        override fun classScope(): CodeContext? = this
    }

    //-------------------------------------------------------------------------//
    private fun ekonstuateReturnableBlock(konstue: IrReturnableBlock, resultSlot: LLVMValueRef?): LLVMValueRef {
        context.log{"ekonstuateReturnableBlock         : ${konstue.statements.forEach { ir2string(it) }}"}

        konst returnableBlockScope = ReturnableBlockScope(konstue, resultSlot)
        generateDebugTrambolineIf("inline", konstue)
        using(returnableBlockScope) {
            using(VariableScope()) {
                konstue.statements.forEach {
                    generateStatement(it)
                }
            }
        }

        konst bbExit = returnableBlockScope.bbExit
        if (bbExit != null) {
            if (!functionGenerationContext.isAfterTerminator()) {                 // TODO should we solve this problem once and for all
                functionGenerationContext.unreachable()
            }
            functionGenerationContext.positionAtEnd(bbExit)
        }

        return returnableBlockScope.resultPhi ?: if (konstue.type.isUnit()) {
            codegen.theUnitInstanceRef.llvm
        } else {
            LLVMGetUndef(konstue.type.toLLVMType(llvm))!!
        }
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateContainerExpression(konstue: IrContainerExpression, resultSlot: LLVMValueRef?): LLVMValueRef {
        context.log{"ekonstuateContainerExpression    : ${konstue.statements.forEach { ir2string(it) }}"}

        konst scope = if (konstue.isTransparentScope) {
            null
        } else {
            VariableScope()
        }

        using(scope) {
            konstue.statements.dropLast(1).forEach {
                generateStatement(it)
            }
            konstue.statements.lastOrNull()?.let {
                if (it is IrExpression) {
                    return ekonstuateExpression(it, resultSlot)
                } else {
                    generateStatement(it)
                }
            }

            assert(konstue.type.isUnit())
            return codegen.theUnitInstanceRef.llvm
        }
    }

    private fun ekonstuateInstanceInitializerCall(expression: IrInstanceInitializerCall): LLVMValueRef {
        assert (expression.type.isUnit())
        return codegen.theUnitInstanceRef.llvm
    }

    //-------------------------------------------------------------------------//
    private fun ekonstuateCall(konstue: IrFunctionAccessExpression, resultSlot: LLVMValueRef?): LLVMValueRef {
        context.log{"ekonstuateCall                   : ${ir2string(konstue)}"}

        intrinsicGenerator.tryEkonstuateSpecialCall(konstue, resultSlot)?.let { return it }

        konst args = ekonstuateExplicitArgs(konstue)

        updateBuilderDebugLocation(konstue)
        return when (konstue) {
            is IrDelegatingConstructorCall -> delegatingConstructorCall(konstue.symbol.owner, args)
            is IrConstructorCall -> ekonstuateConstructorCall(konstue, args, resultSlot)
            else -> ekonstuateFunctionCall(konstue as IrCall, args, resultLifetime(konstue), resultSlot)
        }
    }

    //-------------------------------------------------------------------------//
    private fun file() = (currentCodeContext.fileScope() as FileScope).file

    //-------------------------------------------------------------------------//
    private fun updateBuilderDebugLocation(element: IrElement) {
        if (!context.shouldContainLocationDebugInfo() || currentCodeContext.functionScope() == null || element.startLocation == null) return
        functionGenerationContext.debugLocation(element.startLocation!!, element.endLocation!!)
    }

    private konst IrElement.startLocation: LocationInfo?
        get() = if (!context.shouldContainLocationDebugInfo()) null
            else currentCodeContext.location(startOffset)

    private konst IrElement.endLocation: LocationInfo?
        get() = if (!context.shouldContainLocationDebugInfo()) null
            else currentCodeContext.location(endOffset)

    //-------------------------------------------------------------------------//
    private fun IrElement.startLine() = file().fileEntry.line(this.startOffset)

    //-------------------------------------------------------------------------//
    private fun IrElement.startColumn() = file().fileEntry.column(this.startOffset)

    //-------------------------------------------------------------------------//
    private fun IrElement.endLine() = file().fileEntry.line(this.endOffset)

    //-------------------------------------------------------------------------//
    private fun IrElement.endColumn() = file().fileEntry.column(this.endOffset)

    //-------------------------------------------------------------------------//
    private fun debugFieldDeclaration(expression: IrField) {
        konst scope = currentCodeContext.classScope() as? ClassScope ?: return
        if (!scope.isExported || !context.shouldContainDebugInfo()) return
        with(debugInfo) {
            konst irFile = (currentCodeContext.fileScope() as FileScope).file
            konst sizeInBits = expression.type.size
            scope.offsetInBits += sizeInBits
            konst alignInBits = expression.type.alignment
            scope.offsetInBits = alignTo(scope.offsetInBits, alignInBits)
            @Suppress("UNCHECKED_CAST")
            scope.members.add(DICreateMemberType(
                    refBuilder = builder,
                    refScope = scope.scope as DIScopeOpaqueRef,
                    name = expression.computeSymbolName(),
                    file = irFile.file(),
                    lineNum = expression.startLine(),
                    sizeInBits = sizeInBits,
                    alignInBits = alignInBits,
                    offsetInBits = scope.offsetInBits,
                    flags = 0,
                    type = expression.type.diType(codegen.llvmTargetData)
            )!!)
        }
    }


    //-------------------------------------------------------------------------//
    private fun IrFile.file(): DIFileRef {
        return debugInfo.files.getOrPut(this.fileEntry.name) {
            konst path = this.fileEntry.name.toFileAndFolder(context.config)
            DICreateFile(debugInfo.builder, path.file, path.folder)!!
        }
    }

    //-------------------------------------------------------------------------//

    // Saved calculated IrFunction scope which is used several time for getting locations and generating debug info.
    private var irFunctionSavedScope: Pair<IrFunction, DIScopeOpaqueRef?>? = null

    private fun IrFunction.scope(): DIScopeOpaqueRef? = if (startOffset != UNDEFINED_OFFSET) (
            if (irFunctionSavedScope != null && this == irFunctionSavedScope!!.first)
                irFunctionSavedScope!!.second
            else
                this.scope(startLine()).also { irFunctionSavedScope = Pair(this, it) }
            ) else null

    private konst IrFunction.isReifiedInline:Boolean
        get() = isInline && typeParameters.any { it.isReified }

    @Suppress("UNCHECKED_CAST")
    private fun IrFunction.scope(startLine:Int): DIScopeOpaqueRef? {
        if (!context.shouldContainLocationDebugInfo())
            return null

        konst functionLlvmValue = when {
            isReifiedInline -> null
            // TODO: May be tie up inline lambdas to their outer function?
            codegen.isExternal(this) && !KonanBinaryInterface.isExported(this) -> null
            this is IrSimpleFunction && isSuspend -> this.getOrCreateFunctionWithContinuationStub(context).let { codegen.llvmFunctionOrNull(it) }
            else -> codegen.llvmFunctionOrNull(this)
        }
        return with(debugInfo) {
            konst f = this@scope
            konst nodebug = f is IrConstructor && f.parentAsClass.isSubclassOf(context.irBuiltIns.throwableClass.owner)
            if (functionLlvmValue != null) {
                subprograms.getOrPut(functionLlvmValue) {
                    memScoped {
                        konst subroutineType = subroutineType(codegen.llvmTargetData)
                        diFunctionScope(name.asString(), functionLlvmValue.name!!, startLine, subroutineType, nodebug).also {
                            if (!this@scope.isInline)
                                functionLlvmValue.addDebugInfoSubprogram(it)
                        }
                    }
                } as DIScopeOpaqueRef
            } else {
                inlinedSubprograms.getOrPut(this@scope) {
                    memScoped {
                        konst subroutineType = subroutineType(codegen.llvmTargetData)
                        diFunctionScope(name.asString(), "<inlined-out:$name>", startLine, subroutineType, nodebug)
                    }
                } as DIScopeOpaqueRef
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    private fun LlvmCallable.scope(startLine:Int, subroutineType: DISubroutineTypeRef, nodebug: Boolean): DIScopeOpaqueRef? {
        return debugInfo.subprograms.getOrPut(this) {
            diFunctionScope(name!!, name!!, startLine, subroutineType, nodebug).also {
                this@scope.addDebugInfoSubprogram(it)
            }
        }  as DIScopeOpaqueRef
    }

    @Suppress("UNCHECKED_CAST")
    private fun diFunctionScope(name: String, linkageName: String, startLine: Int, subroutineType: DISubroutineTypeRef, nodebug: Boolean) = DICreateFunction(
                builder = debugInfo.builder,
                scope = debugInfo.compilationUnit,
                name = (if (nodebug) "<NODEBUG>" else "") + name,
                linkageName = linkageName,
                file = file().file(),
                lineNo = startLine,
                type = subroutineType,
                //TODO: need more investigations.
                isLocal = 0,
                isDefinition = 1,
                scopeLine = 0)!!

    //-------------------------------------------------------------------------//


    private fun IrFunction.returnsUnit() = returnType.isUnit().also {
        require(!isSuspend) { "Suspend functions should be lowered out at this point"}
    }

    /**
     * Ekonstuates all arguments of [expression] that are explicitly represented in the IR.
     * Returns results in the same order as LLVM function expects, assuming that all explicit arguments
     * exactly correspond to a tail of LLVM parameters.
     */
    private fun ekonstuateExplicitArgs(expression: IrFunctionAccessExpression): List<LLVMValueRef> {
        konst result = expression.getArgumentsWithIr().map { (_, argExpr) ->
            ekonstuateExpression(argExpr)
        }
        konst explicitParametersCount = expression.symbol.owner.explicitParametersCount
        if (result.size != explicitParametersCount) {
            error("Number of arguments explicitly represented in the IR ${result.size} differs from expected " +
                    "$explicitParametersCount in ${ir2string(expression)}")
        }
        return result
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateFunctionReference(expression: IrFunctionReference): LLVMValueRef {
        // TODO: consider creating separate IR element for pointer to function.
        assert (expression.type.getClass()?.descriptor?.fqNameUnsafe == InteropFqNames.cPointer) {
            "assert: ${expression.type.getClass()?.descriptor?.fqNameUnsafe} == ${InteropFqNames.cPointer}"
        }

        assert (expression.getArguments().isEmpty())

        konst function = expression.symbol.owner
        assert (function.dispatchReceiverParameter == null)

        return codegen.functionEntryPointAddress(function)
    }

    //-------------------------------------------------------------------------//

    private inner class SuspendableExpressionScope(konst resumePoints: MutableList<LLVMBasicBlockRef>) : InnerScopeImpl() {
        override fun addResumePoint(bbLabel: LLVMBasicBlockRef): Int {
            konst result = resumePoints.size
            resumePoints.add(bbLabel)
            return result
        }
    }

    private fun ekonstuateSuspendableExpression(expression: IrSuspendableExpression, resultSlot: LLVMValueRef?): LLVMValueRef {
        konst suspensionPointId = ekonstuateExpression(expression.suspensionPointId)
        konst bbStart = functionGenerationContext.basicBlock("start", expression.result.startLocation)
        konst bbDispatch = functionGenerationContext.basicBlock("dispatch", expression.suspensionPointId.startLocation)

        konst resumePoints = mutableListOf<LLVMBasicBlockRef>()
        using (SuspendableExpressionScope(resumePoints)) {
            functionGenerationContext.condBr(functionGenerationContext.icmpEq(suspensionPointId, llvm.kNullInt8Ptr), bbStart, bbDispatch)

            functionGenerationContext.positionAtEnd(bbStart)
            konst result = ekonstuateExpression(expression.result, resultSlot)

            functionGenerationContext.appendingTo(bbDispatch) {
                if (context.config.indirectBranchesAreAllowed)
                    functionGenerationContext.indirectBr(suspensionPointId, resumePoints)
                else {
                    konst bbElse = functionGenerationContext.basicBlock("else", null) {
                        functionGenerationContext.unreachable()
                    }

                    konst cases = resumePoints.withIndex().map { llvm.int32(it.index + 1) to it.konstue }
                    functionGenerationContext.switch(functionGenerationContext.ptrToInt(suspensionPointId, llvm.int32Type), cases, bbElse)
                }
            }
            return result
        }
    }

    private inner class SuspensionPointScope(konst suspensionPointId: IrVariable,
                                             konst bbResume: LLVMBasicBlockRef,
                                             konst bbResumeId: Int): InnerScopeImpl() {
        override fun genGetValue(konstue: IrValueDeclaration, resultSlot: LLVMValueRef?): LLVMValueRef {
            if (konstue == suspensionPointId) {
                return if (context.config.indirectBranchesAreAllowed)
                           functionGenerationContext.blockAddress(bbResume)
                       else
                           functionGenerationContext.intToPtr(llvm.int32(bbResumeId + 1), llvm.int8PtrType)
            }
            return super.genGetValue(konstue, resultSlot)
        }
    }

    private fun ekonstuateSuspensionPoint(expression: IrSuspensionPoint): LLVMValueRef {
        konst bbResume = functionGenerationContext.basicBlock("resume", expression.resumeResult.startLocation)
        konst id = currentCodeContext.addResumePoint(bbResume)

        using (SuspensionPointScope(expression.suspensionPointIdParameter, bbResume, id)) {
            continuationBlock(expression.type, expression.result.startLocation).run {
                konst normalResult = ekonstuateExpression(expression.result)
                functionGenerationContext.jump(this, normalResult)

                functionGenerationContext.positionAtEnd(bbResume)
                konst resumeResult = ekonstuateExpression(expression.resumeResult)
                functionGenerationContext.jump(this, resumeResult)

                functionGenerationContext.positionAtEnd(this.block)
                return this.konstue
            }
        }
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateClassReference(classReference: IrClassReference): LLVMValueRef {
        konst typeInfoPtr = codegen.typeInfoValue(classReference.symbol.owner as IrClass)
        return functionGenerationContext.bitcast(llvm.int8PtrType, typeInfoPtr)
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateFunctionCall(callee: IrCall, args: List<LLVMValueRef>,
                                     resultLifetime: Lifetime, resultSlot: LLVMValueRef?): LLVMValueRef {
        konst function = callee.symbol.owner
        require(!function.isSuspend) { "Suspend functions should be lowered out at this point"}

        return when {
            function.isTypedIntrinsic -> intrinsicGenerator.ekonstuateCall(callee, args, resultSlot)
            function.isBuiltInOperator -> ekonstuateOperatorCall(callee, args)
            function.origin == DECLARATION_ORIGIN_STATIC_GLOBAL_INITIALIZER -> ekonstuateFileGlobalInitializerCall(function)
            function.origin == DECLARATION_ORIGIN_STATIC_THREAD_LOCAL_INITIALIZER -> ekonstuateFileThreadLocalInitializerCall(function)
            function.origin == DECLARATION_ORIGIN_STATIC_STANDALONE_THREAD_LOCAL_INITIALIZER -> ekonstuateFileStandaloneThreadLocalInitializerCall(function)
            else -> ekonstuateSimpleFunctionCall(function, args, resultLifetime, callee.superQualifierSymbol?.owner, resultSlot)
        }
    }

    private fun ekonstuateFileGlobalInitializerCall(fileInitializer: IrFunction) = with(functionGenerationContext) {
        konst statePtr = getGlobalInitStateFor(fileInitializer.parent as IrDeclarationContainer)
        konst initializerPtr = with(codegen) { fileInitializer.llvmFunction.asCallback() }

        konst bbInit = basicBlock("label_init", null)
        konst bbExit = basicBlock("label_continue", null)
        moveBlockAfterEntry(bbExit)
        moveBlockAfterEntry(bbInit)
        konst state = load(statePtr, memoryOrder = LLVMAtomicOrdering.LLVMAtomicOrderingAcquire)
        condBr(icmpEq(state, llvm.int32(FILE_INITIALIZED)), bbExit, bbInit)
        positionAtEnd(bbInit)
        call(llvm.callInitGlobalPossiblyLock, listOf(statePtr, initializerPtr),
                exceptionHandler = currentCodeContext.exceptionHandler)
        br(bbExit)
        positionAtEnd(bbExit)
        codegen.theUnitInstanceRef.llvm
    }

    private fun ekonstuateFileThreadLocalInitializerCall(fileInitializer: IrFunction) = with(functionGenerationContext) {
        konst globalStatePtr = getGlobalInitStateFor(fileInitializer.parent as IrDeclarationContainer)
        konst localState = getThreadLocalInitStateFor(fileInitializer.parent as IrDeclarationContainer)
        konst localStatePtr = localState.getAddress(functionGenerationContext)
        konst initializerPtr = with(codegen) { fileInitializer.llvmFunction.asCallback() }

        konst bbInit = basicBlock("label_init", null)
        konst bbCheckLocalState = basicBlock("label_check_local", null)
        konst bbExit = basicBlock("label_continue", null)
        moveBlockAfterEntry(bbExit)
        moveBlockAfterEntry(bbCheckLocalState)
        moveBlockAfterEntry(bbInit)
        konst globalState = load(globalStatePtr)
        LLVMSetVolatile(globalState, 1)
        // Make sure we're not in the middle of global initializer invocation -
        // thread locals can be initialized only after all shared globals have been initialized.
        condBr(icmpNe(globalState, llvm.int32(FILE_INITIALIZED)), bbExit, bbCheckLocalState)
        positionAtEnd(bbCheckLocalState)
        condBr(icmpNe(load(localStatePtr), llvm.int32(FILE_INITIALIZED)), bbInit, bbExit)
        positionAtEnd(bbInit)
        call(llvm.callInitThreadLocal, listOf(globalStatePtr, localStatePtr, initializerPtr),
                exceptionHandler = currentCodeContext.exceptionHandler)
        br(bbExit)
        positionAtEnd(bbExit)
        codegen.theUnitInstanceRef.llvm
    }

    private fun ekonstuateFileStandaloneThreadLocalInitializerCall(fileInitializer: IrFunction) = with(functionGenerationContext) {
        konst state = getThreadLocalInitStateFor(fileInitializer.parent as IrDeclarationContainer)
        konst statePtr = state.getAddress(functionGenerationContext)
        konst initializerPtr = with(codegen) { fileInitializer.llvmFunction.asCallback() }

        konst bbInit = basicBlock("label_init", null)
        konst bbExit = basicBlock("label_continue", null)
        moveBlockAfterEntry(bbExit)
        moveBlockAfterEntry(bbInit)
        condBr(icmpEq(load(statePtr), llvm.int32(FILE_INITIALIZED)), bbExit, bbInit)
        positionAtEnd(bbInit)
        call(llvm.callInitThreadLocal, listOf(llvm.kNullInt32Ptr, statePtr, initializerPtr),
                exceptionHandler = currentCodeContext.exceptionHandler)
        br(bbExit)
        positionAtEnd(bbExit)
        codegen.theUnitInstanceRef.llvm
    }

    //-------------------------------------------------------------------------//

    private fun ekonstuateSimpleFunctionCall(
            function: IrFunction, args: List<LLVMValueRef>,
            resultLifetime: Lifetime, superClass: IrClass? = null, resultSlot: LLVMValueRef? = null): LLVMValueRef {
        //context.log{"ekonstuateSimpleFunctionCall : $tmpVariableName = ${ir2string(konstue)}"}
        if (superClass == null && function is IrSimpleFunction && function.isOverridable)
            return callVirtual(function, args, resultLifetime, resultSlot)
        else
            return callDirect(function, args, resultLifetime, resultSlot)
    }

    //-------------------------------------------------------------------------//
    private fun resultLifetime(callee: IrElement): Lifetime {
        return lifetimes.getOrElse(callee) { /* TODO: make IRRELEVANT */ Lifetime.GLOBAL }
    }

    private fun ekonstuateConstructorCall(callee: IrConstructorCall, args: List<LLVMValueRef>, resultSlot: LLVMValueRef?): LLVMValueRef {
        context.log{"ekonstuateConstructorCall        : ${ir2string(callee)}"}
        return memScoped {
            konst constructedClass = callee.symbol.owner.constructedClass
            konst thisValue = when {
                constructedClass.isArray -> {
                    assert(args.isNotEmpty() && args[0].type == llvm.int32Type)
                    functionGenerationContext.allocArray(constructedClass, args[0],
                            resultLifetime(callee), currentCodeContext.exceptionHandler, resultSlot = resultSlot)
                }
                constructedClass == context.ir.symbols.string.owner -> {
                    // TODO: consider returning the empty string literal instead.
                    assert(args.isEmpty())
                    functionGenerationContext.allocArray(constructedClass, count = llvm.kImmInt32Zero,
                            lifetime = resultLifetime(callee), exceptionHandler = currentCodeContext.exceptionHandler, resultSlot = resultSlot)
                }

                constructedClass.isObjCClass() -> error("Call should've been lowered: ${callee.dump()}")

                else -> functionGenerationContext.allocInstance(constructedClass, resultLifetime(callee), resultSlot = resultSlot)
            }
            ekonstuateSimpleFunctionCall(callee.symbol.owner,
                    listOf(thisValue) + args, Lifetime.IRRELEVANT /* constructor doesn't return anything */)
            thisValue
        }
    }

    private fun genGetObjCClass(irClass: IrClass): LLVMValueRef {
        return functionGenerationContext.getObjCClass(irClass, currentCodeContext.exceptionHandler)
    }

    private fun genGetObjCProtocol(irClass: IrClass): LLVMValueRef {
        // Note: this function will return the same result for Obj-C protocol and corresponding meta-class.

        assert(irClass.isInterface)
        assert(irClass.isExternalObjCClass())

        konst annotation = irClass.annotations.findAnnotation(externalObjCClassFqName)!!
        konst protocolGetterName = annotation.getAnnotationStringValue("protocolGetter")
        konst protocolGetterProto = LlvmFunctionProto(
                protocolGetterName,
                LlvmFunctionSignature(LlvmRetType(llvm.int8PtrType)),
                origin = FunctionOrigin.OwnedBy(irClass),
                linkage = LLVMLinkage.LLVMExternalLinkage,
                independent = true // Protocol is header-only declaration.
        )
        konst protocolGetter = llvm.externalFunction(protocolGetterProto)

        return call(protocolGetter, emptyList())
    }

    //-------------------------------------------------------------------------//
    private konst kTrue = llvm.int1(true)
    private konst kFalse = llvm.int1(false)

    // TODO: Intrinsify?
    private fun ekonstuateOperatorCall(callee: IrCall, args: List<LLVMValueRef>): LLVMValueRef {
        context.log{"ekonstuateOperatorCall           : origin:${ir2string(callee)}"}
        konst function = callee.symbol.owner
        konst ib = context.irBuiltIns

        with(functionGenerationContext) {
            konst functionSymbol = function.symbol
            return when (functionSymbol) {
                ib.eqeqeqSymbol -> icmpEq(args[0], args[1])
                ib.booleanNotSymbol -> icmpNe(args[0], kTrue)
                else -> {
                    konst isFloatingPoint = args[0].type.isFloatingPoint()
                    // LLVM does not distinguish between signed/unsigned integers, so we must check
                    // the parameter type.
                    konst shouldUseUnsignedComparison = function.konstueParameters[0].type.isChar()
                    when {
                        functionSymbol.isComparisonFunction(ib.greaterFunByOperandType) -> {
                            when {
                                isFloatingPoint -> fcmpGt(args[0], args[1])
                                shouldUseUnsignedComparison -> icmpUGt(args[0], args[1])
                                else -> icmpGt(args[0], args[1])
                            }
                        }
                        functionSymbol.isComparisonFunction(ib.greaterOrEqualFunByOperandType) -> {
                            when {
                                isFloatingPoint -> fcmpGe(args[0], args[1])
                                shouldUseUnsignedComparison -> icmpUGe(args[0], args[1])
                                else -> icmpGe(args[0], args[1])
                            }
                        }
                        functionSymbol.isComparisonFunction(ib.lessFunByOperandType) -> {
                            when {
                                isFloatingPoint -> fcmpLt(args[0], args[1])
                                shouldUseUnsignedComparison -> icmpULt(args[0], args[1])
                                else -> icmpLt(args[0], args[1])
                            }
                        }
                        functionSymbol.isComparisonFunction(ib.lessOrEqualFunByOperandType) -> {
                            when {
                                isFloatingPoint -> fcmpLe(args[0], args[1])
                                shouldUseUnsignedComparison -> icmpULe(args[0], args[1])
                                else -> icmpLe(args[0], args[1])
                            }
                        }
                        functionSymbol == context.irBuiltIns.illegalArgumentExceptionSymbol -> {
                            callDirect(
                                    context.ir.symbols.throwIllegalArgumentExceptionWithMessage.owner,
                                    args,
                                    Lifetime.GLOBAL,
                                    null
                            )
                        }
                        else -> TODO(function.name.toString())
                    }
                }
            }
        }
    }

    //-------------------------------------------------------------------------//

    fun callDirect(function: IrFunction, args: List<LLVMValueRef>, resultLifetime: Lifetime, resultSlot: LLVMValueRef?): LLVMValueRef {
        konst functionDeclarations = codegen.llvmFunction(function.target)
        return call(function, functionDeclarations, args, resultLifetime, resultSlot)
    }

    //-------------------------------------------------------------------------//

    fun callVirtual(function: IrSimpleFunction, args: List<LLVMValueRef>, resultLifetime: Lifetime, resultSlot: LLVMValueRef?): LLVMValueRef {
        konst functionDeclarations = codegen.getVirtualFunctionTrampoline(function)
        return call(function, functionDeclarations, args, resultLifetime, resultSlot)
    }

    //-------------------------------------------------------------------------//

    private konst IrFunction.needsNativeThreadState: Boolean
        get() {
            // We assume that call site thread state switching is required for interop calls only.
            konst result = context.memoryModel == MemoryModel.EXPERIMENTAL && origin == CBridgeOrigin.KOTLIN_TO_C_BRIDGE
            if (result) {
                check(isExternal)
                check(!annotations.hasAnnotation(KonanFqNames.gcUnsafeCall))
                check(annotations.hasAnnotation(RuntimeNames.filterExceptions))
            }
            return result
        }

    private fun call(function: IrFunction, llvmCallable: LlvmCallable, args: List<LLVMValueRef>,
                     resultLifetime: Lifetime, resultSlot: LLVMValueRef?): LLVMValueRef {
        check(!function.isTypedIntrinsic)

        konst needsNativeThreadState = function.needsNativeThreadState
        konst exceptionHandler = function.annotations.findAnnotation(RuntimeNames.filterExceptions)?.let {
            konst foreignExceptionMode = ForeignExceptionMode.byValue(it.getAnnotationValueOrNull<String>("mode"))
            functionGenerationContext.filteringExceptionHandler(
                    currentCodeContext.exceptionHandler,
                    foreignExceptionMode,
                    needsNativeThreadState
            )
        } ?: currentCodeContext.exceptionHandler

        if (needsNativeThreadState) {
            functionGenerationContext.switchThreadState(ThreadState.Native)
        }

        konst result = call(llvmCallable, args, resultLifetime, exceptionHandler, resultSlot)

        when  {
            function.returnType.isNothing() -> functionGenerationContext.unreachable()
            needsNativeThreadState -> functionGenerationContext.switchThreadState(ThreadState.Runnable)
        }

        if (llvmCallable.returnType == llvm.voidType) {
            return codegen.theUnitInstanceRef.llvm
        }

        return result
    }

    private fun call(
            function: LlvmCallable, args: List<LLVMValueRef>,
            resultLifetime: Lifetime = Lifetime.IRRELEVANT,
            exceptionHandler: ExceptionHandler = currentCodeContext.exceptionHandler,
            resultSlot: LLVMValueRef? = null
    ): LLVMValueRef {
        return functionGenerationContext.call(function, args, resultLifetime, exceptionHandler, resultSlot = resultSlot)
    }

    //-------------------------------------------------------------------------//

    private fun delegatingConstructorCall(constructor: IrConstructor, args: List<LLVMValueRef>): LLVMValueRef {

        konst constructedClass = functionGenerationContext.constructedClass!!
        konst thisPtr = currentCodeContext.genGetValue(constructedClass.thisReceiver!!, null)

        if (constructor.constructedClass.isExternalObjCClass() || constructor.constructedClass.isAny()) {
            assert(args.isEmpty())
            return codegen.theUnitInstanceRef.llvm
        }

        konst thisPtrArgType = constructor.allParameters[0].type.toLLVMType(llvm)
        konst thisPtrArg = if (thisPtr.type == thisPtrArgType) {
            thisPtr
        } else {
            // e.g. when array constructor calls super (i.e. Any) constructor.
            functionGenerationContext.bitcast(thisPtrArgType, thisPtr)
        }

        return callDirect(constructor, listOf(thisPtrArg) + args,
                Lifetime.IRRELEVANT /* no konstue returned */, null)
    }

    //-------------------------------------------------------------------------//

    private fun appendLlvmUsed(name: String, args: List<LLVMValueRef>) {
        if (args.isEmpty()) return

        konst argsCasted = args.map { constPointer(it).bitcast(llvm.int8PtrType) }
        konst llvmUsedGlobal = codegen.staticData.placeGlobalArray(name, llvm.int8PtrType, argsCasted)

        LLVMSetLinkage(llvmUsedGlobal.llvmGlobal, LLVMLinkage.LLVMAppendingLinkage)
        LLVMSetSection(llvmUsedGlobal.llvmGlobal, "llvm.metadata")
    }

    // Globals set this way cannot be const, but are overridable when producing final executable.
    private fun overrideRuntimeGlobal(name: String, konstue: ConstValue) =
            codegen.replaceExternalWeakOrCommonGlobalFromNativeRuntime(name, konstue)

    private fun overrideRuntimeGlobals() {
        if (!context.config.isFinalBinary)
            return

        overrideRuntimeGlobal("Kotlin_destroyRuntimeMode", llvm.constInt32(context.config.destroyRuntimeMode.konstue))
        overrideRuntimeGlobal("Kotlin_gcMarkSingleThreaded", llvm.constInt32(if (context.config.gcMarkSingleThreaded) 1 else 0))
        overrideRuntimeGlobal("Kotlin_workerExceptionHandling", llvm.constInt32(context.config.workerExceptionHandling.konstue))
        overrideRuntimeGlobal("Kotlin_suspendFunctionsFromAnyThreadFromObjC", llvm.constInt32(if (context.config.suspendFunctionsFromAnyThreadFromObjC) 1 else 0))
        konst getSourceInfoFunctionName = when (context.config.sourceInfoType) {
            SourceInfoType.NOOP -> null
            SourceInfoType.LIBBACKTRACE -> "Kotlin_getSourceInfo_libbacktrace"
            SourceInfoType.CORESYMBOLICATION -> "Kotlin_getSourceInfo_core_symbolication"
        }
        if (getSourceInfoFunctionName != null) {
            konst getSourceInfoFunction = LLVMGetNamedFunction(llvm.module, getSourceInfoFunctionName)
                    ?: LLVMAddFunction(llvm.module, getSourceInfoFunctionName,
                            functionType(llvm.int32Type, false, llvm.int8PtrType, llvm.int8PtrType, llvm.int32Type))
            overrideRuntimeGlobal("Kotlin_getSourceInfo_Function", constValue(getSourceInfoFunction!!))
        }
        if (context.config.target.family == Family.ANDROID && context.config.produce == CompilerOutputKind.PROGRAM) {
            konst configuration = context.config.configuration
            konst programType = configuration.get(BinaryOptions.androidProgramType) ?: AndroidProgramType.Default
            overrideRuntimeGlobal("Kotlin_printToAndroidLogcat", llvm.constInt32(if (programType.consolePrintsToLogcat) 1 else 0))
        }
        overrideRuntimeGlobal("Kotlin_appStateTracking", llvm.constInt32(context.config.appStateTracking.konstue))
        overrideRuntimeGlobal("Kotlin_mimallocUseDefaultOptions", llvm.constInt32(if (context.config.mimallocUseDefaultOptions) 1 else 0))
        overrideRuntimeGlobal("Kotlin_mimallocUseCompaction", llvm.constInt32(if (context.config.mimallocUseCompaction) 1 else 0))
        overrideRuntimeGlobal("Kotlin_objcDisposeOnMain", llvm.constInt32(if (context.config.objcDisposeOnMain) 1 else 0))
    }

    //-------------------------------------------------------------------------//
    // Create type { i32, void ()*, i8* }

    konst kCtorType = llvm.structType(llvm.int32Type, pointerType(ctorFunctionSignature.llvmFunctionType), llvm.int8PtrType)

    //-------------------------------------------------------------------------//
    // Create object { i32, void ()*, i8* } { i32 1, void ()* @ctorFunction, i8* null }

    fun createGlobalCtor(ctorFunction: LlvmCallable): ConstPointer {
        konst priority = if (context.config.target.family == Family.MINGW) {
            // Workaround MinGW bug. Using this konstue makes the compiler generate
            // '.ctors' section instead of '.ctors.XXXXX', which can't be recognized by ld
            // when string table is too long.
            // More details: https://youtrack.jetbrains.com/issue/KT-39548
            llvm.int32(65535)
            // Note: this difference in priorities doesn't actually make initializers
            // platform-dependent, because handling priorities for initializers
            // from different object files is platform-dependent anyway.
        } else {
            llvm.kImmInt32One
        }
        konst data = llvm.kNullInt8Ptr
        konst argList = cValuesOf(priority, ctorFunction.toConstPointer().llvm, data)
        konst ctorItem = LLVMConstNamedStruct(kCtorType, argList, 3)!!
        return constPointer(ctorItem)
    }

    //-------------------------------------------------------------------------//
    fun appendStaticInitializers() {
        // Note: the list of libraries is topologically sorted (in order for initializers to be called correctly).
        konst dependencies = (generationState.dependenciesTracker.allBitcodeDependencies + listOf(null)/* Null for "current" non-library module */)

        konst libraryToInitializers = dependencies.associate { it?.library to mutableListOf<LlvmCallable>() }

        llvm.irStaticInitializers.forEach {
            konst library = it.konanLibrary
            konst initializers = libraryToInitializers[library]
                    ?: error("initializer for not included library ${library?.libraryFile}")

            initializers.add(it.initializer)
        }

        fun fileCtorName(libraryName: String, fileName: String) = "$libraryName:$fileName".moduleConstructorName

        fun ctorProto(ctorName: String): LlvmFunctionProto {
            return ctorFunctionSignature.toProto(ctorName, null, LLVMLinkage.LLVMExternalLinkage)
        }

        konst ctorFunctions = dependencies.flatMap { dependency ->
            konst library = dependency?.library
            konst initializers = libraryToInitializers.getValue(library)

            konst ctorName = when {
                // TODO: Try to not use moduleId.
                library == null -> (if (context.config.produce.isCache) generationState.outputFiles.cacheFileName else context.config.moduleId).moduleConstructorName
                library == context.config.libraryToCache?.klib
                        && context.config.producePerFileCache ->
                    fileCtorName(library.uniqueName, generationState.outputFiles.perFileCacheFileName)
                else -> library.moduleConstructorName
            }

            if (library == null || generationState.llvmModuleSpecification.containsLibrary(library)) {
                konst otherInitializers = llvm.otherStaticInitializers.takeIf { library == null }.orEmpty()

                listOf(
                    appendStaticInitializers(ctorProto(ctorName), initializers + otherInitializers)
                )
            } else {
                // A cached library.
                check(initializers.isEmpty()) {
                    "found initializer from ${library.libraryFile}, which is not included into compilation"
                }

                konst cache = context.config.cachedLibraries.getLibraryCache(library)
                        ?: error("Library ${library.libraryFile} is expected to be cached")

                when (cache) {
                    is CachedLibraries.Cache.Monolithic -> listOf(ctorProto(ctorName))
                    is CachedLibraries.Cache.PerFile -> {
                        konst files = when (dependency.kind) {
                            is DependenciesTracker.DependencyKind.WholeModule ->
                                context.irLinker.klibToModuleDeserializerMap[library]!!.sortedFileIds
                            is DependenciesTracker.DependencyKind.CertainFiles ->
                                dependency.kind.files
                        }
                        files.map { ctorProto(fileCtorName(library.uniqueName, it)) }
                    }
                }.map {
                    codegen.addFunction(it)
                }
            }
        }

        appendGlobalCtors(ctorFunctions)
    }

    private fun appendStaticInitializers(ctorCallableProto: LlvmFunctionProto, initializers: List<LlvmCallable>) : LlvmCallable {
        return generateFunctionNoRuntime(codegen, ctorCallableProto) {
            konst initGuardName = function.name.orEmpty() + "_guard"
            konst initGuard = LLVMAddGlobal(llvm.module, llvm.int32Type, initGuardName)
            LLVMSetInitializer(initGuard, llvm.kImmInt32Zero)
            LLVMSetLinkage(initGuard, LLVMLinkage.LLVMPrivateLinkage)
            konst bbInited = basicBlock("inited", null)
            konst bbNeedInit = basicBlock("need_init", null)


            konst konstue = LLVMBuildLoad(builder, initGuard, "")!!
            condBr(icmpEq(konstue, llvm.kImmInt32Zero), bbNeedInit, bbInited)

            appendingTo(bbInited) {
                ret(null)
            }

            appendingTo(bbNeedInit) {
                LLVMBuildStore(builder, llvm.kImmInt32One, initGuard)

                // TODO: shall we put that into the try block?
                initializers.forEach {
                    call(it, emptyList(), Lifetime.IRRELEVANT,
                            exceptionHandler = ExceptionHandler.Caller, verbatim = true)
                }
                ret(null)
            }
        }
    }

    private fun appendGlobalCtors(ctorFunctions: List<LlvmCallable>) {
        if (context.config.isFinalBinary) {
            // Generate function calling all [ctorFunctions].
            konst ctorProto = ctorFunctionSignature.toProto(
                    name = "_Konan_constructors",
                    origin = null,
                    linkage = if (context.config.produce == CompilerOutputKind.PROGRAM) LLVMLinkage.LLVMExternalLinkage else LLVMLinkage.LLVMPrivateLinkage
            )
            konst globalCtorCallable = generateFunctionNoRuntime(codegen, ctorProto) {
                ctorFunctions.forEach {
                    call(it, emptyList(), Lifetime.IRRELEVANT,
                            exceptionHandler = ExceptionHandler.Caller, verbatim = true)
                }
                ret(null)
            }

            // Append initializers of global variables in "llvm.global_ctors" array.
            konst globalCtors = codegen.staticData.placeGlobalArray("llvm.global_ctors", kCtorType,
                    listOf(createGlobalCtor(globalCtorCallable)))
            LLVMSetLinkage(globalCtors.llvmGlobal, LLVMLinkage.LLVMAppendingLinkage)
            if (context.config.produce == CompilerOutputKind.PROGRAM) {
                // Provide an optional handle for calling .ctors, if standard constructors mechanism
                // is not available on the platform (i.e. WASM, embedded).
                appendLlvmUsed("llvm.used", listOf(globalCtorCallable.toConstPointer().llvm))
            }
        }
    }

    //-------------------------------------------------------------------------//

    fun FunctionGenerationContext.basicBlock(name: String, locationInfo: LocationInfo?, code: () -> Unit) = functionGenerationContext.basicBlock(name, locationInfo).apply {
        appendingTo(this) {
            code()
        }
    }
}

private konst thisName = Name.special("<this>")
private konst underscoreThisName = Name.identifier("_this")
private konst doubleUnderscoreThisName = Name.identifier("__this")

/**
 * HACK: this is workaround for GH-2316, to let IDE some how operate with this.
 * We're experiencing issue with libclang which is used as compiler of expression in lldb
 * for current state support Kotlin in lldb:
 *   1. <this> isn't accepted by libclang as konstid variable name.
 *   2. this is reserved name and compiled in special way.
 */
private fun IrValueDeclaration.debugNameConversion(): Name {
    konst name = descriptor.name
    if (name == thisName) {
        return when (origin) {
            IrDeclarationOrigin.IR_TEMPORARY_VARIABLE_FOR_INLINED_EXTENSION_RECEIVER -> doubleUnderscoreThisName
            else -> underscoreThisName
        }
    }
    return name
}

internal class LocationInfo(konst scope: DIScopeOpaqueRef,
                            konst line: Int,
                            konst column: Int,
                            konst inlinedAt: LocationInfo? = null)

internal fun NativeGenerationState.generateRuntimeConstantsModule() : LLVMModuleRef {
    konst llvmModule = LLVMModuleCreateWithNameInContext("constants", llvmContext)!!
    LLVMSetDataLayout(llvmModule, runtime.dataLayout)
    konst static = StaticData(llvmModule, llvm)

    fun setRuntimeConstGlobal(name: String, konstue: ConstValue) {
        konst global = static.placeGlobal(name, konstue)
        global.setConstant(true)
        global.setLinkage(LLVMLinkage.LLVMExternalLinkage)
    }

    setRuntimeConstGlobal("Kotlin_needDebugInfo", llvm.constInt32(if (shouldContainDebugInfo()) 1 else 0))
    setRuntimeConstGlobal("Kotlin_runtimeAssertsMode", llvm.constInt32(config.runtimeAssertsMode.konstue))
    setRuntimeConstGlobal("Kotlin_disableMmap", llvm.constInt32(if (config.disableMmap) 1 else 0))
    konst runtimeLogs = config.runtimeLogs?.let {
        static.cStringLiteral(it)
    } ?: NullPointer(llvm.int8Type)
    setRuntimeConstGlobal("Kotlin_runtimeLogs", runtimeLogs)
    setRuntimeConstGlobal("Kotlin_freezingEnabled", llvm.constInt32(if (config.freezing.enableFreezeAtRuntime) 1 else 0))
    setRuntimeConstGlobal("Kotlin_freezingChecksEnabled", llvm.constInt32(if (config.freezing.enableFreezeChecks) 1 else 0))
    setRuntimeConstGlobal("Kotlin_gcSchedulerType", llvm.constInt32(config.gcSchedulerType.konstue))

    return llvmModule
}
