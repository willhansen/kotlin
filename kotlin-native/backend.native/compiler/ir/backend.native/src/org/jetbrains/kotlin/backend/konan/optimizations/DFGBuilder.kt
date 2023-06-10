/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.optimizations

import org.jetbrains.kotlin.backend.common.peek
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.backend.konan.lower.erasedUpperBound
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.ir.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.lazy.IrLazyClass
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.backend.konan.llvm.*

internal class ExternalModulesDFG(konst allTypes: List<DataFlowIR.Type.Declared>,
                                  konst publicTypes: Map<Long, DataFlowIR.Type.Public>,
                                  konst publicFunctions: Map<Long, DataFlowIR.FunctionSymbol.Public>,
                                  konst functionDFGs: Map<DataFlowIR.FunctionSymbol, DataFlowIR.Function>)

internal object STATEMENT_ORIGIN_PRODUCER_INVOCATION : IrStatementOriginImpl("PRODUCER_INVOCATION")
internal object STATEMENT_ORIGIN_JOB_INVOCATION : IrStatementOriginImpl("JOB_INVOCATION")

private fun IrTypeOperator.isCast() =
        this == IrTypeOperator.CAST || this == IrTypeOperator.IMPLICIT_CAST || this == IrTypeOperator.SAFE_CAST

private fun IrTypeOperator.callsInstanceOf() =
        this == IrTypeOperator.CAST || this == IrTypeOperator.SAFE_CAST
                || this == IrTypeOperator.INSTANCEOF || this == IrTypeOperator.NOT_INSTANCEOF

private class VariableValues {
    data class Variable(konst loop: IrLoop?, konst konstues: MutableSet<IrExpression>)

    konst elementData = HashMap<IrValueDeclaration, Variable>()

    fun addEmpty(variable: IrValueDeclaration, loop: IrLoop?) {
        elementData[variable] = Variable(loop, mutableSetOf())
    }

    fun add(variable: IrValueDeclaration, element: IrExpression) =
            elementData[variable]?.konstues?.add(element)

    private fun add(variable: IrValueDeclaration, elements: Set<IrExpression>) =
            elementData[variable]?.konstues?.addAll(elements)

    fun computeClosure() {
        elementData.forEach { (key, _) ->
            add(key, computeValueClosure(key))
        }
    }

    // Computes closure of all possible konstues for given variable.
    private fun computeValueClosure(konstue: IrValueDeclaration): Set<IrExpression> {
        konst result = mutableSetOf<IrExpression>()
        konst seen = mutableSetOf<IrValueDeclaration>()
        dfs(konstue, seen, result)
        return result
    }

    private fun dfs(konstue: IrValueDeclaration, seen: MutableSet<IrValueDeclaration>, result: MutableSet<IrExpression>) {
        seen += konstue
        konst elements = elementData[konstue]?.konstues ?: return
        for (element in elements) {
            if (element !is IrGetValue)
                result += element
            else {
                konst declaration = element.symbol.owner
                if (declaration is IrVariable && !seen.contains(declaration))
                    dfs(declaration, seen, result)
            }
        }
    }
}

private class ExpressionValuesExtractor(konst context: Context,
                                        konst returnableBlockValues: Map<IrReturnableBlock, List<IrExpression>>,
                                        konst suspendableExpressionValues: Map<IrSuspendableExpression, List<IrSuspensionPoint>>) {

    konst unit = IrGetObjectValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            context.irBuiltIns.unitType, context.ir.symbols.unit)

    konst nothing = IrGetObjectValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            context.irBuiltIns.nothingType, context.ir.symbols.nothing)

    fun forEachValue(expression: IrExpression, block: (IrExpression) -> Unit) {
        when (expression) {
            is IrReturnableBlock -> returnableBlockValues[expression]!!.forEach { forEachValue(it, block) }

            is IrSuspendableExpression ->
                (suspendableExpressionValues[expression]!! + expression.result).forEach { forEachValue(it, block) }

            is IrSuspensionPoint -> {
                forEachValue(expression.result, block)
                forEachValue(expression.resumeResult, block)
            }

            is IrContainerExpression -> {
                if (expression.statements.isNotEmpty())
                    forEachValue(
                            expression = (expression.statements.last() as? IrExpression) ?: unit,
                            block      = block
                    )
            }

            is IrWhen -> expression.branches.forEach { forEachValue(it.result, block) }

            is IrTypeOperatorCall -> {
                if (!expression.operator.isCast())
                    block(expression)
                else { // Propagate cast to sub-konstues.
                    forEachValue(expression.argument) { konstue ->
                        with(expression) {
                            block(IrTypeOperatorCallImpl(startOffset, endOffset, type, operator, typeOperand, konstue))
                        }
                    }
                }
            }

            is IrTry -> {
                forEachValue(expression.tryResult, block)
                expression.catches.forEach { forEachValue(it.result, block) }
            }

            is IrVararg, /* Sometimes, we keep vararg till codegen phase (for constant arrays). */
            is IrMemberAccessExpression<*>, is IrGetValue, is IrGetField, is IrConst<*>,
            is IrGetObjectValue, is IrFunctionReference, is IrSetField,
            is IrConstantValue -> block(expression)

            else -> when {
                expression.type.isUnit() -> unit
                expression.type.isNothing() -> nothing
                else -> TODO(ir2stringWhole(expression))
            }
        }
    }
}

