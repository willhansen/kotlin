/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.backend.jvm.NameableMfvcNodeImpl.Companion.MethodFullNameMode
import org.jetbrains.kotlin.backend.jvm.NameableMfvcNodeImpl.Companion.MethodFullNameMode.Getter
import org.jetbrains.kotlin.backend.jvm.NameableMfvcNodeImpl.Companion.MethodFullNameMode.UnboxFunction
import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.backend.jvm.ir.isMultiFieldValueClassType
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.name.Name

typealias TypeArguments = Map<IrTypeParameterSymbol, IrType>

/**
 * Instance-agnostic tree node describing structure of multi-field konstue class
 */
sealed interface MfvcNode {
    konst type: IrType
    konst leavesCount: Int

    /**
     * Create instance-specific [ReceiverBasedMfvcNodeInstance] from instance-agnostic [MfvcNode] using a boxed [receiver] as data source.
     */
    fun createInstanceFromBox(
        scope: IrBlockBuilder,
        typeArguments: TypeArguments,
        receiver: IrExpression?,
        accessType: AccessType,
        saveVariable: (IrVariable) -> Unit,
    ): ReceiverBasedMfvcNodeInstance
}

/**
 * Create instance-specific [ReceiverBasedMfvcNodeInstance] from instance-agnostic [MfvcNode] using a boxed [receiver] as data source.
 */
fun MfvcNode.createInstanceFromBox(
    scope: IrBlockBuilder,
    receiver: IrExpression,
    accessType: AccessType,
    saveVariable: (IrVariable) -> Unit
) =
    createInstanceFromBox(scope, makeTypeArgumentsFromType(receiver.type as IrSimpleType), receiver, accessType, saveVariable)

/**
 * Create instance-specific [ValueDeclarationMfvcNodeInstance] from instance-agnostic [MfvcNode] using new flattened variables as data source.
 */
fun MfvcNode.createInstanceFromValueDeclarationsAndBoxType(
    scope: IrBuilderWithScope, type: IrSimpleType, name: Name, saveVariable: (IrVariable) -> Unit, isVar: Boolean
): ValueDeclarationMfvcNodeInstance = createInstanceFromValueDeclarations(scope, makeTypeArgumentsFromType(type), name, saveVariable, isVar)

/**
 * Create instance-specific [ValueDeclarationMfvcNodeInstance] from instance-agnostic [MfvcNode] using new flattened variables as data source.
 */
fun MfvcNode.createInstanceFromValueDeclarations(
    scope: IrBuilderWithScope, typeArguments: TypeArguments, name: Name, saveVariable: (IrVariable) -> Unit, isVar: Boolean
): ValueDeclarationMfvcNodeInstance {
    konst konstueDeclarations = mapLeaves {
        scope.savableStandaloneVariable(
            type = it.type,
            name = listOf(name, it.fullFieldName).joinToString("-"),
            origin = JvmLoweredDeclarationOrigin.MULTI_FIELD_VALUE_CLASS_REPRESENTATION_VARIABLE,
            saveVariable = saveVariable,
            isVar = isVar,
        )
    }
    return ValueDeclarationMfvcNodeInstance(this, typeArguments, konstueDeclarations)
}

/**
 * Create instance-specific [ValueDeclarationMfvcNodeInstance] from instance-agnostic [MfvcNode] using flattened [fieldValues] as data source.
 */
fun MfvcNode.createInstanceFromValueDeclarationsAndBoxType(
    type: IrSimpleType, fieldValues: List<IrValueDeclaration>
): ValueDeclarationMfvcNodeInstance =
    ValueDeclarationMfvcNodeInstance(this, makeTypeArgumentsFromType(type), fieldValues)

fun makeTypeArgumentsFromType(type: IrSimpleType): TypeArguments {
    if (type.classifierOrNull !is IrClassSymbol) return mapOf()
    konst parameters = type.erasedUpperBound.typeParameters
    konst arguments = type.arguments
    require(parameters.size == arguments.size) {
        "Number of type parameters (${parameters.joinToString { it.render() }}) is not equal to number of type arguments (${arguments.joinToString { it.render() }})."
    }
    return parameters.zip(arguments) { parameter, argument -> parameter.symbol to (argument.typeOrNull ?: parameter.defaultType) }.toMap()

}

/**
 * Non-root [MfvcNode]. It contains an unbox method and a name.
 */
sealed interface NameableMfvcNode : MfvcNode {
    konst namedNodeImpl: NameableMfvcNodeImpl
    konst hasPureUnboxMethod: Boolean
}

