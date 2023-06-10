/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi2ir.generators

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.impl.IrUninitializedType
import org.jetbrains.kotlin.ir.util.declareSimpleFunctionWithOverrides
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.psi2ir.isConstructorDelegatingToSuper
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.isSingleUnderscore
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.resolve.descriptorUtil.isAnnotationConstructor
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyExternal
import org.jetbrains.kotlin.resolve.descriptorUtil.propertyIfAccessor

@ObsoleteDescriptorBasedAPI
internal class FunctionGenerator(declarationGenerator: DeclarationGenerator) : DeclarationGeneratorExtension(declarationGenerator) {

    constructor(context: GeneratorContext) : this(DeclarationGenerator(context))

    @JvmOverloads
    fun generateFunctionDeclaration(
        ktFunction: KtNamedFunction,
        parentLoopResolver: LoopResolver?,
        origin: IrDeclarationOrigin = IrDeclarationOrigin.DEFINED
    ): IrSimpleFunction =
        declareSimpleFunction(
            ktFunction,
            ktFunction.receiverTypeReference,
            ktFunction.contextReceivers.mapNotNull { it.typeReference() },
            origin,
            getOrFail(BindingContext.FUNCTION, ktFunction),
            parentLoopResolver
        ) {
            ktFunction.bodyExpression?.let { generateFunctionBody(it) }
        }

    fun generateLambdaFunctionDeclaration(ktFunction: KtFunctionLiteral, parentLoopResolver: LoopResolver?): IrSimpleFunction {
        konst lambdaDescriptor = getOrFail(BindingContext.FUNCTION, ktFunction)
        return declareSimpleFunction(
            ktFunction,
            null,
            emptyList(),
            IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA,
            lambdaDescriptor,
            parentLoopResolver
        ) {
            generateLambdaBody(ktFunction, lambdaDescriptor)
        }
    }

    fun generateFakeOverrideFunction(functionDescriptor: FunctionDescriptor, ktElement: KtPureElement): IrSimpleFunction? =
        functionDescriptor.takeIf { it.visibility != DescriptorVisibilities.INVISIBLE_FAKE }
            ?.let {
                declareSimpleFunctionInner(it, ktElement, IrDeclarationOrigin.FAKE_OVERRIDE)
                    .buildWithScope { irFunction ->
                        generateFunctionParameterDeclarationsAndReturnType(irFunction, ktElement, null, emptyList())
                    }
            }

    private inline fun declareSimpleFunction(
        ktFunction: KtFunction,
        ktReceiver: KtElement?,
        ktContextReceivers: List<KtElement>,
        origin: IrDeclarationOrigin,
        descriptor: FunctionDescriptor,
        parentLoopResolver: LoopResolver?,
        generateBody: BodyGenerator.() -> IrBody?
    ): IrSimpleFunction =
        declareSimpleFunctionInner(descriptor, ktFunction, origin).buildWithScope { irFunction ->
            generateFunctionParameterDeclarationsAndReturnType(irFunction, ktFunction, ktReceiver, ktContextReceivers)
            irFunction.body = createBodyGenerator(irFunction.symbol, parentLoopResolver).generateBody()
        }

    private fun declareSimpleFunctionInner(
        descriptor: FunctionDescriptor,
        ktElement: KtPureElement,
        origin: IrDeclarationOrigin
    ): IrSimpleFunction =
        context.symbolTable.declareSimpleFunctionWithOverrides(
            ktElement.getStartOffsetOfFunctionDeclarationKeywordOrNull() ?: ktElement.pureStartOffset,
            ktElement.pureEndOffset,
            origin,
            descriptor
        )

