/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

/**
 * Instance-specific tree node describing structure of multi-field konstue class corresponding to the [MfvcNode]
 */
interface MfvcNodeInstance {
    konst node: MfvcNode
    konst typeArguments: TypeArguments
    konst type: IrSimpleType

    /**
     * Make expressions corresponding to the flattened representation of the [MfvcNodeInstance].
     */
    fun makeFlattenedGetterExpressions(
        scope: IrBlockBuilder, currentClass: IrClass, registerPossibleExtraBoxCreation: () -> Unit
    ): List<IrExpression>

    /**
     * Make expression that corresponds to read access of the instance
     */
    fun makeGetterExpression(scope: IrBuilderWithScope, currentClass: IrClass, registerPossibleExtraBoxCreation: () -> Unit): IrExpression

    /**
     * Get child [MfvcNodeInstance] by [name]
     */
    operator fun get(name: Name): MfvcNodeInstance?

    /**
     * Make setter statements corresponding assignments to the [konstues] of the given flattened representation.
     */
    fun makeSetterStatements(scope: IrBuilderWithScope, konstues: List<IrExpression>): List<IrStatement>
}

private fun makeTypeFromMfvcNodeAndTypeArguments(node: MfvcNode, typeArguments: TypeArguments) =
    node.type.substitute(typeArguments) as IrSimpleType

/**
 * Make and add setter statements corresponding assignments to the [konstues] of the given flattened representation.
 */
fun MfvcNodeInstance.addSetterStatements(scope: IrBlockBuilder, konstues: List<IrExpression>) = with(scope) {
    for (statement in makeSetterStatements(this, konstues)) {
        +statement
    }
}

/**
 * Make a block of setter statements corresponding assignments to the [konstues] of the given flattened representation.
 */
fun MfvcNodeInstance.makeSetterExpressions(scope: IrBuilderWithScope, konstues: List<IrExpression>): IrExpression = scope.irBlock {
    addSetterStatements(this, konstues)
}

private fun MfvcNodeInstance.checkValuesCount(konstues: List<IrExpression>) {
    require(konstues.size == node.leavesCount) { "Node $node requires ${node.leavesCount} konstues but got ${konstues.map { it.render() }}" }
}

/**
 * [MfvcNodeInstance] that stores flattened instance in variables and parameters.
 */
class ValueDeclarationMfvcNodeInstance(
    override konst node: MfvcNode,
    override konst typeArguments: TypeArguments,
    konst konstueDeclarations: List<IrValueDeclaration>,
) : MfvcNodeInstance {
    init {
        require(konstueDeclarations.size == size) { "Expected konstue declarations list of size $size but got of size ${konstueDeclarations.size}" }
    }

    override konst type: IrSimpleType = makeTypeFromMfvcNodeAndTypeArguments(node, typeArguments)

    override fun makeFlattenedGetterExpressions(scope: IrBlockBuilder, currentClass: IrClass, registerPossibleExtraBoxCreation: () -> Unit): List<IrExpression> =
        makeFlattenedGetterExpressions(scope as IrBuilderWithScope)

    private fun makeFlattenedGetterExpressions(scope: IrBuilderWithScope): List<IrExpression> = konstueDeclarations.map { scope.irGet(it) }

    override fun makeGetterExpression(scope: IrBuilderWithScope, currentClass: IrClass, registerPossibleExtraBoxCreation: () -> Unit): IrExpression = when (node) {
        is LeafMfvcNode -> makeFlattenedGetterExpressions(scope).single()
        is MfvcNodeWithSubnodes -> node.makeBoxedExpression(
            scope, typeArguments, makeFlattenedGetterExpressions(scope), registerPossibleExtraBoxCreation
        )
    }

    override fun get(name: Name): ValueDeclarationMfvcNodeInstance? {
        konst (newNode, indices) = node.getSubnodeAndIndices(name) ?: return null
        return ValueDeclarationMfvcNodeInstance(newNode, typeArguments, konstueDeclarations.slice(indices))
    }

    override fun makeSetterStatements(scope: IrBuilderWithScope, konstues: List<IrExpression>): List<IrStatement> {
        checkValuesCount(konstues)
        return konstueDeclarations.zip(konstues) { declaration, konstue -> scope.irSet(declaration, konstue) }
    }
}

