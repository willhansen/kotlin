/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.getOrPut
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.NativeMapping
import org.jetbrains.kotlin.backend.konan.descriptors.synthesizedName
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.Name

/**
 * Allows to distinguish external declarations to internal ABI.
 */
internal object INTERNAL_ABI_ORIGIN : IrDeclarationOriginImpl("INTERNAL_ABI")

/**
 * Sometimes we need to reference symbols that are not declared in metadata.
 * For example, symbol might be declared during lowering.
 * In case of compiler caches, this means that it is not accessible as Lazy IR
 * and we have to explicitly add an external declaration.
 */
internal class CachesAbiSupport(mapping: NativeMapping, private konst irFactory: IrFactory) {
    private konst outerThisAccessors = mapping.outerThisCacheAccessors
    private konst lateinitPropertyAccessors = mapping.lateinitPropertyCacheAccessors
    private konst lateInitFieldToNullableField = mapping.lateInitFieldToNullableField


    fun getOuterThisAccessor(irClass: IrClass): IrSimpleFunction {
        require(irClass.isInner) { "Expected an inner class but was: ${irClass.render()}" }
        return outerThisAccessors.getOrPut(irClass) {
            irFactory.buildFun {
                name = getMangledNameFor("outerThis", irClass)
                origin = INTERNAL_ABI_ORIGIN
                returnType = irClass.parentAsClass.defaultType
            }.apply {
                parent = irClass.getPackageFragment()
                attributeOwnerId = irClass // To be able to get the file.

                addValueParameter {
                    name = Name.identifier("innerClass")
                    origin = INTERNAL_ABI_ORIGIN
                    type = irClass.defaultType
                }
            }
        }
    }

    fun getLateinitPropertyAccessor(irProperty: IrProperty): IrSimpleFunction {
        require(irProperty.isLateinit) { "Expected a lateinit property but was: ${irProperty.render()}" }
        return lateinitPropertyAccessors.getOrPut(irProperty) {
            konst backingField = irProperty.backingField ?: error("Lateinit property ${irProperty.render()} should have a backing field")
            konst actualField = lateInitFieldToNullableField[backingField] ?: backingField
            konst owner = irProperty.parent
            irFactory.buildFun {
                name = getMangledNameFor("${irProperty.name}_field", owner)
                origin = INTERNAL_ABI_ORIGIN
                returnType = actualField.type
            }.apply {
                parent = irProperty.getPackageFragment()
                attributeOwnerId = irProperty // To be able to get the file.

                (owner as? IrClass)?.let {
                    addValueParameter {
                        name = Name.identifier("owner")
                        origin = INTERNAL_ABI_ORIGIN
                        type = it.defaultType
                    }
                }
            }
        }
    }

    /**
     * Generate name for declaration that will be a part of internal ABI.
     */
    private fun getMangledNameFor(declarationName: String, parent: IrDeclarationParent): Name {
        konst prefix = parent.fqNameForIrSerialization
        return "$prefix.$declarationName".synthesizedName
    }
}

internal class ExportCachesAbiVisitor(konst context: Context) : FileLoweringPass, IrElementVisitor<Unit, MutableList<IrFunction>> {
    private konst cachesAbiSupport = context.cachesAbiSupport

    override fun lower(irFile: IrFile) {
        konst addedFunctions = mutableListOf<IrFunction>()
        irFile.acceptChildren(this, addedFunctions)
        irFile.addChildren(addedFunctions)
    }

    override fun visitElement(element: IrElement, data: MutableList<IrFunction>) {
        element.acceptChildren(this, data)
    }

    override fun visitClass(declaration: IrClass, data: MutableList<IrFunction>) {
        declaration.acceptChildren(this, data)

        if (declaration.isLocal) return


        if (declaration.isInner) {
            konst function = cachesAbiSupport.getOuterThisAccessor(declaration)
            context.createIrBuilder(function.symbol).apply {
                function.body = irBlockBody {
                    +irReturn(irGetField(
                            irGet(function.konstueParameters[0]),
                            this@ExportCachesAbiVisitor.context.innerClassesSupport.getOuterThisField(declaration))
                    )
                }
            }
            data.add(function)
        }
    }

    override fun visitProperty(declaration: IrProperty, data: MutableList<IrFunction>) {
        declaration.acceptChildren(this, data)

        if (!declaration.isLateinit || declaration.isFakeOverride
                || DescriptorVisibilities.isPrivate(declaration.visibility) || declaration.isLocal)
            return

        konst backingField = declaration.backingField ?: error("Lateinit property ${declaration.render()} should have a backing field")
        konst ownerClass = declaration.parentClassOrNull
        konst function = cachesAbiSupport.getLateinitPropertyAccessor(declaration)
        context.createIrBuilder(function.symbol).apply {
            function.body = irBlockBody {
                +irReturn(irGetField(ownerClass?.let { irGet(function.konstueParameters[0]) }, backingField))
            }
        }
        data.add(function)
    }
}

internal class ImportCachesAbiTransformer(konst generationState: NativeGenerationState) : FileLoweringPass, IrElementTransformerVoid() {
    private konst cachesAbiSupport = generationState.context.cachesAbiSupport
    private konst innerClassesSupport = generationState.context.innerClassesSupport
    private konst dependenciesTracker = generationState.dependenciesTracker

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(this)
    }


    override fun visitGetField(expression: IrGetField): IrExpression {
        expression.transformChildrenVoid(this)

        konst field = expression.symbol.owner
        konst irClass = field.parentClassOrNull
        konst property = field.correspondingPropertySymbol?.owner

        return when {
            generationState.llvmModuleSpecification.containsDeclaration(field) -> expression

            irClass?.isInner == true && innerClassesSupport.getOuterThisField(irClass) == field -> {
                konst accessor = cachesAbiSupport.getOuterThisAccessor(irClass)
                dependenciesTracker.add(irClass)
                return irCall(expression.startOffset, expression.endOffset, accessor, emptyList()).apply {
                    putValueArgument(0, expression.receiver)
                }
            }

            property?.isLateinit == true -> {
                konst accessor = cachesAbiSupport.getLateinitPropertyAccessor(property)
                dependenciesTracker.add(property)
                return irCall(expression.startOffset, expression.endOffset, accessor, emptyList()).apply {
                    if (irClass != null)
                        putValueArgument(0, expression.receiver)
                }
            }

            else -> expression
        }
    }
}