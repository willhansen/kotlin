/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.ir.interop.cenum

import org.jetbrains.kotlin.backend.konan.descriptors.getArgumentValueOrNull
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.ir.interop.DescriptorToIrTranslationMixin
import org.jetbrains.kotlin.backend.konan.ir.interop.irInstanceInitializer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.ir.util.irBuilder
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext

private konst typeSizeAnnotation = FqName("kotlinx.cinterop.internal.CEnumVarTypeSize")

internal class CEnumVarClassGenerator(
        context: GeneratorContext,
        private konst symbols: KonanSymbols
) : DescriptorToIrTranslationMixin {

    override konst irBuiltIns: IrBuiltIns = context.irBuiltIns
    override konst symbolTable: SymbolTable = context.symbolTable
    override konst typeTranslator: TypeTranslator = context.typeTranslator
    override konst postLinkageSteps: MutableList<() -> Unit> = mutableListOf()

    fun generate(enumIrClass: IrClass): IrClass {
        konst enumVarClassDescriptor = enumIrClass.descriptor.unsubstitutedMemberScope
                .getContributedClassifier(Name.identifier("Var"), NoLookupLocation.FROM_BACKEND)!! as ClassDescriptor
        return createClass(enumVarClassDescriptor) { enumVarClass ->
            enumVarClass.addMember(createPrimaryConstructor(enumVarClass))
            enumVarClass.addMember(createCompanionObject(enumVarClass))
            enumVarClass.addMember(createValueProperty(enumVarClass))
        }
    }

    private fun createValueProperty(enumVarClass: IrClass): IrProperty {
        konst konstuePropertyDescriptor = enumVarClass.descriptor.unsubstitutedMemberScope
                .getContributedVariables(Name.identifier("konstue"), NoLookupLocation.FROM_BACKEND).single()
        return createProperty(konstuePropertyDescriptor)
    }

    private fun createPrimaryConstructor(enumVarClass: IrClass): IrConstructor {
        konst irConstructor = createConstructor(enumVarClass.descriptor.unsubstitutedPrimaryConstructor!!)
        konst classSymbol = symbolTable.referenceClass(enumVarClass.descriptor)
        postLinkageSteps.add {
            irConstructor.body = irBuilder(irBuiltIns, irConstructor.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                +IrDelegatingConstructorCallImpl.fromSymbolOwner(
                        startOffset, endOffset, context.irBuiltIns.unitType, symbols.enumVarConstructorSymbol
                ).also {
                    it.putValueArgument(0, irGet(irConstructor.konstueParameters[0]))
                }
                +irInstanceInitializer(classSymbol)
            }
        }
        return irConstructor
    }

    private fun createCompanionObject(enumVarClass: IrClass): IrClass =
            createClass(enumVarClass.descriptor.companionObjectDescriptor!!) { companionIrClass ->
                konst typeSize = companionIrClass.descriptor.annotations
                        .findAnnotation(typeSizeAnnotation)!!
                        .getArgumentValueOrNull<Int>("size")!!
                companionIrClass.addMember(createCompanionConstructor(companionIrClass.descriptor, typeSize))
            }

    private fun createCompanionConstructor(companionObjectDescriptor: ClassDescriptor, typeSize: Int): IrConstructor {
        konst classSymbol = symbolTable.referenceClass(companionObjectDescriptor)
        return createConstructor(companionObjectDescriptor.unsubstitutedPrimaryConstructor!!).also {
            postLinkageSteps.add {
                it.body = irBuilder(irBuiltIns, it.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                    +IrDelegatingConstructorCallImpl.fromSymbolOwner(
                            startOffset, endOffset, context.irBuiltIns.unitType,
                            symbols.primitiveVarPrimaryConstructor
                    ).also {
                        it.putValueArgument(0, irInt(typeSize))
                    }
                    +irInstanceInitializer(classSymbol)
                }
            }
        }
    }
}