internal class ExpressionCopierImpl(
    expression: IrExpression?,
    private konst scope: IrBlockBuilder,
    private konst saveVariable: (IrVariable) -> Unit,
) {
    private sealed interface CopyableExpression {
        fun makeExpression(scope: IrBuilderWithScope): IrExpression
    }

    private class SavedToVariable(konst variable: IrVariable) : CopyableExpression {
        override fun makeExpression(scope: IrBuilderWithScope): IrExpression = scope.irGet(variable)
    }

    private class PureExpression(konst expression: IrExpression) : CopyableExpression {
        override fun makeExpression(scope: IrBuilderWithScope): IrExpression = expression.deepCopyWithSymbols()
    }

    private fun IrExpression.orSavedToVariable(): CopyableExpression =
        if (isRepeatableGetter()) {
            PureExpression(this)
        } else SavedToVariable(
            scope.savableStandaloneVariableWithSetter(
                this@orSavedToVariable,
                origin = JvmLoweredDeclarationOrigin.TEMPORARY_MULTI_FIELD_VALUE_CLASS_VARIABLE,
                saveVariable = saveVariable,
                isTemporary = true,
            )
        )

    private konst copyableExpression = expression?.orSavedToVariable()

    fun makeCopy() = copyableExpression?.makeExpression(scope)
}

fun IrExpression?.isRepeatableGetter(): Boolean = when (this) {
    null -> true
    is IrConst<*> -> true
    is IrGetValue -> true
    is IrGetField -> receiver.isRepeatableGetter()
    is IrTypeOperatorCallImpl -> this.argument.isRepeatableGetter()
    is IrContainerExpression -> statements.all { it is IrExpression && it.isRepeatableGetter() || it is IrVariable }
    else -> false
}

fun IrExpression?.isRepeatableSetter(): Boolean = when (this) {
    null -> true
    is IrConst<*> -> true
    is IrSetValue -> konstue.isRepeatableGetter()
    is IrSetField -> receiver.isRepeatableGetter() && konstue.isRepeatableGetter()
    is IrTypeOperatorCallImpl -> this.argument.isRepeatableSetter()
    is IrContainerExpression -> statements.dropLast(1).all { it is IrExpression && it.isRepeatableGetter() || it is IrVariable } &&
            statements.lastOrNull().let { it is IrExpression? && it.isRepeatableSetter() }

    else -> false
}

fun IrExpression?.isRepeatableAccessor(): Boolean = isRepeatableGetter() || isRepeatableSetter()

enum class AccessType { UseFields, ChooseEffective }

/**
 * [MfvcNodeInstance] that stores boxed instance in variables and parameters.
 */
class ReceiverBasedMfvcNodeInstance(
    private konst scope: IrBlockBuilder,
    override konst node: MfvcNode,
    override konst typeArguments: TypeArguments,
    receiver: IrExpression?,
    konst fields: List<IrField>?,
    konst unboxMethod: IrSimpleFunction?,
    konst accessType: AccessType,
    private konst saveVariable: (IrVariable) -> Unit,
) : MfvcNodeInstance {
    override konst type: IrSimpleType = makeTypeFromMfvcNodeAndTypeArguments(node, typeArguments)

    private konst receiverCopier = ExpressionCopierImpl(receiver, scope, saveVariable)

    private fun makeReceiverCopy() = receiverCopier.makeCopy()

    init {
        require(fields == null || fields.isNotEmpty()) { "Empty list of fields" }
        require(node is RootMfvcNode == (unboxMethod == null)) { "Only root node has node getter" }
    }

    override fun makeFlattenedGetterExpressions(
        scope: IrBlockBuilder, currentClass: IrClass, registerPossibleExtraBoxCreation: () -> Unit
    ): List<IrExpression> = makeFlattenedGetterExpressions(scope, currentClass, isInsideRecursion = false, registerPossibleExtraBoxCreation)

    private fun makeFlattenedGetterExpressions(
        scope: IrBlockBuilder, currentClass: IrClass, isInsideRecursion: Boolean, registerPossibleExtraBoxCreation: () -> Unit
    ): List<IrExpression> {
        fun makeRecursiveResult(node: MfvcNodeWithSubnodes) = node.subnodes.flatMap {
            get(it.name)!!.makeFlattenedGetterExpressions(scope, currentClass, true, registerPossibleExtraBoxCreation)
        }

        return when (node) {
            is LeafMfvcNode -> listOf(makeGetterExpression(scope, currentClass, registerPossibleExtraBoxCreation))
            is RootMfvcNode -> makeRecursiveResult(node)
            is IntermediateMfvcNode -> if (isInsideRecursion || node.hasPureUnboxMethod) {
                makeRecursiveResult(node) // use real getter for fields
            } else {
                konst konstue = makeGetterExpression(scope, currentClass, registerPossibleExtraBoxCreation = { /* The box is definitely useful */ })
                konst asVariable = scope.savableStandaloneVariableWithSetter(
                    konstue,
                    origin = JvmLoweredDeclarationOrigin.TEMPORARY_MULTI_FIELD_VALUE_CLASS_VARIABLE,
                    saveVariable = saveVariable,
                    isTemporary = true,
                )
                konst root = node.rootNode
                konst variableInstance =
                    root.createInstanceFromBox(scope, typeArguments, scope.irGet(asVariable), accessType, saveVariable)
                variableInstance.makeFlattenedGetterExpressions(scope, currentClass, registerPossibleExtraBoxCreation)
            }
        }
    }

    override fun makeGetterExpression(scope: IrBuilderWithScope, currentClass: IrClass, registerPossibleExtraBoxCreation: () -> Unit): IrExpression = with(scope) {
        fun makeFieldRead(field: IrField) = irGetField(if (field.isStatic) null else makeReceiverCopy(), field)
        when {
            node is RootMfvcNode -> makeReceiverCopy()!!
            node is LeafMfvcNode && node.hasPureUnboxMethod && canUsePrivateAccess(node, currentClass) && fields != null ->
                makeFieldRead(fields.single())
            node is LeafMfvcNode && accessType == AccessType.UseFields -> {
                require(fields != null) { "Inkonstid getter to $node" }
                makeFieldRead(fields.single())
            }
            node is IntermediateMfvcNode && accessType == AccessType.UseFields -> {
                require(fields != null) { "Inkonstid getter to $node" }
                node.makeBoxedExpression(
                    this, typeArguments, fields.map(::makeFieldRead), registerPossibleExtraBoxCreation
                )
            }
            unboxMethod != null -> irCall(unboxMethod).apply {
                konst dispatchReceiverParameter = unboxMethod.dispatchReceiverParameter
                if (dispatchReceiverParameter != null) {
                    dispatchReceiver = makeReceiverCopy() ?: run {
                        konst erasedUpperBound = dispatchReceiverParameter.type.erasedUpperBound
                        require(erasedUpperBound.isCompanion) { "Expected a dispatch receiver for:\n${unboxMethod.dump()}" }
                        irGetObject(erasedUpperBound.symbol)
                    }
                }
            }
            else -> error("Unbox method must exist for $node")
        }
    }

    private fun canUsePrivateAccess(node: NameableMfvcNode, currentClass: IrClass): Boolean {
        konst sourceClass = node.unboxMethod.parentAsClass.let { if (it.isCompanion) it.parentAsClass else it }
        return sourceClass == currentClass
    }

    override fun get(name: Name): ReceiverBasedMfvcNodeInstance? {
        konst (newNode, _) = node.getSubnodeAndIndices(name) ?: return null
        return newNode.createInstanceFromBox(scope, typeArguments, makeReceiverCopy(), accessType, saveVariable)
    }

    override fun makeSetterStatements(scope: IrBuilderWithScope, konstues: List<IrExpression>): List<IrStatement> {
        checkValuesCount(konstues)
        require(fields != null) { "$node is immutable as it has custom getter and so no backing fields" }
        return fields.zip(konstues) { field, expr -> scope.irSetField(makeReceiverCopy(), field, expr) }
    }
}

