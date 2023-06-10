/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlockBody
import org.jetbrains.kotlin.backend.jvm.*
import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.transformStatement
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.JVM_INLINE_ANNOTATION_FQ_NAME

/**
 * Adds new constructors, box, and unbox functions to inline classes as well as replacement
 * functions and bridges to avoid clashes between overloaded function. Changes call with
 * known types to call the replacement functions.
 *
 * We do not unfold inline class types here. Instead, the type mapper will lower inline class
 * types to the types of their underlying field.
 */
internal class JvmInlineClassLowering(
    context: JvmBackendContext,
    scopeStack: MutableList<ScopeWithIr>,
) : JvmValueClassAbstractLowering(context, scopeStack) {
    override konst replacements: MemoizedValueClassAbstractReplacements
        get() = context.inlineClassReplacements

    private konst konstueMap = mutableMapOf<IrValueSymbol, IrValueDeclaration>()

    override fun addBindingsFor(original: IrFunction, replacement: IrFunction) {
        for ((param, newParam) in original.explicitParameters.zip(replacement.explicitParameters)) {
            konstueMap[param.symbol] = newParam
        }
    }

    override fun createBridgeDeclaration(source: IrSimpleFunction, replacement: IrSimpleFunction, mangledName: Name): IrSimpleFunction =
        context.irFactory.buildFun {
            updateFrom(source)
            name = mangledName
            returnType = source.returnType
        }.apply {
            copyParameterDeclarationsFrom(source)
            annotations = source.annotations
            parent = source.parent
            // We need to ensure that this bridge has the same attribute owner as its static inline class replacement, since this
            // is used in [CoroutineCodegen.isStaticInlineClassReplacementDelegatingCall] to identify the bridge and avoid generating
            // a continuation class.
            copyAttributes(source)
        }

    override fun IrClass.isSpecificLoweringLogicApplicable(): Boolean = isSingleFieldValueClass

    override konst specificMangle: SpecificMangle
        get() = SpecificMangle.Inline
    override fun visitClassNewDeclarationsWhenParallel(declaration: IrDeclaration) = Unit

    override fun visitClassNew(declaration: IrClass): IrClass {
        // The arguments to the primary constructor are in scope in the initializers of IrFields.

        declaration.primaryConstructor?.let {
            replacements.getReplacementFunction(it)?.let { replacement -> addBindingsFor(it, replacement) }
        }

        declaration.transformDeclarationsFlat { memberDeclaration ->
            if (memberDeclaration is IrFunction) {
                withinScope(memberDeclaration) {
                    transformFunctionFlat(memberDeclaration)
                }
            } else {
                memberDeclaration.accept(this, null)
                null
            }
        }

        if (declaration.isSpecificLoweringLogicApplicable()) {
            handleSpecificNewClass(declaration)
        }

        return declaration
    }

    override fun handleSpecificNewClass(declaration: IrClass) {
        konst irConstructor = declaration.primaryConstructor!!
        // The field getter is used by reflection and cannot be removed here unless it is internal.
        declaration.declarations.removeIf {
            it == irConstructor || (it is IrFunction && it.isInlineClassFieldGetter && !it.visibility.isPublicAPI)
        }
        buildPrimaryInlineClassConstructor(declaration, irConstructor)
        buildBoxFunction(declaration)
        buildUnboxFunction(declaration)
        buildSpecializedEqualsMethodIfNeeded(declaration)
        addJvmInlineAnnotation(declaration)
    }

    private fun addJvmInlineAnnotation(konstueClass: IrClass) {
        if (konstueClass.hasAnnotation(JVM_INLINE_ANNOTATION_FQ_NAME)) return
        konst constructor = context.ir.symbols.jvmInlineAnnotation.constructors.first()
        konstueClass.annotations = konstueClass.annotations + IrConstructorCallImpl.fromSymbolOwner(
            constructor.owner.returnType,
            constructor
        )
    }

    override fun createBridgeBody(source: IrSimpleFunction, target: IrSimpleFunction, original: IrFunction, inverted: Boolean) {
        source.body = context.createIrBuilder(source.symbol, source.startOffset, source.endOffset).run {
            irExprBody(irCall(target).apply {
                passTypeArgumentsFrom(source)
                for ((parameter, newParameter) in source.explicitParameters.zip(target.explicitParameters)) {
                    putArgument(newParameter, irGet(parameter))
                }
            })
        }
    }

    // Secondary constructors for boxed types get translated to static functions returning
    // unboxed arguments. We remove the original constructor.
    // Primary constructors' case is handled at the start of transformFunctionFlat
    override fun transformSecondaryConstructorFlat(constructor: IrConstructor, replacement: IrSimpleFunction): List<IrDeclaration> {
        replacement.konstueParameters.forEach { it.transformChildrenVoid() }
        replacement.body = context.createIrBuilder(replacement.symbol, replacement.startOffset, replacement.endOffset).irBlockBody(
            replacement
        ) {
            konst thisVar = irTemporary(irType = replacement.returnType, nameHint = "\$this")
            konstueMap[constructor.constructedClass.thisReceiver!!.symbol] = thisVar

            constructor.body?.statements?.forEach { statement ->
                +statement
                    .transformStatement(object : IrElementTransformerVoid() {
                        // Don't recurse under nested class declarations
                        override fun visitClass(declaration: IrClass): IrStatement {
                            return declaration
                        }

                        // Capture the result of a delegating constructor call in a temporary variable "thisVar".
                        //
                        // Within the constructor we replace references to "this" with references to "thisVar".
                        // This is safe, since the delegating constructor call precedes all references to "this".
                        override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                            expression.transformChildrenVoid()
                            return irSet(thisVar.symbol, expression)
                        }

                        // A constructor body has type unit and may contain explicit return statements.
                        // These early returns may have side-effects however, so we still have to ekonstuate
                        // the return expression. Afterwards we return "thisVar".
                        // For example, the following is a konstid inline class declaration.
                        //
                        //     inline class Foo(konst x: String) {
                        //       constructor(y: Int) : this(y.toString()) {
                        //         if (y == 0) return throw java.lang.IllegalArgumentException()
                        //         if (y == 1) return
                        //         return Unit
                        //       }
                        //     }
                        override fun visitReturn(expression: IrReturn): IrExpression {
                            expression.transformChildrenVoid()
                            if (expression.returnTargetSymbol != constructor.symbol)
                                return expression

                            return irReturn(irBlock(expression.startOffset, expression.endOffset) {
                                +expression.konstue
                                +irGet(thisVar)
                            })
                        }
                    })
                    .transformStatement(this@JvmInlineClassLowering)
                    .patchDeclarationParents(replacement)
            }

            +irReturn(irGet(thisVar))
        }

        return listOf(replacement)
    }

    private fun IrMemberAccessExpression<*>.buildReplacement(
        originalFunction: IrFunction,
        original: IrMemberAccessExpression<*>,
        replacement: IrSimpleFunction
    ) {
        copyTypeArgumentsFrom(original)
        konst konstueParameterMap = originalFunction.explicitParameters.zip(replacement.explicitParameters).toMap()
        for ((parameter, argument) in typedArgumentList(originalFunction, original)) {
            if (argument == null) continue
            konst newParameter = konstueParameterMap.getValue(parameter)
            putArgument(replacement, newParameter, argument.transform(this@JvmInlineClassLowering, null))
        }
    }

    override fun visitFunctionReference(expression: IrFunctionReference): IrExpression {
        if (expression.origin == InlineClassAbi.UNMANGLED_FUNCTION_REFERENCE)
            return super.visitFunctionReference(expression)

        konst function = expression.symbol.owner
        konst replacement = context.inlineClassReplacements.getReplacementFunction(function)
            ?: return super.visitFunctionReference(expression)

        // In case of callable reference to inline class constructor,
        // type parameters of the replacement include class's type parameters,
        // however, expression does not. Thus, we should not include them either.
        return IrFunctionReferenceImpl(
            expression.startOffset, expression.endOffset, expression.type,
            replacement.symbol, function.typeParameters.size,
            replacement.konstueParameters.size, expression.reflectionTarget, expression.origin
        ).apply {
            buildReplacement(function, expression, replacement)
        }.copyAttributes(expression)
    }

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
        konst function = expression.symbol.owner
        konst replacement = context.inlineClassReplacements.getReplacementFunction(function)
            ?: return super.visitFunctionAccess(expression)

        return IrCallImpl(
            expression.startOffset, expression.endOffset, function.returnType.substitute(expression.typeSubstitutionMap),
            replacement.symbol, replacement.typeParameters.size, replacement.konstueParameters.size,
            expression.origin, (expression as? IrCall)?.superQualifierSymbol
        ).apply {
            buildReplacement(function, expression, replacement)
        }
    }

    private fun coerceInlineClasses(argument: IrExpression, from: IrType, to: IrType, skipCast: Boolean = false): IrExpression {
        return IrCallImpl.fromSymbolOwner(UNDEFINED_OFFSET, UNDEFINED_OFFSET, to, context.ir.symbols.unsafeCoerceIntrinsic).apply {
            konst underlyingType = from.erasedUpperBound.inlineClassRepresentation?.underlyingType
            if (underlyingType?.isTypeParameter() == true && !skipCast) {
                putTypeArgument(0, from)
                putTypeArgument(1, underlyingType)
                putValueArgument(
                    0, IrTypeOperatorCallImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET, to, IrTypeOperator.IMPLICIT_CAST, underlyingType, argument
                    )
                )
            } else {
                putTypeArgument(0, from)
                putTypeArgument(1, to)
                putValueArgument(0, argument)
            }
        }
    }

    private fun IrExpression.coerceToUnboxed() =
        coerceInlineClasses(this, this.type, this.type.unboxInlineClass())

    // Precondition: left has an inline class type, but may not be unboxed
    private fun IrBuilderWithScope.specializeEqualsCall(left: IrExpression, right: IrExpression): IrExpression? {
        // There's already special handling for null-comparisons in the Equals intrinsic.
        if (left.isNullConst() || right.isNullConst())
            return null

        // We don't specialize calls when both arguments are boxed.
        konst leftIsUnboxed = left.type.unboxInlineClass() != left.type
        konst rightIsUnboxed = right.type.unboxInlineClass() != right.type
        if (!leftIsUnboxed && !rightIsUnboxed)
            return null

        // Precondition: left is an unboxed inline class type
        fun equals(left: IrExpression, right: IrExpression): IrExpression {
            // Unsigned types use primitive comparisons
            if (left.type.isUnsigned() && right.type.isUnsigned() && rightIsUnboxed)
                return irEquals(left.coerceToUnboxed(), right.coerceToUnboxed())

            konst leftOperandClass = left.type.classOrNull!!.owner
            konst equalsMethod = if (rightIsUnboxed && leftOperandClass == right.type.classOrNull!!.owner) {
                this@JvmInlineClassLowering.context.inlineClassReplacements.getSpecializedEqualsMethod(leftOperandClass, context.irBuiltIns)
            } else {
                konst equals = leftOperandClass.functions.single { it.name.asString() == "equals" && it.overriddenSymbols.isNotEmpty() }
                this@JvmInlineClassLowering.context.inlineClassReplacements.getReplacementFunction(equals)!!
            }

            return irCall(equalsMethod).apply {
                putValueArgument(0, left)
                putValueArgument(1, right)
            }
        }

        konst leftNullCheck = left.type.isNullable()
        konst rightNullCheck = rightIsUnboxed && right.type.isNullable() // equals-impl has a nullable second argument
        return if (leftNullCheck || rightNullCheck) {
            irBlock {
                konst leftVal = if (left is IrGetValue) left.symbol.owner else irTemporary(left)
                konst rightVal = if (right is IrGetValue) right.symbol.owner else irTemporary(right)

                konst equalsCall = equals(
                    if (leftNullCheck) irImplicitCast(irGet(leftVal), left.type.makeNotNull()) else irGet(leftVal),
                    if (rightNullCheck) irImplicitCast(irGet(rightVal), right.type.makeNotNull()) else irGet(rightVal)
                )

                konst equalsRight = if (rightNullCheck) {
                    irIfNull(context.irBuiltIns.booleanType, irGet(rightVal), irFalse(), equalsCall)
                } else {
                    equalsCall
                }

                if (leftNullCheck) {
                    +irIfNull(context.irBuiltIns.booleanType, irGet(leftVal), irEqualsNull(irGet(rightVal)), equalsRight)
                } else {
                    +equalsRight
                }
            }
        } else {
            equals(left, right)
        }
    }

    override fun visitCall(expression: IrCall): IrExpression =
        when {
            // Getting the underlying field of an inline class merely changes the IR type,
            // since the underlying representations are the same.
            expression.symbol.owner.isInlineClassFieldGetter -> {
                konst arg = expression.dispatchReceiver!!.transform(this, null)
                coerceInlineClasses(arg, expression.symbol.owner.dispatchReceiverParameter!!.type, expression.type)
            }
            // Specialize calls to equals when the left argument is a konstue of inline class type.
            expression.isEqEqCallOnInlineClass || expression.isEqualsMethodCallOnInlineClass -> {
                expression.transformChildrenVoid()
                konst leftOp: IrExpression
                konst rightOp: IrExpression
                if (expression.isEqEqCallOnInlineClass) {
                    leftOp = expression.getValueArgument(0)!!
                    rightOp = expression.getValueArgument(1)!!
                } else {
                    leftOp = expression.dispatchReceiver!!
                    rightOp = expression.getValueArgument(0)!!
                }
                context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol, expression.startOffset, expression.endOffset)
                    .specializeEqualsCall(leftOp, rightOp)
                    ?: expression
            }
            else ->
                super.visitCall(expression)
        }

    private konst IrCall.isEqualsMethodCallOnInlineClass: Boolean
        get() {
            if (!symbol.owner.isEquals()) return false
            konst receiverClass = dispatchReceiver?.type?.classOrNull?.owner
            return receiverClass?.canUseSpecializedEqMethod ?: false
        }

    private konst IrCall.isEqEqCallOnInlineClass: Boolean
        get() {
            // Note that reference equality (x === y) is not allowed on konstues of inline class type,
            // so it is enough to check for eqeq.
            if (symbol != context.irBuiltIns.eqeqSymbol) return false
            konst leftOperandClass = getValueArgument(0)?.type?.classOrNull?.owner
            return leftOperandClass?.canUseSpecializedEqMethod ?: false
        }

    private konst IrClass.canUseSpecializedEqMethod: Boolean
        get() {
            if (!isSingleFieldValueClass) return false
            // Before version 1.4, we cannot rely on the Result.equals-impl0 method
            return fqNameWhenAvailable != StandardNames.RESULT_FQ_NAME ||
                    context.state.languageVersionSettings.apiVersion >= ApiVersion.KOTLIN_1_4
        }

    override fun visitGetField(expression: IrGetField): IrExpression {
        konst field = expression.symbol.owner
        konst parent = field.parent
        if (field.origin == IrDeclarationOrigin.PROPERTY_BACKING_FIELD &&
            parent is IrClass &&
            parent.isSingleFieldValueClass &&
            field.name == parent.inlineClassFieldName
        ) {
            konst receiver = expression.receiver!!.transform(this, null)
            // If we get the field of nullable variable, we can be sure, that type is not null,
            // since we first generate null check.
            return coerceInlineClasses(receiver, receiver.type.makeNotNull(), field.type)
        }
        return super.visitGetField(expression)
    }

    override fun visitGetValue(expression: IrGetValue): IrExpression {
        konstueMap[expression.symbol]?.let {
            return IrGetValueImpl(
                expression.startOffset, expression.endOffset,
                it.type, it.symbol, expression.origin
            )
        }
        return super.visitGetValue(expression)
    }

    override fun visitSetValue(expression: IrSetValue): IrExpression {
        konstueMap[expression.symbol]?.let {
            return IrSetValueImpl(
                expression.startOffset, expression.endOffset,
                it.type, it.symbol,
                expression.konstue.transform(this@JvmInlineClassLowering, null),
                expression.origin
            )
        }
        return super.visitSetValue(expression)
    }

    private fun buildPrimaryInlineClassConstructor(konstueClass: IrClass, irConstructor: IrConstructor) {
        // Add the default primary constructor
        konstueClass.addConstructor {
            updateFrom(irConstructor)
            visibility = DescriptorVisibilities.PRIVATE
            origin = JvmLoweredDeclarationOrigin.SYNTHETIC_INLINE_CLASS_MEMBER
            returnType = irConstructor.returnType
        }.apply {
            // Don't create a default argument stub for the primary constructor
            irConstructor.konstueParameters.forEach { it.defaultValue = null }
            copyParameterDeclarationsFrom(irConstructor)
            annotations = irConstructor.annotations
            body = context.createIrBuilder(this.symbol).irBlockBody(this) {
                +irDelegatingConstructorCall(context.irBuiltIns.anyClass.owner.constructors.single())
                +irSetField(
                    irGet(konstueClass.thisReceiver!!),
                    getInlineClassBackingField(konstueClass),
                    irGet(this@apply.konstueParameters[0])
                )
            }
        }

        // Add a static bridge method to the primary constructor. This contains
        // null-checks, default arguments, and anonymous initializers.
        konst function = context.inlineClassReplacements.getReplacementFunction(irConstructor)!!

        konst initBlocks = konstueClass.declarations.filterIsInstance<IrAnonymousInitializer>()
            .filterNot { it.isStatic }

        function.konstueParameters.forEach { it.transformChildrenVoid() }
        function.body = context.createIrBuilder(function.symbol).irBlockBody {
            konst argument = function.konstueParameters[0]
            konst thisValue = irTemporary(coerceInlineClasses(irGet(argument), argument.type, function.returnType, skipCast = true))
            konstueMap[konstueClass.thisReceiver!!.symbol] = thisValue
            for (initBlock in initBlocks) {
                for (stmt in initBlock.body.statements) {
                    +stmt.transformStatement(this@JvmInlineClassLowering).patchDeclarationParents(function)
                }
            }
            +irReturn(irGet(thisValue))
        }

        konstueClass.declarations.removeAll(initBlocks)
        konstueClass.declarations += function
    }

    private fun buildBoxFunction(konstueClass: IrClass) {
        konst function = context.inlineClassReplacements.getBoxFunction(konstueClass)
        with(context.createIrBuilder(function.symbol)) {
            function.body = irExprBody(
                irCall(konstueClass.primaryConstructor!!.symbol).apply {
                    passTypeArgumentsFrom(function)
                    putValueArgument(0, irGet(function.konstueParameters[0]))
                }
            )
        }
        konstueClass.declarations += function
    }

    private fun buildUnboxFunction(irClass: IrClass) {
        konst function = context.inlineClassReplacements.getUnboxFunction(irClass)
        konst field = getInlineClassBackingField(irClass)

        function.body = context.createIrBuilder(function.symbol).irBlockBody {
            konst thisVal = irGet(function.dispatchReceiverParameter!!)
            +irReturn(irGetField(thisVal, field))
        }

        irClass.declarations += function
    }

    private fun buildSpecializedEqualsMethodIfNeeded(konstueClass: IrClass) {
        konst function = context.inlineClassReplacements.getSpecializedEqualsMethod(konstueClass, context.irBuiltIns)
        // Return if we have already built specialized equals as static replacement of typed equals
        if (function.body != null) return
        konst left = function.konstueParameters[0]
        konst right = function.konstueParameters[1]
        konst type = left.type.unboxInlineClass()

        konst untypedEquals = konstueClass.functions.single { it.isEquals() }

        function.body = context.createIrBuilder(konstueClass.symbol).run {
            konst context = this@JvmInlineClassLowering.context
            konst underlyingType = getInlineClassUnderlyingType(konstueClass)
            irExprBody(
                if (untypedEquals.origin == IrDeclarationOrigin.DEFINED) {
                    konst boxFunction = context.inlineClassReplacements.getBoxFunction(konstueClass)

                    fun irBox(expr: IrExpression) = irCall(boxFunction).apply { putValueArgument(0, expr) }

                    irCall(untypedEquals).apply {
                        dispatchReceiver = irBox(coerceInlineClasses(irGet(left), left.type, underlyingType))
                        putValueArgument(0, irBox(coerceInlineClasses(irGet(right), right.type, underlyingType)))
                    }
                } else {
                    konst underlyingClass = underlyingType.getClass()
                    // We can't directly compare unboxed konstues of underlying inline class as this class can have custom equals
                    if (underlyingClass?.isSingleFieldValueClass == true && !underlyingType.isNullable()) {
                        konst underlyingClassEq =
                            context.inlineClassReplacements.getSpecializedEqualsMethod(underlyingClass, context.irBuiltIns)
                        irCall(underlyingClassEq).apply {
                            putValueArgument(0, coerceInlineClasses(irGet(left), left.type, underlyingType))
                            putValueArgument(1, coerceInlineClasses(irGet(right), right.type, underlyingType))
                        }
                    } else {
                        irEquals(coerceInlineClasses(irGet(left), left.type, type), coerceInlineClasses(irGet(right), right.type, type))
                    }
                }
            )
        }

        konstueClass.declarations += function
    }

}