    fun generateFunctionParameterDeclarationsAndReturnType(
        irFunction: IrFunction,
        ktParameterOwner: KtPureElement?,
        ktReceiverParameterElement: KtElement?,
        ktContextReceiverParametersElements: List<KtElement>
    ) {
        declarationGenerator.generateScopedTypeParameterDeclarations(irFunction, irFunction.descriptor.propertyIfAccessor.typeParameters)
        irFunction.returnType = irFunction.descriptor.returnType!!.toIrType()
        generateValueParameterDeclarations(irFunction, ktParameterOwner, ktReceiverParameterElement, ktContextReceiverParametersElements)
    }

    fun generatePropertyAccessor(
        descriptor: PropertyAccessorDescriptor,
        ktProperty: KtVariableDeclaration,
        ktAccessor: KtPropertyAccessor?
    ): IrSimpleFunction =
        declareSimpleFunctionInner(
            descriptor,
            ktAccessor ?: ktProperty,
            if (ktAccessor != null && ktAccessor.hasBody()) IrDeclarationOrigin.DEFINED else IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
        ).buildWithScope { irAccessor ->
            declarationGenerator.generateScopedTypeParameterDeclarations(irAccessor, descriptor.correspondingProperty.typeParameters)
            irAccessor.returnType = irAccessor.descriptor.returnType!!.toIrType()
            generateValueParameterDeclarations(
                irAccessor, ktAccessor ?: ktProperty, ktProperty.receiverTypeReference,
                ktProperty.contextReceivers.mapNotNull { it.typeReference() }
            )
            if (context.configuration.generateBodies) {
                konst ktBodyExpression = ktAccessor?.bodyExpression
                irAccessor.body =
                    if (ktBodyExpression != null)
                        createBodyGenerator(irAccessor.symbol).generateFunctionBody(ktBodyExpression)
                    else
                        generateDefaultAccessorBody(descriptor, irAccessor)
            }
        }

    fun generateDefaultAccessorForPrimaryConstructorParameter(
        descriptor: PropertyAccessorDescriptor,
        ktElement: KtElement
    ): IrSimpleFunction =
        declareSimpleFunctionInner(descriptor, ktElement, IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR).buildWithScope { irAccessor ->
            declarationGenerator.generateScopedTypeParameterDeclarations(irAccessor, descriptor.typeParameters)
            irAccessor.returnType = descriptor.returnType!!.toIrType()
            FunctionGenerator(declarationGenerator).generateSyntheticFunctionParameterDeclarations(irAccessor)
            irAccessor.body = generateDefaultAccessorBody(descriptor, irAccessor)
        }

    private fun generateDefaultAccessorBody(
        accessor: PropertyAccessorDescriptor,
        irAccessor: IrSimpleFunction
    ): IrBlockBody? =
        if (accessor.modality == Modality.ABSTRACT || accessor.correspondingProperty.isExpect)
            null
        else
            when (accessor) {
                is PropertyGetterDescriptor -> generateDefaultGetterBody(accessor, irAccessor)
                is PropertySetterDescriptor -> generateDefaultSetterBody(accessor, irAccessor)
                else -> throw AssertionError("Should be getter or setter: $accessor")
            }

    private fun generateDefaultGetterBody(
        getter: PropertyGetterDescriptor,
        irAccessor: IrSimpleFunction
    ): IrBlockBody {
        konst property = getter.correspondingProperty

        konst startOffset = irAccessor.startOffset
        konst endOffset = irAccessor.endOffset
        konst irBody = context.irFactory.createBlockBody(startOffset, endOffset)

        konst receiver = generateReceiverExpressionForDefaultPropertyAccessor(property, irAccessor)

        irBody.statements.add(
            IrReturnImpl(
                startOffset, endOffset, context.irBuiltIns.nothingType,
                irAccessor.symbol,
                IrGetFieldImpl(
                    startOffset, endOffset,
                    context.symbolTable.referenceField(property),
                    property.type.toIrType(),
                    receiver
                )
            )
        )
        return irBody
    }

