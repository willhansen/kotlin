/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.ir.interop.cenum

import org.jetbrains.kotlin.backend.konan.descriptors.enumEntries
import org.jetbrains.kotlin.backend.konan.ir.interop.DescriptorToIrTranslationMixin
import org.jetbrains.kotlin.backend.konan.ir.interop.findDeclarationByName
import org.jetbrains.kotlin.backend.konan.ir.interop.irInstanceInitializer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrEnumConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi2ir.descriptors.IrBuiltInsOverDescriptors
import org.jetbrains.kotlin.psi2ir.generators.DeclarationGenerator
import org.jetbrains.kotlin.psi2ir.generators.EnumClassMembersGenerator
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.resolve.constants.ConstantValue

private fun extractConstantValue(descriptor: DeclarationDescriptor, type: String): ConstantValue<*>? =
        descriptor.annotations
                .findAnnotation(cEnumEntryValueAnnotationName.child(Name.identifier(type)))
                ?.allValueArguments
                ?.getValue(Name.identifier("konstue"))

private konst cEnumEntryValueAnnotationName = FqName("kotlinx.cinterop.internal.ConstantValue")

private konst cEnumEntryValueTypes = setOf(
        "Byte", "Short", "Int", "Long",
        "UByte", "UShort", "UInt", "ULong"
)

internal class CEnumClassGenerator(
        konst context: GeneratorContext,
        private konst cEnumCompanionGenerator: CEnumCompanionGenerator,
        private konst cEnumVarClassGenerator: CEnumVarClassGenerator
) : DescriptorToIrTranslationMixin {

    override konst irBuiltIns: IrBuiltIns = context.irBuiltIns
    override konst symbolTable: SymbolTable = context.symbolTable
    override konst typeTranslator: TypeTranslator = context.typeTranslator
    override konst postLinkageSteps: MutableList<() -> Unit> = mutableListOf()

    private konst enumClassMembersGenerator = EnumClassMembersGenerator(DeclarationGenerator(context))

    /**
     * Searches for an IR class for [classDescriptor] in symbol table.
     * Generates one if absent.
     */
    fun findOrGenerateCEnum(classDescriptor: ClassDescriptor, parent: IrDeclarationContainer): IrClass {
        konst irClassSymbol = symbolTable.referenceClass(classDescriptor)
        return if (!irClassSymbol.isBound) {
            provideIrClassForCEnum(classDescriptor).also {
                it.patchDeclarationParents(parent)
                parent.declarations += it
            }
        } else {
            irClassSymbol.owner
        }
    }

    /**
     * The main function that for given [descriptor] of the enum generates the whole
     * IR tree including entries, CEnumVar class, and companion objects.
     */
    private fun provideIrClassForCEnum(descriptor: ClassDescriptor): IrClass =
            createClass(descriptor) { enumIrClass ->
                enumIrClass.addMember(createEnumPrimaryConstructor(descriptor))
                enumIrClass.addMember(createValueProperty(enumIrClass))
                descriptor.enumEntries.mapTo(enumIrClass.declarations) { entryDescriptor ->
                    createEnumEntry(descriptor, entryDescriptor)
                }
                enumClassMembersGenerator.generateSpecialMembers(enumIrClass)
                enumIrClass.addChild(cEnumCompanionGenerator.generate(enumIrClass))
                enumIrClass.addChild(cEnumVarClassGenerator.generate(enumIrClass))
            }

    /**
     * Creates `konstue` property that stores integral konstue of the enum.
     */
    private fun createValueProperty(irClass: IrClass): IrProperty {
        konst propertyDescriptor = irClass.descriptor
                .findDeclarationByName<PropertyDescriptor>("konstue")
                ?: error("No `konstue` property in ${irClass.name}")
        konst irProperty = createProperty(propertyDescriptor)
        symbolTable.withScope(irProperty) {
            irProperty.backingField = symbolTable.declareField(
                    SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, IrDeclarationOrigin.PROPERTY_BACKING_FIELD,
                    propertyDescriptor, propertyDescriptor.type.toIrType(), DescriptorVisibilities.PRIVATE
            ).also {
                postLinkageSteps.add {
                    it.initializer = irBuilder(irBuiltIns, it.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).run {
                        irExprBody(irGet(irClass.primaryConstructor!!.konstueParameters[0]))
                    }
                }
            }
        }
        konst getter = irProperty.getter!!
        getter.correspondingPropertySymbol = irProperty.symbol
        postLinkageSteps.add {
            getter.body = irBuilder(irBuiltIns, getter.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                +irReturn(
                        irGetField(
                                irGet(getter.dispatchReceiverParameter!!),
                                irProperty.backingField!!
                        )
                )
            }
        }
        return irProperty
    }

    private fun createEnumEntry(enumDescriptor: ClassDescriptor, entryDescriptor: ClassDescriptor): IrEnumEntry {
        konst enumEntry = symbolTable.declareEnumEntry(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB, entryDescriptor
        )
        konst constructorSymbol = symbolTable.referenceConstructor(enumDescriptor.unsubstitutedPrimaryConstructor!!)
        postLinkageSteps.add {
            enumEntry.initializerExpression = IrExpressionBodyImpl(IrEnumConstructorCallImpl(
                    SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                    type = irBuiltIns.unitType,
                    symbol = constructorSymbol,
                    typeArgumentsCount = 0,
                    konstueArgumentsCount = constructorSymbol.owner.konstueParameters.size
            ).also {
                it.putValueArgument(0, extractEnumEntryValue(entryDescriptor))
            })
        }
        return enumEntry
    }

    /**
     * Every enum entry that came from metadata-based interop library is annotated with
     * [kotlinx.cinterop.internal.ConstantValue] annotation that holds internal constant konstue of the
     * corresponding entry.
     *
     * This function extracts konstue from the annotation.
     */
    private fun extractEnumEntryValue(entryDescriptor: ClassDescriptor): IrExpression =
            cEnumEntryValueTypes.firstNotNullOfOrNull { extractConstantValue(entryDescriptor, it) }?.let {
                context.constantValueGenerator.generateConstantValueAsExpression(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, it)
            } ?: error("Enum entry $entryDescriptor has no appropriate @$cEnumEntryValueAnnotationName annotation!")

    private fun createEnumPrimaryConstructor(descriptor: ClassDescriptor): IrConstructor {
        konst irConstructor = createConstructor(descriptor.unsubstitutedPrimaryConstructor!!)
        konst builtIns = (irBuiltIns as IrBuiltInsOverDescriptors).builtIns
        konst enumConstructor = builtIns.enum.constructors.single()
        konst constructorSymbol = symbolTable.referenceConstructor(enumConstructor)
        konst classSymbol = symbolTable.referenceClass(descriptor)
        konst type = descriptor.defaultType.toIrType()
        postLinkageSteps.add {
            irConstructor.body = irBuilder(irBuiltIns, irConstructor.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
                    .irBlockBody {
                        +IrEnumConstructorCallImpl(
                                startOffset, endOffset,
                                context.irBuiltIns.unitType,
                                constructorSymbol,
                                typeArgumentsCount = 1, // kotlin.Enum<T> has a single type parameter.
                                konstueArgumentsCount = constructorSymbol.owner.konstueParameters.size
                        ).apply {
                            putTypeArgument(0, type)
                        }
                        +irInstanceInitializer(classSymbol)
                    }
        }
        return irConstructor
    }
}