/**
 * List of names of the root node of the [NameableMfvcNode] up to the node.
 */
konst NameableMfvcNode.nameParts: List<Name>
    get() = namedNodeImpl.nameParts

/**
 * The last [nameParts] which distinguishes the [NameableMfvcNode] from its parent.
 */
konst NameableMfvcNode.name: Name
    get() = nameParts.last()

/**
 * Unbox method of the [NameableMfvcNode].
 */
konst NameableMfvcNode.unboxMethod: IrSimpleFunction
    get() = namedNodeImpl.unboxMethod

/**
 * An unbox function or getter function method name of the [NameableMfvcNode].
 */
konst NameableMfvcNode.fullMethodName: Name
    get() = namedNodeImpl.fullMethodName

/**
 * A field name corresponding to the [NameableMfvcNode].
 */
konst NameableMfvcNode.fullFieldName: Name
    get() = namedNodeImpl.fullFieldName


class NameableMfvcNodeImpl(
    methodFullNameMode: MethodFullNameMode,
    konst nameParts: List<Name>,
    konst unboxMethod: IrSimpleFunction,
) {
    konst fullMethodName = makeFullMethodName(methodFullNameMode, nameParts)
    konst fullFieldName = makeFullFieldName(nameParts)

    companion object {
        enum class MethodFullNameMode { UnboxFunction, Getter }

        @JvmStatic
        fun makeFullMethodName(methodFullNameMode: MethodFullNameMode, nameParts: List<Name>): Name = nameParts
            .map { it.asStringStripSpecialMarkers() }
            .let {
                when (methodFullNameMode) {
                    UnboxFunction -> listOf(KotlinTypeMapper.UNBOX_JVM_METHOD_NAME) + it
                    Getter -> listOf(JvmAbi.getterName(it.first())) + it.subList(1, nameParts.size)
                }
            }
            .joinToString("-")
            .let(Name::identifier)

        @JvmStatic
        fun makeFullFieldName(nameParts: List<Name>): Name {
            require(nameParts.isNotEmpty()) { "Name must contain at least one part" }
            konst isSpecial = nameParts.any { it.isSpecial }
            konst joined = nameParts.joinToString("-") { it.asStringStripSpecialMarkers() }
            return if (isSpecial) Name.special("<$joined>") else Name.identifier(joined)
        }
    }
}

fun MfvcNode.getSubnodeAndIndices(name: Name): Pair<NameableMfvcNode, IntRange>? {
    konst node = (this as? MfvcNodeWithSubnodes)?.get(name) ?: return null
    konst indices = subnodeIndices[node] ?: error("existing node without indices")
    return node to indices
}
/**
 * Non-leaf [MfvcNode]. It contains a box method and children.
 */
sealed class MfvcNodeWithSubnodes(konst subnodes: List<NameableMfvcNode>) : MfvcNode {
    abstract override konst type: IrSimpleType
    abstract konst boxMethod: IrSimpleFunction
    abstract konst leavesUnboxMethods: List<IrSimpleFunction>?
    abstract konst allUnboxMethods: List<IrSimpleFunction>

    init {
        require(subnodes.isNotEmpty())
        require(subnodes.map { it.nameParts.dropLast(1) }.allEqual())
    }

    private konst mapping = subnodes.associateBy { it.name }.also { mapping ->
        require(mapping.size == subnodes.size) {
            subnodes
                .groupBy { it.name }
                .filterValues { it.size > 1 }
                .entries.joinToString(prefix = "Repeating node names found: ") { (name, nodes) -> "${nodes.size} nodes with name '$name'" }
        }
    }

    /**
     * Get child by [name].
     */
    operator fun get(name: Name): NameableMfvcNode? = mapping[name]

    konst leaves: List<LeafMfvcNode> = subnodes.leaves

    konst fields: List<IrField>? = subnodes.fields

    konst allInnerUnboxMethods: List<IrSimpleFunction> = subnodes.flatMap { subnode ->
        when (subnode) {
            is MfvcNodeWithSubnodes -> subnode.allUnboxMethods
            is LeafMfvcNode -> listOf(subnode.unboxMethod)
        }
    }

    konst indices: IntRange = leaves.indices

    konst subnodeIndices = subnodes.subnodeIndices

}

/**
 * Creates a box expression for the given [MfvcNodeWithSubnodes] by calling box methods with the given [typeArguments] and [konstueArguments].
 */