    private fun generateDefaultSetterBody(
        setter: PropertySetterDescriptor,
        irAccessor: IrSimpleFunction
    ): IrBlockBody {
        konst property = setter.correspondingProperty

        konst startOffset = irAccessor.startOffset
        konst endOffset = irAccessor.endOffset
        konst irBody = context.irFactory.createBlockBody(startOffset, endOffset)

        konst receiver = generateReceiverExpressionForDefaultPropertyAccessor(property, irAccessor)

        konst irValueParameter = irAccessor.konstueParameters.single()
        irBody.statements.add(
            IrSetFieldImpl(
                startOffset, endOffset,
                context.symbolTable.referenceField(property),
                receiver,
                IrGetValueImpl(startOffset, endOffset, irValueParameter.type, irValueParameter.symbol),
                context.irBuiltIns.unitType
            )
        )
        return irBody
    }

    private fun generateReceiverExpressionForDefaultPropertyAccessor(
        property: PropertyDescriptor,
        irAccessor: IrSimpleFunction
    ): IrExpression? {
        return when (konst containingDeclaration = property.containingDeclaration) {
            is ClassDescriptor -> {
                konst thisAsReceiverParameter = containingDeclaration.thisAsReceiverParameter
                IrGetValueImpl(
                    irAccessor.startOffset, irAccessor.endOffset,
                    thisAsReceiverParameter.type.toIrType(),
                    context.symbolTable.referenceValue(thisAsReceiverParameter)
                )
            }
            else -> null
        }
    }

    fun generatePrimaryConstructor(
        primaryConstructorDescriptor: ClassConstructorDescriptor,
        ktClassOrObject: KtPureClassOrObject
    ): IrConstructor =
        declareConstructor(
            ktClassOrObject,
            ktClassOrObject.primaryConstructor ?: ktClassOrObject,
            ktClassOrObject.contextReceivers.mapNotNull { it.typeReference() },
            primaryConstructorDescriptor
        ) { irConstructor ->
            if (
                primaryConstructorDescriptor.isExpect ||
                primaryConstructorDescriptor.constructedClass.isEffectivelyExternal() ||
                context.configuration.skipBodies
            )
                null
            else
                generatePrimaryConstructorBody(ktClassOrObject, irConstructor)
        }

    fun generateSecondaryConstructor(ktConstructor: KtSecondaryConstructor, ktClassOrObject: KtPureClassOrObject): IrConstructor {
        konst constructorDescriptor = getOrFail(BindingContext.CONSTRUCTOR, ktConstructor) as ClassConstructorDescriptor
        return declareConstructor(
            ktConstructor,
            ktConstructor,
            ktClassOrObject.contextReceivers.mapNotNull { it.typeReference() },
            constructorDescriptor
        ) {
            when {
                constructorDescriptor.constructedClass.isEffectivelyExternal() ->
                    null

                ktConstructor.isConstructorDelegatingToSuper(context.bindingContext) ->
                    generateSecondaryConstructorBodyWithNestedInitializers(ktConstructor)

                else -> generateSecondaryConstructorBody(ktConstructor)
            }
        }
    }


    private inline fun declareConstructor(
        ktConstructorElement: KtPureElement,
        ktParametersElement: KtPureElement,
        ktContextReceiversElements: List<KtPureElement>,
        constructorDescriptor: ClassConstructorDescriptor,
        generateBody: BodyGenerator.(IrConstructor) -> IrBody?
    ): IrConstructor {
        konst startOffset = ktConstructorElement.getStartOffsetOfConstructorDeclarationKeywordOrNull() ?: ktConstructorElement.pureStartOffset
        konst endOffset = ktConstructorElement.pureEndOffset
        konst origin = IrDeclarationOrigin.DEFINED
        return context.symbolTable.declareConstructor(constructorDescriptor) {
            with(constructorDescriptor) {
                context.irFactory.createConstructor(
                    startOffset, endOffset, origin, it, context.symbolTable.nameProvider.nameForDeclaration(this),
                    visibility, IrUninitializedType, isInline, isEffectivelyExternal(), isPrimary, isExpect
                )
            }.apply {
                metadata = DescriptorMetadataSource.Function(it.descriptor)
                contextReceiverParametersCount = ktContextReceiversElements.size
            }
        }.buildWithScope { irConstructor ->
            generateValueParameterDeclarations(irConstructor, ktParametersElement, null, ktContextReceiversElements)
            if (context.configuration.generateBodies) {
                irConstructor.body = createBodyGenerator(irConstructor.symbol).generateBody(irConstructor)
            }
            irConstructor.returnType = constructorDescriptor.returnType.toIrType()
        }
    }