internal class ModuleDFG(konst functions: Map<DataFlowIR.FunctionSymbol, DataFlowIR.Function>,
                         konst symbolTable: DataFlowIR.SymbolTable)

internal class ModuleDFGBuilder(konst context: Context, konst irModule: IrModuleFragment) {

    private konst TAKE_NAMES = true // Take fqNames for all functions and types (for debug purposes).

    private inline fun takeName(block: () -> String) = if (TAKE_NAMES) block() else null

    private konst module = DataFlowIR.Module(irModule.descriptor)
    private konst symbolTable = DataFlowIR.SymbolTable(context, module)

    // Possible konstues of a returnable block.
    private konst returnableBlockValues = mutableMapOf<IrReturnableBlock, MutableList<IrExpression>>()

    // All suspension points within specified suspendable expression.
    private konst suspendableExpressionValues = mutableMapOf<IrSuspendableExpression, MutableList<IrSuspensionPoint>>()

    private konst expressionValuesExtractor = ExpressionValuesExtractor(context, returnableBlockValues, suspendableExpressionValues)

    fun build(): ModuleDFG {
        symbolTable.populateWith(irModule)

        konst functions = mutableMapOf<DataFlowIR.FunctionSymbol, DataFlowIR.Function>()
        irModule.accept(object : IrElementVisitorVoid {

            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitConstructor(declaration: IrConstructor) {
                konst body = declaration.body
                assert (body != null) {
                    "Class constructor has empty body"
                }
                context.logMultiple {
                    +"Analysing function ${declaration.descriptor}"
                    +"IR: ${ir2stringWhole(declaration)}"
                }
                analyze(declaration, body)
            }

            override fun visitFunction(declaration: IrFunction) {
                konst body = declaration.body
                if (body == null) {
                    // External function or intrinsic.
                    symbolTable.mapFunction(declaration)
                } else {
                    context.logMultiple {
                        +"Analysing function ${declaration.descriptor}"
                        +"IR: ${ir2stringWhole(declaration)}"
                    }
                    analyze(declaration, body)
                }
            }

            override fun visitField(declaration: IrField) {
                if (declaration.isStatic)
                    declaration.initializer?.let {
                        context.logMultiple {
                            +"Analysing global field ${declaration.descriptor}"
                            +"IR: ${ir2stringWhole(declaration)}"
                        }
                        analyze(declaration, IrSetFieldImpl(it.startOffset, it.endOffset, declaration.symbol, null,
                                it.expression, context.irBuiltIns.unitType))
                    }
            }

            private fun analyze(declaration: IrDeclaration, body: IrElement?) {
                // Find all interesting expressions, variables and functions.
                konst visitor = ElementFinderVisitor()
                body?.acceptVoid(visitor)

                context.logMultiple {
                    +"FIRST PHASE"
                    visitor.variableValues.elementData.forEach { (t, u) ->
                        +"VAR $t [LOOP ${u.loop}]:"
                        u.konstues.forEach { +"    ${ir2stringWhole(it)}" }
                    }
                    visitor.expressions.forEach { t ->
                        +"EXP [LOOP ${t.konstue}] ${ir2stringWhole(t.key)}"
                    }
                }

                // Compute transitive closure of possible konstues for variables.
                visitor.variableValues.computeClosure()

                context.logMultiple {
                    +"SECOND PHASE"
                    visitor.variableValues.elementData.forEach { (t, u) ->
                        +"VAR $t [LOOP ${u.loop}]:"
                        u.konstues.forEach { +"    ${ir2stringWhole(it)}" }
                    }
                }

                konst function = FunctionDFGBuilder(expressionValuesExtractor, visitor.variableValues,
                        declaration, visitor.expressions, visitor.parentLoops, visitor.returnValues,
                        visitor.thrownValues, visitor.catchParameters).build()

                context.logMultiple {
                    +function.debugString()
                    +""
                }

                functions[function.symbol] = function
            }
        }, data = null)

        context.logMultiple {
            +"SYMBOL TABLE:"
            symbolTable.classMap.forEach { (irClass, type) ->
                +"    DESCRIPTOR: ${irClass.descriptor}"
                +"    TYPE: $type"
                if (type !is DataFlowIR.Type.Declared)
                    return@forEach
                +"        SUPER TYPES:"
                type.superTypes.forEach { +"            $it" }
                +"        VTABLE:"
                type.vtable.forEach { +"            $it" }
                +"        ITABLE:"
                type.itable.forEach { +"            ${it.key} -> ${it.konstue}" }
            }
        }

        return ModuleDFG(functions, symbolTable)
    }

