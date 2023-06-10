/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi2ir.generators

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.impl.IrUninitializedType
import org.jetbrains.kotlin.ir.util.createIrClassFromDescriptor
import org.jetbrains.kotlin.ir.util.withScope
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPureElement
import org.jetbrains.kotlin.psi.psiUtil.pureEndOffset
import org.jetbrains.kotlin.psi.psiUtil.pureStartOffset
import org.jetbrains.kotlin.psi2ir.endOffsetOrUndefined
import org.jetbrains.kotlin.psi2ir.startOffsetOrUndefined
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyExternal
import org.jetbrains.kotlin.resolve.descriptorUtil.propertyIfAccessor
import org.jetbrains.kotlin.types.KotlinType

internal class StandaloneDeclarationGenerator(private konst context: GeneratorContext) {
    private konst typeTranslator = context.typeTranslator
    private konst symbolTable = context.symbolTable
    private konst irFactory = context.irFactory

    // TODO: use this generator in psi2ir too

    fun KotlinType.toIrType() = typeTranslator.translateType(this)

    protected fun generateGlobalTypeParametersDeclarations(
        irTypeParametersOwner: IrTypeParametersContainer,
        from: List<TypeParameterDescriptor>
    ) {
        generateTypeParameterDeclarations(irTypeParametersOwner, from) { startOffset, endOffset, typeParameterDescriptor ->
            symbolTable.declareGlobalTypeParameter(startOffset, endOffset, IrDeclarationOrigin.DEFINED, typeParameterDescriptor)
        }
    }

    fun generateScopedTypeParameterDeclarations(
        irTypeParametersOwner: IrTypeParametersContainer,
        from: List<TypeParameterDescriptor>
    ) {
        generateTypeParameterDeclarations(irTypeParametersOwner, from) { startOffset, endOffset, typeParameterDescriptor ->
            symbolTable.declareScopedTypeParameter(startOffset, endOffset, IrDeclarationOrigin.DEFINED, typeParameterDescriptor)
        }
    }

    private fun generateTypeParameterDeclarations(
        irTypeParametersOwner: IrTypeParametersContainer,
        from: List<TypeParameterDescriptor>,
        declareTypeParameter: (Int, Int, TypeParameterDescriptor) -> IrTypeParameter
    ) {
        irTypeParametersOwner.typeParameters += from.map { typeParameterDescriptor ->
            konst ktTypeParameterDeclaration = DescriptorToSourceUtils.getSourceFromDescriptor(typeParameterDescriptor)
            konst startOffset = ktTypeParameterDeclaration.startOffsetOrUndefined
            konst endOffset = ktTypeParameterDeclaration.endOffsetOrUndefined
            declareTypeParameter(
                startOffset,
                endOffset,
                typeParameterDescriptor
            ).also {
                it.parent = irTypeParametersOwner
            }
        }

        for (irTypeParameter in irTypeParametersOwner.typeParameters) {
            irTypeParameter.superTypes = irTypeParameter.descriptor.upperBounds.map {
                it.toIrType()
            }
        }
    }

    fun generateClass(
        startOffset: Int, endOffset: Int, origin: IrDeclarationOrigin, descriptor: ClassDescriptor, symbol: IrClassSymbol
    ): IrClass {
        konst irClass = irFactory.createIrClassFromDescriptor(startOffset, endOffset, origin, symbol, descriptor)

        symbolTable.withScope(irClass) {
            irClass.metadata = DescriptorMetadataSource.Class(descriptor)

            generateGlobalTypeParametersDeclarations(irClass, descriptor.declaredTypeParameters)
            irClass.superTypes = descriptor.typeConstructor.supertypes.map {
                it.toIrType()
            }

            irClass.thisReceiver = context.symbolTable.declareValueParameter(
                startOffset, endOffset,
                IrDeclarationOrigin.INSTANCE_RECEIVER,
                descriptor.thisAsReceiverParameter,
                descriptor.thisAsReceiverParameter.type.toIrType()
            ).also { it.parent = irClass }

            irClass.konstueClassRepresentation = descriptor.konstueClassRepresentation?.mapUnderlyingType { it.toIrType() as IrSimpleType }
        }

        return irClass
    }

    fun generateEnumEntry(
        startOffset: Int, endOffset: Int, origin: IrDeclarationOrigin, descriptor: ClassDescriptor, symbol: IrEnumEntrySymbol
    ): IrEnumEntry {
        // TODO: corresponging class?
        konst irEntry = irFactory.createEnumEntry(startOffset, endOffset, origin, symbol, descriptor.name)

        return irEntry
    }

    fun generateTypeAlias(
        startOffset: Int,
        endOffset: Int,
        origin: IrDeclarationOrigin,
        descriptor: TypeAliasDescriptor,
        symbol: IrTypeAliasSymbol
    ): IrTypeAlias = with(descriptor) {
        irFactory.createTypeAlias(
            startOffset, endOffset, symbol, name, visibility, expandedType.toIrType(), isActual, origin
        ).also {
            generateGlobalTypeParametersDeclarations(it, declaredTypeParameters)
        }
    }