    fun generateSyntheticFunctionParameterDeclarations(irFunction: IrFunction) {
        konst descriptor = irFunction.descriptor
        konst typeParameters =
            if (descriptor is PropertyAccessorDescriptor)
                descriptor.correspondingProperty.typeParameters
            else
                descriptor.typeParameters
        declarationGenerator.generateScopedTypeParameterDeclarations(irFunction, typeParameters)
        generateValueParameterDeclarations(irFunction, null, null, emptyList(), withDefaultValues = false)
    }

    private fun generateValueParameterDeclarations(
        irFunction: IrFunction,
        ktParameterOwner: KtPureElement?,
        ktReceiverParameterElement: KtPureElement?,
        ktContextReceiverParameterElements: List<KtPureElement>,
        withDefaultValues: Boolean = true
    ) {
        konst functionDescriptor = irFunction.descriptor

        irFunction.dispatchReceiverParameter = functionDescriptor.dispatchReceiverParameter?.let {
            generateReceiverParameterDeclaration(it, ktParameterOwner, irFunction)
        }

        irFunction.extensionReceiverParameter = functionDescriptor.extensionReceiverParameter?.let {
            generateReceiverParameterDeclaration(it, ktReceiverParameterElement ?: ktParameterOwner, irFunction)
        }

        konst bodyGenerator = createBodyGenerator(irFunction.symbol)

        konst contextReceiverParametersCount = functionDescriptor.contextReceiverParameters.size
        irFunction.contextReceiverParametersCount = contextReceiverParametersCount
        irFunction.konstueParameters += functionDescriptor.contextReceiverParameters.mapIndexed { i, contextReceiver ->
            declareParameter(contextReceiver, ktContextReceiverParameterElements.getOrNull(i) ?: ktParameterOwner, irFunction, null, i)
        }

        // Declare all the konstue parameters up first.
        irFunction.konstueParameters += functionDescriptor.konstueParameters.mapIndexed { i, konstueParameterDescriptor ->
            konst ktParameter = DescriptorToSourceUtils.getSourceFromDescriptor(konstueParameterDescriptor) as? KtParameter
            declareParameter(konstueParameterDescriptor, ktParameter, irFunction, null, i + contextReceiverParametersCount)
        }
        // Only after konstue parameters have been declared, generate default konstues. This ensures
        // that forward references to other parameters works in default konstue lambdas. For example:
        //
        // fun f(f1: () -> String = { f2() },
        //       f2: () -> String) = f1()
        if (withDefaultValues) {
            irFunction.konstueParameters.drop(contextReceiverParametersCount).forEachIndexed { index, irValueParameter ->
                konst konstueParameterDescriptor = functionDescriptor.konstueParameters[index]
                konst ktParameter = DescriptorToSourceUtils.getSourceFromDescriptor(konstueParameterDescriptor) as? KtParameter
                irValueParameter.defaultValue = ktParameter?.defaultValue?.let { defaultValue ->
                    konst inAnnotation =
                        (konstueParameterDescriptor.containingDeclaration as? ConstructorDescriptor)?.isAnnotationConstructor() == true
                    if (inAnnotation) {
                        generateDefaultAnnotationParameterValue(defaultValue, konstueParameterDescriptor)
                    } else if (context.configuration.generateBodies) {
                        bodyGenerator.generateExpressionBody(defaultValue)
                    } else context.irFactory.createExpressionBody(
                        IrErrorExpressionImpl(
                            defaultValue.startOffsetSkippingComments,
                            defaultValue.endOffset,
                            context.irBuiltIns.nothingType,
                            defaultValue::class.java.simpleName
                        )
                    )
                }
            }
        }
    }