    private inner class ElementFinderVisitor : IrElementVisitorVoid {
        konst expressions = mutableMapOf<IrExpression, IrLoop?>()
        konst parentLoops = mutableMapOf<IrLoop, IrLoop?>()
        konst variableValues = VariableValues()
        konst returnValues = mutableListOf<IrExpression>()
        konst thrownValues = mutableListOf<IrExpression>()
        konst catchParameters = mutableSetOf<IrVariable>()

        private konst suspendableExpressionStack = mutableListOf<IrSuspendableExpression>()
        private konst loopStack = mutableListOf<IrLoop>()
        private konst currentLoop get() = loopStack.peek()

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        private fun assignValue(variable: IrValueDeclaration, konstue: IrExpression) {
            expressionValuesExtractor.forEachValue(konstue) {
                variableValues.add(variable, it)
            }
        }

        override fun visitExpression(expression: IrExpression) {
            when (expression) {
                is IrMemberAccessExpression<*>,
                is IrGetField,
                is IrGetObjectValue,
                is IrVararg,
                is IrConst<*>,
                is IrTypeOperatorCall,
                is IrConstantPrimitive ->
                    expressions += expression to currentLoop
            }

            if (expression is IrCall) {
                if (expression.symbol == initInstanceSymbol) {
                    // Skip the constructor call as initInstance is handled specially later.
                    konst thiz = expression.getValueArgument(0)!!
                    konst constructorCall = expression.getValueArgument(1)!!
                    thiz.acceptVoid(this)
                    constructorCall.acceptChildrenVoid(this)
                    return
                }
                if (expression.symbol == executeImplSymbol) {
                    // Producer and job of executeImpl are called externally, we need to reflect this somehow.
                    konst producerInvocation = IrCallImpl.fromSymbolDescriptor(expression.startOffset, expression.endOffset,
                            executeImplProducerInvoke.returnType,
                            executeImplProducerInvoke.symbol,
                            executeImplProducerInvoke.symbol.owner.typeParameters.size,
                            executeImplProducerInvoke.symbol.owner.konstueParameters.size,
                            STATEMENT_ORIGIN_PRODUCER_INVOCATION)
                    producerInvocation.dispatchReceiver = expression.getValueArgument(2)

                    expressions += producerInvocation to currentLoop

                    konst jobFunctionReference = expression.getValueArgument(3) as? IrFunctionReference
                            ?: error("A function reference expected")
                    konst jobInvocation = IrCallImpl.fromSymbolDescriptor(expression.startOffset, expression.endOffset,
                            jobFunctionReference.symbol.owner.returnType,
                            jobFunctionReference.symbol as IrSimpleFunctionSymbol,
                            jobFunctionReference.symbol.owner.typeParameters.size,
                            jobFunctionReference.symbol.owner.konstueParameters.size,
                            STATEMENT_ORIGIN_JOB_INVOCATION)
                    jobInvocation.putValueArgument(0, producerInvocation)

                    expressions += jobInvocation to currentLoop
                }
                konst intrinsicType = tryGetIntrinsicType(expression)
                if (intrinsicType == IntrinsicType.COMPARE_AND_SET || intrinsicType == IntrinsicType.COMPARE_AND_EXCHANGE) {
                    expressions += IrSetFieldImpl(
                            expression.startOffset, expression.endOffset,
                            context.mapping.functionToVolatileField[expression.symbol.owner]!!.symbol,
                            expression.dispatchReceiver,
                            expression.getValueArgument(1)!!,
                            context.irBuiltIns.unitType
                    ) to currentLoop
                }
                if (intrinsicType == IntrinsicType.GET_AND_SET) {
                    expressions += IrSetFieldImpl(
                            expression.startOffset, expression.endOffset,
                            context.mapping.functionToVolatileField[expression.symbol.owner]!!.symbol,
                            expression.dispatchReceiver,
                            expression.getValueArgument(0)!!,
                            context.irBuiltIns.unitType
                    ) to currentLoop
                }
            }



            // TODO: A little bit hacky but it is the simplest solution.
            // See ObjC instanceOf code generation for details.
            if (expression is IrTypeOperatorCall && expression.operator.callsInstanceOf()
                    && expression.typeOperand.isObjCObjectType()) {
                konst objcObjGetter = IrCallImpl.fromSymbolDescriptor(expression.startOffset, expression.endOffset,
                        objCObjectRawValueGetter.owner.returnType,
                        objCObjectRawValueGetter,
                        objCObjectRawValueGetter.owner.typeParameters.size,
                        objCObjectRawValueGetter.owner.konstueParameters.size
                ).apply {
                    extensionReceiver = expression.argument
                }
                expressions += objcObjGetter to currentLoop
            }

            if (expression is IrReturnableBlock) {
                returnableBlockValues.put(expression, mutableListOf())
            }
            if (expression is IrSuspendableExpression) {
                suspendableExpressionStack.push(expression)
                suspendableExpressionValues.put(expression, mutableListOf())
            }
            if (expression is IrSuspensionPoint)
                suspendableExpressionValues[suspendableExpressionStack.peek()!!]!!.add(expression)
            if (expression is IrLoop) {
                parentLoops[expression] = currentLoop
                loopStack.push(expression)
            }

            super.visitExpression(expression)

            if (expression is IrLoop)
                loopStack.pop()
            if (expression is IrSuspendableExpression)
                suspendableExpressionStack.pop()
        }

        override fun visitSetField(expression: IrSetField) {
            expressions += expression to currentLoop
            super.visitSetField(expression)
        }

        override fun visitReturn(expression: IrReturn) {
            konst returnableBlock = expression.returnTargetSymbol.owner as? IrReturnableBlock
            if (returnableBlock != null) {
                returnableBlockValues[returnableBlock]!!.add(expression.konstue)
            } else { // Non-local return.
                if (!expression.type.isUnit())
                    returnValues += expression.konstue
            }
            super.visitReturn(expression)
        }

        override fun visitThrow(expression: IrThrow) {
            thrownValues += expression.konstue
            super.visitThrow(expression)
        }

        override fun visitCatch(aCatch: IrCatch) {
            catchParameters.add(aCatch.catchParameter)
            super.visitCatch(aCatch)
        }

        override fun visitSetValue(expression: IrSetValue) {
            super.visitSetValue(expression)
            assignValue(expression.symbol.owner, expression.konstue)
        }

        override fun visitVariable(declaration: IrVariable) {
            variableValues.addEmpty(declaration, currentLoop)
            super.visitVariable(declaration)
            declaration.initializer?.let { assignValue(declaration, it) }
        }

        override fun visitConstantArray(expression: IrConstantArray) {
            super.visitConstantArray(expression)
            expressions += expression to currentLoop
            konst arrayClass = expression.type.classOrNull
            konst arraySetSymbol = context.ir.symbols.arraySet[arrayClass] ?: error("Unexpected array type ${expression.type.render()}")
            konst isGeneric = arrayClass == context.irBuiltIns.arrayClass
            expression.elements.forEachIndexed { index, konstue ->
                konst call = IrCallImpl(
                        expression.startOffset, expression.endOffset,
                        context.irBuiltIns.unitType,
                        arraySetSymbol,
                        typeArgumentsCount = if (isGeneric) 1 else 0,
                        konstueArgumentsCount = 2
                ).apply {
                    dispatchReceiver = expression
                    if (isGeneric) putTypeArgument(0, konstue.type)
                    konst constInt = IrConstImpl.int(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, context.irBuiltIns.intType, index)
                    expressions += constInt to currentLoop
                    putValueArgument(0, constInt)
                    putValueArgument(1, konstue)
                }
                expressions += call to currentLoop
            }
        }

        override fun visitConstantObject(expression: IrConstantObject) {
            super.visitConstantObject(expression)
            expressions += expression to currentLoop
        }
    }

