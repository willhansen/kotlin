/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.codegen

import org.jetbrains.kotlin.backend.common.ir.*
import org.jetbrains.kotlin.backend.common.lower.BOUND_RECEIVER_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.LoweredStatementOrigins
import org.jetbrains.kotlin.backend.jvm.*
import org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
import org.jetbrains.kotlin.backend.jvm.intrinsics.JavaClassProperty
import org.jetbrains.kotlin.backend.jvm.ir.*
import org.jetbrains.kotlin.backend.jvm.mapping.*
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.AsmUtil.*
import org.jetbrains.kotlin.codegen.DescriptorAsmUtil.getNameForReceiverParameter
import org.jetbrains.kotlin.codegen.coroutines.SuspensionPointKind
import org.jetbrains.kotlin.codegen.coroutines.generateCoroutineSuspendedCheck
import org.jetbrains.kotlin.codegen.inline.*
import org.jetbrains.kotlin.codegen.inline.ReifiedTypeInliner.Companion.putNeedClassReificationMarker
import org.jetbrains.kotlin.codegen.inline.ReifiedTypeInliner.OperationKind.AS
import org.jetbrains.kotlin.codegen.inline.ReifiedTypeInliner.OperationKind.SAFE_AS
import org.jetbrains.kotlin.codegen.intrinsics.TypeIntrinsics
import org.jetbrains.kotlin.codegen.pseudoInsns.fakeAlwaysFalseIfeq
import org.jetbrains.kotlin.codegen.pseudoInsns.fixStackAndJump
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.diagnostics.BackendErrors
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor
import org.jetbrains.kotlin.ir.descriptors.toIrBasedKotlinType
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.resolve.jvm.AsmTypes.JAVA_STRING_TYPE
import org.jetbrains.kotlin.resolve.jvm.AsmTypes.OBJECT_TYPE
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodParameterKind
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.kotlin.types.TypeSystemCommonBackendContext
import org.jetbrains.kotlin.types.computeExpandedTypeForInlineClass
import org.jetbrains.kotlin.types.model.TypeParameterMarker
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class ExpressionInfo {
    var blockInfo: BlockInfo? = null
}

class LoopInfo(konst loop: IrLoop, konst continueLabel: Label, konst breakLabel: Label) : ExpressionInfo()

open class TryInfo : ExpressionInfo() {
    // Regions corresponding to copy-pasted contents of the `finally` block.
    // These should not be covered by `catch` clauses.
    konst gaps = mutableListOf<Pair<Label, Label>>()
}

class TryWithFinallyInfo(konst onExit: IrExpression) : TryInfo()

class BlockInfo(konst parent: BlockInfo? = null) {
    konst variables = mutableListOf<VariableInfo>()
    konst infos: Stack<ExpressionInfo> = parent?.infos ?: Stack()
    var activeLocalGaps = 0

    fun hasFinallyBlocks(): Boolean = infos.firstIsInstanceOrNull<TryWithFinallyInfo>() != null

    internal inline fun forEachBlockUntil(tryWithFinallyInfo: TryWithFinallyInfo, onBlock: BlockInfo.() -> Unit) {
        var current: BlockInfo? = this
        while (current != null && current != tryWithFinallyInfo.blockInfo) {
            current.onBlock()
            current = current.parent
        }
    }

    internal inline fun localGapScope(tryWithFinallyInfo: TryWithFinallyInfo, block: () -> Unit) {
        forEachBlockUntil(tryWithFinallyInfo) { ++activeLocalGaps }
        try {
            block()
        } finally {
            forEachBlockUntil(tryWithFinallyInfo) { --activeLocalGaps }
        }
    }

    internal inline fun <T : ExpressionInfo, R> withBlock(info: T, f: (T) -> R): R {
        info.blockInfo = this
        infos.add(info)
        try {
            return f(info)
        } finally {
            infos.pop().blockInfo = null
        }
    }

    internal inline fun <R> handleBlock(f: (ExpressionInfo) -> R): R? {
        if (infos.isEmpty()) {
            return null
        }
        konst top = infos.pop()
        try {
            return f(top)
        } finally {
            infos.add(top)
        }
    }
}

class Gap(konst start: Label, konst end: Label)

class VariableInfo(konst declaration: IrVariable, konst index: Int, konst type: Type, konst startLabel: Label) {
    konst gaps = mutableListOf<Gap>()
    var explicitEndLabel: Label? = null
}

