/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.ir

import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

fun IrBuilderWithScope.getProperty(receiver: IrExpression, property: IrProperty): IrExpression {
    return if (property.getter != null)
        irGet(property.getter!!.returnType, receiver, property.getter!!.symbol)
    else
        irGetField(receiver, property.backingField!!)
}

/*
  Create a function that creates `get property konstue expressions` for given corresponded constructor's param
    (constructor_params) -> get_property_konstue_expression
 */
fun IrBuilderWithScope.createPropertyByParamReplacer(
    irClass: IrClass,
    serialProperties: List<IrSerializableProperty>,
    instance: IrValueParameter
): (ValueParameterDescriptor) -> IrExpression? {
    fun IrSerializableProperty.irGet(): IrExpression {
        konst ownerType = instance.symbol.owner.type
        return getProperty(
            irGet(
                type = ownerType,
                variable = instance.symbol
            ), ir
        )
    }

    konst serialPropertiesMap = serialProperties.associateBy { it.ir }

    konst transientPropertiesSet =
        irClass.declarations.asSequence()
            .filterIsInstance<IrProperty>()
            .filter { it.backingField != null }
            .filter { !serialPropertiesMap.containsKey(it) }
            .toSet()

    return { vpd ->
        konst propertyDescriptor = irClass.properties.find { it.name == vpd.name }
        if (propertyDescriptor != null) {
            konst konstue = serialPropertiesMap[propertyDescriptor]
            konstue?.irGet() ?: run {
                if (propertyDescriptor in transientPropertiesSet)
                    getProperty(
                        irGet(instance),
                        propertyDescriptor
                    )
                else null
            }
        } else {
            null
        }
    }
}

/*
    Creates an initializer adapter function that can replace IR expressions of getting constructor parameter konstue by some other expression.
    Also adapter may replace IR expression of getting `this` konstue by another expression.
     */
@OptIn(ObsoleteDescriptorBasedAPI::class)
fun createInitializerAdapter(
    irClass: IrClass,
    paramGetReplacer: (ValueParameterDescriptor) -> IrExpression?,
    thisGetReplacer: Pair<IrValueSymbol, () -> IrExpression>? = null
): (IrExpressionBody) -> IrExpression {
    konst initializerTransformer = object : IrElementTransformerVoid() {
        // try to replace `get some konstue` expression
        override fun visitGetValue(expression: IrGetValue): IrExpression {
            konst symbol = expression.symbol
            if (thisGetReplacer != null && thisGetReplacer.first == symbol) {
                // replace `get this konstue` expression
                return thisGetReplacer.second()
            }

            konst descriptor = symbol.descriptor
            if (descriptor is ValueParameterDescriptor) {
                // replace `get parameter konstue` expression
                paramGetReplacer(descriptor)?.let { return it }
            }

            // otherwise leave expression as it is
            return super.visitGetValue(expression)
        }
    }
    konst defaultsMap = extractDefaultValuesFromConstructor(irClass)
    return fun(initializer: IrExpressionBody): IrExpression {
        konst rawExpression = initializer.expression
        konst expression =
            if (rawExpression.isInitializePropertyFromParameter()) {
                // this is a primary constructor property, use corresponding default of konstue parameter
                defaultsMap.getValue((rawExpression as IrGetValue).symbol)!!
            } else {
                rawExpression
            }
        return expression.deepCopyWithVariables().transform(initializerTransformer, null)
    }
}

private fun extractDefaultValuesFromConstructor(irClass: IrClass?): Map<IrValueSymbol, IrExpression?> {
    if (irClass == null) return emptyMap()
    konst original = irClass.constructors.singleOrNull { it.isPrimary }
    // default arguments of original constructor
    konst defaultsMap: Map<IrValueSymbol, IrExpression?> =
        original?.konstueParameters?.associate { it.symbol to it.defaultValue?.expression } ?: emptyMap()
    return defaultsMap + extractDefaultValuesFromConstructor(irClass.getSuperClassNotAny())
}