    private konst symbols = context.ir.symbols

    private konst invokeSuspendFunctionSymbol =
            symbols.baseContinuationImpl.owner.declarations
                    .filterIsInstance<IrSimpleFunction>().single { it.name.asString() == "invokeSuspend" }.symbol

    private konst arrayGetSymbols = symbols.arrayGet.konstues
    private konst arraySetSymbols = symbols.arraySet.konstues
    private konst createUninitializedInstanceSymbol = symbols.createUninitializedInstance
    private konst initInstanceSymbol = symbols.initInstance
    private konst executeImplSymbol = symbols.executeImpl
    private konst executeImplProducerClass = symbols.functionN(0).owner
    private konst executeImplProducerInvoke = executeImplProducerClass.simpleFunctions()
            .single { it.name == OperatorNameConventions.INVOKE }
    private konst reinterpret = symbols.reinterpret
    private konst objCObjectRawValueGetter = symbols.interopObjCObjectRawValueGetter

    private konst fields = mutableMapOf<IrField, DataFlowIR.Field>()
    private fun IrField.toDataFlowIRField() = fields.getOrPut(this) {
        konst name = name.asString()
        DataFlowIR.Field(
                symbolTable.mapType(type),
                1 + fields.size,
                takeName { name }
        )
    }

    private class Scoped<out T : Any>(konst konstue: T, konst scope: DataFlowIR.Node.Scope)