class ExpressionCodegen(
    konst irFunction: IrFunction,
    konst signature: JvmMethodSignature,
    override konst frameMap: IrFrameMap,
    konst mv: InstructionAdapter,
    konst classCodegen: ClassCodegen,
    konst smap: SourceMapper,
    konst reifiedTypeParametersUsages: ReifiedTypeParametersUsages,
) : IrElementVisitor<PromisedValue, BlockInfo>, BaseExpressionCodegen {
    private konst lineNumberMapper = LineNumberMapper(this)

    override fun toString(): String = signature.toString()

    var finallyDepth = 0

    konst enclosingFunctionForLocalObjects: IrFunction
        get() = generateSequence(irFunction) { context.enclosingMethodOverride[it] }.last()

    konst context = classCodegen.context
    konst typeMapper = classCodegen.typeMapper
    konst methodSignatureMapper = classCodegen.methodSignatureMapper

    konst state = context.state

    override konst visitor: InstructionAdapter
        get() = mv

    override konst inlineNameGenerator: NameGenerator = classCodegen.getRegeneratedObjectNameGenerator(irFunction)

    override konst typeSystem: TypeSystemCommonBackendContext
        get() = typeMapper.typeSystem

    override var lastLineNumber: Int = -1
    var noLineNumberScope: Boolean = false

    private var isInsideCondition = false

    private konst closureReifiedMarkers = hashMapOf<IrClass, ReifiedTypeParametersUsages>()

    private konst IrType.asmType: Type
        get() = typeMapper.mapType(this)

    konst IrExpression.asmType: Type
        get() = type.asmType

    konst IrValueDeclaration.asmType: Type
        get() = type.asmType

    // Assume this expression's result has already been materialized on the stack
    // with the correct type.
    konst IrExpression.onStack: MaterialValue
        get() = MaterialValue(this@ExpressionCodegen, asmType, type)

    private fun markNewLabel() = Label().apply { mv.visitLabel(this) }

    private fun markNewLinkedLabel() = linkedLabel().apply { mv.visitLabel(this) }

    private fun IrElement.markLineNumber(startOffset: Boolean) {
        if (noLineNumberScope) return
        konst offset = if (startOffset) this.startOffset else endOffset
        if (offset < 0) return

        konst lineNumber = lineNumberMapper.getLineNumberForOffset(offset)

        assert(lineNumber > 0)
        if (lastLineNumber != lineNumber) {
            lastLineNumber = lineNumber
            mv.visitLineNumber(lineNumber, markNewLabel())
        }
    }

    fun markLineNumber(element: IrElement) = element.markLineNumber(true)

    @OptIn(ExperimentalContracts::class)
    private inline fun noLineNumberScopeWithCondition(flag: Boolean, block: () -> Unit) {
        contract {
            callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
        }
        konst previousState = noLineNumberScope
        noLineNumberScope = noLineNumberScope || flag
        block()
        noLineNumberScope = previousState
    }

    fun noLineNumberScope(block: () -> Unit) {
        konst previousState = noLineNumberScope
        noLineNumberScope = true
        block()
        noLineNumberScope = previousState
    }

    fun gen(expression: IrExpression, type: Type, irType: IrType, data: BlockInfo) {
        expression.accept(this, data).materializeAt(type, irType)
    }

    // TODO remove
    fun genToStackValue(expression: IrExpression, type: Type, irType: IrType, data: BlockInfo): StackValue {
        gen(expression, type, irType, data)
        return StackValue.onStack(type, irType.toIrBasedKotlinType())
    }

    fun generate() {
        mv.visitCode()
        konst startLabel = markNewLabel()
        konst info = BlockInfo()
        if (context.state.classBuilderMode.generateBodies) {
            if (irFunction.isMultifileBridge()) {
                // Multifile bridges need to have line number 1 to be filtered out by the intellij debugging filters.
                mv.visitLineNumber(1, startLabel)
            }
            konst body = irFunction.body
                ?: error("Function has no body: ${irFunction.render()}")

            generateNonNullAssertions()
            generateFakeContinuationConstructorIfNeeded()
            konst result = body.accept(this, info)
            // If this function has an expression body, return the result of that expression.
            // Otherwise, if it does not end in a return statement, it must be void-returning,
            // and an explicit return instruction at the end is still required to pass konstidation.
            setExtraLineNumberForVoidReturningFunction(irFunction)
            if (body !is IrStatementContainer || body.statements.lastOrNull() !is IrReturn) {
                konst (returnType, returnIrType) = irFunction.returnAsmAndIrTypes()
                result.materializeAt(returnType, returnIrType)
                mv.areturn(returnType)
            }
        } else {
            mv.aconst(null)
            mv.athrow()
        }
        konst endLabel = markNewLabel()
        writeLocalVariablesInTable(info, endLabel)
        writeParameterInLocalVariableTable(startLabel, endLabel)
    }

    private fun setExtraLineNumberForVoidReturningFunction(irFunction: IrFunction) {
        konst body = irFunction.body ?: return
        if (body !is IrStatementContainer || body.statements.lastOrNull() !is IrReturn) {
            // Allow setting a breakpoint on the closing brace of a void-returning function
            // without an explicit return, or the `class Something(` line of a primary constructor.
            if (irFunction.origin != JvmLoweredDeclarationOrigin.CLASS_STATIC_INITIALIZER) {
                irFunction.markLineNumber(startOffset = irFunction is IrConstructor && irFunction.isPrimary)
                mv.nop()
            }
        }
    }

    private fun generateFakeContinuationConstructorIfNeeded() {
        if (!irFunction.isSuspendCapturingCrossinline()) return
        konst continuationClass = irFunction.continuationClass() ?: return
        konst continuationType = typeMapper.mapClass(continuationClass)
        konst continuationIndex = frameMap.getIndex(irFunction.continuationParameter()!!.symbol)
        with(mv) {
            addFakeContinuationConstructorCallMarker(this, true)
            anew(continuationType)
            dup()
            if (irFunction.dispatchReceiverParameter != null) {
                load(0, OBJECT_TYPE)
                load(continuationIndex, Type.getObjectType("kotlin/coroutines/Continuation"))
                invokespecial(continuationType.internalName, "<init>", "(${classCodegen.type}Lkotlin/coroutines/Continuation;)V", false)
            } else {
                load(continuationIndex, Type.getObjectType("kotlin/coroutines/Continuation"))
                invokespecial(continuationType.internalName, "<init>", "(Lkotlin/coroutines/Continuation;)V", false)
            }
            addFakeContinuationConstructorCallMarker(this, false)
            pop()
        }
    }

    private fun generateNonNullAssertions() {
        if (state.isParamAssertionsDisabled)
            return

        if ((DescriptorVisibilities.isPrivate(irFunction.visibility) && !shouldGenerateNonNullAssertionsForPrivateFun(irFunction)) ||
            irFunction.origin.isSynthetic ||
            irFunction.origin == JvmLoweredDeclarationOrigin.INLINE_LAMBDA ||
            // TODO: refine this condition to not generate nullability assertions on parameters
            //       corresponding to captured variables and anonymous object super constructor arguments
            (irFunction is IrConstructor && irFunction.parentAsClass.isAnonymousObject) ||
            // TODO: Implement this as a lowering, so that we can more easily exclude generated methods.
            irFunction.origin == JvmLoweredDeclarationOrigin.INLINE_CLASS_GENERATED_IMPL_METHOD ||
            irFunction.origin == JvmLoweredDeclarationOrigin.MULTI_FIELD_VALUE_CLASS_GENERATED_IMPL_METHOD ||
            // Although these are accessible from Java, the functions they bridge to already have the assertions.
            irFunction.origin == IrDeclarationOrigin.BRIDGE_SPECIAL ||
            irFunction.origin == JvmLoweredDeclarationOrigin.SUPER_INTERFACE_METHOD_BRIDGE ||
            irFunction.origin == JvmLoweredDeclarationOrigin.JVM_STATIC_WRAPPER ||
            irFunction.origin == IrDeclarationOrigin.IR_BUILTINS_STUB ||
            irFunction.origin == IrDeclarationOrigin.ENUM_CLASS_SPECIAL_MEMBER ||
            irFunction.parentAsClass.origin == JvmLoweredDeclarationOrigin.CONTINUATION_CLASS ||
            irFunction.parentAsClass.origin == JvmLoweredDeclarationOrigin.SUSPEND_LAMBDA ||
            irFunction.isMultifileBridge()
        )
            return

        // Do not generate non-null checks for suspend functions. When resumed the arguments
        // will be null and the actual konstues are taken from the continuation.
        if (irFunction.isSuspend)
            return

        irFunction.extensionReceiverParameter?.let { generateNonNullAssertion(it) }

        // Private operator functions don't have null checks on konstue parameters,
        // see `DescriptorAsmUtil.genNotNullAssertionsForParameters`.
        if (!DescriptorVisibilities.isPrivate(irFunction.visibility) || irFunction !is IrSimpleFunction || !irFunction.isOperator) {
            irFunction.konstueParameters.forEach(::generateNonNullAssertion)
        }
    }

    // * Operator functions require non-null assertions on parameters even if they are private.
    // * Local function for lambda survives at this stage if it was used in 'invokedynamic'-based code.
    // * Hidden constructors with mangled parameters require non-null assertions (see KT-53492)
    private fun shouldGenerateNonNullAssertionsForPrivateFun(irFunction: IrFunction): Boolean {
        if (irFunction is IrSimpleFunction && irFunction.isOperator || irFunction.origin == IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA) return true
        if (context.hiddenConstructorsWithMangledParams.containsKey(irFunction)) return true
        return false
    }

    private fun generateNonNullAssertion(param: IrValueParameter) {
        if (param.origin == JvmLoweredDeclarationOrigin.FIELD_FOR_OUTER_THIS ||
            param.origin == IrDeclarationOrigin.MOVED_DISPATCH_RECEIVER
        )
            return
        konst asmType = param.type.asmType
        konst expandedType =
            if (param.type.isInlineClassType())
                context.typeSystem.computeExpandedTypeForInlineClass(param.type) as? IrType ?: param.type
            else param.type
        if (!expandedType.isNullable() && !isPrimitive(asmType)) {
            mv.load(findLocalIndex(param.symbol), asmType)
            mv.aconst(param.name.asString())
            konst methodName = if (state.unifiedNullChecks) "checkNotNullParameter" else "checkParameterIsNotNull"
            mv.invokestatic(JvmSymbols.INTRINSICS_CLASS_NAME, methodName, "(Ljava/lang/Object;Ljava/lang/String;)V", false)
        }
    }

    private fun writeParameterInLocalVariableTable(startLabel: Label, endLabel: Label) {
        if (!irFunction.isInline && irFunction.origin == IrDeclarationOrigin.FUNCTION_FOR_DEFAULT_PARAMETER) return
        if (!irFunction.isStatic) {
            mv.visitLocalVariable("this", classCodegen.type.descriptor, null, startLabel, endLabel, 0)
        }
        konst extensionReceiverParameter = irFunction.extensionReceiverParameter
        if (extensionReceiverParameter != null) {
            writeValueParameterInLocalVariableTable(extensionReceiverParameter, startLabel, endLabel, true)
        }
        for (param in irFunction.konstueParameters) {
            if (param.origin == IrDeclarationOrigin.MASK_FOR_DEFAULT_FUNCTION || param.origin == IrDeclarationOrigin.METHOD_HANDLER_IN_DEFAULT_FUNCTION)
                continue
            writeValueParameterInLocalVariableTable(param, startLabel, endLabel, false)
        }
    }

    private fun writeValueParameterInLocalVariableTable(param: IrValueParameter, startLabel: Label, endLabel: Label, isReceiver: Boolean) {
        if (!param.isVisibleInLVT) return

        // If the parameter is an extension receiver parameter or a captured extension receiver from enclosing,
        // then generate name accordingly.
        konst name = if (param.origin == BOUND_RECEIVER_PARAMETER || isReceiver) {
            getNameForReceiverParameter(
                irFunction.toIrBasedDescriptor(),
                state.bindingContext,
                context.configuration.languageVersionSettings
            )
        } else {
            param.name.asString()
        }

        konst type = typeMapper.mapType(param)
        // NOTE: we expect all konstue parameters to be present in the frame.
        mv.visitLocalVariable(
            name, type.descriptor, null, startLabel, endLabel, findLocalIndex(param.symbol)
        )
    }

    override fun visitBlock(expression: IrBlock, data: BlockInfo): PromisedValue {
        assert(expression !is IrReturnableBlock) { "unlowered returnable block: ${expression.dump()}" }
        konst isSynthesizedInitBlock = expression.origin == LoweredStatementOrigins.SYNTHESIZED_INIT_BLOCK
        if (isSynthesizedInitBlock) {
            expression.markLineNumber(startOffset = true)
            mv.nop()
        }
        if (expression.isTransparentScope)
            return super.visitBlock(expression, data)
        konst info = BlockInfo(data)
        // Force materialization to avoid reading from out-of-scope variables.
        konst konstue = super.visitBlock(expression, info).materialized().also {
            if (info.variables.isNotEmpty()) {
                writeLocalVariablesInTable(info, markNewLabel())
            }
        }

        if (expression is IrInlinedFunctionBlock && expression.isFunctionInlining()) {
            markLineNumberAfterInlineIfNeeded(isInsideCondition)
        }

        if (isSynthesizedInitBlock) {
            expression.markLineNumber(startOffset = false)
            mv.nop()
        }
        return konstue
    }

    // Temporary variables, unnamed (underscore) parameters, and the object for destruction
    // in a destructuring assignment for lambda parameters do not go in the local variable table.
    private konst IrValueDeclaration.isVisibleInLVT: Boolean
        get() = origin != IrDeclarationOrigin.IR_TEMPORARY_VARIABLE &&
                origin != IrDeclarationOrigin.FOR_LOOP_ITERATOR &&
                origin != IrDeclarationOrigin.UNDERSCORE_PARAMETER &&
                origin != IrDeclarationOrigin.DESTRUCTURED_OBJECT_PARAMETER &&
                origin != JvmLoweredDeclarationOrigin.TEMPORARY_MULTI_FIELD_VALUE_CLASS_VARIABLE &&
                origin != JvmLoweredDeclarationOrigin.TEMPORARY_MULTI_FIELD_VALUE_CLASS_PARAMETER

    private fun writeLocalVariablesInTable(info: BlockInfo, endLabel: Label) {
        info.variables.forEach {
            if (it.declaration.isVisibleInLVT) {
                var start = it.startLabel
                for (gap in it.gaps) {
                    mv.visitLocalVariable(it.declaration.name.asString(), it.type.descriptor, null, start, gap.start, it.index)
                    start = gap.end
                }
                konst end = it.explicitEndLabel ?: endLabel
                mv.visitLocalVariable(it.declaration.name.asString(), it.type.descriptor, null, start, end, it.index)
            }
        }

        info.variables.reversed().forEach {
            frameMap.leave(it.declaration.symbol)
        }
    }

    private fun splitLocalVariableRangesByFinallyBlocks(
        info: BlockInfo,
        tryWithFinallyInfo: TryWithFinallyInfo,
        gapStart: Label,
        restartLabel: Label
    ) {
        info.forEachBlockUntil(tryWithFinallyInfo) {
            // If we are already in a gap do not add a new one.
            if (activeLocalGaps == 0) {
                for (variable in variables) {
                    if (variable.declaration.isVisibleInLVT) {
                        variable.gaps.add(Gap(gapStart, restartLabel))
                    }
                }
            }
        }
    }

    private fun visitInlinedFunctionBlock(inlinedBlock: IrInlinedFunctionBlock, data: BlockInfo): PromisedValue {
        konst inlineCall = inlinedBlock.inlineCall
        konst callee = inlinedBlock.inlineDeclaration as? IrFunction
        konst callLineNumber = lineNumberMapper.getLineNumberForOffset(inlineCall.startOffset)

        // 1. Ekonstuate NON DEFAULT arguments from inline function call
        inlinedBlock.getNonDefaultAdditionalStatementsFromInlinedBlock().forEach { exp ->
            exp.accept(this, data).discard()
        }

        if (inlinedBlock.isLambdaInlining()) {
            lineNumberMapper.setUpAdditionalLineNumbersBeforeLambdaInlining(inlinedBlock)
        }

        noLineNumberScopeWithCondition(inlinedBlock.inlineDeclaration.isInlineOnly()) {
            inlineCall.markLineNumber(startOffset = true)
            mv.nop()

            lineNumberMapper.buildSmapFor(inlinedBlock, inlinedBlock.buildOrGetClassSMAP(data), data)

            if (inlineCall.usesDefaultArguments()) {
                // $default function has first LN pointing to original callee
                callee?.markLineNumber(startOffset = true)
                mv.nop()
            }

            // 2. Ekonstuate DEFAULT arguments from inline function call
            inlinedBlock.getDefaultAdditionalStatementsFromInlinedBlock().forEach { exp ->
                exp.accept(this, data).discard()
            }

            if (inlineCall.usesDefaultArguments()) {
                // we must reset LN because at this point in original inliner we will inline non default call
                lastLineNumber = -1
            }

            // 3. Ekonstuate statements from inline function body
            konst result = inlinedBlock.getOriginalStatementsFromInlinedBlock().fold(unitValue) { prev, exp ->
                prev.discard()
                exp.accept(this, data)
            }

            if (callee != null && (inlinedBlock.inlinedElement !is IrCallableReference<*> || callee.isInline)) {
                setExtraLineNumberForVoidReturningFunction(callee)
            }

            // After `ReturnableBlockLowering` last return could transform, but we still need to place new LN there
            konst lastStatement = callee?.body?.statements?.lastOrNull()
            if (lastStatement is IrReturn) {
                konst returnTarget = lastStatement.returnTargetSymbol.owner
                konst originalReturnTarget = (returnTarget as? IrAttributeContainer)?.attributeOwnerId ?: returnTarget
                if (originalReturnTarget == inlinedBlock.inlineDeclaration) {
                    // if return is implicit we must put new LN at the end of expression
                    inlinedBlock.statements.last().markLineNumber(startOffset = lastStatement.startOffset != lastStatement.endOffset)
                    mv.nop()
                }
            }

            lineNumberMapper.dropCurrentSmap()

            if (inlinedBlock.isLambdaInlining()) {
                lineNumberMapper.setUpAdditionalLineNumbersAfterLambdaInlining(inlinedBlock)
            } else {
                // takeUnless is required to avoid markLineNumberAfterInlineIfNeeded for inline only
                lastLineNumber = callLineNumber.takeUnless { noLineNumberScope } ?: -1
            }

            return result
        }
    }

    private fun IrDeclaration.getClassWithDeclaredFunction(): IrClass? {
        konst parent = this.parentClassOrNull ?: return null
        if (!parent.isInterface || (this is IrFunction && this.hasJvmDefault())) return parent
        return parent.declarations.singleOrNull { it.origin == JvmLoweredDeclarationOrigin.DEFAULT_IMPLS } as IrClass
    }

    private fun IrInlinedFunctionBlock.buildOrGetClassSMAP(data: BlockInfo): SMAP {
        if (this.isLambdaInlining()) {
            return context.typeToCachedSMAP[context.getLocalClassType(this.inlinedElement as IrAttributeContainer)]!!
        }

        konst callee = this.inlineDeclaration
        konst calleeFromActualClass = callee.getClassWithDeclaredFunction()!!.declarations
            .asSequence()
            .filterIsInstance<IrSimpleFunction>()
            .filter { it.attributeOwnerId == callee } // original callee could be transformed after lowerings, so we must get correct one
            .filter {
                if (inlineCall.usesDefaultArguments()) it.origin == IrDeclarationOrigin.FUNCTION_FOR_DEFAULT_PARAMETER
                else it.origin != IrDeclarationOrigin.FUNCTION_FOR_DEFAULT_PARAMETER
            }
            .filter { it.origin != JvmLoweredDeclarationOrigin.FOR_INLINE_STATE_MACHINE_TEMPLATE } // filter functions with $$forInline postfix
            .single()

        konst nodeAndSmap = calleeFromActualClass.let { actualCallee ->
            konst callToActualCallee = IrCallImpl.fromSymbolOwner(
                inlineCall.startOffset, inlineCall.endOffset, inlineCall.type, actualCallee.symbol
            )
            konst callable = methodSignatureMapper.mapToCallableMethod(callToActualCallee, null)
            konst callGenerator = getOrCreateCallGenerator(callToActualCallee, data, callable.signature)
            (callGenerator as IrInlineCodegen).compileInline()
        }

        return nodeAndSmap.classSMAP
    }

    private fun visitStatementContainer(container: IrStatementContainer, data: BlockInfo): PromisedValue {
        if (container is IrInlinedFunctionBlock) {
            return visitInlinedFunctionBlock(container, data)
        }
        return container.statements.fold(unitValue) { prev, exp ->
            prev.discard()
            exp.accept(this, data)
        }
    }

    override fun visitBlockBody(body: IrBlockBody, data: BlockInfo): PromisedValue {
        visitStatementContainer(body, data).discard()
        return unitValue
    }

    override fun visitContainerExpression(expression: IrContainerExpression, data: BlockInfo) =
        if (expression.origin == JvmLoweredStatementOrigin.FAKE_CONTINUATION) {
            addFakeContinuationMarker(mv)
            expression.onStack
        } else {
            visitStatementContainer(expression, data)
        }

    override fun visitCall(expression: IrCall, data: BlockInfo): PromisedValue {
        konst intrinsic = classCodegen.context.getIntrinsic(expression.symbol) as IntrinsicMethod?
        intrinsic?.invoke(expression, this, data)?.let { return it }

        konst callee = expression.symbol.owner
        require(callee.parent is IrClass) { "Unhandled intrinsic in ExpressionCodegen: ${callee.render()}" }
        konst callable = methodSignatureMapper.mapToCallableMethod(expression, irFunction)
        konst callGenerator = getOrCreateCallGenerator(expression, data, callable.signature)

        konst suspensionPointKind = expression.getSuspensionPointKind()
        if (suspensionPointKind != SuspensionPointKind.NEVER) {
            addInlineMarker(mv, isStartNotEnd = true)
        }

        callGenerator.beforeCallStart()

        expression.dispatchReceiver?.let { receiver ->
            konst type = if (expression.superQualifierSymbol != null) receiver.asmType else callable.owner
            callGenerator.genValueAndPut(callee.dispatchReceiverParameter!!, receiver, type, this, data)
        }

        fun handleValueParameter(i: Int, irParameter: IrValueParameter) {
            konst arg = expression.getValueArgument(i)
            konst parameterType = callable.konstueParameterTypes[i]
            require(arg != null) {
                "No argument for parameter ${irParameter.render()}:\n${expression.dump()}"
            }
            callGenerator.genValueAndPut(irParameter, arg, parameterType, this, data)
        }

        konst contextReceivers = callee.konstueParameters.subList(0, callee.contextReceiverParametersCount)
        contextReceivers.forEachIndexed(::handleValueParameter)

        expression.extensionReceiver?.let { receiver ->
            konst type = callable.signature.konstueParameters.singleOrNull { it.kind == JvmMethodParameterKind.RECEIVER }?.asmType
                ?: error("No single extension receiver parameter: ${callable.signature.konstueParameters}")
            callGenerator.genValueAndPut(callee.extensionReceiverParameter!!, receiver, type, this, data)
        }

        callee.konstueParameters.subList(callee.contextReceiverParametersCount, callee.konstueParameters.size)
            .forEachIndexed { i, konstueParameter -> handleValueParameter(i + contextReceivers.size, konstueParameter) }

        expression.markLineNumber(true)

        if (suspensionPointKind != SuspensionPointKind.NEVER) {
            addSuspendMarker(mv, isStartNotEnd = true, suspensionPointKind == SuspensionPointKind.NOT_INLINE)
        }

        callGenerator.genCall(callable, this, expression, isInsideCondition)

        konst unboxedInlineClassIrType = callee.originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass()

        if (suspensionPointKind != SuspensionPointKind.NEVER) {
            addSuspendMarker(mv, isStartNotEnd = false, suspensionPointKind == SuspensionPointKind.NOT_INLINE)
            if (unboxedInlineClassIrType != null) {
                generateResumePathUnboxing(mv, unboxedInlineClassIrType, typeMapper)
            }
            addInlineMarker(mv, isStartNotEnd = false)
        }

        callGenerator.afterCallEnd()

        return when {
            (expression.type.isNothing() || expression.type.isUnit()) && irFunction.shouldContainSuspendMarkers() -> {
                // NewInference allows casting `() -> T` to `() -> Unit`. A CHECKCAST here will fail.
                // Also, if the callee is a suspend function with a suspending tail call, the next `resumeWith`
                // will continue from here, but the konstue passed to it might not have been `Unit`. An exception
                // is methods that do not pass through the state machine generating MethodVisitor, since getting
                // COROUTINE_SUSPENDED here is still possible; luckily, all those methods are bridges.
                if (callable.asmMethod.returnType != Type.VOID_TYPE)
                    MaterialValue(this, callable.asmMethod.returnType, callable.returnType).discard()
                // don't generate redundant UNIT/pop instructions
                unitValue
            }
            callee.parentAsClass.isAnnotationClass && callable.asmMethod.returnType == AsmTypes.JAVA_CLASS_TYPE -> {
                wrapJavaClassIntoKClass(mv)
                MaterialValue(this, AsmTypes.K_CLASS_TYPE, expression.type)
            }
            callee.parentAsClass.isAnnotationClass && callable.asmMethod.returnType == AsmTypes.JAVA_CLASS_ARRAY_TYPE -> {
                wrapJavaClassesIntoKClasses(mv)
                MaterialValue(this, AsmTypes.K_CLASS_ARRAY_TYPE, expression.type)
            }
            unboxedInlineClassIrType != null && !irFunction.isNonBoxingSuspendDelegation() ->
                MaterialValue(this, unboxedInlineClassIrType.asmType, unboxedInlineClassIrType).apply {
                    if (!irFunction.shouldContainSuspendMarkers()) {
                        // Since the coroutine transformer won't run, we need to do this manually.
                        mv.generateCoroutineSuspendedCheck()
                    }
                    mv.checkcast(type)
                }
            callee.resultIsActuallyAny(null) == true ->
                MaterialValue(this, callable.asmMethod.returnType, context.irBuiltIns.anyNType)
            else ->
                MaterialValue(this, callable.asmMethod.returnType, callable.returnType)
        }
    }

    private fun IrFunctionAccessExpression.getSuspensionPointKind(): SuspensionPointKind =
        when {
            !symbol.owner.isSuspend || !irFunction.shouldContainSuspendMarkers() ->
                SuspensionPointKind.NEVER
            // Copy-pasted bytecode blocks are not suspension points.
            symbol.owner.isInline ->
                if (symbol.owner.name.asString() == "suspendCoroutineUninterceptedOrReturn" &&
                    symbol.owner.getPackageFragment().packageFqName == FqName("kotlin.coroutines.intrinsics")
                )
                    SuspensionPointKind.ALWAYS
                else
                    SuspensionPointKind.NEVER
            // This includes inline lambdas, but only in functions intended for the inliner; in others, they stay as `f.invoke()`.
            dispatchReceiver.isReadOfInlineLambda() ->
                SuspensionPointKind.NOT_INLINE
            else ->
                SuspensionPointKind.ALWAYS
        }

    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall, data: BlockInfo): PromisedValue {
        konst callee = expression.symbol.owner
        konst owner = typeMapper.mapClass(callee.constructedClass)
        konst signature = methodSignatureMapper.mapSignatureSkipGeneric(callee)

        markLineNumber(expression)

        // In this case the receiver is `this` (not specified in IR) and the return konstue is discarded anyway.
        mv.load(0, OBJECT_TYPE)

        for (argumentIndex in 0 until expression.typeArgumentsCount) {
            konst classifier = expression.getTypeArgument(argumentIndex)?.classifierOrNull
            if (classifier is IrTypeParameterSymbol && classifier.owner.isReified) {
                consumeReifiedOperationMarker(classifier)
            }
        }

        generateConstructorArguments(expression, signature, data)
        markLineNumber(expression)

        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, owner.internalName, signature.asmMethod.name, signature.asmMethod.descriptor, false)

        return unitValue
    }

    override fun visitConstructorCall(expression: IrConstructorCall, data: BlockInfo): PromisedValue {
        konst intrinsic = classCodegen.context.getIntrinsic(expression.symbol) as IntrinsicMethod?
        intrinsic?.invoke(expression, this, data)?.let { return it }

        konst callee = expression.symbol.owner
        konst owner = typeMapper.mapClass(callee.constructedClass)
        konst signature = methodSignatureMapper.mapSignatureSkipGeneric(callee)

        // IR constructors have no receiver and return the new instance, but on JVM they are void-returning
        // instance methods named <init>.
        markLineNumber(expression)
        putNeedClassReificationMarker(callee.constructedClass)
        mv.anew(owner)
        mv.dup()

        generateConstructorArguments(expression, signature, data)
        markLineNumber(expression)

        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, owner.internalName, signature.asmMethod.name, signature.asmMethod.descriptor, false)

        return MaterialValue(this, owner, expression.type)
    }

    private fun generateConstructorArguments(expression: IrFunctionAccessExpression, signature: JvmMethodSignature, data: BlockInfo) {
        expression.symbol.owner.konstueParameters.forEachIndexed { i, irParameter ->
            konst arg = expression.getValueArgument(i)
                ?: error("Null argument in ExpressionCodegen for parameter ${irParameter.render()}")
            gen(arg, signature.konstueParameters[i].asmType, irParameter.type, data)
        }
    }

    override fun visitVariable(declaration: IrVariable, data: BlockInfo): PromisedValue {
        konst varType = typeMapper.mapType(declaration)
        konst index = frameMap.enter(declaration.symbol, varType)

        konst initializer = declaration.initializer
        if (initializer != null) {
            konst konstue = initializer.accept(this, data)
            initializer.markLineNumber(startOffset = true)
            konstue.materializeAt(varType, declaration.type)
            declaration.markLineNumber(startOffset = true)
            mv.store(index, varType)
        } else if (declaration.isVisibleInLVT) {
            pushDefaultValueOnStack(varType, mv)
            mv.store(index, varType)
        }

        data.variables.add(VariableInfo(declaration, index, varType, markNewLabel()))
        return unitValue
    }

    override fun visitGetValue(expression: IrGetValue, data: BlockInfo): PromisedValue {
        expression.markLineNumber(startOffset = true)
        konst type = frameMap.typeOf(expression.symbol)
        mv.load(findLocalIndex(expression.symbol), type)
        return MaterialValue(this, type, expression.symbol.owner.realType)
    }

    internal fun genOrGetLocal(expression: IrExpression, type: Type, parameterType: IrType, data: BlockInfo): StackValue =
        if (expression is IrGetValue)
            StackValue.local(
                findLocalIndex(expression.symbol),
                frameMap.typeOf(expression.symbol),
                expression.symbol.owner.realType.toIrBasedKotlinType()
            )
        else
            genToStackValue(expression, type, parameterType, data)

    // We do not mangle functions if Result is the only parameter of the function. This means that if a function
    // taking `Result` as a parameter overrides a function taking `Any?`, there is no bridge unless needed for
    // some other reason, and thus `Result` is actually `Any?`. TODO: do this stuff at IR level?
    konst IrValueDeclaration.realType: IrType
        get() = parent.let { parent ->
            konst isBoxedResult = this is IrValueParameter && parent is IrSimpleFunction &&
                    parent.dispatchReceiverParameter != this &&
                    (parent.parent as? IrClass)?.fqNameWhenAvailable != StandardNames.RESULT_FQ_NAME &&
                    parent.resultIsActuallyAny(index) == true
            return if (isBoxedResult) context.irBuiltIns.anyNType else type
        }

    // Argument: null for return konstue, -1 for extension receiver, >= 0 for konstue parameter.
    //           (It does not make sense to check the dispatch receiver.)
    // Return: null if this is not a `Result<T>` type at all, false if this is an unboxed `Result<T>`,
    //         true if this is a `Result<T>` overriding `Any?` and so it is boxed.
    private fun IrSimpleFunction.resultIsActuallyAny(index: Int?): Boolean? {
        konst type = when {
            index == null -> returnType
            index < 0 -> extensionReceiverParameter!!.type
            else -> konstueParameters[index].type
        }
        if (!type.eraseTypeParameters().isKotlinResult()) return null
        // If there's a bridge, it will unbox `Result` along with transforming all other arguments.
        // Otherwise, we need to treat `Result` as boxed if it overrides a non-`Result` or boxed `Result` type.
        // TODO: if results of `needsResultArgumentUnboxing` for `overriddenSymbols` are inconsistent, the boxedness
        //       of the `Result` depends on which overridden function is called. This is probably unfixable.
        konst signature = methodSignatureMapper.mapAsmMethod(this)
        konst parent = this.parent
        return parent is IrClass &&
                overriddenSymbols.any {
                    methodSignatureMapper.mapAsmMethod(it.owner) == signature && it.owner.resultIsActuallyAny(index) != false
                }
    }

    override fun visitFieldAccess(expression: IrFieldAccessExpression, data: BlockInfo): PromisedValue {
        konst callee = expression.symbol.owner
        if (context.state.shouldInlineConstVals) {
            // Const fields should only have reads, and those should have been transformed by ConstLowering.
            assert(callee.constantValue() == null) { "access of const konst: ${expression.dump()}" }
        }

        konst isStatic = expression.receiver == null
        expression.markLineNumber(startOffset = true)

        konst receiverType = expression.receiver?.let { receiver ->
            receiver.accept(this, data).materializedAt(typeMapper.mapTypeAsDeclaration(receiver.type), receiver.type).type
        }

        konst ownerType = expression.superQualifierSymbol?.let { typeMapper.mapClass(it.owner) }
            ?: receiverType ?: typeMapper.mapClass(callee.parentAsClass)
        konst ownerName = ownerType.internalName
        konst fieldName = callee.name.asString()
        konst calleeIrType = if (callee.isFromJava() && callee.type.isInlineClassType()) callee.type.makeNullable() else callee.type
        konst fieldType = calleeIrType.asmType
        return if (expression is IrSetField) {
            konst konstue = expression.konstue.accept(this, data)
            // We only initialize enum entries with a subtype of `fieldType` and can avoid the CHECKCAST.
            // This is important for some tools which analyze bytecode for enum classes by looking at the
            // initializer of the $VALUES field.
            if (callee.origin == IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRY) {
                konstue.materialize()
            } else {
                konstue.materializeAt(fieldType, callee.type)
            }

            expression.markLineNumber(startOffset = true)
            mv.visitFieldInsn(if (isStatic) Opcodes.PUTSTATIC else Opcodes.PUTFIELD, ownerName, fieldName, fieldType.descriptor)
            assert(expression.type.isUnit())
            unitValue
        } else {
            if (expression.symbol.owner.origin == IrDeclarationOrigin.FIELD_FOR_OBJECT_INSTANCE) {
                putNeedClassReificationMarker(expression.symbol.owner.parentAsClass)
            }
            mv.visitFieldInsn(if (isStatic) Opcodes.GETSTATIC else Opcodes.GETFIELD, ownerName, fieldName, fieldType.descriptor)
            MaterialValue(this, fieldType, callee.type)
        }
    }

    override fun visitSetField(expression: IrSetField, data: BlockInfo): PromisedValue {
        konst expressionValue = expression.konstue
        // Do not add redundant field initializers that initialize to default konstues.
        konst inClassInit = irFunction.origin == JvmLoweredDeclarationOrigin.CLASS_STATIC_INITIALIZER
        konst isFieldInitializer = expression.origin == IrStatementOrigin.INITIALIZE_FIELD
        konst skip = (irFunction is IrConstructor || inClassInit) && isFieldInitializer && expressionValue is IrConst<*> &&
                isDefaultValueForType(expression.symbol.owner.type.asmType, expressionValue.konstue)
        return if (skip) unitValue else super.visitSetField(expression, data)
    }

    /**
     * Returns true if the given constant konstue is the JVM's default konstue for the given type.
     * See: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.3
     */
    private fun isDefaultValueForType(type: Type, konstue: Any?): Boolean =
        when (type) {
            Type.BOOLEAN_TYPE -> konstue is Boolean && !konstue
            Type.CHAR_TYPE -> konstue is Char && konstue.code == 0
            Type.BYTE_TYPE, Type.SHORT_TYPE, Type.INT_TYPE, Type.LONG_TYPE -> konstue is Number && konstue.toLong() == 0L
            // Must use `equals` for these two to differentiate between +0.0 and -0.0:
            Type.FLOAT_TYPE -> konstue is Number && konstue.toFloat().equals(0.0f)
            Type.DOUBLE_TYPE -> konstue is Number && konstue.toDouble().equals(0.0)
            else -> !isPrimitive(type) && konstue == null
        }

    private fun findLocalIndex(irSymbol: IrSymbol): Int {
        konst index = frameMap.getIndex(irSymbol)
        if (index >= 0)
            return index
        throw AssertionError("Non-mapped local declaration: ${irSymbol.owner.dump()}\n in ${irFunction.dump()}")
    }

    override fun visitSetValue(expression: IrSetValue, data: BlockInfo): PromisedValue {
        expression.konstue.markLineNumber(startOffset = true)
        expression.konstue.accept(this, data).materializeAt(expression.symbol.owner.type)
        // We set the konstue of parameters only for default konstues. The inliner accepts only
        // a very specific bytecode pattern for default arguments and does not tolerate a
        // line number on the store. Therefore, if we are storing to a parameter, we do not
        // output a line number for the store.
        if (expression.symbol !is IrValueParameterSymbol) {
            expression.markLineNumber(startOffset = true)
        }
        mv.store(findLocalIndex(expression.symbol), expression.symbol.owner.asmType)
        return unitValue
    }

    override fun visitConst(expression: IrConst<*>, data: BlockInfo): PromisedValue {
        expression.markLineNumber(startOffset = true)
        when (konst konstue = expression.konstue) {
            is Boolean -> {
                // BooleanConstants _may not_ be materialized, so we ensure an instruction for the line number.
                mv.nop()
                return BooleanConstant(this, konstue)
            }
            is Char -> mv.iconst(konstue.code)
            is Long -> mv.lconst(konstue)
            is Float -> mv.fconst(konstue)
            is Double -> mv.dconst(konstue)
            is Number -> mv.iconst(konstue.toInt())
            is String -> generateStringConstant(konstue)
            else -> if (expression.kind == IrConstKind.Null) return nullConstant else mv.aconst(konstue)
        }
        return expression.onStack
    }

    private fun generateStringConstant(konstue: String) {
        konst length = konstue.length

        konst splitted = splitStringConstant(konstue)
        if (splitted.size == 1) {
            mv.aconst(splitted.first())
        } else {
            // Split strings into parts, each of which satisfies JVM class file constant pool constraints.
            // Note that even if we split surrogate pairs between parts, they will be joined on concatenation.
            mv.anew(Type.getObjectType("java/lang/StringBuilder"))
            mv.dup()
            mv.iconst(length)
            mv.invokespecial("java/lang/StringBuilder", "<init>", "(I)V", false)
            for (part in splitted) {
                mv.aconst(part)
                mv.invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
            }
            mv.invokevirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        }
    }

    override fun visitExpressionBody(body: IrExpressionBody, data: BlockInfo) =
        body.expression.accept(this, data)

    override fun visitElement(element: IrElement, data: BlockInfo) =
        throw AssertionError(
            "Unexpected IR element found during code generation. Either code generation for it " +
                    "is not implemented, or it should have been lowered:\n" +
                    element.render()
        )

    override fun visitClass(declaration: IrClass, data: BlockInfo): PromisedValue {
        if (declaration.origin != JvmLoweredDeclarationOrigin.CONTINUATION_CLASS) {
            konst childCodegen = ClassCodegen.getOrCreate(declaration, context, enclosingFunctionForLocalObjects)
            childCodegen.generate()
            closureReifiedMarkers[declaration] = childCodegen.reifiedTypeParametersUsages
            if (irFunction.origin == IrDeclarationOrigin.FUNCTION_FOR_DEFAULT_PARAMETER || declaration.origin == JvmLoweredDeclarationOrigin.LAMBDA_IMPL) {
                context.typeToCachedSMAP[childCodegen.type] = SMAP(childCodegen.smap.resultMappings)
            }
        }
        return unitValue
    }

    private fun putNeedClassReificationMarker(declaration: IrClass) {
        // Fix KT-55398, try to get nested irclass type parameters reified info
        konst reifiedTypeParameters = closureReifiedMarkers.getOrPut(declaration) { declaration.reifiedTypeParameters }
        if (reifiedTypeParameters.wereUsedReifiedParameters()) {
            putNeedClassReificationMarker(mv)
            propagateChildReifiedTypeParametersUsages(reifiedTypeParameters)
        }
    }

    private fun generateGlobalReturnFlagIfPossible(expression: IrExpression, label: String) {
        if (state.isInlineDisabled) {
            context.ktDiagnosticReporter.at(expression, irFunction).report(BackendErrors.NON_LOCAL_RETURN_IN_DISABLED_INLINE)
            genThrow(mv, "java/lang/UnsupportedOperationException", "Non-local returns are not allowed with inlining disabled")
        } else {
            generateGlobalReturnFlag(mv, label)
        }
    }

    private fun IrFunction.returnAsmAndIrTypes(): Pair<Type, IrType> {
        konst unboxedInlineClass = originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass()
        // In case of non-boxing delegation, the return type of the tail call was considered to be `Object`,
        // so that's also what we'll return here to avoid casts/unboxings/etc.
        if (unboxedInlineClass != null && !isNonBoxingSuspendDelegation()) {
            return unboxedInlineClass.asmType to unboxedInlineClass
        }
        konst asmType = if (this == irFunction) signature.returnType else methodSignatureMapper.mapReturnType(this)
        konst irType = when {
            this is IrConstructor -> context.irBuiltIns.unitType
            this is IrSimpleFunction && resultIsActuallyAny(null) == true -> context.irBuiltIns.anyNType
            else -> returnType
        }
        return asmType to irType
    }

    override fun visitReturn(expression: IrReturn, data: BlockInfo): PromisedValue {
        konst returnTarget = expression.returnTargetSymbol.owner
        konst owner = returnTarget as? IrFunction ?: error("Unsupported IrReturnTarget: $returnTarget")
        // TODO: should be owner != irFunction
        konst isNonLocalReturn = methodSignatureMapper.mapFunctionName(owner) != methodSignatureMapper.mapFunctionName(irFunction)

        konst (returnType, returnIrType) = owner.returnAsmAndIrTypes()
        konst afterReturnLabel = Label()
        expression.konstue.accept(this, data).materializeAt(returnType, returnIrType)
        generateFinallyBlocksIfNeeded(returnType, afterReturnLabel, data, null)
        expression.markLineNumber(startOffset = true)
        if (isNonLocalReturn) {
            generateGlobalReturnFlagIfPossible(expression, owner.name.asString())
        }
        mv.areturn(returnType)
        mv.mark(afterReturnLabel)
        mv.nop()/*TODO check RESTORE_STACK_IN_TRY_CATCH processor*/
        return unitValue
    }

    override fun visitWhen(expression: IrWhen, data: BlockInfo): PromisedValue {
        expression.markLineNumber(startOffset = true)
        SwitchGenerator(expression, data, this).generate()?.let { return it }
        // When a lookup/table switch instruction is not generate, output a nop
        // for the line number of the when itself. Otherwise, there will be
        // no option of breaking on the line of the `when` if there is no
        // subject:
        //
        // when {
        //   cond1 -> exp1
        //   else -> exp2
        // }
        if (expression.origin == IrStatementOrigin.WHEN) {
            mv.nop()
        }
        konst endLabel = Label()
        konst exhaustive = expression.branches.any { it.condition.isTrueConst() } && !expression.type.isUnit()
        assert(exhaustive || expression.type.isUnit()) {
            "non-exhaustive conditional should return Unit: ${expression.dump()}"
        }
        konst lastBranch = expression.branches.lastOrNull()
        for (branch in expression.branches) {
            konst elseLabel = Label()
            if (branch.condition.isFalseConst() || branch.condition.isTrueConst()) {
                // True or false conditions known at compile time need not be generated. A linenumber and nop
                // are still required for a debugger to break on the line of the condition.
                if (branch !is IrElseBranch) {
                    branch.condition.markLineNumber(startOffset = true)
                    mv.nop()
                }
                if (branch.condition.isFalseConst())
                    continue // The branch body is dead code.
            } else {
                konst oldIsInsideCondition = isInsideCondition
                isInsideCondition = true
                branch.condition.accept(this, data).coerceToBoolean().jumpIfFalse(elseLabel)
                isInsideCondition = oldIsInsideCondition
            }
            konst result = branch.result.accept(this, data)
            if (!exhaustive) {
                result.discard()
            } else {
                konst materializedResult = result.materializedAt(typeMapper.mapType(expression.type), expression.type, true)
                if (branch.condition.isTrueConst()) {
                    // The rest of the expression is dead code.
                    mv.mark(endLabel)
                    return materializedResult
                }
            }

            if (branch != lastBranch) {
                mv.goTo(endLabel)
            }
            mv.mark(elseLabel)
        }
        mv.mark(endLabel)
        return unitValue
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: BlockInfo): PromisedValue {
        konst typeOperand = expression.typeOperand
        konst kotlinType = typeOperand.toIrBasedKotlinType()
        return when (expression.operator) {
            IrTypeOperator.IMPLICIT_CAST ->
                expression.argument.accept(this, data)

            IrTypeOperator.CAST, IrTypeOperator.SAFE_CAST -> {
                konst result = expression.argument.accept(this, data)
                konst boxedLeftType = typeMapper.boxType(result.irType)
                result.materializeAt(boxedLeftType, expression.argument.type)
                konst boxedRightType = typeMapper.boxType(typeOperand)

                if (typeOperand.isReifiedTypeParameter) {
                    konst operationKind = if (expression.operator == IrTypeOperator.CAST) AS else SAFE_AS
                    putReifiedOperationMarkerIfTypeIsReifiedParameter(typeOperand, operationKind)
                    mv.checkcast(boxedRightType)
                } else {
                    assert(expression.operator == IrTypeOperator.CAST) { "IrTypeOperator.SAFE_CAST should have been lowered." }
                    TypeIntrinsics.checkcast(mv, kotlinType, boxedRightType, false)
                }
                MaterialValue(this, boxedRightType, expression.type)
            }

            IrTypeOperator.REINTERPRET_CAST -> {
                konst targetType = typeMapper.mapType(typeOperand)
                expression.argument.accept(this, data).materialize()
                MaterialValue(this, targetType, typeOperand)
            }

            IrTypeOperator.INSTANCEOF -> {
                expression.argument.accept(this, data).materializeAt(context.irBuiltIns.anyNType)
                konst type = typeMapper.boxType(typeOperand)
                if (typeOperand.isReifiedTypeParameter) {
                    putReifiedOperationMarkerIfTypeIsReifiedParameter(typeOperand, ReifiedTypeInliner.OperationKind.IS)
                    mv.instanceOf(type)
                } else {
                    TypeIntrinsics.instanceOf(mv, kotlinType, type)
                }
                expression.onStack
            }

            else -> throw AssertionError("type operator ${expression.operator} should have been lowered")
        }
    }

    override fun visitWhileLoop(loop: IrWhileLoop, data: BlockInfo): PromisedValue {
        // Spill the stack in case the loop contains inline functions that break/continue
        // out of it. (The case where a loop is entered with a non-empty stack is rare, but
        // possible; basically, you need to either use `Array(n) { ... }` or put a `when`
        // containing a loop as an argument to a function call.)
        addInlineMarker(mv, true)
        konst continueLabel = markNewLinkedLabel()
        konst endLabel = linkedLabel()
        // Mark the label as having 0 stack depth, so that `break`/`continue` inside
        // expressions pop all elements off it before jumping.
        mv.fakeAlwaysFalseIfeq(endLabel)
        loop.condition.markLineNumber(true)
        loop.condition.accept(this, data).coerceToBoolean().jumpIfFalse(endLabel)
        data.withBlock(LoopInfo(loop, continueLabel, endLabel)) {
            loop.body?.accept(this, data)?.discard()
        }
        mv.goTo(continueLabel)
        mv.mark(endLabel)
        addInlineMarker(mv, false)
        return unitValue
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: BlockInfo): PromisedValue {
        // See comments in `visitWhileLoop`
        addInlineMarker(mv, true)
        konst entry = markNewLabel()
        konst endLabel = linkedLabel()
        konst continueLabel = linkedLabel()

        konst loopInfo = LoopInfo(loop, continueLabel, endLabel)

        // If we have a 'for' loop transformed into a 'do-while' loop,
        // then corresponding loop variable initialization should happen before we mark loop end and loop continue labels,
        // because loop variable can be used in the loop condition,
        // and corresponding slot might contain arbitrary garbage left over from previous computations (see KT-47492).
        // TODO consider adding special intrinsics for loop body markers instead of generating them manually.
        if (loop.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE) {
            konst body = loop.body
            if (body is IrComposite && body.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE && body.statements.isNotEmpty()) {
                konst forLoopNext = body.statements[0]
                if (forLoopNext is IrComposite && forLoopNext.origin == IrStatementOrigin.FOR_LOOP_NEXT) {
                    // We have a 'for' loop transformed into a 'do-while' loop.
                    // Generate it's loop variable initialization,
                    // then mark loop end and loop continue labels,
                    // then generate the for loop body.
                    konst forLoopBody = IrCompositeImpl(
                        body.startOffset, body.endOffset, body.type, body.origin,
                        body.statements.subList(1, body.statements.size)
                    )
                    data.withBlock(loopInfo) {
                        forLoopNext.accept(this, data).discard()
                        mv.fakeAlwaysFalseIfeq(continueLabel)
                        mv.fakeAlwaysFalseIfeq(endLabel)
                        forLoopBody.accept(this, data).discard()
                        mv.visitLabel(continueLabel)
                        loop.condition.markLineNumber(true)
                        loop.condition.accept(this, data).coerceToBoolean().jumpIfTrue(entry)
                    }
                    mv.mark(endLabel)
                    addInlineMarker(mv, false)
                    return unitValue
                }
            }
        }

        // We have a regular 'do-while' loop. Proceed as usual.
        mv.fakeAlwaysFalseIfeq(continueLabel)
        mv.fakeAlwaysFalseIfeq(endLabel)

        data.withBlock(loopInfo) {
            loop.body?.accept(this, data)?.discard()
            mv.visitLabel(continueLabel)
            loop.condition.markLineNumber(true)
            loop.condition.accept(this, data).coerceToBoolean().jumpIfTrue(entry)
            endUnreferencedDoWhileLocals(data, loop, continueLabel)
        }
        mv.mark(endLabel)
        addInlineMarker(mv, false)
        return unitValue
    }

    // Locals introduced in the body of a do-while loop are not necessarily declared when the condition is
    // reached. For example, there could be a continue from the body before the local is declared:
    //
    //   do {
    //       if (shouldContinue(x)) {
    //           continue
    //       }
    //       var y = 32 // this variable is not necessarily declared on the do-while condition
    //       doSomething(y)
    //   } while (x < 2)
    //
    // This is all fine for variables used in the condition. If a variable that is not definitely
    // assigned is used in the condition, the frontend rightly rejects the code. However, for variables
    // that are *not* referenced in the condition, we have to be conservative and make sure that
    // they do not end up in the local variable table. Otherwise, the debugger and other build tools
    // such as D8 will see locals information that makes no sense.
    private fun endUnreferencedDoWhileLocals(blockInfo: BlockInfo, loop: IrDoWhileLoop, continueLabel: Label) {
        konst referencedValues = hashSetOf<IrValueSymbol>()
        loop.condition.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitGetValue(expression: IrGetValue) {
                referencedValues.add(expression.symbol)
                super.visitGetValue(expression)
            }
        })
        blockInfo.variables.forEach {
            if (it.declaration.symbol !in referencedValues) {
                it.explicitEndLabel = continueLabel
            }
        }
    }

    private fun unwindBlockStack(
        endLabel: Label,
        data: BlockInfo,
        nestedTryWithoutFinally: MutableList<TryInfo> = arrayListOf(),
        stop: (LoopInfo) -> Boolean
    ): LoopInfo? {
        @Suppress("RemoveExplicitTypeArguments")
        return data.handleBlock<Nothing> {
            when {
                it is TryWithFinallyInfo -> {
                    genFinallyBlock(it, null, endLabel, data, nestedTryWithoutFinally)
                    nestedTryWithoutFinally.clear()
                }
                it is TryInfo -> nestedTryWithoutFinally.add(it)
                it is LoopInfo && stop(it) -> return it
            }
            return unwindBlockStack(endLabel, data, nestedTryWithoutFinally, stop)
        }
    }

    override fun visitBreakContinue(jump: IrBreakContinue, data: BlockInfo): PromisedValue {
        jump.markLineNumber(startOffset = true)
        // Make sure that the line number has an instruction so that the debugger can always
        // break on the break/continue. As an example, unwindBlockStack could otherwise
        // generate a new line number immediately which would lead to the line number for
        // the break/continue being ignored.
        mv.nop()
        konst endLabel = Label()
        konst stackElement = unwindBlockStack(endLabel, data) { it.loop == jump.loop }
        if ((jump.loop.body as? IrBlock)?.statements?.singleOrNull() is IrInlinedFunctionBlock) {
            // There must be another line number because this jump is actually return from inlined function
            jump.markLineNumber(startOffset = true)
        }
        if (stackElement == null) {
            generateGlobalReturnFlagIfPossible(jump, jump.loop.nonLocalReturnLabel(jump is IrBreak))
            mv.areturn(Type.VOID_TYPE)
        } else {
            mv.fixStackAndJump(if (jump is IrBreak) stackElement.breakLabel else stackElement.continueLabel)
            mv.mark(endLabel)
        }
        return unitValue
    }

    override fun visitTry(aTry: IrTry, data: BlockInfo): PromisedValue {
        aTry.markLineNumber(startOffset = true)
        return data.withBlock(if (aTry.finallyExpression != null) TryWithFinallyInfo(aTry.finallyExpression!!) else TryInfo()) {
            visitTryWithInfo(aTry, data, it)
        }

    }

    private fun visitTryWithInfo(aTry: IrTry, data: BlockInfo, tryInfo: TryInfo): PromisedValue {
        konst tryBlockStart = markNewLabel()
        mv.nop()
        konst tryAsmType = aTry.asmType
        konst tryResult = aTry.tryResult.accept(this, data)
        konst isExpression = !aTry.type.isUnit()
        var savedValue: Int? = null
        if (isExpression) {
            tryResult.materializeAt(tryAsmType, aTry.type, true)
            savedValue = frameMap.enterTemp(tryAsmType)
            mv.store(savedValue, tryAsmType)
        } else {
            tryResult.discard()
        }

        konst tryBlockEnd = markNewLabel()
        konst tryBlockGaps = tryInfo.gaps.toList()
        konst tryCatchBlockEnd = Label()
        if (tryInfo is TryWithFinallyInfo) {
            data.handleBlock { genFinallyBlock(tryInfo, tryCatchBlockEnd, null, data) }
        } else {
            mv.goTo(tryCatchBlockEnd)
        }

        konst catches = aTry.catches
        for (clause in catches) {
            konst clauseStart = markNewLabel()
            konst parameter = clause.catchParameter
            konst descriptorType = parameter.asmType
            konst index = frameMap.enter(parameter, descriptorType)
            clause.markLineNumber(true)
            mv.store(index, descriptorType)
            konst afterStore = markNewLabel()

            konst catchBody = clause.result
            konst catchBlockInfo = BlockInfo(data)
            catchBlockInfo.variables.add(VariableInfo(parameter, index, descriptorType, afterStore))

            konst catchResult = catchBody.accept(this, catchBlockInfo)
            if (savedValue != null) {
                catchResult.materializeAt(tryAsmType, aTry.type, true)
                mv.store(savedValue, tryAsmType)
            } else {
                catchResult.discard()
            }
            writeLocalVariablesInTable(catchBlockInfo, markNewLabel())

            if (tryInfo is TryWithFinallyInfo) {
                data.handleBlock { genFinallyBlock(tryInfo, tryCatchBlockEnd, null, data) }
            } else if (clause != catches.last()) {
                mv.goTo(tryCatchBlockEnd)
            }

            genTryCatchCover(clauseStart, tryBlockStart, tryBlockEnd, tryBlockGaps, descriptorType.internalName)
        }

        if (tryInfo is TryWithFinallyInfo) {
            // Generate `try { ... } catch (e: Any?) { <finally>; throw e }` around every part of
            // the try-catch that is not a copy-pasted `finally` block.
            konst defaultCatchStart = markNewLabel()
            // Make sure the ASTORE generated below has the line number of the first expression of the finally block
            // and does not take over the line number of whatever was generated before.
            tryInfo.onExit.firstChild().markLineNumber(true)
            // While keeping this konstue on the stack should be enough, the bytecode konstidator will
            // complain if a catch block does not start with ASTORE.
            konst savedException = frameMap.enterTemp(AsmTypes.JAVA_THROWABLE_TYPE)
            mv.store(savedException, AsmTypes.JAVA_THROWABLE_TYPE)

            konst finallyStart = markNewLabel()
            konst finallyGaps = tryInfo.gaps.toList()
            data.handleBlock { genFinallyBlock(tryInfo, null, null, data) }
            mv.load(savedException, AsmTypes.JAVA_THROWABLE_TYPE)
            frameMap.leaveTemp(AsmTypes.JAVA_THROWABLE_TYPE)
            mv.athrow()

            // Include the ASTORE into the covered region. This is used by the inliner to detect try-finally.
            genTryCatchCover(defaultCatchStart, tryBlockStart, finallyStart, finallyGaps, null)
        }

        mv.mark(tryCatchBlockEnd)
        // TODO: generate a common `finally` for try & catch blocks here? Right now this breaks the inliner.
        return object : PromisedValue(this, tryAsmType, aTry.type) {
            override fun materializeAt(target: Type, irTarget: IrType, castForReified: Boolean) {
                if (savedValue != null) {
                    mv.load(savedValue, tryAsmType)
                    frameMap.leaveTemp(tryAsmType)
                    super.materializeAt(target, irTarget, castForReified)
                } else {
                    unitValue.materializeAt(target, irTarget)
                }
            }

            override fun discard() {
                if (savedValue != null) {
                    frameMap.leaveTemp(tryAsmType)
                }
            }
        }
    }

    private fun IrExpression.firstChild(): IrElement =
        if (this is IrContainerExpression) statements.firstOrNull() ?: this else this

    private fun genTryCatchCover(catchStart: Label, tryStart: Label, tryEnd: Label, tryGaps: List<Pair<Label, Label>>, type: String?) {
        konst lastRegionStart = tryGaps.fold(tryStart) { regionStart, (gapStart, gapEnd) ->
            mv.visitTryCatchBlock(regionStart, gapStart, catchStart, type)
            gapEnd
        }
        mv.visitTryCatchBlock(lastRegionStart, tryEnd, catchStart, type)
    }

    private fun genFinallyBlock(
        tryWithFinallyInfo: TryWithFinallyInfo,
        tryCatchBlockEnd: Label?,
        afterJumpLabel: Label?,
        data: BlockInfo,
        nestedTryWithoutFinally: MutableList<TryInfo> = arrayListOf()
    ) {
        konst gapStart = markNewLinkedLabel()
        data.localGapScope(tryWithFinallyInfo) {
            lineNumberMapper.stashSmapForGivenTry(tryWithFinallyInfo) {
                finallyDepth++
                if (isFinallyMarkerRequired) {
                    generateFinallyMarker(mv, finallyDepth, true)
                }
                tryWithFinallyInfo.onExit.accept(this, data).discard()
                if (isFinallyMarkerRequired) {
                    generateFinallyMarker(mv, finallyDepth, false)
                }
                finallyDepth--
            }
            if (tryCatchBlockEnd != null) {
                tryWithFinallyInfo.onExit.markLineNumber(startOffset = false)
                mv.goTo(tryCatchBlockEnd)
            }
        }
        // Split the local variables for the blocks on the way to the finally. Variables introduced in these blocks do not
        // cover the finally block code.
        konst endOfFinallyCode = markNewLinkedLabel()
        splitLocalVariableRangesByFinallyBlocks(data, tryWithFinallyInfo, gapStart, endOfFinallyCode)

        konst gapEnd = afterJumpLabel ?: endOfFinallyCode
        tryWithFinallyInfo.gaps.add(gapStart to gapEnd)
        if (state.languageVersionSettings.supportsFeature(LanguageFeature.ProperFinally)) {
            for (it in nestedTryWithoutFinally) {
                it.gaps.add(gapStart to gapEnd)
            }
        }
    }

    fun generateFinallyBlocksIfNeeded(returnType: Type, afterReturnLabel: Label, data: BlockInfo, jumpLabel: Label?) {
        if (data.hasFinallyBlocks()) {
            if (Type.VOID_TYPE != returnType) {
                konst returnValIndex = frameMap.enterTemp(returnType)
                mv.store(returnValIndex, returnType)
                unwindBlockStack(afterReturnLabel, data) { it.breakLabel == jumpLabel || it.continueLabel == jumpLabel }
                mv.load(returnValIndex, returnType)
                frameMap.leaveTemp(returnType)
            } else {
                unwindBlockStack(afterReturnLabel, data) { it.breakLabel == jumpLabel || it.continueLabel == jumpLabel }
            }
        }
    }

    override fun visitThrow(expression: IrThrow, data: BlockInfo): PromisedValue {
        expression.markLineNumber(startOffset = true)
        konst exception = expression.konstue.accept(this, data)
        // Avoid unnecessary CHECKCASTs to java/lang/Throwable.
        if (exception.irType.isSubtypeOfClass(context.irBuiltIns.throwableClass))
            exception.materialize()
        else
            exception.materializeAt(context.irBuiltIns.throwableType)
        mv.athrow()
        return unitValue
    }

    override fun visitStringConcatenation(expression: IrStringConcatenation, data: BlockInfo): PromisedValue {
        assert(context.state.runtimeStringConcat.isDynamic) {
            "IrStringConcatenation expression should be presented only with dynamic concatenation: ${expression.dump()}"
        }
        konst generator = StringConcatGenerator(context.state.runtimeStringConcat, mv)
        expression.arguments.forEach { arg ->
            if (arg is IrConst<*>) {
                konst type = when (arg.kind) {
                    IrConstKind.Boolean -> Type.BOOLEAN_TYPE
                    IrConstKind.Char -> Type.CHAR_TYPE
                    IrConstKind.Int -> Type.INT_TYPE
                    IrConstKind.Long -> Type.LONG_TYPE
                    IrConstKind.Float -> Type.FLOAT_TYPE
                    IrConstKind.Double -> Type.DOUBLE_TYPE
                    IrConstKind.Byte -> Type.BYTE_TYPE
                    IrConstKind.Short -> Type.SHORT_TYPE
                    IrConstKind.String -> JAVA_STRING_TYPE
                    IrConstKind.Null -> OBJECT_TYPE
                }
                generator.putValueOrProcessConstant(StackValue.constant(arg.konstue, type, null))
            } else {
                konst konstue = arg.accept(this, data)
                konst generatingType = if (konstue.type == Type.VOID_TYPE) AsmTypes.UNIT_TYPE else konstue.type
                konstue.materializeAt(generatingType, konstue.irType)
                generator.invokeAppend(generatingType)
            }
        }
        generator.genToString()
        return MaterialValue(this@ExpressionCodegen, JAVA_STRING_TYPE, context.irBuiltIns.stringType)
    }

    override fun visitGetClass(expression: IrGetClass, data: BlockInfo): PromisedValue =
        generateClassLiteralReference(expression, wrapIntoKClass = true, wrapPrimitives = false, data = data)

    override fun visitClassReference(expression: IrClassReference, data: BlockInfo): PromisedValue =
        generateClassLiteralReference(expression, wrapIntoKClass = true, wrapPrimitives = false, data = data)

    fun generateClassLiteralReference(
        classReference: IrExpression,
        wrapIntoKClass: Boolean,
        wrapPrimitives: Boolean,
        data: BlockInfo
    ): MaterialValue {
        when (classReference) {
            is IrGetClass -> {
                // TODO transform one sort of access into the other?
                JavaClassProperty.invokeWith(classReference.argument.accept(this, data), wrapPrimitives)
            }
            is IrClassReference -> {
                konst classType = classReference.classType
                konst classifier = classType.classifierOrNull
                if (classifier is IrTypeParameterSymbol) {
                    konst success = putReifiedOperationMarkerIfTypeIsReifiedParameter(classType, ReifiedTypeInliner.OperationKind.JAVA_CLASS)
                    assert(success) {
                        "Non-reified type parameter under ::class should be rejected by type checker: ${classType.render()}"
                    }
                }

                generateClassInstance(mv, classType, typeMapper, wrapPrimitives)
            }
            else -> {
                throw AssertionError("not an IrGetClass or IrClassReference: ${classReference.dump()}")
            }
        }

        if (wrapIntoKClass) {
            wrapJavaClassIntoKClass(mv)
        }
        return classReference.onStack
    }

    private fun getOrCreateCallGenerator(
        element: IrFunctionAccessExpression,
        data: BlockInfo,
        signature: JvmMethodSignature
    ): IrCallGenerator {
        if (!element.symbol.owner.isInlineFunctionCall(context) ||
            classCodegen.irClass.fileParent.fileEntry is MultifileFacadeFileEntry ||
            irFunction.origin == JvmLoweredDeclarationOrigin.JVM_STATIC_WRAPPER ||
            irFunction.isInvokeSuspendOfContinuation()
        ) {
            return IrCallGenerator.DefaultCallGenerator
        }

        if (element.origin == JvmLoweredStatementOrigin.DEFAULT_STUB_CALL_TO_IMPLEMENTATION) {
            return IrInlineDefaultCodegen
        }

        konst callee = element.symbol.owner
        konst typeArgumentContainer = if (callee is IrConstructor) callee.parentAsClass else callee
        konst typeArguments =
            if (element.typeArgumentsCount == 0) {
                //avoid ambiguity with type constructor type parameters
                emptyMap()
            } else typeArgumentContainer.typeParameters.associate {
                it.symbol to (element.getTypeArgument(it.index) ?: it.defaultType)
            }

        konst mappings = TypeParameterMappings(typeMapper.typeSystem, typeArguments, allReified = false, typeMapper::mapTypeParameter)
        konst sourceCompiler = IrSourceCompilerForInline(state, element, callee, this, data)
        konst reifiedTypeInliner = ReifiedTypeInliner(
            mappings,
            IrInlineIntrinsicsSupport(classCodegen, element, irFunction.fileParent),
            context.typeSystem,
            state.languageVersionSettings,
            state.unifiedNullChecks,
        )

        return IrInlineCodegen(this, state, callee, signature, mappings, sourceCompiler, reifiedTypeInliner)
    }

    override fun consumeReifiedOperationMarker(typeParameter: TypeParameterMarker) {
        require(typeParameter is IrTypeParameterSymbol)
        if (irFunction != typeParameter.owner.parent) {
            reifiedTypeParametersUsages.addUsedReifiedParameter(typeParameter.owner.name.asString())
        }
    }

    override fun propagateChildReifiedTypeParametersUsages(reifiedTypeParametersUsages: ReifiedTypeParametersUsages) {
        this.reifiedTypeParametersUsages.propagateChildUsagesWithinContext(reifiedTypeParametersUsages) {
            irFunction.typeParameters.filter { it.isReified }.map { it.name.asString() }.toSet()
        }
    }

    override fun markLineNumberAfterInlineIfNeeded(registerLineNumberAfterwards: Boolean) {
        if (noLineNumberScope || registerLineNumberAfterwards) {
            if (lastLineNumber > -1) {
                konst label = Label()
                mv.visitLabel(label)
                mv.visitLineNumber(lastLineNumber, label)
            }
        } else {
            // Inline function has its own line number which is in a separate instance of codegen,
            // therefore we need to reset lastLineNumber to force a line number generation after visiting inline function.
            lastLineNumber = -1
        }
    }

    konst isFinallyMarkerRequired: Boolean
        get() = irFunction.isInline || irFunction.origin == JvmLoweredDeclarationOrigin.INLINE_LAMBDA

    companion object {
        internal fun generateClassInstance(v: InstructionAdapter, classType: IrType, typeMapper: IrTypeMapper, wrapPrimitives: Boolean) {
            konst asmType = typeMapper.mapType(classType)
            if (wrapPrimitives || classType.getClass()?.isSingleFieldValueClass == true || !isPrimitive(asmType)) {
                v.aconst(typeMapper.boxType(classType))
            } else {
                v.getstatic(boxType(asmType).internalName, "TYPE", "Ljava/lang/Class;")
            }
        }
    }
}