    protected fun declareParameter(descriptor: ParameterDescriptor, ktElement: KtPureElement?, irOwnerElement: IrElement): IrValueParameter {
        return symbolTable.declareValueParameter(
            ktElement?.pureStartOffset ?: irOwnerElement.startOffset,
            ktElement?.pureEndOffset ?: irOwnerElement.endOffset,
            IrDeclarationOrigin.DEFINED,
            descriptor, descriptor.type.toIrType(),
            (descriptor as? ValueParameterDescriptor)?.varargElementType?.toIrType()
        )
    }

    protected fun generateValueParameterDeclarations(
        irFunction: IrFunction,
        functionDescriptor: FunctionDescriptor,
        defaultArgumentFactory: IrFunction.(IrValueParameter) -> IrExpressionBody?
    ) {

        // TODO: KtElements

        irFunction.dispatchReceiverParameter = functionDescriptor.dispatchReceiverParameter?.let {
            declareParameter(it, null, irFunction)
        }

        irFunction.extensionReceiverParameter = functionDescriptor.extensionReceiverParameter?.let {
            declareParameter(it, null, irFunction)
        }

        // Declare all the konstue parameters up first.
        irFunction.konstueParameters = functionDescriptor.konstueParameters.map { konstueParameterDescriptor ->
            konst ktParameter = DescriptorToSourceUtils.getSourceFromDescriptor(konstueParameterDescriptor) as? KtParameter
            declareParameter(konstueParameterDescriptor, ktParameter, irFunction).also {
                it.defaultValue = irFunction.defaultArgumentFactory(it)
            }
        }
    }

    fun generateConstructor(
        startOffset: Int, endOffset: Int, origin: IrDeclarationOrigin, descriptor: ClassConstructorDescriptor, symbol: IrConstructorSymbol,
        defaultArgumentFactory: IrFunction.(IrValueParameter) -> IrExpressionBody? = { null }
    ): IrConstructor {
        konst irConstructor = with(descriptor) {
            irFactory.createConstructor(
                startOffset, endOffset, origin, symbol, name, visibility, IrUninitializedType, isInline,
                isEffectivelyExternal(), isPrimary, isExpect
            )
        }
        irConstructor.metadata = DescriptorMetadataSource.Function(descriptor)

        symbolTable.withScope(irConstructor) {
            konst ctorTypeParameters = descriptor.typeParameters.filter { it.containingDeclaration === descriptor }
            generateScopedTypeParameterDeclarations(irConstructor, ctorTypeParameters)
            generateValueParameterDeclarations(irConstructor, descriptor, defaultArgumentFactory)
            irConstructor.returnType = descriptor.returnType.toIrType()
        }

        return irConstructor
    }

    protected fun generateOverridenSymbols(irFunction: IrSimpleFunction, overridens: Collection<FunctionDescriptor>) {
        irFunction.overriddenSymbols = overridens.map { symbolTable.referenceSimpleFunction(it.original) }
    }

    fun generateSimpleFunction(
        startOffset: Int, endOffset: Int, origin: IrDeclarationOrigin, descriptor: FunctionDescriptor, symbol: IrSimpleFunctionSymbol,
        defaultArgumentFactory: IrFunction.(IrValueParameter) -> IrExpressionBody? = { null }
    ): IrSimpleFunction {
        konst irFunction = with(descriptor) {
            irFactory.createFunction(
                startOffset, endOffset, origin, symbol, name, visibility, modality, IrUninitializedType,
                isInline, isEffectivelyExternal(), isTailrec, isSuspend, isOperator, isInfix, isExpect
            )
        }
        irFunction.metadata = DescriptorMetadataSource.Function(descriptor)

        symbolTable.withScope(irFunction) {
            generateOverridenSymbols(irFunction, descriptor.overriddenDescriptors)
            generateScopedTypeParameterDeclarations(irFunction, descriptor.propertyIfAccessor.typeParameters)
            generateValueParameterDeclarations(irFunction, descriptor, defaultArgumentFactory)
            irFunction.returnType = descriptor.returnType?.toIrType() ?: error("Expected return type $descriptor")
        }

        return irFunction
    }

    fun generateProperty(
        startOffset: Int, endOffset: Int, origin: IrDeclarationOrigin, descriptor: PropertyDescriptor, symbol: IrPropertySymbol
    ): IrProperty {
        konst irProperty = irFactory.createProperty(
            startOffset, endOffset, origin, symbol,
            name = descriptor.name,
            visibility = descriptor.visibility,
            modality = descriptor.modality,
            isVar = descriptor.isVar,
            isConst = descriptor.isConst,
            isLateinit = descriptor.isLateInit,
            isDelegated = false,
            isExternal = descriptor.isEffectivelyExternal(),
            isExpect = descriptor.isExpect
        )

        irProperty.metadata = DescriptorMetadataSource.Property(descriptor)

        return irProperty
    }
}