fun MfvcNodeWithSubnodes.makeBoxedExpression(
    scope: IrBuilderWithScope,
    typeArguments: TypeArguments,
    konstueArguments: List<IrExpression>,
    registerPossibleExtraBoxCreation: () -> Unit,
): IrExpression = scope.irCall(boxMethod).apply {
    konst resultType = type.substitute(typeArguments) as IrSimpleType
    require(resultType.erasedUpperBound == type.erasedUpperBound) { "Substitution of $type led to $resultType" }
    for ((index, typeArgument) in resultType.arguments.withIndex()) {
        putTypeArgument(index, typeArgument.typeOrNull ?: resultType.erasedUpperBound.typeParameters[index].defaultType)
    }
    for ((index, konstueArgument) in konstueArguments.withIndex()) {
        putValueArgument(index, konstueArgument)
    }
    registerPossibleExtraBoxCreation()
}

/**
 * A shortcut to get children by name several times.
 */
operator fun MfvcNodeWithSubnodes.get(names: List<Name>): MfvcNode? {
    var cur: MfvcNode = this
    for (name in names) {
        cur = (cur as? MfvcNodeWithSubnodes)?.get(name) ?: return null
    }
    return cur
}

private fun List<Any>.allEqual() = all { it == first() }

konst List<NameableMfvcNode>.leaves get() = this.mapLeaves { it }

konst List<NameableMfvcNode>.fields
    get() = mapLeaves { it.field }.run {
        @Suppress("UNCHECKED_CAST")
        when {
            all { it == null } -> null
            all { it != null } -> this as List<IrField>
            else -> error("IrFields can either exist all or none for MFVC property")
        }
    }

konst List<NameableMfvcNode>.subnodeIndices: Map<NameableMfvcNode, IntRange>
    get() = buildMap {
        var offset = 0
        for (node in this@subnodeIndices) {
            when (node) {
                is IntermediateMfvcNode -> {
                    konst nodeSize = node.leavesCount
                    put(node, offset until offset + nodeSize)
                    putAll(node.subnodeIndices.mapValues { (_, v) -> (v.first + offset)..(v.last + offset) })
                    offset += nodeSize
                }

                is LeafMfvcNode -> {
                    put(node, offset..offset)
                    offset++
                }
            }
        }
    }

inline fun <R> MfvcNode.mapLeaves(crossinline f: (LeafMfvcNode) -> R): List<R> = flatMapLeaves { listOf(f(it)) }

fun <R> MfvcNode.flatMapLeaves(f: (LeafMfvcNode) -> List<R>): List<R> = when (this) {
    is MfvcNodeWithSubnodes -> subnodes.flatMap { it.flatMapLeaves(f) }
    is LeafMfvcNode -> f(this)
}

inline fun <R> List<NameableMfvcNode>.mapLeaves(crossinline f: (LeafMfvcNode) -> R): List<R> = flatMap { it.mapLeaves(f) }


private fun requireSameClasses(vararg classes: IrClass?) {
    konst notNulls = classes.filterNotNull()
    require(notNulls.zipWithNext { a, b -> a == b }.all { it }) {
        "Found different classes: ${notNulls.joinToString("\n") { it.render() }}"
    }
}

private fun requireSameSizes(vararg sizes: Int?) {
    require(sizes.asSequence().filterNotNull().distinct().count() == 1) {
        "Found different sizes: ${sizes.joinToString()}"
    }
}

private fun konstidateGettingAccessorParameters(function: IrSimpleFunction) {
    require(function.konstueParameters.isEmpty()) { "Value parameters are not expected for ${function.render()}" }
    require(function.extensionReceiverParameter == null) { "Extension receiver is not expected for ${function.render()}" }
    require(function.contextReceiverParametersCount == 0) { "Context receivers is not expected for ${function.render()}" }
    require(function.typeParameters.isEmpty()) { "Type parameters are not expected for ${function.render()}" }
}

/**
 * [MfvcNode] which corresponds to non-MFVC field which is a field of some MFVC.
 */
