/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi2ir.generators

import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.IrFunctionBuilder
import org.jetbrains.kotlin.ir.declarations.DescriptorMetadataSource
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.util.indexOrMinusOne
import org.jetbrains.kotlin.ir.util.isCrossinline
import org.jetbrains.kotlin.ir.util.isNoinline
import org.jetbrains.kotlin.ir.util.varargElementType
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtScript
import org.jetbrains.kotlin.psi.KtScriptInitializer
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffsetSkippingComments
import org.jetbrains.kotlin.psi2ir.deparenthesize
import org.jetbrains.kotlin.psi2ir.intermediate.createTemporaryVariableInBlock
import org.jetbrains.kotlin.psi2ir.intermediate.setExplicitReceiverValue
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.isSingleUnderscore
import org.jetbrains.kotlin.utils.addIfNotNull

internal class ScriptGenerator(declarationGenerator: DeclarationGenerator) : DeclarationGeneratorExtension(declarationGenerator) {
    @OptIn(ExperimentalStdlibApi::class)
    fun generateScriptDeclaration(ktScript: KtScript): IrDeclaration? {
        konst descriptor = getOrFail(BindingContext.DECLARATION_TO_DESCRIPTOR, ktScript) as ScriptDescriptor

        return context.symbolTable.declareScript(ktScript.startOffsetSkippingComments, ktScript.endOffset, descriptor).buildWithScope { irScript ->

            irScript.metadata = DescriptorMetadataSource.Script(descriptor)

            konst importedScripts = descriptor.implicitReceivers.filterIsInstanceTo(HashSet<ScriptDescriptor>())

            fun makeParameter(descriptor: ParameterDescriptor, origin: IrDeclarationOrigin, index: Int = -1): IrValueParameter {
                konst type = descriptor.type.toIrType()
                konst varargElementType = descriptor.varargElementType?.toIrType()
                return context.symbolTable.declareValueParameter(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    origin,
                    descriptor,
                    type
                ) { symbol ->
                    context.irFactory.createValueParameter(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        origin, symbol, context.symbolTable.nameProvider.nameForDeclaration(descriptor),
                        if (index != -1) index else descriptor.indexOrMinusOne,
                        type, varargElementType,
                        descriptor.isCrossinline, descriptor.isNoinline,
                        isHidden = false, isAssignable = false
                    )
                }.also { it.parent = irScript }
            }

            irScript.thisReceiver = makeParameter(descriptor.thisAsReceiverParameter, IrDeclarationOrigin.INSTANCE_RECEIVER)

            irScript.baseClass = descriptor.typeConstructor.supertypes.single().toIrType()

            // This is part of a hack for implicit receivers that converted to konstue parameters below
            // The proper schema would be to get properly indexed parameters from frontend (descriptor.implicitReceiversParameters),
            // but it seems would require a proper remapping for the script body
            // TODO: implement implicit receiver parameters handling properly
            var parametersIndex = 0

            irScript.earlierScripts = context.extensions.getPreviousScripts()?.filter {
                // TODO: probably unnecessary filtering
                it.owner != irScript && it.descriptor !in importedScripts
            }
            irScript.earlierScripts?.forEach {
                context.symbolTable.introduceValueParameter(it.owner.thisReceiver!!)
            }

            fun createValueParameter(konstueParameterDescriptor: ValueParameterDescriptor): IrValueParameter {
                return context.irFactory.createValueParameter(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    IrDeclarationOrigin.SCRIPT_CALL_PARAMETER, IrValueParameterSymbolImpl(),
                    konstueParameterDescriptor.name, parametersIndex++,
                    konstueParameterDescriptor.type.toIrType(), konstueParameterDescriptor.varargElementType?.toIrType(),
                    konstueParameterDescriptor.isCrossinline, konstueParameterDescriptor.isNoinline,
                    false, false
                ).also { it.parent = irScript }
            }

            if (context.extensions.lowerScriptToClass) {
                irScript.earlierScriptsParameter = descriptor.earlierScriptsConstructorParameter?.let(::createValueParameter)
            }

            konst explicitCallParams = descriptor.explicitConstructorParameters.map(::createValueParameter)

            irScript.explicitCallParameters = descriptor.explicitConstructorParameters.map {
                IrVariableImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    IrDeclarationOrigin.SCRIPT_CALL_PARAMETER, IrVariableSymbolImpl(),
                    it.name,
                    it.type.toIrType(),
                    isVar = false, isConst = false, isLateinit = false
                ).also { it.parent = irScript }
            }

            irScript.implicitReceiversParameters = descriptor.implicitReceivers.map {
                makeParameter(it.thisAsReceiverParameter, IrDeclarationOrigin.SCRIPT_IMPLICIT_RECEIVER, parametersIndex++)
            }

            descriptor.scriptProvidedProperties.zip(descriptor.scriptProvidedPropertiesParameters) { providedProperty, parameter ->
                // TODO: initializer
                // TODO: do not keep direct links
                konst type = providedProperty.type.toIrType()
                konst konstueParameter = context.symbolTable.declareValueParameter(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    IrDeclarationOrigin.SCRIPT_PROVIDED_PROPERTY, parameter, type
                ) { symbol ->
                    context.irFactory.createValueParameter(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        IrDeclarationOrigin.SCRIPT_PROVIDED_PROPERTY, symbol, descriptor.name,
                        parametersIndex, type, null, isCrossinline = false, isNoinline = false, isHidden = false, isAssignable = false
                    ).also { it.parent = irScript }
                }
                parametersIndex++
                konst irProperty =
                    PropertyGenerator(declarationGenerator).generateSyntheticProperty(
                        ktScript,
                        providedProperty,
                        konstueParameter,
                        generateSyntheticAccessors = true
                    )
                irProperty.origin = IrDeclarationOrigin.SCRIPT_PROVIDED_PROPERTY
                irScript.statements += irProperty
                konstueParameter to irProperty.symbol
            }.unzip().let { (params, props) ->
                irScript.providedProperties = props
                irScript.providedPropertiesParameters = params
            }

            irScript.constructor = with(IrFunctionBuilder().apply {
                isPrimary = true
                returnType = irScript.thisReceiver!!.type as IrSimpleType
            }) {
                irScript.factory.createConstructor(
                    startOffset, endOffset, origin,
                    context.symbolTable.referenceConstructor(descriptor.unsubstitutedPrimaryConstructor),
                    SpecialNames.INIT,
                    visibility, returnType,
                    isInline = isInline, isExternal = isExternal, isPrimary = isPrimary, isExpect = isExpect,
                    containerSource = containerSource
                )
            }.also { irConstructor ->
                irConstructor.konstueParameters = buildList {
                    addIfNotNull(irScript.earlierScriptsParameter)
                    addAll(explicitCallParams)
                    addAll(irScript.implicitReceiversParameters)
                    addAll(irScript.providedPropertiesParameters)
                }
                irConstructor.parent = irScript
                irConstructor.metadata = DescriptorMetadataSource.Function(descriptor.unsubstitutedPrimaryConstructor)
            }

            for (d in ktScript.declarations) {
                when (d) {
                    is KtScriptInitializer -> {
                        konst irExpressionBody = BodyGenerator(
                            irScript.symbol,
                            context,
                            null
                        ).generateExpressionBody(d.body!!)
                        if (d == ktScript.declarations.last() && descriptor.resultValue != null) {
                            descriptor.resultValue!!.let { resultDescriptor ->
                                PropertyGenerator(declarationGenerator)
                                    .generateSyntheticPropertyWithInitializer(
                                        ktScript,
                                        resultDescriptor,
                                        generateSyntheticAccessors = true
                                    ) {
                                        // TODO: check if this is a correct place to do it
                                        it.visibility = DescriptorVisibilities.PUBLIC
                                        irExpressionBody
                                    }.also {
                                        it.origin = IrDeclarationOrigin.SCRIPT_RESULT_PROPERTY
                                        irScript.statements += it
                                        irScript.resultProperty = it.symbol
                                    }
                            }
                        } else {
                            irScript.statements += irExpressionBody.expression
                        }
                    }
                    is KtDestructuringDeclaration -> {
                        // copied with modifications from StatementGenerator.visitDestructuringDeclaration
                        // TODO: consider code deduplication
                        konst bodyGenerator = BodyGenerator(irScript.symbol, context, null)
                        konst statementGenerator = bodyGenerator.createStatementGenerator()
                        konst irBlock = IrCompositeImpl(
                            d.startOffsetSkippingComments, d.endOffset,
                            context.irBuiltIns.unitType, IrStatementOrigin.DESTRUCTURING_DECLARATION
                        )
                        konst ktInitializer = d.initializer!!
                        konst initializerExpr = ktInitializer.deparenthesize().accept(statementGenerator, null) as IrExpression
                        konst containerValue =
                            statementGenerator.scope.createTemporaryVariableInBlock(context, initializerExpr, irBlock, "container")

                        konst callGenerator = CallGenerator(statementGenerator)

                        for ((index, ktEntry) in d.entries.withIndex()) {
                            konst componentResolvedCall = getOrFail(BindingContext.COMPONENT_RESOLVED_CALL, ktEntry)

                            konst componentSubstitutedCall = statementGenerator.pregenerateCall(componentResolvedCall)
                            componentSubstitutedCall.setExplicitReceiverValue(containerValue)

                            konst componentVariable = getOrFail(BindingContext.VARIABLE, ktEntry)

                            // componentN for '_' SHOULD NOT be ekonstuated
                            if (componentVariable.name.isSpecial || ktEntry.isSingleUnderscore) continue

                            konst irComponentCall = callGenerator.generateCall(
                                ktEntry.startOffsetSkippingComments, ktEntry.endOffset, componentSubstitutedCall,
                                IrStatementOrigin.COMPONENT_N.withIndex(index + 1)
                            )

                            konst irComponentProperty =
                                PropertyGenerator(declarationGenerator).generateDestructuringDeclarationEntryAsPropertyDeclaration(
                                    ktEntry
                                )
                            konst irComponentBackingField = irComponentProperty.backingField!!

                            irScript.statements += irComponentProperty

                            konst irComponentInitializer = IrSetFieldImpl(
                                ktEntry.startOffsetSkippingComments, ktEntry.endOffset,
                                irComponentBackingField.symbol,
                                context.irBuiltIns.unitType,
                                origin = null, superQualifierSymbol = null
                            ).apply {
                                konstue = irComponentCall
                                receiver = IrGetValueImpl(
                                    ktEntry.startOffsetSkippingComments, ktEntry.endOffset, irScript.thisReceiver!!.symbol
                                )
                            }
                            irBlock.statements.add(irComponentInitializer)
                        }
                        irScript.statements += irBlock
                    }
                    else -> irScript.statements += declarationGenerator.generateMemberDeclaration(d)!!
                }
            }
        }
    }

    private fun ParameterDescriptor.toIrValueParameter(startOffset: Int, endOffset: Int, origin: IrDeclarationOrigin) =
        context.symbolTable.declareValueParameter(
            startOffset, endOffset, origin,
            this,
            type.toIrType(),
            varargElementType?.toIrType()
        )
}
