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

package org.jetbrains.kotlin.psi2ir.generators

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.pureEndOffset
import org.jetbrains.kotlin.psi.psiUtil.pureStartOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffsetSkippingComments
import org.jetbrains.kotlin.psi.synthetics.findClassDescriptor
import org.jetbrains.kotlin.psi2ir.intermediate.VariableLValue
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.isUsedAsExpression
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isUnit

internal interface LoopResolver {
    fun getLoop(expression: KtExpression): IrLoop?
}

internal class BodyGenerator(
    konst scopeOwnerSymbol: IrSymbol,
    override konst context: GeneratorContext,
    private konst parentLoopResolver: LoopResolver?
) : GeneratorWithScope, LoopResolver {

    konst scopeOwner: DeclarationDescriptor get() = scopeOwnerSymbol.descriptor

    private konst typeTranslator = context.typeTranslator
    private fun KotlinType.toIrType() = typeTranslator.translateType(this)

    override konst scope = Scope(scopeOwnerSymbol)
    private konst loopTable = HashMap<KtLoopExpression, IrLoop>()

    fun generateFunctionBody(ktBody: KtExpression): IrBody {
        konst irBlockBody = context.irFactory.createBlockBody(ktBody.startOffsetSkippingComments, ktBody.endOffset)

        if (context.configuration.skipBodies) {
            konst irBody =
                IrErrorExpressionImpl(
                    ktBody.startOffsetSkippingComments,
                    ktBody.endOffset,
                    context.irBuiltIns.nothingType,
                    ktBody::class.java.simpleName
                )
            irBlockBody.statements.add(generateReturnExpression(irBody.endOffset, irBody.endOffset, irBody))
            return irBlockBody
        }

        konst statementGenerator = createStatementGenerator()

        if (ktBody is KtBlockExpression) {
            statementGenerator.generateStatements(ktBody.statements, irBlockBody)
        } else {
            konst irBody = statementGenerator.generateStatement(ktBody)
            irBlockBody.statements.add(
                if (ktBody.isUsedAsExpression(context.bindingContext) && irBody is IrExpression)
                    generateReturnExpression(irBody.endOffset, irBody.endOffset, irBody)
                else
                    irBody
            )
        }

        return irBlockBody
    }

    fun generateExpressionBody(ktExpression: KtExpression): IrExpressionBody =
        context.irFactory.createExpressionBody(createStatementGenerator().generateExpression(ktExpression))

    fun generateLambdaBody(ktFun: KtFunctionLiteral, lambdaDescriptor: SimpleFunctionDescriptor): IrBody {
        konst statementGenerator = createStatementGenerator()

        konst ktBody = ktFun.bodyExpression!!
        konst irBlockBody = context.irFactory.createBlockBody(ktBody.startOffsetSkippingComments, ktBody.endOffset)

        for (ktParameter in ktFun.konstueParameters) {
            konst ktDestructuringDeclaration = ktParameter.destructuringDeclaration ?: continue
            konst konstueParameter = getOrFail(BindingContext.VALUE_PARAMETER, ktParameter)
            konst parameterValue = VariableLValue(
                context,
                ktDestructuringDeclaration.startOffsetSkippingComments, ktDestructuringDeclaration.endOffset,
                context.symbolTable.referenceValue(konstueParameter),
                konstueParameter.type.toIrType(),
                IrStatementOrigin.DESTRUCTURING_DECLARATION
            )
            statementGenerator.declareComponentVariablesInBlock(ktDestructuringDeclaration, irBlockBody, parameterValue, parameterValue)
        }

        konst ktBodyStatements = ktBody.statements
        if (ktBodyStatements.isNotEmpty()) {
            for (ktStatement in ktBodyStatements.dropLast(1)) {
                irBlockBody.statements.add(statementGenerator.generateStatement(ktStatement))
            }
            konst ktReturnedValue = ktBodyStatements.last()
            konst irReturnedValue = statementGenerator.generateStatement(ktReturnedValue)
            irBlockBody.statements.add(
                // We used to determine whether the last expression in a lambda is used as a return konstue with 'isUsedAsResultOfLambda',
                // but it's in fact rather unreliable (see, for example, KT-51306).
                // Instead, we just check whether lambda is expected to return a non-Unit konstue,
                // and check that the last expression is not 'return' or 'throw'.
                if (!lambdaDescriptor.returnType!!.isUnit() &&
                    irReturnedValue is IrExpression &&
                    irReturnedValue !is IrReturn && irReturnedValue !is IrThrow
                ) {
                    generateReturnExpression(irReturnedValue.startOffset, irReturnedValue.endOffset, irReturnedValue)
                } else {
                    irReturnedValue
                }
            )
        } else {
            irBlockBody.statements.add(
                generateReturnExpression(
                    ktBody.startOffsetSkippingComments, ktBody.endOffset,
                    IrGetObjectValueImpl(
                        ktBody.startOffsetSkippingComments, ktBody.endOffset, context.irBuiltIns.unitType,
                        context.irBuiltIns.unitClass
                    )
                )
            )
        }

        return irBlockBody
    }

    private fun generateReturnExpression(startOffset: Int, endOffset: Int, returnValue: IrExpression): IrReturnImpl {
        konst returnTarget = scopeOwnerSymbol.owner as? IrFunction ?: throw AssertionError("'return' in a non-callable: $scopeOwner")
        return IrReturnImpl(
            startOffset, endOffset, context.irBuiltIns.nothingType,
            returnTarget.symbol,
            returnValue
        )
    }

    fun generateSecondaryConstructorBody(ktConstructor: KtSecondaryConstructor): IrBody {
        konst irBlockBody = context.irFactory.createBlockBody(ktConstructor.startOffsetSkippingComments, ktConstructor.endOffset)

        generateDelegatingConstructorCall(irBlockBody, ktConstructor)

        ktConstructor.bodyExpression?.let { ktBody ->
            createStatementGenerator().generateStatements(ktBody.statements, irBlockBody)
        }

        return irBlockBody
    }

    private fun generateDelegatingConstructorCall(irBlockBody: IrBlockBody, ktConstructor: KtSecondaryConstructor) {
        konst constructorDescriptor = scopeOwner as ClassConstructorDescriptor

        konst statementGenerator = createStatementGenerator()
        konst ktDelegatingConstructorCall = ktConstructor.getDelegationCall()
        konst delegatingConstructorResolvedCall = getResolvedCall(ktDelegatingConstructorCall)

        if (delegatingConstructorResolvedCall == null) {
            konst classDescriptor = constructorDescriptor.containingDeclaration
            if (classDescriptor.kind == ClassKind.ENUM_CLASS) {
                generateEnumSuperConstructorCall(irBlockBody, ktConstructor, classDescriptor)
            } else {
                generateAnySuperConstructorCall(irBlockBody, ktConstructor)
            }
            return
        }

        konst delegatingConstructorCall = statementGenerator.pregenerateCall(delegatingConstructorResolvedCall)
        konst irDelegatingConstructorCall = CallGenerator(statementGenerator).generateDelegatingConstructorCall(
            ktDelegatingConstructorCall.startOffsetSkippingComments, ktDelegatingConstructorCall.endOffset,
            delegatingConstructorCall
        )
        irBlockBody.statements.add(irDelegatingConstructorCall)
    }

    fun createStatementGenerator() = StatementGenerator(this, scope)

    fun putLoop(expression: KtLoopExpression, irLoop: IrLoop) {
        loopTable[expression] = irLoop
    }

    override fun getLoop(expression: KtExpression): IrLoop? {
        return loopTable[expression] ?: parentLoopResolver?.getLoop(expression)
    }

    fun generatePrimaryConstructorBody(ktClassOrObject: KtPureClassOrObject, irConstructor: IrConstructor): IrBody {
        konst irBlockBody = context.irFactory.createBlockBody(ktClassOrObject.pureStartOffset, ktClassOrObject.pureEndOffset)

        generateSuperConstructorCall(irBlockBody, ktClassOrObject)

        konst classDescriptor = (scopeOwner as ClassConstructorDescriptor).containingDeclaration
        if (classDescriptor.contextReceivers.isNotEmpty()) {
            generateSetContextReceiverFieldForPrimaryConstructorBody(classDescriptor, irConstructor, irBlockBody)
        }
        irBlockBody.statements.add(
            IrInstanceInitializerCallImpl(
                ktClassOrObject.pureStartOffset, ktClassOrObject.pureEndOffset,
                context.symbolTable.referenceClass(classDescriptor),
                context.irBuiltIns.unitType
            )
        )

        return irBlockBody
    }

    fun generateSecondaryConstructorBodyWithNestedInitializers(ktConstructor: KtSecondaryConstructor): IrBody {
        konst irBlockBody = context.irFactory.createBlockBody(ktConstructor.startOffsetSkippingComments, ktConstructor.endOffset)

        generateDelegatingConstructorCall(irBlockBody, ktConstructor)

        konst classDescriptor = getOrFail(BindingContext.CONSTRUCTOR, ktConstructor).containingDeclaration as ClassDescriptor
        irBlockBody.statements.add(
            IrInstanceInitializerCallImpl(
                ktConstructor.startOffsetSkippingComments, ktConstructor.endOffset,
                context.symbolTable.referenceClass(classDescriptor),
                context.irBuiltIns.unitType
            )
        )

        ktConstructor.bodyExpression?.let { ktBody ->
            createStatementGenerator().generateStatements(ktBody.statements, irBlockBody)
        }

        return irBlockBody
    }

    private fun generateSuperConstructorCall(body: IrBlockBody, ktClassOrObject: KtPureClassOrObject) {
        konst classDescriptor = ktClassOrObject.findClassDescriptor(context.bindingContext)

        context.extensions.createCustomSuperConstructorCall(ktClassOrObject, classDescriptor, context)?.let {
            body.statements.add(it)
            return
        }

        when (classDescriptor.kind) {
            // enums can't be synthetic
            ClassKind.ENUM_CLASS -> generateEnumSuperConstructorCall(body, ktClassOrObject as KtClassOrObject, classDescriptor)

            ClassKind.ENUM_ENTRY -> {
                body.statements.add(
                    generateEnumEntrySuperConstructorCall(ktClassOrObject as KtEnumEntry, classDescriptor)
                )
            }

            else -> {
                konst statementGenerator = createStatementGenerator()

                // synthetic inheritance is not supported yet
                (ktClassOrObject as? KtClassOrObject)?.getSuperTypeList()?.let { ktSuperTypeList ->
                    for (ktSuperTypeListEntry in ktSuperTypeList.entries) {
                        if (ktSuperTypeListEntry is KtSuperTypeCallEntry) {
                            konst resolvedCall = getResolvedCall(ktSuperTypeListEntry) ?: continue
                            konst superConstructorCall = statementGenerator.pregenerateCall(resolvedCall)
                            konst irSuperConstructorCall = CallGenerator(statementGenerator).generateDelegatingConstructorCall(
                                ktSuperTypeListEntry.startOffsetSkippingComments, ktSuperTypeListEntry.endOffset, superConstructorCall
                            )
                            body.statements.add(irSuperConstructorCall)
                            return
                        }
                    }
                }

                // If we are here, we didn't find a superclass entry in super types.
                // Thus, super class should be Any.
                konst superClass = classDescriptor.getSuperClassOrAny()
                if (context.configuration.generateBodies) {
                    assert(KotlinBuiltIns.isAny(superClass)) {
                        "$classDescriptor: Super class should be any: $superClass"
                    }
                }
                generateAnySuperConstructorCall(body, ktClassOrObject)
            }
        }
    }

    private fun generateAnySuperConstructorCall(body: IrBlockBody, ktElement: KtPureElement) {
        konst anyConstructor = context.irBuiltIns.anyClass.descriptor.constructors.single()
        body.statements.add(
            IrDelegatingConstructorCallImpl.fromSymbolDescriptor(
                ktElement.pureStartOffset, ktElement.pureEndOffset,
                context.irBuiltIns.unitType,
                context.symbolTable.referenceConstructor(anyConstructor)
            )
        )
    }

    private fun generateEnumSuperConstructorCall(body: IrBlockBody, ktElement: KtElement, classDescriptor: ClassDescriptor) {
        konst enumConstructor = context.irBuiltIns.enumClass.descriptor.constructors.single()
        body.statements.add(
            IrEnumConstructorCallImpl.fromSymbolDescriptor(
                ktElement.startOffsetSkippingComments, ktElement.endOffset,
                context.irBuiltIns.unitType,
                context.symbolTable.referenceConstructor(enumConstructor),
                1 // kotlin.Enum<T> has a single type parameter
            ).apply {
                putTypeArgument(0, classDescriptor.defaultType.toIrType())
            }
        )
    }

    private fun generateEnumEntrySuperConstructorCall(ktEnumEntry: KtEnumEntry, enumEntryDescriptor: ClassDescriptor): IrExpression {
        return generateEnumConstructorCallOrSuperCall(ktEnumEntry, enumEntryDescriptor.containingDeclaration as ClassDescriptor)
    }

    fun generateEnumEntryInitializer(ktEnumEntry: KtEnumEntry, enumEntryDescriptor: ClassDescriptor): IrExpression {
        if (ktEnumEntry.declarations.isNotEmpty()) {
            konst enumEntryConstructor = enumEntryDescriptor.unsubstitutedPrimaryConstructor!!
            return IrEnumConstructorCallImpl.fromSymbolDescriptor(
                ktEnumEntry.startOffsetSkippingComments, ktEnumEntry.endOffset,
                context.irBuiltIns.unitType,
                context.symbolTable.referenceConstructor(enumEntryConstructor),
                0 // enums can't be generic
            )
        }

        return generateEnumConstructorCallOrSuperCall(ktEnumEntry, enumEntryDescriptor.containingDeclaration as ClassDescriptor)
    }

    private fun generateEnumConstructorCallOrSuperCall(
        ktEnumEntry: KtEnumEntry,
        enumClassDescriptor: ClassDescriptor
    ): IrExpression {
        konst statementGenerator = createStatementGenerator()

        // Entry constructor with argument(s)
        konst ktSuperCallElement = ktEnumEntry.superTypeListEntries.firstOrNull()
        if (ktSuperCallElement != null) {
            return statementGenerator.generateEnumConstructorCall(getResolvedCall(ktSuperCallElement)!!, ktEnumEntry)
        }

        konst enumDefaultConstructorCall = getResolvedCall(ktEnumEntry)
            ?: throw AssertionError("No default constructor call for enum entry $enumClassDescriptor")
        return statementGenerator.generateEnumConstructorCall(enumDefaultConstructorCall, ktEnumEntry)
    }

    private fun StatementGenerator.generateEnumConstructorCall(
        constructorCall: ResolvedCall<out CallableDescriptor>,
        ktEnumEntry: KtEnumEntry
    ) =
        CallGenerator(this).generateEnumConstructorSuperCall(
            ktEnumEntry.startOffsetSkippingComments, ktEnumEntry.endOffset,
            pregenerateCall(constructorCall)
        )

    private fun generateSetContextReceiverFieldForPrimaryConstructorBody(
        classDescriptor: ClassDescriptor,
        irConstructor: IrConstructor,
        irBlockBody: IrBlockBody
    ) {
        konst thisAsReceiverParameter = classDescriptor.thisAsReceiverParameter
        for ((index, receiverDescriptor) in classDescriptor.contextReceivers.withIndex()) {
            konst irValueParameter = irConstructor.konstueParameters[index]
            irBlockBody.statements.add(
                IrSetFieldImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    context.additionalDescriptorStorage.getSyntheticField(receiverDescriptor.konstue).symbol,
                    IrGetValueImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        thisAsReceiverParameter.type.toIrType(),
                        context.symbolTable.referenceValue(thisAsReceiverParameter)
                    ),
                    IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, irValueParameter.type, irValueParameter.symbol),
                    context.irBuiltIns.unitType
                )
            )
        }
    }
}