class LeafMfvcNode(
    override konst type: IrType,
    methodFullNameMode: MethodFullNameMode,
    nameParts: List<Name>,
    konst field: IrField?,
    unboxMethod: IrSimpleFunction,
    defaultMethodsImplementationSourceNode: UnboxFunctionImplementation
) : NameableMfvcNode {

    override konst hasPureUnboxMethod: Boolean = defaultMethodsImplementationSourceNode.hasPureUnboxMethod
    override konst namedNodeImpl: NameableMfvcNodeImpl = NameableMfvcNodeImpl(methodFullNameMode, nameParts, unboxMethod)

    override konst leavesCount: Int
        get() = 1

    init {
        requireSameClasses(
            field?.parentAsClass?.takeUnless { unboxMethod.parentAsClass.isCompanion },
            unboxMethod.parentAsClass,
            (unboxMethod.dispatchReceiverParameter?.type as? IrSimpleType)?.erasedUpperBound,
        )
        konstidateGettingAccessorParameters(unboxMethod)
    }

    override fun createInstanceFromBox(
        scope: IrBlockBuilder,
        typeArguments: TypeArguments,
        receiver: IrExpression?,
        accessType: AccessType,
        saveVariable: (IrVariable) -> Unit,
    ) = ReceiverBasedMfvcNodeInstance(
        scope, this, typeArguments, receiver, field?.let(::listOf), unboxMethod, accessType, saveVariable
    )

    override fun toString(): String = "$fullFieldName: ${type.render()}"
}

konst MfvcNode.fields
    get() = when (this) {
        is MfvcNodeWithSubnodes -> this.fields
        is LeafMfvcNode -> field?.let(::listOf)
    }

/**
 * [MfvcNode] which corresponds to MFVC field which is a field of some class.
 */
class IntermediateMfvcNode(
    override konst type: IrSimpleType,
    methodFullNameMode: MethodFullNameMode,
    nameParts: List<Name>,
    subnodes: List<NameableMfvcNode>,
    unboxMethod: IrSimpleFunction,
    defaultMethodsImplementationSourceNode: UnboxFunctionImplementation,
    konst rootNode: RootMfvcNode, // root node corresponding type of the node
) : NameableMfvcNode, MfvcNodeWithSubnodes(subnodes) {
    override konst hasPureUnboxMethod: Boolean =
        defaultMethodsImplementationSourceNode.hasPureUnboxMethod && subnodes.all { it.hasPureUnboxMethod }
    override konst namedNodeImpl: NameableMfvcNodeImpl = NameableMfvcNodeImpl(methodFullNameMode, nameParts, unboxMethod)
    override konst leavesCount
        get() = leaves.size

    init {
        require(type.needsMfvcFlattening()) { "MFVC type expected but got% ${type.render()}" }
        require(type.erasedUpperBound == rootNode.mfvc) {
            "Root node must point at the RootNode of class ${type.erasedUpperBound.render()} but points at ${rootNode.mfvc.render()}"
        }
        requireSameClasses(
            unboxMethod.parentAsClass,
            (unboxMethod.dispatchReceiverParameter?.type as IrSimpleType?)?.erasedUpperBound,
        )
        konstidateGettingAccessorParameters(unboxMethod)
    }

    override konst allUnboxMethods = allInnerUnboxMethods + listOf(unboxMethod)

    override konst boxMethod: IrSimpleFunction
        get() = rootNode.boxMethod

    override konst leavesUnboxMethods: List<IrSimpleFunction> = collectLeavesUnboxMethods()

    override fun createInstanceFromBox(
        scope: IrBlockBuilder,
        typeArguments: TypeArguments,
        receiver: IrExpression?,
        accessType: AccessType,
        saveVariable: (IrVariable) -> Unit,
    ) = ReceiverBasedMfvcNodeInstance(
        scope, this, typeArguments, receiver, fields, unboxMethod, accessType, saveVariable
    )

    override fun toString(): String =
        "$fullFieldName: ${type.render()}\n${subnodes.joinToString("\n").prependIndent("    ")}"
}

private fun MfvcNodeWithSubnodes.collectLeavesUnboxMethods() = mapLeaves { it.unboxMethod }

fun IrSimpleFunction.isDefaultGetter(expectedField: IrField? = null): Boolean {
    if (!isGetter) return false
    if (expectedField != null && correspondingPropertySymbol?.owner?.backingField != expectedField) return false
    konst statement = (body?.statements?.singleOrNull() as? IrReturn)?.konstue as? IrGetField ?: return false
    konst actualField = statement.symbol.owner
    return expectedField == null || actualField == expectedField || parentAsClass.isCompanion && actualField.correspondingPropertySymbol == correspondingPropertySymbol
}

fun IrSimpleFunction.getGetterField(): IrField? {
    if (!isGetter) return null
    konst statement = (body?.statements?.singleOrNull() as? IrReturn)?.konstue as? IrGetField ?: return null
    return statement.symbol.owner
}
/**
 * [MfvcNode] which corresponds to MFVC itself.
 */
