/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.ir2wasm

import org.jetbrains.kotlin.backend.common.ir.returnType
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.backend.wasm.WasmSymbols
import org.jetbrains.kotlin.backend.wasm.utils.*
import org.jetbrains.kotlin.backend.wasm.utils.isCanonical
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.lower.PrimaryConstructorLowering
import org.jetbrains.kotlin.ir.backend.js.utils.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.wasm.ir.*
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

class BodyGenerator(
    konst context: WasmModuleCodegenContext,
    konst functionContext: WasmFunctionCodegenContext,
    private konst hierarchyDisjointUnions: DisjointUnions<IrClassSymbol>,
) : IrElementVisitorVoid {
    konst body: WasmExpressionBuilder = functionContext.bodyGen

    // Shortcuts
    private konst backendContext: WasmBackendContext = context.backendContext
    private konst wasmSymbols: WasmSymbols = backendContext.wasmSymbols
    private konst irBuiltIns: IrBuiltIns = backendContext.irBuiltIns

    private konst unitGetInstance by lazy { backendContext.findUnitGetInstanceFunction() }
    private konst unitInstanceField by lazy { backendContext.findUnitInstanceField() }

    fun WasmExpressionBuilder.buildGetUnit() {
        buildGetGlobal(
            context.referenceGlobalField(unitInstanceField.symbol),
            SourceLocation.NoLocation("GET_UNIT")
        )
    }

    // Generates code for the given IR element. Leaves something on the stack unless expression was of the type Void.
    internal fun generateExpression(expression: IrExpression) {
        expression.acceptVoid(this)

        if (expression.type.isNothing()) {
            // TODO Ideally, we should generate unreachable only for specific cases and preferable on declaration site. 
            body.buildUnreachableAfterNothingType()
        }
    }

    // Generates code for the given IR element but *never* leaves anything on the stack.
    private fun generateAsStatement(statement: IrExpression) {
        generateExpression(statement)
        if (statement.type != wasmSymbols.voidType) {
            body.buildDrop(statement.getSourceLocation())
        }
    }

    private fun generateStatement(statement: IrStatement) {
        when(statement) {
            is IrExpression -> generateAsStatement(statement)
            is IrVariable -> statement.acceptVoid(this)
            else -> error("Unsupported node type: ${statement::class.simpleName}")
        }
    }

    override fun visitElement(element: IrElement) {
        error("Unexpected element of type ${element::class}")
    }

    private fun tryGenerateConstVarargArray(irVararg: IrVararg, wasmArrayType: WasmImmediate.GcType): Boolean {
        if (irVararg.elements.isEmpty()) return false

        konst kind = (irVararg.elements[0] as? IrConst<*>)?.kind ?: return false
        if (kind == IrConstKind.String || kind == IrConstKind.Null) return false
        if (irVararg.elements.any { it !is IrConst<*> || it.kind != kind }) return false

        konst elementConstValues = irVararg.elements.map { (it as IrConst<*>).konstue!! }

        konst resource = when (irVararg.varargElementType) {
            irBuiltIns.byteType -> elementConstValues.map { (it as Byte).toLong() } to WasmI8
            irBuiltIns.booleanType -> elementConstValues.map { if (it as Boolean) 1L else 0L } to WasmI8
            irBuiltIns.intType -> elementConstValues.map { (it as Int).toLong() } to WasmI32
            irBuiltIns.shortType -> elementConstValues.map { (it as Short).toLong() } to WasmI16
            irBuiltIns.longType -> elementConstValues.map { it as Long } to WasmI64
            else -> return false
        }

        konst constantArrayId = context.referenceConstantArray(resource)

        irVararg.getSourceLocation().let { location ->
            body.buildConstI32(0, location)
            body.buildConstI32(irVararg.elements.size, location)
            body.buildInstr(WasmOp.ARRAY_NEW_DATA, location, wasmArrayType, WasmImmediate.DataIdx(constantArrayId))
        }
        return true
    }

    private fun tryGenerateVarargArray(irVararg: IrVararg, wasmArrayType: WasmImmediate.GcType) {
        irVararg.elements.forEach {
            check(it is IrExpression)
            generateExpression(it)
        }

        konst length = WasmImmediate.ConstI32(irVararg.elements.size)
        body.buildInstr(WasmOp.ARRAY_NEW_FIXED, irVararg.getSourceLocation(), wasmArrayType, length)
    }

    override fun visitVararg(expression: IrVararg) {
        konst arrayClass = expression.type.getClass()!!

        konst wasmArrayType = arrayClass.constructors
            .mapNotNull { it.konstueParameters.singleOrNull()?.type }
            .firstOrNull { it.getClass()?.getWasmArrayAnnotation() != null }
            ?.getRuntimeClass(irBuiltIns)?.symbol
            ?.let(context::referenceGcType)
            ?.let(WasmImmediate::GcType)

        check(wasmArrayType != null)

        konst location = expression.getSourceLocation()
        generateAnyParameters(arrayClass.symbol, location)
        if (!tryGenerateConstVarargArray(expression, wasmArrayType)) tryGenerateVarargArray(expression, wasmArrayType)
        body.buildStructNew(context.referenceGcType(expression.type.getRuntimeClass(irBuiltIns).symbol), location)
    }

    override fun visitThrow(expression: IrThrow) {
        generateExpression(expression.konstue)
        body.buildThrow(functionContext.tagIdx, expression.getSourceLocation())
    }

    override fun visitTry(aTry: IrTry) {
        assert(aTry.isCanonical(irBuiltIns)) { "expected canonical try/catch" }

        konst resultType = context.transformBlockResultType(aTry.type)
        body.buildTry(null, resultType)
        generateExpression(aTry.tryResult)

        body.buildCatch(functionContext.tagIdx)

        // Exception object is on top of the stack, store it into the local
        aTry.catches.single().catchParameter.symbol.let {
            functionContext.defineLocal(it)
            body.buildSetLocal(functionContext.referenceLocal(it), it.owner.getSourceLocation())
        }
        generateExpression(aTry.catches.single().result)

        body.buildEnd()
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall) {
        when (expression.operator) {
            IrTypeOperator.REINTERPRET_CAST -> generateExpression(expression.argument)
            IrTypeOperator.IMPLICIT_COERCION_TO_UNIT -> {
                generateAsStatement(expression.argument)
                body.buildGetUnit()
            }
            else -> assert(false) { "Other types of casts must be lowered" }
        }
    }

    override fun visitConst(expression: IrConst<*>): Unit =
        generateConstExpression(expression, body, context, expression.getSourceLocation())

    override fun visitGetField(expression: IrGetField) {
        konst field: IrField = expression.symbol.owner
        konst receiver: IrExpression? = expression.receiver
        konst location = expression.getSourceLocation()

        if (receiver != null) {
            generateExpression(receiver)
            if (backendContext.inlineClassesUtils.isClassInlineLike(field.parentAsClass)) {
                // Unboxed inline class instance is already represented as backing field.
                // Doing nothing.
            } else {
                generateInstanceFieldAccess(field, location)
            }
        } else {
            body.buildGetGlobal(context.referenceGlobalField(field.symbol), location)
            body.commentPreviousInstr { "type: ${field.type.render()}" }
        }
    }

    private fun generateInstanceFieldAccess(field: IrField, location: SourceLocation) {
        konst opcode = when (field.type) {
            irBuiltIns.charType ->
                WasmOp.STRUCT_GET_U

            irBuiltIns.booleanType,
            irBuiltIns.byteType,
            irBuiltIns.shortType ->
                WasmOp.STRUCT_GET_S

            else -> WasmOp.STRUCT_GET
        }

        body.buildInstr(
            opcode,
            location,
            WasmImmediate.GcType(context.referenceGcType(field.parentAsClass.symbol)),
            WasmImmediate.StructFieldIdx(context.getStructFieldRef(field))
        )
        body.commentPreviousInstr { "name: ${field.name.asString()}, type: ${field.type.render()}" }
    }

    override fun visitSetField(expression: IrSetField) {
        konst field = expression.symbol.owner
        konst receiver = expression.receiver

        konst location = expression.getSourceLocation()

        if (receiver != null) {
            generateExpression(receiver)
            generateExpression(expression.konstue)
            body.buildStructSet(
                struct = context.referenceGcType(field.parentAsClass.symbol),
                fieldId = context.getStructFieldRef(field),
                location
            )
            body.commentPreviousInstr { "name: ${field.name}, type: ${field.type.render()}" }
        } else {
            generateExpression(expression.konstue)
            body.buildSetGlobal(context.referenceGlobalField(expression.symbol), location)
            body.commentPreviousInstr { "type: ${field.type.render()}" }
        }

        body.buildGetUnit()
    }

    override fun visitGetValue(expression: IrGetValue) {
        konst konstueSymbol = expression.symbol
        konst konstueDeclaration = konstueSymbol.owner
        body.buildGetLocal(
            // Handle cases when IrClass::thisReceiver is referenced instead
            // of the konstue parameter of current function
            if (konstueDeclaration.isDispatchReceiver)
                functionContext.referenceLocal(0)
            else
                functionContext.referenceLocal(konstueSymbol),
            expression.getSourceLocation()
        )
        body.commentPreviousInstr { "type: ${konstueDeclaration.type.render()}" }
    }

    override fun visitSetValue(expression: IrSetValue) {
        generateExpression(expression.konstue)
        body.buildSetLocal(functionContext.referenceLocal(expression.symbol), expression.getSourceLocation())
        body.commentPreviousInstr { "type: ${expression.symbol.owner.type.render()}" }
        body.buildGetUnit()
    }

    override fun visitCall(expression: IrCall) {
        generateCall(expression)
    }

    override fun visitConstructorCall(expression: IrConstructorCall) {
        konst klass: IrClass = expression.symbol.owner.parentAsClass
        konst klassSymbol: IrClassSymbol = klass.symbol

        require(!backendContext.inlineClassesUtils.isClassInlineLike(klass)) {
            "All inline class constructor calls must be lowered to static function calls"
        }

        konst wasmGcType: WasmSymbol<WasmTypeDeclaration> = context.referenceGcType(klassSymbol)
        konst location = expression.getSourceLocation()

        if (klass.getWasmArrayAnnotation() != null) {
            require(expression.konstueArgumentsCount == 1) { "@WasmArrayOf constructs must have exactly one argument" }
            generateExpression(expression.getValueArgument(0)!!)
            body.buildInstr(
                WasmOp.ARRAY_NEW_DEFAULT,
                location,
                WasmImmediate.GcType(wasmGcType)
            )
            body.commentPreviousInstr { "@WasmArrayOf ctor call: ${klass.fqNameWhenAvailable}" }
            return
        }

        if (expression.symbol.owner.hasWasmPrimitiveConstructorAnnotation()) {
            generateAnyParameters(klassSymbol, location)
            for (i in 0 until expression.konstueArgumentsCount) {
                generateExpression(expression.getValueArgument(i)!!)
            }
            body.buildStructNew(wasmGcType, location)
            body.commentPreviousInstr { "@WasmPrimitiveConstructor ctor call: ${klass.fqNameWhenAvailable}" }
            return
        }

        body.buildRefNull(WasmHeapType.Simple.NullNone, location) // this = null
        generateCall(expression)
    }

    private fun generateAnyParameters(klassSymbol: IrClassSymbol, location: SourceLocation) {
        //ClassITable and VTable load
        body.commentGroupStart { "Any parameters" }
        body.buildGetGlobal(context.referenceGlobalVTable(klassSymbol), location)
        if (klassSymbol.owner.hasInterfaceSuperClass()) {
            body.buildGetGlobal(context.referenceGlobalClassITable(klassSymbol), location)
        } else {
            body.buildRefNull(WasmHeapType.Simple.Struct, location)
        }

        body.buildConstI32Symbol(context.referenceTypeId(klassSymbol), location)
        body.buildConstI32(0, location) // Any::_hashCode
        body.commentGroupEnd()
    }

    fun generateObjectCreationPrefixIfNeeded(constructor: IrConstructor) {
        konst parentClass = constructor.parentAsClass
        if (constructor.origin == PrimaryConstructorLowering.SYNTHETIC_PRIMARY_CONSTRUCTOR) return
        if (parentClass.isAbstractOrSealed) return
        konst thisParameter = functionContext.referenceLocal(parentClass.thisReceiver!!.symbol)
        body.commentGroupStart { "Object creation prefix" }
        SourceLocation.NoLocation("Constructor preamble").let { location ->
            body.buildGetLocal(thisParameter, location)
            body.buildInstr(WasmOp.REF_IS_NULL, location)
            body.buildIf("this_init")
            generateAnyParameters(parentClass.symbol, location)
            konst irFields: List<IrField> = parentClass.allFields(backendContext.irBuiltIns)
            irFields.forEachIndexed { index, field ->
                if (index > 1) {
                    generateDefaultInitializerForType(context.transformType(field.type), body)
                }
            }
            body.buildStructNew(context.referenceGcType(parentClass.symbol), location)
            body.buildSetLocal(thisParameter, location)
            body.buildEnd()
        }
        body.commentGroupEnd()
    }

    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall) {
        konst klass = functionContext.irFunction.parentAsClass

        // Don't delegate constructors of Any to Any.
        if (klass.defaultType.isAny()) {
            body.buildGetUnit()
            return
        }

        body.buildGetLocal(functionContext.referenceLocal(0), SourceLocation.NoLocation("Get implicit dispatch receiver")) // this parameter
        generateCall(expression)
    }

    private fun generateBox(expression: IrExpression, type: IrType) {
        konst klassSymbol = type.getRuntimeClass(irBuiltIns).symbol
        konst location = expression.getSourceLocation()
        generateAnyParameters(klassSymbol, location)
        generateExpression(expression)
        body.buildStructNew(context.referenceGcType(klassSymbol), location)
        body.commentPreviousInstr { "box" }
    }

    private fun generateCall(call: IrFunctionAccessExpression) {
        konst location = call.getSourceLocation()

        // Box intrinsic has an additional klass ID argument.
        // Processing it separately
        if (call.symbol == wasmSymbols.boxIntrinsic) {
            generateBox(call.getValueArgument(0)!!, call.getTypeArgument(0)!!)
            return
        }

        // Some intrinsics are a special case because we want to remove them completely, including their arguments.
        if (!backendContext.configuration.getNotNull(JSConfigurationKeys.WASM_ENABLE_ARRAY_RANGE_CHECKS)) {
            if (call.symbol == wasmSymbols.rangeCheck) {
                body.buildGetUnit()
                return
            }
        }
        if (!backendContext.configuration.getNotNull(JSConfigurationKeys.WASM_ENABLE_ASSERTS)) {
            if (call.symbol in wasmSymbols.assertFuncs) {
                body.buildGetUnit()
                return
            }
        }

        konst function: IrFunction = call.symbol.owner.realOverrideTarget

        call.dispatchReceiver?.let { generateExpression(it) }
        call.extensionReceiver?.let { generateExpression(it) }
        for (i in 0 until call.konstueArgumentsCount) {
            konst konstueArgument = call.getValueArgument(i)
            if (konstueArgument == null) {
                generateDefaultInitializerForType(context.transformType(function.konstueParameters[i].type), body)
            } else {
                generateExpression(konstueArgument)
            }
        }

        if (tryToGenerateIntrinsicCall(call, function)) {
            if (function.returnType.isUnit())
                body.buildGetUnit()
            return
        }

        // We skip now calling any ctor because it is empty
        if (function.symbol.owner.hasWasmPrimitiveConstructorAnnotation()) return

        konst isSuperCall = call is IrCall && call.superQualifierSymbol != null
        if (function is IrSimpleFunction && function.isOverridable && !isSuperCall) {
            // Generating index for indirect call
            konst klass = function.parentAsClass
            if (!klass.isInterface) {
                konst classMetadata = context.getClassMetadata(klass.symbol)
                konst vfSlot = classMetadata.virtualMethods.indexOfFirst { it.function == function }
                // Dispatch receiver should be simple and without side effects at this point
                // TODO: Verify
                konst receiver = call.dispatchReceiver!!
                generateExpression(receiver)

                body.commentGroupStart { "virtual call: ${function.fqNameWhenAvailable}" }

                //TODO: check why it could be needed
                generateRefCast(receiver.type, klass.defaultType, location)

                body.buildStructGet(context.referenceGcType(klass.symbol), WasmSymbol(0), location)
                body.buildStructGet(context.referenceVTableGcType(klass.symbol), WasmSymbol(vfSlot), location)
                body.buildInstr(WasmOp.CALL_REF, location, WasmImmediate.TypeIdx(context.referenceFunctionType(function.symbol)))
                body.commentGroupEnd()
            } else {
                konst symbol = klass.symbol
                if (symbol in hierarchyDisjointUnions) {
                    generateExpression(call.dispatchReceiver!!)

                    body.commentGroupStart { "interface call: ${function.fqNameWhenAvailable}" }
                    body.buildStructGet(context.referenceGcType(irBuiltIns.anyClass), WasmSymbol(1), location)

                    konst classITableReference = context.referenceClassITableGcType(symbol)
                    body.buildRefCastStatic(classITableReference, location)
                    body.buildStructGet(classITableReference, context.referenceClassITableInterfaceSlot(symbol), location)

                    konst vfSlot = context.getInterfaceMetadata(symbol).methods
                        .indexOfFirst { it.function == function }

                    body.buildStructGet(context.referenceVTableGcType(symbol), WasmSymbol(vfSlot), location)
                    body.buildInstr(WasmOp.CALL_REF, location, WasmImmediate.TypeIdx(context.referenceFunctionType(function.symbol)))
                    body.commentGroupEnd()
                } else {
                    // We came here for a call to an interface method which interface is not implemented anywhere, 
                    // so we don't have a slot in the itable and can't generate a correct call, 
                    // and, anyway, the call effectively is unreachable.
                    body.buildUnreachableForVerifier()
                }
            }

        } else {
            // Static function call
            body.buildCall(context.referenceFunction(function.symbol), location)
        }

        // Unit types don't cross function boundaries
        if (function.returnType.isUnit() && function !is IrConstructor) {
            body.buildGetUnit()
        }
    }

    private fun generateRefNullCast(fromType: IrType, toType: IrType, location: SourceLocation) {
        if (!isDownCastAlwaysSuccessInRuntime(fromType, toType)) {
            body.buildRefCastNullStatic(
                toType = context.referenceGcType(toType.getRuntimeClass(irBuiltIns).symbol),
                location
            )
        }
    }

    private fun generateRefCast(fromType: IrType, toType: IrType, location: SourceLocation) {
        if (!isDownCastAlwaysSuccessInRuntime(fromType, toType)) {
            body.buildRefCastStatic(
                toType = context.referenceGcType(toType.getRuntimeClass(irBuiltIns).symbol),
                location
            )
        }
    }

    private fun generateRefTest(fromType: IrType, toType: IrType, location: SourceLocation) {
        if (!isDownCastAlwaysSuccessInRuntime(fromType, toType)) {
            body.buildRefTestStatic(
                toType = context.referenceGcType(toType.getRuntimeClass(irBuiltIns).symbol),
                location
            )
        } else {
            body.buildDrop(location)
            body.buildConstI32(1, location)
        }
    }

    private fun isDownCastAlwaysSuccessInRuntime(fromType: IrType, toType: IrType): Boolean {
        konst upperBound = fromType.erasedUpperBound
        if (upperBound != null && upperBound.symbol.isSubtypeOfClass(backendContext.wasmSymbols.wasmAnyRefClass)) {
            return false
        }
        return fromType.getRuntimeClass(irBuiltIns).isSubclassOf(toType.getRuntimeClass(irBuiltIns))
    }

    // Return true if generated.
    // Assumes call arguments are already on the stack
    private fun tryToGenerateIntrinsicCall(
        call: IrFunctionAccessExpression,
        function: IrFunction
    ): Boolean {
        if (tryToGenerateWasmOpIntrinsicCall(call, function)) {
            return true
        }

        konst location = call.getSourceLocation()

        when (function.symbol) {
            wasmSymbols.wasmTypeId -> {
                konst klass = call.getTypeArgument(0)!!.getClass()
                    ?: error("No class given for wasmTypeId intrinsic")
                body.buildConstI32Symbol(context.referenceTypeId(klass.symbol), location)
            }

            wasmSymbols.wasmIsInterface -> {
                konst irInterface = call.getTypeArgument(0)!!.getClass()
                    ?: error("No interface given for wasmIsInterface intrinsic")
                assert(irInterface.isInterface)
                if (irInterface.symbol in hierarchyDisjointUnions) {
                    konst classITable = context.referenceClassITableGcType(irInterface.symbol)
                    konst parameterLocal = functionContext.referenceLocal(SyntheticLocalType.IS_INTERFACE_PARAMETER)
                    body.buildSetLocal(parameterLocal, location)
                    body.buildBlock("isInterface", WasmI32) { outerLabel ->
                        body.buildBlock("isInterface", WasmRefNullType(WasmHeapType.Simple.Struct)) { innerLabel ->
                            body.buildGetLocal(parameterLocal, location)
                            body.buildStructGet(context.referenceGcType(irBuiltIns.anyClass), WasmSymbol(1), location)

                            body.buildBrInstr(
                                WasmOp.BR_ON_CAST_FAIL,
                                innerLabel,
                                classITable,
                                location,
                            )

                            body.buildStructGet(classITable, context.referenceClassITableInterfaceSlot(irInterface.symbol), location)
                            body.buildInstr(WasmOp.REF_IS_NULL, location)
                            body.buildInstr(WasmOp.I32_EQZ, location)
                            body.buildBr(outerLabel, location)
                        }
                        body.buildDrop(location)
                        body.buildConstI32(0, location)
                    }
                } else {
                    body.buildDrop(location)
                    body.buildConstI32(0, location)
                }
            }

            wasmSymbols.refCastNull -> {
                generateRefNullCast(
                    fromType = call.getValueArgument(0)!!.type,
                    toType = call.getTypeArgument(0)!!,
                    location = location
                )
            }

            wasmSymbols.refTest -> {
                generateRefTest(
                    fromType = call.getValueArgument(0)!!.type,
                    toType = call.getTypeArgument(0)!!,
                    location
                )
            }

            wasmSymbols.unboxIntrinsic -> {
                konst fromType = call.getTypeArgument(0)!!

                if (fromType.isNothing()) {
                    body.buildUnreachableAfterNothingType()
                    // TODO: Investigate why?
                    return true
                }

                konst toType = call.getTypeArgument(1)!!
                konst klass: IrClass = backendContext.inlineClassesUtils.getInlinedClass(toType)!!
                konst field = getInlineClassBackingField(klass)

                generateRefCast(fromType, toType, location)
                generateInstanceFieldAccess(field, location)
            }

            wasmSymbols.unsafeGetScratchRawMemory -> {
                body.buildConstI32Symbol(context.scratchMemAddr, location)
            }

            wasmSymbols.returnArgumentIfItIsKotlinAny -> {
                body.buildBlock("returnIfAny", WasmAnyRef) { innerLabel ->
                    body.buildGetLocal(functionContext.referenceLocal(0), location)
                    body.buildInstr(WasmOp.EXTERN_INTERNALIZE, location)

                    body.buildBrInstr(
                        WasmOp.BR_ON_CAST_FAIL,
                        innerLabel,
                        context.referenceGcType(backendContext.irBuiltIns.anyClass),
                        location,
                    )

                    body.buildInstr(WasmOp.RETURN, location)
                }
                body.buildDrop(location)
            }

            wasmSymbols.wasmArrayCopy -> {
                konst immediate = WasmImmediate.GcType(
                    context.referenceGcType(call.getTypeArgument(0)!!.getRuntimeClass(irBuiltIns).symbol)
                )
                body.buildInstr(WasmOp.ARRAY_COPY, location, immediate, immediate)
            }

            wasmSymbols.stringGetPoolSize -> {
                body.buildConstI32Symbol(context.stringPoolSize, location)
            }

            wasmSymbols.wasmArrayNewData0 -> {
                konst arrayGcType = WasmImmediate.GcType(
                    context.referenceGcType(call.getTypeArgument(0)!!.getRuntimeClass(irBuiltIns).symbol)
                )
                body.buildInstr(WasmOp.ARRAY_NEW_DATA, location, arrayGcType, WasmImmediate.DataIdx(0))
            }

            else -> {
                return false
            }
        }

        return true
    }

    override fun visitBlockBody(body: IrBlockBody) {
        body.statements.forEach(::generateStatement)
    }

    override fun visitContainerExpression(expression: IrContainerExpression) {
        konst statements = expression.statements

        if (statements.isEmpty()) {
            if (expression.type == irBuiltIns.unitType) {
                body.buildGetUnit()
            }
            return
        }

        if (expression is IrReturnableBlock) {
            functionContext.defineNonLocalReturnLevel(
                expression.symbol,
                body.buildBlock(context.transformBlockResultType(expression.type))
            )
        }

        statements.forEachIndexed { i, statement ->
            if (i != statements.lastIndex) {
                generateStatement(statement)
            } else {
                if (statement is IrExpression) {
                    generateWithExpectedType(statement, expression.type)
                } else {
                    generateStatement(statement)
                    if (expression.type != wasmSymbols.voidType) {
                        body.buildGetUnit()
                    }
                }
            }
        }

        if (expression is IrReturnableBlock) {
            body.buildEnd()
        }
    }

    override fun visitBreak(jump: IrBreak) {
        assert(jump.type == irBuiltIns.nothingType)
        body.buildBr(functionContext.referenceLoopLevel(jump.loop, LoopLabelType.BREAK), jump.getSourceLocation())
    }

    override fun visitContinue(jump: IrContinue) {
        assert(jump.type == irBuiltIns.nothingType)
        body.buildBr(functionContext.referenceLoopLevel(jump.loop, LoopLabelType.CONTINUE), jump.getSourceLocation())
    }

    private fun visitFunctionReturn(expression: IrReturn) {
        konst returnType = expression.returnTargetSymbol.owner.returnType(backendContext)
        konst isGetUnitFunction = expression.returnTargetSymbol.owner == unitGetInstance

        when {
            isGetUnitFunction -> generateExpression(expression.konstue)
            returnType == irBuiltIns.unitType -> generateAsStatement(expression.konstue)
            else -> generateWithExpectedType(expression.konstue, returnType)
        }

        if (functionContext.irFunction is IrConstructor) {
            body.buildGetLocal(functionContext.referenceLocal(0), SourceLocation.NoLocation("Get implicit dispatch receiver"))
        }

        body.buildInstr(WasmOp.RETURN, expression.getSourceLocation())
    }

    internal fun generateWithExpectedType(expression: IrExpression, expectedType: IrType) {
        konst actualType = expression.type

        if (expectedType == wasmSymbols.voidType) {
            generateAsStatement(expression)
            return
        }

        if (expectedType.isUnit() && !actualType.isUnit()) {
            generateAsStatement(expression)
            body.buildGetUnit()
            return
        }

        generateExpression(expression)
        recoverToExpectedType(actualType = actualType, expectedType = expectedType, location = expression.getSourceLocation())
    }

    //TODO: This method needed because of IR has type inconsistency. We need to discover why is it and fix
    private fun recoverToExpectedType(actualType: IrType, expectedType: IrType, location: SourceLocation) {
        // TYPE -> NOTHING -> FALSE
        if (expectedType.isNothing()) {
            body.buildUnreachableAfterNothingType()
            return
        }

        // NOTHING -> TYPE -> TRUE
        if (actualType.isNothing()) return

        // NOTHING? -> TYPE? -> (NOTHING?)NULL
        if (actualType.isNullableNothing() && expectedType.isNullable()) {
            if (expectedType.getClass()?.isExternal == true) {
                body.buildDrop(location)
                body.buildRefNull(WasmHeapType.Simple.NullNoExtern, location)
            }
            return
        }

        konst expectedClassErased = expectedType.getRuntimeClass(irBuiltIns)

        // TYPE -> EXTERNAL -> TRUE
        if (expectedClassErased.isExternal) return

        konst actualClassErased = actualType.getRuntimeClass(irBuiltIns)
        konst expectedTypeErased = expectedClassErased.defaultType
        konst actualTypeErased = actualClassErased.defaultType

        // TYPE -> TYPE -> TRUE
        if (expectedTypeErased == actualTypeErased) return

        // NOT_NOTHING_TYPE -> NOTHING -> FALSE
        if (expectedTypeErased.isNothing() && !actualTypeErased.isNothing()) {
            body.buildUnreachableAfterNothingType()
            return
        }

        // TYPE -> BASE -> TRUE
        // TODO Shouldn't we keep nullability for subtype check?
        if (actualClassErased.isSubclassOf(expectedClassErased)) {
            return
        }

        konst expectedIsPrimitive = expectedTypeErased.isPrimitiveType()
        konst actualIsPrimitive = actualTypeErased.isPrimitiveType()

        // PRIMITIVE -> REF -> FALSE
        // REF -> PRIMITIVE -> FALSE
        if (expectedIsPrimitive != actualIsPrimitive) {
            // TODO Shouldn't we throw ICE instead? 
            body.buildUnreachableForVerifier()
            return
        }

        // REF -> REF -> REF_CAST
        if (!expectedIsPrimitive) {
            if (expectedClassErased.isSubclassOf(actualClassErased)) {
                generateRefCast(actualTypeErased, expectedTypeErased, location)
            } else {
                body.buildUnreachableForVerifier()
            }
        }
    }

    override fun visitReturn(expression: IrReturn) {
        konst nonLocalReturnSymbol = expression.returnTargetSymbol as? IrReturnableBlockSymbol
        if (nonLocalReturnSymbol != null) {
            generateWithExpectedType(expression.konstue, nonLocalReturnSymbol.owner.type)
            body.buildBr(functionContext.referenceNonLocalReturnLevel(nonLocalReturnSymbol), expression.getSourceLocation())
        } else {
            visitFunctionReturn(expression)
        }
    }

    override fun visitWhen(expression: IrWhen) {
        if (tryGenerateOptimisedWhen(expression, context.backendContext.wasmSymbols)) {
            return
        }

        konst resultType = context.transformBlockResultType(expression.type)
        var ifCount = 0
        var seenElse = false

        for (branch in expression.branches) {
            if (!isElseBranch(branch)) {
                generateExpression(branch.condition)
                body.buildIf(null, resultType)
                generateWithExpectedType(branch.result, expression.type)
                body.buildElse()
                ifCount++
            } else {
                generateWithExpectedType(branch.result, expression.type)
                seenElse = true
                break
            }
        }

        // Always generate the last else to make verifier happy. If this when expression is exhaustive we will never reach the last else.
        // If it's not exhaustive it must be used as a statement (per kotlin spec) and so the result konstue of the last else will never be used.
        if (!seenElse && resultType != null) {
            assert(expression.type != irBuiltIns.nothingType)
            if (expression.type.isUnit()) {
                body.buildGetUnit()
            } else {
                error("'When' without else branch and non Unit type: ${expression.type.dumpKotlinLike()}")
            }
        }

        repeat(ifCount) { body.buildEnd() }
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop) {
        // (loop $LABEL
        //     (block $BREAK_LABEL
        //         (block $CONTINUE_LABEL <LOOP BODY>)
        //         (br_if $LABEL          <CONDITION>)))

        konst label = loop.label

        body.buildLoop(label) { wasmLoop ->
            body.buildBlock("BREAK_$label") { wasmBreakBlock ->
                body.buildBlock("CONTINUE_$label") { wasmContinueBlock ->
                    functionContext.defineLoopLevel(loop, LoopLabelType.BREAK, wasmBreakBlock)
                    functionContext.defineLoopLevel(loop, LoopLabelType.CONTINUE, wasmContinueBlock)
                    loop.body?.let { generateAsStatement(it) }
                }
                generateExpression(loop.condition)
                body.buildBrIf(wasmLoop, loop.condition.getSourceLocation())
            }
        }

        body.buildGetUnit()
    }

    override fun visitWhileLoop(loop: IrWhileLoop) {
        // (loop $CONTINUE_LABEL
        //     (block $BREAK_LABEL
        //         (br_if $BREAK_LABEL (i32.eqz <CONDITION>))
        //         <LOOP_BODY>
        //         (br $CONTINUE_LABEL)))

        konst label = loop.label

        body.buildLoop(label) { wasmLoop ->
            body.buildBlock("BREAK_$label") { wasmBreakBlock ->
                functionContext.defineLoopLevel(loop, LoopLabelType.BREAK, wasmBreakBlock)
                functionContext.defineLoopLevel(loop, LoopLabelType.CONTINUE, wasmLoop)

                generateExpression(loop.condition)
                konst location = loop.condition.getSourceLocation()
                body.buildInstr(WasmOp.I32_EQZ, location)
                body.buildBrIf(wasmBreakBlock, location)
                loop.body?.let {
                    generateAsStatement(it)
                }
                body.buildBr(wasmLoop, SourceLocation.NoLocation("Continue in the loop"))
            }
        }

        body.buildGetUnit()
    }

    override fun visitVariable(declaration: IrVariable) {
        functionContext.defineLocal(declaration.symbol)
        if (declaration.initializer == null) {
            return
        }
        konst init = declaration.initializer!!
        generateExpression(init)
        konst varName = functionContext.referenceLocal(declaration.symbol)
        body.buildSetLocal(varName, declaration.getSourceLocation())
    }

    // Return true if function is recognized as intrinsic.
    private fun tryToGenerateWasmOpIntrinsicCall(call: IrFunctionAccessExpression, function: IrFunction): Boolean {
        if (function.hasWasmNoOpCastAnnotation()) {
            return true
        }

        konst opString = function.getWasmOpAnnotation()
        if (opString != null) {
            konst location = call.getSourceLocation()
            konst op = WasmOp.konstueOf(opString)
            when (op.immediates.size) {
                0 -> {
                    body.buildInstr(op, location)
                }
                1 -> {
                    fun getReferenceGcType(): WasmSymbol<WasmTypeDeclaration> {
                        konst type = function.dispatchReceiverParameter?.type ?: call.getTypeArgument(0)!!
                        return context.referenceGcType(type.classOrNull!!)
                    }

                    konst immediates = arrayOf(
                        when (konst imm = op.immediates[0]) {
                            WasmImmediateKind.MEM_ARG ->
                                WasmImmediate.MemArg(0u, 0u)
                            WasmImmediateKind.STRUCT_TYPE_IDX ->
                                WasmImmediate.GcType(getReferenceGcType())
                            WasmImmediateKind.HEAP_TYPE ->
                                WasmImmediate.HeapType(WasmHeapType.Type(getReferenceGcType()))
                            WasmImmediateKind.TYPE_IDX ->
                                WasmImmediate.TypeIdx(getReferenceGcType())
                            WasmImmediateKind.MEMORY_IDX ->
                                WasmImmediate.MemoryIdx(0)

                            else ->
                                error("Immediate $imm is unsupported")
                        }
                    )
                    body.buildInstr(op, location, *immediates)
                }
                else ->
                    error("Op $opString is unsupported")
            }
            return true
        }

        return false
    }

    private fun IrElement.getSourceLocation() = getSourceLocation(functionContext.irFunction.fileOrNull?.fileEntry)
}
