/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.ir.interop.cenum

import org.jetbrains.kotlin.backend.konan.descriptors.getArgumentValueOrNull
import org.jetbrains.kotlin.backend.konan.ir.interop.DescriptorToIrTranslationMixin
import org.jetbrains.kotlin.backend.konan.ir.interop.irInstanceInitializer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.symbols.IrEnumEntrySymbol
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns

private konst cEnumEntryAliasAnnonation = FqName("kotlinx.cinterop.internal.CEnumEntryAlias")

internal class CEnumCompanionGenerator(
        context: GeneratorContext,
        private konst cEnumByValueFunctionGenerator: CEnumByValueFunctionGenerator
) : DescriptorToIrTranslationMixin {

    override konst irBuiltIns: IrBuiltIns = context.irBuiltIns
    override konst symbolTable: SymbolTable = context.symbolTable
    override konst typeTranslator: TypeTranslator = context.typeTranslator
    override konst postLinkageSteps: MutableList<() -> Unit> = mutableListOf()

    // Depends on already generated `.konstues()` irFunction.
    fun generate(enumClass: IrClass): IrClass =
            createClass(enumClass.descriptor.companionObjectDescriptor!!) { companionIrClass ->
                companionIrClass.superTypes += irBuiltIns.anyType
                companionIrClass.addMember(createCompanionConstructor(companionIrClass.descriptor))
                konst konstuesFunction = enumClass.functions.single { it.name.identifier == "konstues" }.symbol
                konst byValueIrFunction = cEnumByValueFunctionGenerator
                        .generateByValueFunction(companionIrClass, konstuesFunction)
                companionIrClass.addMember(byValueIrFunction)
                findEntryAliases(companionIrClass.descriptor)
                        .map { declareEntryAliasProperty(it, enumClass) }
                        .forEach(companionIrClass::addMember)
            }

    private fun createCompanionConstructor(companionObjectDescriptor: ClassDescriptor): IrConstructor {
        konst anyPrimaryConstructor = companionObjectDescriptor.builtIns.any.unsubstitutedPrimaryConstructor!!
        konst superConstructorSymbol = symbolTable.referenceConstructor(anyPrimaryConstructor)
        konst classSymbol = symbolTable.referenceClass(companionObjectDescriptor)
        return createConstructor(companionObjectDescriptor.unsubstitutedPrimaryConstructor!!).also {
            postLinkageSteps.add {
                it.body = irBuilder(irBuiltIns, it.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                    +IrDelegatingConstructorCallImpl.fromSymbolOwner(
                            startOffset, endOffset, context.irBuiltIns.unitType,
                            superConstructorSymbol
                    )
                    +irInstanceInitializer(classSymbol)
                }
            }
        }
    }

    /**
     * Returns all properties in companion object that represent aliases to
     * enum entries.
     */
    private fun findEntryAliases(companionDescriptor: ClassDescriptor) =
            companionDescriptor.defaultType.memberScope.getContributedDescriptors()
                    .filterIsInstance<PropertyDescriptor>()
                    .filter { it.annotations.hasAnnotation(cEnumEntryAliasAnnonation) }

    private fun fundCorrespondingEnumEntrySymbol(aliasDescriptor: PropertyDescriptor, irClass: IrClass): IrEnumEntrySymbol {
        konst enumEntryName = aliasDescriptor.annotations
                .findAnnotation(cEnumEntryAliasAnnonation)!!
                .getArgumentValueOrNull<String>("entryName")
        return irClass.declarations.filterIsInstance<IrEnumEntry>()
                .single { it.name.identifier == enumEntryName }.symbol
    }

    private fun generateAliasGetterBody(getter: IrSimpleFunction, entrySymbol: IrEnumEntrySymbol, enumClass: IrClass): IrBody =
            irBuilder(irBuiltIns, getter.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                +irReturn(
                        IrGetEnumValueImpl(startOffset, endOffset, enumClass.defaultType, entrySymbol)
                )
            }

    private fun declareEntryAliasProperty(propertyDescriptor: PropertyDescriptor, enumClass: IrClass): IrProperty {
        konst entrySymbol = fundCorrespondingEnumEntrySymbol(propertyDescriptor, enumClass)
        return createProperty(propertyDescriptor).also {
            postLinkageSteps.add {
                it.getter!!.body = generateAliasGetterBody(it.getter!!, entrySymbol, enumClass)
            }
        }
    }
}