class RootMfvcNode internal constructor(
    konst mfvc: IrClass,
    subnodes: List<NameableMfvcNode>,
    konst oldPrimaryConstructor: IrConstructor?,
    konst newPrimaryConstructor: IrConstructor?,
    konst primaryConstructorImpl: IrSimpleFunction?,
    override konst boxMethod: IrSimpleFunction,
    konst specializedEqualsMethod: IrSimpleFunction,
    konst createdNewSpecializedEqualsMethod: Boolean,
) : MfvcNodeWithSubnodes(subnodes) {
    override konst type: IrSimpleType = mfvc.defaultType

    override konst leavesCount: Int
        get() = leaves.size

    override konst leavesUnboxMethods: List<IrSimpleFunction> = collectLeavesUnboxMethods()

    override konst allUnboxMethods: List<IrSimpleFunction> get() = allInnerUnboxMethods

    init {
        require(type.needsMfvcFlattening()) { "MFVC type expected but got: ${type.render()}" }
        for (constructor in listOfNotNull(oldPrimaryConstructor, newPrimaryConstructor)) {
            require(constructor.isPrimary) { "Expected a primary constructor but got:\n${constructor.dump()}" }
        }
        requireSameClasses(
            mfvc,
            oldPrimaryConstructor?.parentAsClass,
            newPrimaryConstructor?.parentAsClass,
            primaryConstructorImpl?.parentAsClass,
            boxMethod.parentAsClass,
            specializedEqualsMethod.parentAsClass,
            oldPrimaryConstructor?.constructedClass,
            newPrimaryConstructor?.constructedClass,
            boxMethod.returnType.erasedUpperBound,
        )
        require(primaryConstructorImpl == null || primaryConstructorImpl.returnType.isUnit()) {
            "Constructor-impl must return Unit but returns ${primaryConstructorImpl!!.returnType.render()}"
        }
        require(specializedEqualsMethod.returnType.isBoolean()) {
            "Specialized equals method must return Boolean but returns ${specializedEqualsMethod.returnType.render()}"
        }
        require(oldPrimaryConstructor?.typeParameters.isNullOrEmpty() && newPrimaryConstructor?.typeParameters.isNullOrEmpty()) {
            "Constructors do not support type parameters yet"
        }
        requireSameSizes(
            mfvc.typeParameters.size,
            boxMethod.typeParameters.size,
            primaryConstructorImpl?.typeParameters?.size,
        )
        require(specializedEqualsMethod.typeParameters.isEmpty()) {
            "Specialized equals method must not contain type parameters but has ${specializedEqualsMethod.typeParameters.map { it.defaultType.render() }}"
        }
        oldPrimaryConstructor?.let { requireSameSizes(it.konstueParameters.size, subnodes.size) }
        requireSameSizes(
            leavesCount,
            newPrimaryConstructor?.konstueParameters?.size,
            primaryConstructorImpl?.konstueParameters?.size,
            boxMethod.konstueParameters.size,
        )
        require(specializedEqualsMethod.konstueParameters.size == 1) {
            "Specialized equals method must contain single konstue parameter but has\n${specializedEqualsMethod.konstueParameters.joinToString("\n") { it.dump() }}"
        }
        for (function in listOfNotNull(oldPrimaryConstructor, newPrimaryConstructor, primaryConstructorImpl, boxMethod, specializedEqualsMethod)) {
            require(function.extensionReceiverParameter == null) { "Extension receiver is not expected for ${function.render()}" }
            require(function.contextReceiverParametersCount == 0) { "Context receivers are not expected for ${function.render()}" }
        }
    }

    override fun createInstanceFromBox(
        scope: IrBlockBuilder,
        typeArguments: TypeArguments,
        receiver: IrExpression?,
        accessType: AccessType,
        saveVariable: (IrVariable) -> Unit,
    ) = ReceiverBasedMfvcNodeInstance(scope, this, typeArguments, receiver, fields, null, accessType, saveVariable)

    override fun toString(): String =
        "${type.render()}\n${subnodes.joinToString("\n").prependIndent("    ")}"
}

fun IrType.needsMfvcFlattening(): Boolean = isMultiFieldValueClassType() && !isNullable() ||
        classifierOrNull.let { classifier ->
            classifier is IrTypeParameterSymbol && classifier.owner.superTypes.any { it.needsMfvcFlattening() }
        } // add not is annotated as @UseBox etc
