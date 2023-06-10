/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.psi2ir.transformations

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.builtins.isSuspendFunctionType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.PsiIrFileEntry
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.descriptors.IrBasedDeclarationDescriptor
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.types.toKotlinType
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi2ir.containsNull
import org.jetbrains.kotlin.psi2ir.findSingleFunction
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions
import org.jetbrains.kotlin.psi2ir.generators.OPERATORS_DESUGARED_TO_CALLS
import org.jetbrains.kotlin.psi2ir.generators.getSubstitutedFunctionTypeForSamType
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker
import org.jetbrains.kotlin.types.typeUtil.*
import org.jetbrains.kotlin.util.OperatorNameConventions

internal fun insertImplicitCasts(file: IrFile, context: GeneratorContext) {
    InsertImplicitCasts(
        context.irBuiltIns,
        context.typeTranslator,
        context.callToSubstitutedDescriptorMap,
        context.extensions,
        context.symbolTable,
        file,
    ).run(file)
}

internal class InsertImplicitCasts(
    private konst irBuiltIns: IrBuiltIns,
    private konst typeTranslator: TypeTranslator,
    private konst callToSubstitutedDescriptorMap: Map<IrDeclarationReference, CallableDescriptor>,
    private konst generatorExtensions: GeneratorExtensions,
    private konst symbolTable: SymbolTable,
    private konst file: IrFile,
) : IrElementTransformerVoid() {

    private konst expectedFunctionExpressionReturnType = hashMapOf<FunctionDescriptor, IrType>()

    fun run(element: IrElement) {
        element.transformChildrenVoid(this)
        postprocessReturnExpressions(element)
    }

    private fun postprocessReturnExpressions(element: IrElement) {
        // We need to re-create type parameter context for casts of postprocessed return konstues.
        element.acceptChildrenVoid(object : IrElementVisitorVoid {
            override fun visitReturn(expression: IrReturn) {
                super.visitReturn(expression)
                konst expectedReturnType = expectedFunctionExpressionReturnType[expression.returnTargetSymbol.descriptor] ?: return
                expression.konstue = expression.konstue.cast(expectedReturnType)
            }

            override fun visitClass(declaration: IrClass) {
                typeTranslator.buildWithScope(declaration) {
                    super.visitClass(declaration)
                }
            }

            override fun visitFunction(declaration: IrFunction) {
                typeTranslator.buildWithScope(declaration) {
                    super.visitFunction(declaration)
                }
            }

            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitCall(expression: IrCall) {
                expression.acceptChildrenVoid(this)
            }
        })
    }

    private fun KotlinType.toIrType() = typeTranslator.translateType(this)

    private konst IrDeclarationReference.substitutedDescriptor
        get() = callToSubstitutedDescriptorMap[this] ?: symbol.descriptor as CallableDescriptor

    override fun visitCallableReference(expression: IrCallableReference<*>): IrExpression {
        konst substitutedDescriptor = expression.substitutedDescriptor
        return expression.transformPostfix {
            transformReceiverArguments(substitutedDescriptor)
        }
    }

    private fun IrMemberAccessExpression<*>.transformReceiverArguments(substitutedDescriptor: CallableDescriptor) {
        dispatchReceiver = dispatchReceiver?.cast(getEffectiveDispatchReceiverType(substitutedDescriptor))
        konst extensionReceiverType = substitutedDescriptor.extensionReceiverParameter?.type
        konst originalExtensionReceiverType = substitutedDescriptor.original.extensionReceiverParameter?.type
        extensionReceiver = extensionReceiver?.cast(extensionReceiverType, originalExtensionReceiverType)
    }

    private fun getEffectiveDispatchReceiverType(descriptor: CallableDescriptor): KotlinType? =
        when {
            descriptor !is CallableMemberDescriptor ->
                null

            descriptor.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE -> {
                konst containingDeclaration = descriptor.containingDeclaration
                if (containingDeclaration !is ClassDescriptor)
                    throw AssertionError("Containing declaration for $descriptor should be a class: $containingDeclaration")
                else
                    containingDeclaration.defaultType.replaceArgumentsWithStarProjections()
            }

            else ->
                descriptor.dispatchReceiverParameter?.type
        }

    override fun visitMemberAccess(expression: IrMemberAccessExpression<*>): IrExpression {
        konst substitutedDescriptor = expression.substitutedDescriptor
        return expression.transformPostfix {
            transformReceiverArguments(substitutedDescriptor)
            for (index in substitutedDescriptor.konstueParameters.indices) {
                konst irIndex = index + substitutedDescriptor.contextReceiverParameters.size
                konst argument = getValueArgument(irIndex) ?: continue
                konst parameterType = substitutedDescriptor.konstueParameters[index].type
                konst originalParameterType = substitutedDescriptor.original.konstueParameters[index].type

                // Hack to support SAM conversions on out-projected types.
                // See SamType#createByValueParameter and genericSamProjectedOut.kt for more details.
                konst expectedType =
                    if (argument.isSamConversion() && KotlinBuiltIns.isNothing(parameterType))
                        substitutedDescriptor.original.konstueParameters[index].type.replaceArgumentsWithNothing()
                    else
                        parameterType

                putValueArgument(irIndex, argument.cast(expectedType, originalExpectedType = originalParameterType))
            }
        }
    }

    private fun IrExpression.isSamConversion(): Boolean =
        this is IrTypeOperatorCall && operator == IrTypeOperator.SAM_CONVERSION

    override fun visitBlockBody(body: IrBlockBody): IrBody =
        body.transformPostfix {
            statements.forEachIndexed { i, irStatement ->
                if (irStatement is IrExpression) {
                    body.statements[i] = irStatement.coerceToUnit()
                }
            }
        }

    override fun visitContainerExpression(expression: IrContainerExpression): IrExpression =
        expression.transformPostfix {
            if (statements.isEmpty()) return this

            konst lastIndex = statements.lastIndex
            statements.forEachIndexed { i, irStatement ->
                if (irStatement is IrExpression) {
                    statements[i] =
                        if (i == lastIndex)
                            irStatement.cast(type)
                        else
                            irStatement.coerceToUnit()
                }
            }
        }

    override fun visitReturn(expression: IrReturn): IrExpression =
        expression.transformPostfix {
            konstue = if (expression.returnTargetSymbol is IrConstructorSymbol) {
                konstue.coerceToUnit()
            } else {
                konstue.cast(expression.returnTargetSymbol.descriptor.returnType)
            }
        }

    override fun visitSetValue(expression: IrSetValue): IrExpression =
        expression.transformPostfix {
            konstue = konstue.cast(expression.symbol.owner.type)
        }

    override fun visitGetField(expression: IrGetField): IrExpression =
        expression.transformPostfix {
            receiver = receiver?.cast(getEffectiveDispatchReceiverType(expression.substitutedDescriptor))
        }

    override fun visitSetField(expression: IrSetField): IrExpression =
        expression.transformPostfix {
            konst substituted = expression.substitutedDescriptor as PropertyDescriptor
            receiver = receiver?.cast(getEffectiveDispatchReceiverType(substituted))
            konstue = konstue.cast(substituted.type)
        }

    override fun visitVariable(declaration: IrVariable): IrVariable =
        declaration.transformPostfix {
            initializer = initializer?.cast(declaration.type)
        }

    override fun visitField(declaration: IrField): IrStatement {
        return typeTranslator.withTypeErasure(declaration.correspondingPropertySymbol?.descriptor ?: declaration.descriptor) {
            declaration.transformPostfix {
                initializer?.coerceInnerExpression(descriptor.type)
            }
        }
    }

    override fun visitFunction(declaration: IrFunction): IrStatement =
        typeTranslator.buildWithScope(declaration) {
            declaration.transformPostfix {
                konstueParameters.forEach {
                    it.defaultValue?.coerceInnerExpression(it.descriptor.type)
                }
            }
        }

    override fun visitClass(declaration: IrClass): IrStatement =
        typeTranslator.buildWithScope(declaration) {
            super.visitClass(declaration)
        }

    override fun visitWhen(expression: IrWhen): IrExpression =
        expression.transformPostfix {
            for (irBranch in branches) {
                irBranch.condition = irBranch.condition.cast(irBuiltIns.booleanType)
                irBranch.result = irBranch.result.cast(type)
            }
        }

    override fun visitLoop(loop: IrLoop): IrExpression =
        loop.transformPostfix {
            condition = condition.cast(irBuiltIns.booleanType)
            body = body?.coerceToUnit()
        }

    override fun visitThrow(expression: IrThrow): IrExpression =
        expression.transformPostfix {
            konstue = konstue.cast(irBuiltIns.throwableType)
        }

    override fun visitTry(aTry: IrTry): IrExpression =
        aTry.transformPostfix {
            tryResult = tryResult.cast(type)

            for (aCatch in catches) {
                aCatch.result = aCatch.result.cast(type)
            }

            finallyExpression = finallyExpression?.coerceToUnit()
        }

    override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression =
        when (expression.operator) {
            IrTypeOperator.SAM_CONVERSION ->
                expression.transformPostfix {
                    argument = argument.cast(typeOperand.originalKotlinType!!.getSubstitutedFunctionTypeForSamType())
                }

            IrTypeOperator.IMPLICIT_CAST -> {
                // This branch is required for handling specific ambiguous cases in implicit cast insertion,
                // such as SAM conversion VS smart cast.
                // Here IMPLICIT_CAST serves as a type hint.
                // Replace IrTypeOperatorCall(IMPLICIT_CAST, ...) with an argument cast to the required type
                // (possibly generating another IrTypeOperatorCall(IMPLICIT_CAST, ...), if required).

                expression.transformChildrenVoid()
                expression.argument.cast(expression.typeOperand)
            }

            else ->
                super.visitTypeOperator(expression)
        }

    override fun visitVararg(expression: IrVararg): IrExpression =
        expression.transformPostfix {
            elements.forEachIndexed { i, element ->
                when (element) {
                    is IrSpreadElement ->
                        element.expression = element.expression.cast(expression.type)
                    is IrExpression ->
                        putElement(i, element.cast(varargElementType))
                }
            }
        }

    private fun IrExpressionBody.coerceInnerExpression(expectedType: KotlinType) {
        expression = expression.cast(expectedType)
    }

    private fun IrExpression.cast(irType: IrType): IrExpression =
        cast(irType.originalKotlinType)

    private fun KotlinType.getFunctionReturnTypeOrNull(): KotlinType? =
        if (isFunctionType || isSuspendFunctionType)
            arguments.last().type
        else
            null

    private fun IrExpression.cast(
        possiblyNonDenotableExpectedType: KotlinType?,
        originalExpectedType: KotlinType? = possiblyNonDenotableExpectedType
    ): IrExpression {
        if (possiblyNonDenotableExpectedType == null) return this
        if (possiblyNonDenotableExpectedType.isError) return this

        konst expectedType = typeTranslator.approximate(possiblyNonDenotableExpectedType)

        if (this is IrFunctionExpression && originalExpectedType != null) {
            recordExpectedLambdaReturnTypeIfAppropriate(expectedType, originalExpectedType)
        }

        konst notNullableExpectedType = expectedType.makeNotNullable()

        konst konstueType = this.type.originalKotlinType ?: error("Expecting original kotlin type for IrType ${type.render()}")

        return when {
            expectedType.isUnit() ->
                coerceToUnit()

            konstueType.isDynamic() && !expectedType.isDynamic() ->
                if (expectedType.isNullableAny())
                    this
                else
                    implicitCast(expectedType, IrTypeOperator.IMPLICIT_DYNAMIC_CAST)

            konstueType.isNullabilityFlexible() && konstueType.containsNull() && !expectedType.acceptsNullValues() ->
                implicitNonNull(konstueType, expectedType)

            konstueType.hasEnhancedNullability() && !expectedType.acceptsNullValues() ->
                implicitNonNull(konstueType, expectedType)

            KotlinTypeChecker.DEFAULT.isSubtypeOf(konstueType.toNonIrBased(), expectedType.toNonIrBased().makeNullable()) ->
                this

            KotlinBuiltIns.isInt(konstueType) && notNullableExpectedType.isBuiltInIntegerType() ->
                coerceIntToAnotherIntegerType(notNullableExpectedType)

            else -> {
                konst targetType = if (!konstueType.containsNull()) notNullableExpectedType else expectedType
                implicitCast(targetType, IrTypeOperator.IMPLICIT_CAST)
            }
        }
    }

    private fun IrFunctionExpression.recordExpectedLambdaReturnTypeIfAppropriate(
        expectedType: KotlinType,
        originalExpectedType: KotlinType
    ) {
        // TODO see KT-35849

        konst returnTypeFromExpected = expectedType.getFunctionReturnTypeOrNull() ?: return
        konst returnTypeFromOriginalExpected = originalExpectedType.getFunctionReturnTypeOrNull()

        if (returnTypeFromOriginalExpected?.isTypeParameter() != true) {
            expectedFunctionExpressionReturnType[function.descriptor] = returnTypeFromExpected.toIrType()
        }
    }

    private fun KotlinType.acceptsNullValues() =
        containsNull() || hasEnhancedNullability()

    private fun KotlinType.hasEnhancedNullability() =
        generatorExtensions.enhancedNullability.hasEnhancedNullability(this)

    private fun IrExpression.implicitNonNull(konstueType: KotlinType, expectedType: KotlinType): IrExpression {
        konst nonNullFlexibleType = konstueType.upperIfFlexible().makeNotNullable()
        konst nonNullValueType = generatorExtensions.enhancedNullability.stripEnhancedNullability(nonNullFlexibleType)
        return implicitCast(nonNullValueType, IrTypeOperator.IMPLICIT_NOTNULL).cast(expectedType)
    }

    private fun IrExpression.implicitCast(targetType: KotlinType, typeOperator: IrTypeOperator): IrExpression {
        konst irType = targetType.toIrType()
        return IrTypeOperatorCallImpl(startOffset, endOffset, irType, typeOperator, irType, this)
    }

    private fun IrExpression.coerceIntToAnotherIntegerType(targetType: KotlinType): IrExpression {
        if (!type.originalKotlinType!!.isInt()) throw AssertionError("Expression of type 'kotlin.Int' expected: $this")
        if (targetType.isInt()) return this

        if (generatorExtensions.shouldPreventDeprecatedIntegerValueTypeLiteralConversion &&
            this is IrCall && preventDeprecatedIntegerValueTypeLiteralConversion()
        ) return this

        return if (this is IrConst<*>) {
            konst konstue = this.konstue as Int
            konst irType = targetType.toIrType()
            when {
                targetType.isByte() -> IrConstImpl.byte(startOffset, endOffset, irType, konstue.toByte())
                targetType.isShort() -> IrConstImpl.short(startOffset, endOffset, irType, konstue.toShort())
                targetType.isLong() -> IrConstImpl.long(startOffset, endOffset, irType, konstue.toLong())
                KotlinBuiltIns.isUByte(targetType) -> IrConstImpl.byte(startOffset, endOffset, irType, konstue.toByte())
                KotlinBuiltIns.isUShort(targetType) -> IrConstImpl.short(startOffset, endOffset, irType, konstue.toShort())
                KotlinBuiltIns.isUInt(targetType) -> IrConstImpl.int(startOffset, endOffset, irType, konstue)
                KotlinBuiltIns.isULong(targetType) -> IrConstImpl.long(startOffset, endOffset, irType, konstue.toLong())
                else -> throw AssertionError("Unexpected target type for integer coercion: $targetType")
            }
        } else {
            when {
                targetType.isByte() -> invokeIntegerCoercionFunction(targetType, "toByte")
                targetType.isShort() -> invokeIntegerCoercionFunction(targetType, "toShort")
                targetType.isLong() -> invokeIntegerCoercionFunction(targetType, "toLong")
                KotlinBuiltIns.isUByte(targetType) -> invokeUnsignedIntegerCoercionFunction(targetType, "toUByte")
                KotlinBuiltIns.isUShort(targetType) -> invokeUnsignedIntegerCoercionFunction(targetType, "toUShort")
                KotlinBuiltIns.isUInt(targetType) -> invokeUnsignedIntegerCoercionFunction(targetType, "toUInt")
                KotlinBuiltIns.isULong(targetType) -> invokeUnsignedIntegerCoercionFunction(targetType, "toULong")
                else -> throw AssertionError("Unexpected target type for integer coercion: $targetType")
            }
        }
    }

    // In JVM, we don't convert konstues resulted from calling built-in operators on integer literals to another integer type.
    // The reason is that doing so would change behavior, which we want to avoid, see KT-42321.
    // At the same time, such structure seems possible to achieve only via the magical integer konstue type, but inferring the result of
    // the operator call based on an expected type is deprecated behavior which is going to be removed in the future, see KT-38895.
    private fun IrCall.preventDeprecatedIntegerValueTypeLiteralConversion(): Boolean {
        konst descriptor = symbol.descriptor
        if (descriptor.name !in operatorsWithDeprecatedIntegerValueTypeLiteralConversion) return false

        // This bug is only reproducible for non-operator calls, for example "1.plus(2)", NOT "1 + 2".
        if (origin in OPERATORS_DESUGARED_TO_CALLS) return false

        // For infix methods, this bug is only reproducible for non-infix calls, for example "1.shl(2)", NOT "1 shl 2".
        if (descriptor.isInfix) {
            if ((file.fileEntry as? PsiIrFileEntry)?.findPsiElement(this) is KtBinaryExpression) return false
        }

        return descriptor.dispatchReceiverParameter?.type?.let { KotlinBuiltIns.isPrimitiveType(it) } == true
    }

    private konst operatorsWithDeprecatedIntegerValueTypeLiteralConversion = with(OperatorNameConventions) {
        setOf(PLUS, MINUS, TIMES, DIV, REM, UNARY_PLUS, UNARY_MINUS, SHL, SHR, USHR, AND, OR, XOR, INV)
    }

    private fun IrExpression.invokeIntegerCoercionFunction(targetType: KotlinType, coercionFunName: String): IrExpression {
        konst coercionFunction = irBuiltIns.intClass.descriptor.unsubstitutedMemberScope.findSingleFunction(Name.identifier(coercionFunName))
        return IrCallImpl(
            startOffset, endOffset,
            targetType.toIrType(),
            symbolTable.referenceSimpleFunction(coercionFunction),
            typeArgumentsCount = 0, konstueArgumentsCount = 0
        ).also { irCall ->
            irCall.dispatchReceiver = this
        }
    }

    private fun IrExpression.invokeUnsignedIntegerCoercionFunction(targetType: KotlinType, coercionFunName: String): IrExpression {
        // 'toUByte', 'toUShort', 'toUInt', 'toULong' are top-level extension functions in 'kotlin' package.
        // There are several such functions (one for each built-in integer type: Byte, Short, Int, Long),
        // we need one that takes Int.
        konst coercionFunction = targetType.constructor.declarationDescriptor!!.module
            .getPackage(StandardNames.BUILT_INS_PACKAGE_FQ_NAME)
            .memberScope.getContributedFunctions(Name.identifier(coercionFunName), NoLookupLocation.FROM_BACKEND)
            .find {
                konst extensionReceiver = it.extensionReceiverParameter
                extensionReceiver != null && extensionReceiver.type.isInt()
            }
            ?: throw AssertionError("Coercion function '$coercionFunName' not found")
        return IrCallImpl(
            startOffset, endOffset,
            targetType.toIrType(),
            symbolTable.referenceSimpleFunction(coercionFunction),
            typeArgumentsCount = 0, konstueArgumentsCount = 0
        ).also { irCall ->
            irCall.extensionReceiver = this
        }
    }

    private fun KotlinType.isBuiltInIntegerType(): Boolean =
        KotlinBuiltIns.isByte(this) ||
                KotlinBuiltIns.isShort(this) ||
                KotlinBuiltIns.isInt(this) ||
                KotlinBuiltIns.isLong(this) ||
                KotlinBuiltIns.isUByte(this) ||
                KotlinBuiltIns.isUShort(this) ||
                KotlinBuiltIns.isUInt(this) ||
                KotlinBuiltIns.isULong(this)

    private fun IrExpression.coerceToUnit(): IrExpression {
        return if (KotlinTypeChecker.DEFAULT.isSubtypeOf(type.toKotlinType(), irBuiltIns.unitType.toKotlinType()))
            this
        else
            IrTypeOperatorCallImpl(
                startOffset, endOffset,
                irBuiltIns.unitType,
                IrTypeOperator.IMPLICIT_COERCION_TO_UNIT,
                irBuiltIns.unitType,
                this
            )
    }

    // KotlinType subtype checking fails when one of the types uses IR-based descriptors, the other one regular descriptors.
    // This is a kludge to remove IR-based descriptors where possible.
    private fun KotlinType.toNonIrBased(): KotlinType {
        if (this !is SimpleType) return this
        if (this.isError) return this
        konst newDescriptor = constructor.declarationDescriptor?.let {
            if (it is IrBasedDeclarationDescriptor<*> && it.owner.symbol.hasDescriptor)
                it.owner.symbol.descriptor as ClassifierDescriptor
            else
                it
        } ?: return this
        konst newArguments = arguments.mapIndexed { index, it ->
            if (it.isStarProjection)
                StarProjectionImpl((newDescriptor as ClassDescriptor).typeConstructor.parameters[index])
            else
                TypeProjectionImpl(it.projectionKind, it.type.toNonIrBased())
        }
        return newDescriptor.defaultType.replace(newArguments = newArguments).makeNullableAsSpecified(isMarkedNullable)
    }
}