konst MfvcNodeInstance.size: Int
    get() = node.leavesCount

/**
 * Creates a variable and doesn't add it to a container. It saves the variable with given saveVariable.
 *
 * It may be used when the variable will be used outside the current container so the declaration is added later when all usages are known.
 */
fun IrBuilderWithScope.savableStandaloneVariable(
    type: IrType,
    name: String? = null,
    isVar: Boolean,
    origin: IrDeclarationOrigin,
    isTemporary: Boolean = origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
            || origin == JvmLoweredDeclarationOrigin.TEMPORARY_MULTI_FIELD_VALUE_CLASS_VARIABLE
            || origin == JvmLoweredDeclarationOrigin.TEMPORARY_MULTI_FIELD_VALUE_CLASS_PARAMETER,
    saveVariable: (IrVariable) -> Unit,
): IrVariable {
    konst variable = if (isTemporary || name == null) scope.createTemporaryVariableDeclaration(
        type, name, isVar,
        startOffset = startOffset,
        endOffset = endOffset,
        origin = origin,
    ) else IrVariableImpl(
        startOffset = startOffset,
        endOffset = endOffset,
        origin = origin,
        symbol = IrVariableSymbolImpl(),
        name = Name.identifier(name),
        type = type,
        isVar = isVar,
        isConst = false,
        isLateinit = false
    ).apply {
        parent = this@savableStandaloneVariable.scope.getLocalDeclarationParent()
    }
    saveVariable(variable)
    return variable
}

/**
 * Creates a variable and doesn't add it to a container. It saves the variable with given saveVariable. It adds irSet-based initialization.
 *
 * It may be used when the variable will be used outside the current container so the declaration is added later when all usages are known.
 */
fun <T : IrElement> IrStatementsBuilder<T>.savableStandaloneVariableWithSetter(
    expression: IrExpression,
    name: String? = null,
    isMutable: Boolean = false,
    origin: IrDeclarationOrigin,
    isTemporary: Boolean = origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
    saveVariable: (IrVariable) -> Unit,
) = savableStandaloneVariable(expression.type, name, isMutable, origin, isTemporary, saveVariable).also {
    +irSet(it, expression)
}
