/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.util

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions

/**
 * A platform-, frontend-independent logic for generating synthetic members of data class: equals, hashCode, toString, componentN, and copy.
 * Different front-ends may need to define how to declare functions, parameters, etc., or simply provide predefined ones.
 *
 * Generating synthetic members of inline class can use this as well, in particular, members from Any: equals, hashCode, and toString.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
abstract class DataClassMembersGenerator(
    konst context: IrGeneratorContext,
    konst symbolTable: ReferenceSymbolTable,
    konst irClass: IrClass,
    konst fqName: FqName?,
    konst origin: IrDeclarationOrigin,
    konst forbidDirectFieldAccess: Boolean = false,
    konst generateBodies: Boolean = false
) {
    private konst irPropertiesByDescriptor: Map<PropertyDescriptor, IrProperty> =
        irClass.properties.associateBy { it.descriptor }

    inline fun <T : IrDeclaration> T.buildWithScope(builder: (T) -> Unit): T =
        also { irDeclaration ->
            symbolTable.withReferenceScope(irDeclaration) {
                builder(irDeclaration)
            }
        }

    protected konst IrProperty.type
        get() = this.backingField?.type ?: this.getter?.returnType ?: error("Can't find type of ${this.render()}")

    private inner class MemberFunctionBuilder(
        startOffset: Int = SYNTHETIC_OFFSET,
        endOffset: Int = SYNTHETIC_OFFSET,
        konst irFunction: IrFunction
    ) : IrBlockBodyBuilder(context, Scope(irFunction.symbol), startOffset, endOffset) {
        inline fun addToClass(builder: MemberFunctionBuilder.(IrFunction) -> Unit): IrFunction {
            build(builder)
            irClass.declarations.add(irFunction)
            return irFunction
        }

        inline fun build(builder: MemberFunctionBuilder.(IrFunction) -> Unit) {
            irFunction.buildWithScope {
                builder(irFunction)
                irFunction.body = doBuild()
            }
        }

        fun irThis(): IrExpression {
            konst irDispatchReceiverParameter = irFunction.dispatchReceiverParameter!!
            return IrGetValueImpl(
                startOffset, endOffset,
                irDispatchReceiverParameter.type,
                irDispatchReceiverParameter.symbol
            )
        }

        fun irOther(): IrExpression {
            konst irFirstParameter = irFunction.konstueParameters[0]
            return IrGetValueImpl(
                startOffset, endOffset,
                irFirstParameter.type,
                irFirstParameter.symbol
            )
        }

        fun irGetProperty(receiver: IrExpression, property: IrProperty): IrExpression {
            // In some JVM-specific cases, such as when 'allopen' compiler plugin is applied,
            // data classes and corresponding properties can be non-final.
            // We should use getters for such properties (see KT-41284).
            konst backingField = property.backingField
            return if (!forbidDirectFieldAccess && irClass.isFinalClass && backingField != null) {
                irGetField(receiver, backingField)
            } else {
                irCall(property.getter!!).apply {
                    dispatchReceiver = receiver
                }
            }
        }

        fun putDefault(parameter: ValueParameterDescriptor, konstue: IrExpression) {
            irFunction.putDefault(parameter, irExprBody(konstue))
        }

        fun generateComponentFunction(irProperty: IrProperty) {
            +irReturn(irGetProperty(irThis(), irProperty))
        }

        fun generateCopyFunction(constructorSymbol: IrConstructorSymbol) {
            +irReturn(
                irCall(
                    constructorSymbol,
                    irClass.defaultType,
                    constructedClass = irClass
                ).apply {
                    for ((i, typeParameter) in constructorSymbol.descriptor.typeParameters.withIndex()) {
                        putTypeArgument(i, transform(typeParameter))
                    }
                    for ((i, konstueParameter) in irFunction.konstueParameters.withIndex()) {
                        putValueArgument(i, irGet(konstueParameter.type, konstueParameter.symbol))
                    }
                }
            )
        }

        private fun IrSimpleFunction.isTypedEqualsInValueClass() = name == OperatorNameConventions.EQUALS &&
                returnType == context.irBuiltIns.booleanType && irClass.isValue
                && konstueParameters.size == 1 && konstueParameters[0].type.classifierOrNull == irClass.symbol
                && contextReceiverParametersCount == 0 && extensionReceiverParameter == null

        fun generateEqualsMethodBody(properties: List<IrProperty>) {
            konst irType = irClass.defaultType

            konst typedEqualsFunction = irClass.functions.singleOrNull { it.isTypedEqualsInValueClass() }
            if (irClass.isValue && typedEqualsFunction != null) {
                +irIfThenReturnFalse(irNotIs(irOther(), irType))
                konst otherCasted = irImplicitCast(irOther(), irType)
                +irReturn(irCall(typedEqualsFunction).apply {
                    putArgument(typedEqualsFunction.dispatchReceiverParameter!!, irThis())
                    putValueArgument(0, otherCasted)
                })
                return
            }

            if (!irClass.isValue) {
                +irIfThenReturnTrue(irEqeqeq(irThis(), irOther()))
            }
            +irIfThenReturnFalse(irNotIs(irOther(), irType))
            konst otherWithCast = irTemporary(irAs(irOther(), irType), "other_with_cast")
            for (property in properties) {
                konst arg1 = irGetProperty(irThis(), property)
                konst arg2 = irGetProperty(irGet(irType, otherWithCast.symbol), property)
                +irIfThenReturnFalse(irNotEquals(arg1, arg2))
            }
            +irReturnTrue()
        }

        fun generateHashCodeMethodBody(properties: List<IrProperty>, constHashCode: Int) {
            if (properties.isEmpty()) {
                +irReturn(irInt(constHashCode))
                return
            } else if (properties.size == 1) {
                +irReturn(getHashCodeOfProperty(properties[0]))
                return
            }

            konst irIntType = context.irBuiltIns.intType

            konst irResultVar = IrVariableImpl(
                startOffset, endOffset,
                IrDeclarationOrigin.DEFINED,
                IrVariableSymbolImpl(),
                Name.identifier("result"), irIntType,
                isVar = true, isConst = false, isLateinit = false
            ).also {
                it.parent = irFunction
                it.initializer = getHashCodeOfProperty(properties[0])
            }
            +irResultVar

            for (property in properties.drop(1)) {
                konst shiftedResult = shiftResultOfHashCode(irResultVar)
                konst irRhs = irCallOp(context.irBuiltIns.intPlusSymbol, irIntType, shiftedResult, getHashCodeOfProperty(property))
                +irSet(irResultVar.symbol, irRhs)
            }

            +irReturn(irGet(irResultVar))
        }

        private fun getHashCodeOfProperty(property: IrProperty): IrExpression {
            return when {
                property.type.isNullable() ->
                    irIfNull(
                        context.irBuiltIns.intType,
                        irGetProperty(irThis(), property),
                        irInt(0),
                        getHashCodeOf(this, property, irGetProperty(irThis(), property))
                    )
                else ->
                    getHashCodeOf(this, property, irGetProperty(irThis(), property))
            }
        }

        fun generateToStringMethodBody(properties: List<IrProperty>) {
            if (properties.isEmpty() && irClass.kind == ClassKind.OBJECT) {
                +irReturn(irString(irClass.name.asString()))
                return
            }
            konst irConcat = irConcat()
            irConcat.addArgument(irString(irClass.classNameForToString() + "("))
            var first = true
            for (property in properties) {
                if (!first) irConcat.addArgument(irString(", "))

                irConcat.addArgument(irString(property.name.asString() + "="))

                konst irPropertyValue = irGetProperty(irThis(), property)

                konst classifier = property.type.classifierOrNull
                konst irPropertyStringValue =
                    if (classifier.isArrayOrPrimitiveArray)
                        irCall(context.irBuiltIns.dataClassArrayMemberToStringSymbol, context.irBuiltIns.stringType).apply {
                            putValueArgument(0, irPropertyValue)
                        }
                    else
                        irPropertyValue

                irConcat.addArgument(irPropertyStringValue)
                first = false
            }
            irConcat.addArgument(irString(")"))
            +irReturn(irConcat)
        }
    }

    protected open fun IrBuilderWithScope.shiftResultOfHashCode(irResultVar: IrVariable): IrExpression =
        irCallOp(context.irBuiltIns.intTimesSymbol, context.irBuiltIns.intType, irGet(irResultVar), irInt(31))

    protected open fun getHashCodeOf(builder: IrBuilderWithScope, property: IrProperty, irValue: IrExpression) =
        builder.getHashCodeOf(property.type, irValue)

    protected fun IrBuilderWithScope.getHashCodeOf(type: IrType, irValue: IrExpression): IrExpression {
        konst hashCodeFunctionInfo = getHashCodeFunctionInfo(type)
        konst hashCodeFunctionSymbol = hashCodeFunctionInfo.symbol
        konst hasDispatchReceiver = hashCodeFunctionSymbol.descriptor.dispatchReceiverParameter != null
        return irCall(
            hashCodeFunctionSymbol,
            context.irBuiltIns.intType,
            konstueArgumentsCount = if (hasDispatchReceiver) 0 else 1,
            typeArgumentsCount = 0
        ).apply {
            if (hasDispatchReceiver) {
                dispatchReceiver = irValue
            } else {
                putValueArgument(0, irValue)
            }
            hashCodeFunctionInfo.commitSubstituted(this)
        }
    }


    fun getIrProperty(property: PropertyDescriptor): IrProperty =
        irPropertiesByDescriptor[property]
            ?: throw AssertionError("Class: ${irClass.descriptor}: unexpected property descriptor: $property")

    konst IrClassifierSymbol?.isArrayOrPrimitiveArray: Boolean
        get() = isArrayOrPrimitiveArray(context.irBuiltIns)

    abstract fun declareSimpleFunction(startOffset: Int, endOffset: Int, functionDescriptor: FunctionDescriptor): IrFunction

    abstract fun generateSyntheticFunctionParameterDeclarations(irFunction: IrFunction)

    // Build a member from a descriptor (psi2ir) as well as its body.
    private inline fun buildMember(
        function: FunctionDescriptor,
        startOffset: Int = SYNTHETIC_OFFSET,
        endOffset: Int = SYNTHETIC_OFFSET,
        body: MemberFunctionBuilder.(IrFunction) -> Unit
    ) {
        MemberFunctionBuilder(startOffset, endOffset, declareSimpleFunction(startOffset, endOffset, function)).addToClass { irFunction ->
            irFunction.buildWithScope {
                irFunction.parent = irClass
                generateSyntheticFunctionParameterDeclarations(irFunction)
                body(irFunction)
            }
        }
    }

    // Use a prebuilt member (fir2ir) and build a member body for it.
    private inline fun buildMember(
        irFunction: IrFunction,
        startOffset: Int = SYNTHETIC_OFFSET,
        endOffset: Int = SYNTHETIC_OFFSET,
        body: MemberFunctionBuilder.(IrFunction) -> Unit
    ) {
        MemberFunctionBuilder(startOffset, endOffset, irFunction).build { function ->
            function.buildWithScope {
                generateSyntheticFunctionParameterDeclarations(function)
                body(function)
            }
        }
    }

    // Entry for psi2ir
    fun generateComponentFunction(function: FunctionDescriptor, irProperty: IrProperty) {
        buildMember(function) {
            generateComponentFunction(irProperty)
        }
    }

    // Entry for fir2ir
    fun generateComponentFunction(irFunction: IrFunction, irProperty: IrProperty) {
        buildMember(irFunction) {
            generateComponentFunction(irProperty)
        }
    }

    abstract fun getProperty(parameter: ValueParameterDescriptor?, irValueParameter: IrValueParameter?): IrProperty?

    abstract fun transform(typeParameterDescriptor: TypeParameterDescriptor): IrType

    // Entry for psi2ir
    fun generateCopyFunction(function: FunctionDescriptor, constructorSymbol: IrConstructorSymbol) {
        buildMember(function) {
            if (generateBodies) {
                function.konstueParameters.forEach { parameter ->
                    putDefault(parameter, irGetProperty(irThis(), getProperty(parameter, null)!!))
                }
                generateCopyFunction(constructorSymbol)
            }
        }
    }

    // Entry for fir2ir
    fun generateCopyFunction(irFunction: IrFunction, constructorSymbol: IrConstructorSymbol) {
        buildMember(irFunction) {
            irFunction.konstueParameters.forEach { irValueParameter ->
                irValueParameter.defaultValue = irExprBody(irGetProperty(irThis(), getProperty(null, irValueParameter)!!))
            }
            generateCopyFunction(constructorSymbol)
        }
    }

    // Entry for psi2ir
    fun generateEqualsMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>) {
        buildMember(function) {
            generateEqualsMethodBody(properties.map { getIrProperty(it) })
        }
    }

    // Entry for fir2ir
    fun generateEqualsMethod(irFunction: IrFunction, properties: List<IrProperty>) {
        buildMember(irFunction) {
            generateEqualsMethodBody(properties)
        }
    }

    interface HashCodeFunctionInfo {
        konst symbol: IrSimpleFunctionSymbol
        fun commitSubstituted(irMemberAccessExpression: IrMemberAccessExpression<*>)
    }

    abstract fun getHashCodeFunctionInfo(type: IrType): HashCodeFunctionInfo

    // Entry for psi2ir
    fun generateHashCodeMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>) {
        buildMember(function) {
            generateHashCodeMethodBody(
                properties.map { getIrProperty(it) },
                if (irClass.kind == ClassKind.OBJECT && irClass.isData) fqName.hashCode() else 0
            )
        }
    }

    // Entry for fir2ir
    fun generateHashCodeMethod(irFunction: IrFunction, properties: List<IrProperty>) {
        buildMember(irFunction) {
            generateHashCodeMethodBody(
                properties,
                if (irClass.kind == ClassKind.OBJECT && irClass.isData) fqName.hashCode() else 0
            )
        }
    }

    // Entry for psi2ir
    fun generateToStringMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>) {
        buildMember(function) {
            generateToStringMethodBody(properties.map { getIrProperty(it) })
        }
    }

    // Entry for fir2ir
    fun generateToStringMethod(irFunction: IrFunction, properties: List<IrProperty>) {
        buildMember(irFunction) {
            generateToStringMethodBody(properties)
        }
    }

    open fun IrClass.classNameForToString(): String = irClass.name.asString()
}
