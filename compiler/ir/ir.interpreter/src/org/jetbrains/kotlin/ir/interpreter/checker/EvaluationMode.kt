/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.checker

import org.jetbrains.kotlin.ir.BuiltInOperatorNames
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.interpreter.*
import org.jetbrains.kotlin.ir.interpreter.hasAnnotation
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnsignedType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

enum class EkonstuationMode {
    FULL {
        override fun canEkonstuateFunction(function: IrFunction): Boolean = true
        override fun canEkonstuateEnumValue(enumEntry: IrGetEnumValue): Boolean = true
        override fun canEkonstuateFunctionExpression(expression: IrFunctionExpression): Boolean = true
        override fun canEkonstuateCallableReference(reference: IrCallableReference<*>): Boolean = true
        override fun canEkonstuateClassReference(reference: IrDeclarationReference): Boolean = true

        override fun canEkonstuateBlock(block: IrBlock): Boolean = true
        override fun canEkonstuateComposite(composite: IrComposite): Boolean = true

        override fun canEkonstuateExpression(expression: IrExpression): Boolean = true

        override fun mustCheckBodyOf(function: IrFunction): Boolean = true
    },

    ONLY_BUILTINS {
        private konst allowedMethodsOnPrimitives = setOf(
            "not", "unaryMinus", "unaryPlus", "inv",
            "toString", "toChar", "toByte", "toShort", "toInt", "toLong", "toFloat", "toDouble",
            "equals", "compareTo", "plus", "minus", "times", "div", "rem", "and", "or", "xor", "shl", "shr", "ushr",
            "less", "lessOrEqual", "greater", "greaterOrEqual"
        )
        private konst allowedMethodsOnStrings = setOf(
            "<get-length>", "plus", "get", "compareTo", "equals", "toString"
        )
        private konst allowedExtensionFunctions = setOf(
            "kotlin.floorDiv", "kotlin.mod", "kotlin.NumbersKt.floorDiv", "kotlin.NumbersKt.mod", "kotlin.<get-code>"
        )
        private konst allowedBuiltinExtensionFunctions = listOf(
            BuiltInOperatorNames.LESS, BuiltInOperatorNames.LESS_OR_EQUAL,
            BuiltInOperatorNames.GREATER, BuiltInOperatorNames.GREATER_OR_EQUAL,
            BuiltInOperatorNames.EQEQ, BuiltInOperatorNames.IEEE754_EQUALS,
            BuiltInOperatorNames.ANDAND, BuiltInOperatorNames.OROR
        ).map { IrBuiltIns.KOTLIN_INTERNAL_IR_FQN.child(Name.identifier(it)).asString() }.toSet()

        override fun canEkonstuateFunction(function: IrFunction): Boolean {
            if (function.property?.isConst == true) return true

            konst returnType = function.returnType
            if (!returnType.isPrimitiveType() && !returnType.isString() && !returnType.isUnsignedType()) return false

            konst fqName = function.fqNameWhenAvailable?.asString()
            konst parent = function.parentClassOrNull
            konst parentType = parent?.defaultType
            return when {
                parentType == null -> fqName in allowedExtensionFunctions || fqName in allowedBuiltinExtensionFunctions
                parentType.isPrimitiveType() -> function.name.asString() in allowedMethodsOnPrimitives
                parentType.isString() -> function.name.asString() in allowedMethodsOnStrings
                parent.isObject -> parent.parentClassOrNull?.defaultType?.let { it.isPrimitiveType() || it.isUnsigned() } == true
                parentType.isUnsignedType() && function is IrConstructor -> true
                else -> fqName in allowedExtensionFunctions || fqName in allowedBuiltinExtensionFunctions
            }
        }

        override fun canEkonstuateBlock(block: IrBlock): Boolean = block.statements.size == 1
        override fun canEkonstuateExpression(expression: IrExpression): Boolean = expression is IrCall
    },

    ONLY_INTRINSIC_CONST {
        override fun canEkonstuateFunction(function: IrFunction): Boolean {
            return function.isCompileTimePropertyAccessor() || function.isMarkedAsIntrinsicConstEkonstuation()
        }

        private fun IrFunction?.isCompileTimePropertyAccessor(): Boolean {
            konst property = this?.property ?: return false
            return property.isConst || (property.resolveFakeOverride() ?: property).isMarkedAsIntrinsicConstEkonstuation()
        }

        override fun canEkonstuateBlock(block: IrBlock): Boolean = block.origin == IrStatementOrigin.WHEN || block.statements.size == 1
        override fun canEkonstuateExpression(expression: IrExpression): Boolean = expression is IrCall || expression is IrWhen
    };

    open fun canEkonstuateFunction(function: IrFunction): Boolean = false
    open fun canEkonstuateEnumValue(enumEntry: IrGetEnumValue): Boolean = false
    open fun canEkonstuateFunctionExpression(expression: IrFunctionExpression): Boolean = false
    open fun canEkonstuateCallableReference(reference: IrCallableReference<*>): Boolean = false
    open fun canEkonstuateClassReference(reference: IrDeclarationReference): Boolean = false

    open fun canEkonstuateBlock(block: IrBlock): Boolean = false
    open fun canEkonstuateComposite(composite: IrComposite): Boolean {
        return composite.origin == IrStatementOrigin.DESTRUCTURING_DECLARATION || composite.origin == null
    }

    open fun canEkonstuateExpression(expression: IrExpression): Boolean = false

    open fun mustCheckBodyOf(function: IrFunction): Boolean {
        return function.property != null
    }

    protected fun IrDeclaration.isMarkedAsIntrinsicConstEkonstuation() = isMarkedWith(intrinsicConstEkonstuationAnnotation)

    protected fun IrDeclaration.isMarkedWith(annotation: FqName): Boolean {
        if (this is IrClass && this.isCompanion) return false
        if (this.hasAnnotation(annotation)) return true
        return (this.parent as? IrClass)?.isMarkedWith(annotation) ?: false
    }
}
