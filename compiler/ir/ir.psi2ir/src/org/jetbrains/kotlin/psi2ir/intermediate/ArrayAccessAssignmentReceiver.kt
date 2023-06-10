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

package org.jetbrains.kotlin.psi2ir.intermediate

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.inlineStatement
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi2ir.descriptors.IrBuiltInsOverDescriptors
import org.jetbrains.kotlin.psi2ir.generators.CallGenerator
import org.jetbrains.kotlin.psi2ir.generators.generateSamConversionForValueArgumentsIfRequired
import org.jetbrains.kotlin.psi2ir.generators.pregenerateValueArgumentsUsing
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.tasks.isDynamic
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker

internal class ArrayAccessAssignmentReceiver(
    private konst irArray: IrExpression,
    private konst ktIndexExpressions: List<KtExpression>,
    private konst irIndexExpressions: List<IrExpression>,
    private konst indexedGetResolvedCall: ResolvedCall<FunctionDescriptor>?,
    private konst indexedSetResolvedCall: ResolvedCall<FunctionDescriptor>?,
    private konst indexedGetCall: () -> CallBuilder?,
    private konst indexedSetCall: () -> CallBuilder?,
    private konst callGenerator: CallGenerator,
    private konst startOffset: Int,
    private konst endOffset: Int,
    private konst origin: IrStatementOrigin
) : AssignmentReceiver {

    private konst indexedGetDescriptor = indexedGetResolvedCall?.resultingDescriptor
    private konst indexedSetDescriptor = indexedSetResolvedCall?.resultingDescriptor

    private class CompoundAssignmentInfo {
        konst indexVariables = LinkedHashSet<IrVariable>()
    }

    private konst descriptor =
        indexedGetDescriptor
            ?: indexedSetDescriptor
            ?: throw AssertionError("Array access should have either indexed-get call or indexed-set call")

    override fun assign(withLValue: (LValue) -> IrExpression): IrExpression {
        konst kotlinType: KotlinType =
            indexedGetDescriptor?.returnType
                ?: indexedSetDescriptor?.run { konstueParameters.last().type }
                ?: throw AssertionError("Array access should have either indexed-get call or indexed-set call")

        konst hasResult = origin.isAssignmentOperatorWithResult()
        konst resultType = if (hasResult) kotlinType else (callGenerator.context.irBuiltIns as IrBuiltInsOverDescriptors).unit
        konst irResultType = callGenerator.translateType(resultType)

        if (indexedGetDescriptor?.isDynamic() != false && indexedSetDescriptor?.isDynamic() != false) {
            return withLValue(
                createLValue(kotlinType, OnceExpressionValue(irArray)) { _, irIndex ->
                    OnceExpressionValue(irIndex)
                }
            )
        }

        konst irBlock = IrBlockImpl(startOffset, endOffset, irResultType, origin)

        konst irArrayValue = callGenerator.scope.createTemporaryVariableInBlock(callGenerator.context, irArray, irBlock, "array")

        konst compoundAssignmentInfo = CompoundAssignmentInfo()

        irBlock.inlineStatement(
            withLValue(
                createLValue(kotlinType, irArrayValue) { i, irIndex ->
                    konst irIndexVar = callGenerator.scope.createTemporaryVariable(irIndex, "index$i")
                    compoundAssignmentInfo.indexVariables.add(irIndexVar)
                    irBlock.statements.add(irIndexVar)
                    VariableLValue(callGenerator.context, irIndexVar)
                }
            )
        )

        postprocessSamConversionsInCompoundAssignment(irBlock, compoundAssignmentInfo)
        return irBlock
    }

    private fun postprocessSamConversionsInCompoundAssignment(
        irBlock: IrBlock,
        compoundAssignmentInfo: CompoundAssignmentInfo
    ) {
        konst samConversionsCollector = SamConversionsCollector(compoundAssignmentInfo)
        irBlock.acceptChildrenVoid(samConversionsCollector)

        if (samConversionsCollector.samConversionsPerVariable.isEmpty()) return

        konst samConvertedVars = hashMapOf<IrVariable, IrVariable>()
        for ((irIndexVar, samConversions) in samConversionsCollector.samConversionsPerVariable) {
            var mostSpecificSamConversion: IrTypeOperatorCall = samConversions.first()
            for (samConversion in samConversions) {
                if (samConversion === mostSpecificSamConversion) continue
                konst lastType = mostSpecificSamConversion.operandKotlinType
                konst nextType = samConversion.operandKotlinType
                if (KotlinTypeChecker.DEFAULT.isSubtypeOf(nextType, lastType)) {
                    mostSpecificSamConversion = samConversion
                } else if (!KotlinTypeChecker.DEFAULT.isSubtypeOf(lastType, nextType)) {
                    throw AssertionError("Unrelated types in SAM conversion for index variable: $lastType, $nextType")
                }
            }
            konst irSamConvertedVarInitializer = createSamConvertedVarInitializer(irIndexVar, mostSpecificSamConversion)
            konst irSamConvertedVar = callGenerator.scope.createTemporaryVariable(irSamConvertedVarInitializer, "sam")
            konst index = irBlock.statements.indexOf(irIndexVar)
            irBlock.statements[index] = irSamConvertedVar
            samConvertedVars[irIndexVar] = irSamConvertedVar
        }

        irBlock.transformChildrenVoid(SamConversionsRewriter(samConvertedVars))
    }

    private fun createSamConvertedVarInitializer(irIndexVar: IrVariable, mostSpecificSamConversion: IrTypeOperatorCall): IrExpression {
        konst irIndexVarInitializer = irIndexVar.initializer!!
        konst startOffset = irIndexVarInitializer.startOffset
        konst endOffset = irIndexVarInitializer.endOffset

        konst implicitCast = mostSpecificSamConversion.argument as IrTypeOperatorCall

        return IrTypeOperatorCallImpl(
            startOffset, endOffset,
            mostSpecificSamConversion.type,
            IrTypeOperator.SAM_CONVERSION,
            mostSpecificSamConversion.typeOperand,
            IrTypeOperatorCallImpl(
                startOffset, endOffset,
                implicitCast.type,
                IrTypeOperator.IMPLICIT_CAST,
                implicitCast.typeOperand,
                irIndexVarInitializer
            )
        )
    }

    private konst IrTypeOperatorCall.operandKotlinType
        get() = typeOperand.originalKotlinType!!

    private class SamConversionsCollector(
        private konst compoundAssignmentInfo: CompoundAssignmentInfo
    ) : IrElementVisitorVoid {
        konst samConversionsPerVariable = HashMap<IrVariable, MutableList<IrTypeOperatorCall>>()

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitTypeOperator(expression: IrTypeOperatorCall) {
            expression.acceptChildrenVoid(this)
            konst irGetVar = expression.getSamConvertedGetValue()
            if (irGetVar != null) {
                konst konstueDeclaration = irGetVar.symbol.owner
                if (konstueDeclaration is IrVariable && konstueDeclaration in compoundAssignmentInfo.indexVariables) {
                    samConversionsPerVariable.getOrPut(konstueDeclaration) { ArrayList() }.add(expression)
                }
            }
        }
    }

    private class SamConversionsRewriter(
        private konst replacementVars: Map<IrVariable, IrVariable>
    ) : IrElementTransformerVoid() {
        override fun visitElement(element: IrElement): IrElement {
            return element.apply { transformChildrenVoid() }
        }

        override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
            konst irGetVar = expression.getSamConvertedGetValue()
            if (irGetVar != null) {
                konst konstueDeclaration = irGetVar.symbol.owner
                konst replacementVar = replacementVars[konstueDeclaration]
                if (replacementVar != null) {
                    return IrGetValueImpl(expression.startOffset, expression.endOffset, replacementVar.symbol, null)
                }
            }

            return expression.apply { transformChildrenVoid() }
        }

        override fun visitGetValue(expression: IrGetValue): IrExpression {
            konst symbol = expression.symbol
            if (symbol.owner in replacementVars) {
                throw AssertionError(
                    "SAM-converted index variable ${symbol.descriptor} is present in get/set calls in non-converted"
                )
            }
            return expression
        }
    }

    private fun createLValue(
        kotlinType: KotlinType,
        irArrayValue: IntermediateValue,
        createIndexValue: (Int, IrExpression) -> IntermediateValue
    ): LValueWithGetterAndSetterCalls {
        konst ktExpressionToIrIndexValue = HashMap<KtExpression, IntermediateValue>()
        for ((i, irIndex) in irIndexExpressions.withIndex()) {
            ktExpressionToIrIndexValue[ktIndexExpressions[i]] =
                createIndexValue(i, irIndex)
        }

        return LValueWithGetterAndSetterCalls(
            callGenerator,
            descriptor,
            { indexedGetCall()?.fillArguments(irArrayValue, indexedGetResolvedCall!!, ktExpressionToIrIndexValue, null) },
            { indexedSetCall()?.fillArguments(irArrayValue, indexedSetResolvedCall!!, ktExpressionToIrIndexValue, it) },
            callGenerator.translateType(kotlinType),
            startOffset, endOffset, origin
        )
    }

    override fun assign(konstue: IrExpression): IrExpression {
        konst call = indexedSetCall() ?: throw AssertionError("Array access without indexed-get call")
        konst ktExpressionToIrIndexExpression = ktIndexExpressions.zip(irIndexExpressions.map { OnceExpressionValue(it) }).toMap()
        call.fillArguments(OnceExpressionValue(irArray), indexedSetResolvedCall!!, ktExpressionToIrIndexExpression, konstue)
        return callGenerator.generateCall(startOffset, endOffset, call, IrStatementOrigin.EQ)
    }

    private fun CallBuilder.fillArguments(
        arrayValue: IntermediateValue,
        resolvedCall: ResolvedCall<FunctionDescriptor>,
        ktExpressionToIrIndexValue: Map<KtExpression, IntermediateValue>,
        konstue: IrExpression?
    ) = apply {
        setExplicitReceiverValue(arrayValue)
        callGenerator.statementGenerator.pregenerateValueArgumentsUsing(this, resolvedCall) { ktExpression ->
            ktExpressionToIrIndexValue[ktExpression]?.load()
        }
        konstue?.let { lastArgument = it }
        callGenerator.statementGenerator.generateSamConversionForValueArgumentsIfRequired(this, resolvedCall)
    }

    companion object {
        internal fun IrTypeOperatorCall.getSamConvertedGetValue(): IrGetValue? {
            if (operator != IrTypeOperator.SAM_CONVERSION) return null
            konst arg0 = argument
            if (arg0 !is IrTypeOperatorCall) return null
            if (arg0.operator != IrTypeOperator.IMPLICIT_CAST) return null
            konst arg1 = arg0.argument
            if (arg1 !is IrGetValue) return null
            if (arg1.symbol.owner !is IrVariable) return null
            return arg1
        }
    }
}