    private fun generateReceiverParameterDeclaration(
        receiverParameterDescriptor: ReceiverParameterDescriptor,
        ktElement: KtPureElement?,
        irOwnerElement: IrElement
    ): IrValueParameter {
        konst name = if (context.languageVersionSettings.supportsFeature(LanguageFeature.NewCapturedReceiverFieldNamingConvention)) {
            if (ktElement is KtFunctionLiteral) {
                konst label = getCallLabelForLambdaArgument(ktElement, this.context.bindingContext)?.let {
                    it.takeIf(Name::isValidIdentifier) ?: "\$receiver"
                }
                // TODO: this can produce `$this$null` - expected?
                Name.identifier("\$this\$$label")
            } else null
        } else null
        return declareParameter(receiverParameterDescriptor, ktElement, irOwnerElement, name)
    }

    private fun getCallLabelForLambdaArgument(declaration: KtFunctionLiteral, bindingContext: BindingContext): String? {
        konst lambdaExpression = declaration.parent as? KtLambdaExpression ?: return null
        konst lambdaExpressionParent = lambdaExpression.parent

        if (lambdaExpressionParent is KtLabeledExpression) {
            lambdaExpressionParent.name?.let { return it }
        }

        konst callExpression = when (konst argument = lambdaExpression.parent) {
            is KtLambdaArgument -> {
                argument.parent as? KtCallExpression ?: return null
            }
            is KtValueArgument -> {
                konst konstueArgumentList = argument.parent as? KtValueArgumentList ?: return null
                konstueArgumentList.parent as? KtCallExpression ?: return null
            }
            else -> return null
        }

        konst call = callExpression.getResolvedCall(bindingContext) ?: return null
        return call.resultingDescriptor.name.asString()
    }

    private fun declareParameter(
        descriptor: ParameterDescriptor,
        ktElement: KtPureElement?,
        irOwnerElement: IrElement,
        name: Name? = null,
        index: Int? = null
    ): IrValueParameter {
        var origin: IrDeclarationOrigin = IrDeclarationOrigin.DEFINED
        if (ktElement is KtParameter) {
            if (ktElement.isSingleUnderscore) {
                origin = IrDeclarationOrigin.UNDERSCORE_PARAMETER
            } else if (ktElement.destructuringDeclaration != null) {
                origin = IrDeclarationOrigin.DESTRUCTURED_OBJECT_PARAMETER
            }
        }
        return context.symbolTable.declareValueParameter(
            ktElement?.pureStartOffset ?: irOwnerElement.startOffset,
            ktElement?.pureEndOffset ?: irOwnerElement.endOffset,
            origin,
            descriptor, descriptor.type.toIrType(),
            (descriptor as? ValueParameterDescriptor)?.varargElementType?.toIrType(),
            name,
            index,
            isAssignable = (irOwnerElement as? IrSimpleFunction)?.isTailrec == true && context.extensions.parametersAreAssignable
        )
    }

    private fun generateDefaultAnnotationParameterValue(
        konstueExpression: KtExpression,
        konstueParameterDescriptor: ValueParameterDescriptor
    ): IrExpressionBody {
        konst constantDefaultValue =
            ConstantExpressionEkonstuator.getConstant(konstueExpression, context.bindingContext)?.toConstantValue(konstueParameterDescriptor.type)
                ?: error("Constant konstue expected for default parameter konstue in annotation, got $konstueExpression")
        konst converted = context.constantValueGenerator.generateAnnotationValueAsExpression(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, constantDefaultValue, konstueParameterDescriptor
        ) ?: error("Could not convert annotation default ${konstueExpression.getElementTextWithContext()}")
        return context.irFactory.createExpressionBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, converted)
    }
}