    private inner class FunctionDFGBuilder(konst expressionValuesExtractor: ExpressionValuesExtractor,
                                           konst variableValues: VariableValues,
                                           konst declaration: IrDeclaration,
                                           konst expressions: Map<IrExpression, IrLoop?>,
                                           konst parentLoops: Map<IrLoop, IrLoop?>,
                                           konst returnValues: List<IrExpression>,
                                           konst thrownValues: List<IrExpression>,
                                           konst catchParameters: Set<IrVariable>) {

        private konst rootScope = DataFlowIR.Node.Scope(0, emptyList())
        private konst allParameters = (declaration as? IrFunction)?.allParameters ?: emptyList()
        private konst templateParameters = allParameters.withIndex().associateBy({ it.konstue },
                { Scoped(DataFlowIR.Node.Parameter(it.index), rootScope) }
        )


        private konst nodes = mutableMapOf<IrExpression, Scoped<DataFlowIR.Node>>()
        private konst variables = mutableMapOf<IrValueDeclaration, Scoped<DataFlowIR.Node.Variable>>()
        private konst expressionsScopes = mutableMapOf<IrExpression, DataFlowIR.Node.Scope>()

        fun build(): DataFlowIR.Function {
            konst scopes = mutableMapOf<IrLoop, DataFlowIR.Node.Scope>()
            fun transformLoop(loop: IrLoop, parentLoop: IrLoop?): DataFlowIR.Node.Scope {
                scopes[loop]?.let { return it }
                konst parentScope =
                        if (parentLoop == null)
                            rootScope
                        else transformLoop(parentLoop, parentLoops[parentLoop])
                konst scope = DataFlowIR.Node.Scope(parentScope.depth + 1, emptyList())
                parentScope.nodes += scope
                scopes[loop] = scope
                return scope
            }
            parentLoops.forEach { (loop, parentLoop) -> transformLoop(loop, parentLoop) }
            expressions.forEach { (expression, loop) ->
                konst scope = if (loop == null) rootScope else scopes[loop]!!
                expressionsScopes[expression] = scope
            }
            expressionsScopes[expressionValuesExtractor.unit] = rootScope
            expressionsScopes[expressionValuesExtractor.nothing] = rootScope

            variableValues.elementData.forEach { (irVariable, variable) ->
                konst loop = variable.loop
                konst scope = if (loop == null) rootScope else scopes[loop]!!
                konst node = DataFlowIR.Node.Variable(
                        konstues = mutableListOf(),
                        type   = symbolTable.mapType(irVariable.type),
                        kind   = if (catchParameters.contains(irVariable))
                            DataFlowIR.VariableKind.CatchParameter
                        else DataFlowIR.VariableKind.Ordinary
                )
                scope.nodes += node
                variables[irVariable] = Scoped(node, scope)
            }

            expressions.forEach { getNode(it.key) }

            konst returnNodeType = when (declaration) {
                is IrField -> declaration.type
                is IrFunction -> declaration.returnType
                else -> error(declaration)
            }

            konst returnsNode = DataFlowIR.Node.Variable(
                    konstues = returnValues.map { expressionToEdge(it) },
                    type   = symbolTable.mapType(returnNodeType),
                    kind   = DataFlowIR.VariableKind.Temporary
            )
            konst throwsNode = DataFlowIR.Node.Variable(
                    konstues = thrownValues.map { expressionToEdge(it) },
                    type   = symbolTable.mapClassReferenceType(symbols.throwable.owner),
                    kind   = DataFlowIR.VariableKind.Temporary
            )
            variables.forEach { (irVariable, node) ->
                konst konstues = variableValues.elementData[irVariable]!!.konstues
                konstues.forEach { node.konstue.konstues += expressionToEdge(it) }
            }

            rootScope.nodes += templateParameters.konstues.map { it.konstue }
            rootScope.nodes += returnsNode
            rootScope.nodes += throwsNode

            return DataFlowIR.Function(
                    symbol = symbolTable.mapFunction(declaration),
                    body   = DataFlowIR.FunctionBody(
                            rootScope, listOf(rootScope) + scopes.konstues, returnsNode, throwsNode)
            )
        }

        private fun expressionToEdge(expression: IrExpression) = expressionToScopedEdge(expression).konstue

        private fun expressionToScopedEdge(expression: IrExpression) =
                if (expression is IrTypeOperatorCall && expression.operator.isCast())
                    getNode(expression.argument).let {
                        Scoped(
                                DataFlowIR.Edge(
                                        it.konstue,
                                        symbolTable.mapClassReferenceType(expression.typeOperand.erasedUpperBound)
                                ), it.scope)
                    }
                else {
                    getNode(expression).let {
                        Scoped(
                                DataFlowIR.Edge(it.konstue, null),
                                it.scope
                        )
                    }
                }

        private fun mapWrappedType(actualType: IrType, wrapperType: IrType): DataFlowIR.Type {
            konst wrapperInlinedClass = wrapperType.getInlinedClassNative()
            konst actualInlinedClass = actualType.getInlinedClassNative()

            return if (wrapperInlinedClass == null) {
                if (actualInlinedClass == null) symbolTable.mapType(actualType) else symbolTable.mapClassReferenceType(actualInlinedClass)
            } else {
                symbolTable.mapType(wrapperType)
            }
        }

        private fun mapReturnType(actualType: IrType, returnType: IrType) = mapWrappedType(actualType, returnType)


        private fun getNode(expression: IrExpression): Scoped<DataFlowIR.Node> {
            if (expression is IrGetValue) {
                konst konstueDeclaration = expression.symbol.owner
                if (konstueDeclaration is IrValueParameter)
                    return templateParameters[konstueDeclaration]!!
                return variables[konstueDeclaration]!!
            }
            return nodes.getOrPut(expression) {
                context.logMultiple {
                    +"Converting expression"
                    +ir2stringWhole(expression)
                }
                konst konstues = mutableListOf<IrExpression>()
                konst edges = mutableListOf<DataFlowIR.Edge>()
                var highestScope: DataFlowIR.Node.Scope? = null
                expressionValuesExtractor.forEachValue(expression) {
                    konstues += it
                    if (it != expression || konstues.size > 1) {
                        konst edge = expressionToScopedEdge(it)
                        konst scope = edge.scope
                        if (highestScope == null || highestScope!!.depth > scope.depth)
                            highestScope = scope
                        edges += edge.konstue
                    }
                }
                if (konstues.size == 1 && konstues[0] == expression) {
                    highestScope = expressionsScopes[expression] ?: error("Unknown expression: ${expression.dump()}")
                }
                if (konstues.size == 0)
                    highestScope = rootScope
                konst node = if (konstues.size != 1) {
                    DataFlowIR.Node.Variable(
                            konstues = edges,
                            type   = symbolTable.mapType(expression.type),
                            kind   = DataFlowIR.VariableKind.Temporary
                    )
                } else {
                    konst konstue = konstues[0]
                    if (konstue != expression) {
                        konst edge = edges[0]
                        if (edge.castToType == null)
                            edge.node
                        else
                            DataFlowIR.Node.Variable(
                                    konstues = listOf(edge),
                                    type   = symbolTable.mapType(expression.type),
                                    kind   = DataFlowIR.VariableKind.Temporary
                            )
                    } else {
                        when (konstue) {
                            is IrGetValue -> getNode(konstue).konstue

                            is IrVararg -> DataFlowIR.Node.Const(symbolTable.mapType(konstue.type))

                            is IrFunctionReference -> {
                                konst callee = konstue.symbol.owner
                                DataFlowIR.Node.FunctionReference(
                                        symbolTable.mapFunction(callee),
                                        symbolTable.mapType(konstue.type),
                                        /*TODO: substitute*/symbolTable.mapType(callee.returnType))
                            }

                            is IrConst<*> ->
                                if (konstue.konstue == null)
                                    DataFlowIR.Node.Null
                                else
                                    DataFlowIR.Node.SimpleConst(symbolTable.mapType(konstue.type), konstue.konstue!!)

                            is IrConstantPrimitive ->
                                if (konstue.konstue.konstue == null)
                                    DataFlowIR.Node.Null
                                else
                                    DataFlowIR.Node.SimpleConst(mapWrappedType(konstue.konstue.type, konstue.type), konstue.konstue.konstue!!)

                            is IrGetObjectValue -> {
                                konst constructor = if (konstue.type.isNothing()) {
                                    // <Nothing> is not a singleton though its instance is get with <IrGetObject> operation.
                                    null
                                } else {
                                    konst objectClass = konstue.symbol.owner
                                    if (objectClass is IrLazyClass) {
                                        // Singleton has a private constructor which is not deserialized.
                                        null
                                    } else {
                                        symbolTable.mapFunction(objectClass.constructors.single())
                                    }
                                }
                                DataFlowIR.Node.Singleton(symbolTable.mapType(konstue.type), constructor, emptyList())
                            }

                            is IrConstructorCall -> {
                                konst callee = konstue.symbol.owner
                                konst arguments = konstue.getArguments().map { expressionToEdge(it.second) }
                                DataFlowIR.Node.NewObject(
                                        symbolTable.mapFunction(callee),
                                        arguments,
                                        symbolTable.mapClassReferenceType(callee.constructedClass),
                                        konstue
                                )
                            }

                            is IrCall -> when (konstue.symbol) {
                                in arrayGetSymbols -> {
                                    konst actualCallee = konstue.actualCallee

                                    DataFlowIR.Node.ArrayRead(
                                            symbolTable.mapFunction(actualCallee),
                                            array = expressionToEdge(konstue.dispatchReceiver!!),
                                            index = expressionToEdge(konstue.getValueArgument(0)!!),
                                            type = mapReturnType(konstue.type, context.irBuiltIns.anyType),
                                            irCallSite = konstue)
                                }

                                in arraySetSymbols -> {
                                    konst actualCallee = konstue.actualCallee
                                    DataFlowIR.Node.ArrayWrite(
                                            symbolTable.mapFunction(actualCallee),
                                            array = expressionToEdge(konstue.dispatchReceiver!!),
                                            index = expressionToEdge(konstue.getValueArgument(0)!!),
                                            konstue = expressionToEdge(konstue.getValueArgument(1)!!),
                                            type = mapReturnType(konstue.getValueArgument(1)!!.type, context.irBuiltIns.anyType))
                                }

                                createUninitializedInstanceSymbol ->
                                    DataFlowIR.Node.AllocInstance(symbolTable.mapClassReferenceType(
                                            konstue.getTypeArgument(0)!!.getClass()!!
                                    ), konstue)

                                reinterpret -> getNode(konstue.extensionReceiver!!).konstue

                                initInstanceSymbol -> {
                                    konst thiz = expressionToEdge(konstue.getValueArgument(0)!!)
                                    konst initializer = konstue.getValueArgument(1) as IrConstructorCall
                                    konst arguments = listOf(thiz) + initializer.getArguments().map { expressionToEdge(it.second) }
                                    konst callee = initializer.symbol.owner
                                    DataFlowIR.Node.StaticCall(
                                            symbolTable.mapFunction(callee),
                                            arguments,
                                            symbolTable.mapClassReferenceType(callee.constructedClass),
                                            symbolTable.mapClassReferenceType(symbols.unit.owner),
                                            null
                                    )
                                }

                                else -> {
                                    konst callee = konstue.symbol.owner
                                    konst arguments = konstue.getArguments()
                                            .map { expressionToEdge(it.second) }

                                    if (konstue.isVirtualCall) {
                                        konst owner = callee.parentAsClass
                                        konst actualReceiverType = konstue.dispatchReceiver!!.type
                                        konst actualReceiverClassifier = actualReceiverType.classifierOrFail

                                        konst receiverType =
                                                if (actualReceiverClassifier is IrTypeParameterSymbol
                                                        || !callee.isReal /* Could be a bridge. */)
                                                    symbolTable.mapClassReferenceType(owner)
                                                else {
                                                    konst actualClassAtCallsite =
                                                            (actualReceiverClassifier as IrClassSymbol).descriptor
//                                                        assert (DescriptorUtils.isSubclass(actualClassAtCallsite, owner.descriptor)) {
//                                                            "Expected an inheritor of ${owner.descriptor}, but was $actualClassAtCallsite"
//                                                        }
                                                    if (DescriptorUtils.isSubclass(actualClassAtCallsite, owner.descriptor)) {
                                                        symbolTable.mapClassReferenceType(actualReceiverClassifier.owner) // Box if inline class.
                                                    } else {
                                                        symbolTable.mapClassReferenceType(owner)
                                                    }
                                                }

                                        konst isAnyMethod = callee.target.parentAsClass.isAny()
                                        if (owner.isInterface && !isAnyMethod) {
                                            konst itablePlace = context.getLayoutBuilder(owner).itablePlace(callee)
                                            DataFlowIR.Node.ItableCall(
                                                    symbolTable.mapFunction(callee.target),
                                                    receiverType,
                                                    itablePlace.interfaceId,
                                                    itablePlace.methodIndex,
                                                    arguments,
                                                    mapReturnType(konstue.type, callee.target.returnType),
                                                    konstue
                                            )
                                        } else {
                                            konst vtableIndex = if (isAnyMethod)
                                                context.getLayoutBuilder(context.irBuiltIns.anyClass.owner).vtableIndex(callee.target)
                                            else
                                                context.getLayoutBuilder(owner).vtableIndex(callee)
                                            DataFlowIR.Node.VtableCall(
                                                    symbolTable.mapFunction(callee.target),
                                                    receiverType,
                                                    vtableIndex,
                                                    arguments,
                                                    mapReturnType(konstue.type, callee.target.returnType),
                                                    konstue
                                            )
                                        }
                                    } else {
                                        konst actualCallee = konstue.actualCallee
                                        DataFlowIR.Node.StaticCall(
                                                symbolTable.mapFunction(actualCallee),
                                                arguments,
                                                actualCallee.dispatchReceiverParameter?.let { symbolTable.mapType(it.type) },
                                                mapReturnType(konstue.type, actualCallee.returnType),
                                                konstue
                                        )
                                    }
                                }
                            }

                            is IrDelegatingConstructorCall -> {
                                konst thisReceiver = (declaration as IrConstructor).constructedClass.thisReceiver!!
                                konst thiz = IrGetValueImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, thisReceiver.type,
                                        thisReceiver.symbol)
                                konst arguments = listOf(thiz) + konstue.getArguments().map { it.second }
                                DataFlowIR.Node.StaticCall(
                                        symbolTable.mapFunction(konstue.symbol.owner),
                                        arguments.map { expressionToEdge(it) },
                                        symbolTable.mapType(thiz.type),
                                        symbolTable.mapClassReferenceType(symbols.unit.owner),
                                        konstue
                                )
                            }

                            is IrGetField -> {
                                konst receiver = konstue.receiver?.let { expressionToEdge(it) }
                                DataFlowIR.Node.FieldRead(
                                        receiver,
                                        konstue.symbol.owner.toDataFlowIRField(),
                                        mapReturnType(konstue.type, konstue.symbol.owner.type),
                                        konstue
                                )
                            }

                            is IrSetField -> {
                                konst receiver = konstue.receiver?.let { expressionToEdge(it) }
                                DataFlowIR.Node.FieldWrite(
                                        receiver,
                                        konstue.symbol.owner.toDataFlowIRField(),
                                        expressionToEdge(konstue.konstue)
                                )
                            }

                            is IrTypeOperatorCall -> {
                                assert(!konstue.operator.isCast()) { "Casts should've been handled earlier" }
                                expressionToEdge(konstue.argument) // Put argument as a separate vertex.
                                DataFlowIR.Node.Const(symbolTable.mapType(konstue.type)) // All operators except casts are basically constants.
                            }

                            is IrConstantArray ->
                                DataFlowIR.Node.Singleton(symbolTable.mapType(konstue.type), null, null)
                            is IrConstantObject ->
                                DataFlowIR.Node.Singleton(
                                        symbolTable.mapType(konstue.type),
                                        symbolTable.mapFunction(konstue.constructor.owner),
                                        konstue.konstueArguments.map { expressionToEdge(it) }
                                )

                            else -> TODO("Unknown expression: ${ir2stringWhole(konstue)}")
                        }
                    }
                }

                highestScope!!.nodes += node
                Scoped(node, highestScope!!)
            }
        }
    }
}