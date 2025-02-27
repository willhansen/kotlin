/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.ir.interop.cstruct

import org.jetbrains.kotlin.backend.konan.RuntimeNames
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.ir.interop.DescriptorToIrTranslationMixin
import org.jetbrains.kotlin.backend.konan.ir.interop.irInstanceInitializer
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext

internal class CStructVarClassGenerator(
        context: GeneratorContext,
        private konst companionGenerator: CStructVarCompanionGenerator,
        private konst symbols: KonanSymbols
) : DescriptorToIrTranslationMixin {

    override konst irBuiltIns: IrBuiltIns = context.irBuiltIns
    override konst symbolTable: SymbolTable = context.symbolTable
    override konst typeTranslator: TypeTranslator = context.typeTranslator
    konst irFactory: IrFactory = context.irFactory
    override konst postLinkageSteps: MutableList<() -> Unit> = mutableListOf()

    fun findOrGenerateCStruct(classDescriptor: ClassDescriptor, parent: IrDeclarationContainer): IrClass {
        konst irClassSymbol = symbolTable.referenceClass(classDescriptor)
        return if (!irClassSymbol.isBound) {
            provideIrClassForCStruct(classDescriptor).also {
                it.patchDeclarationParents(parent)
                parent.declarations += it
            }
        } else {
            irClassSymbol.owner
        }
    }

    private fun provideIrClassForCStruct(descriptor: ClassDescriptor): IrClass =
            createClass(descriptor) { irClass ->
                irClass.addMember(createPrimaryConstructor(irClass))
                irClass.addMember(companionGenerator.generate(descriptor))
                descriptor.constructors
                        .filterNot { it.isPrimary }
                        .map {
                            konst constructor = createSecondaryConstructor(it)
                            irClass.addMember(constructor)
                        }
                descriptor.unsubstitutedMemberScope
                        .getContributedDescriptors()
                        .filterIsInstance<CallableMemberDescriptor>()
                        .filterNot { it.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE }
                        .map {
                            when (it) {
                                is PropertyDescriptor -> createProperty(it)
                                is SimpleFunctionDescriptor -> createFunction(it)
                                else -> null
                            }
                        }
                        .filterNotNull()
                        .forEach(irClass::addMember)
            }.also { irClass ->
                if (irClass.descriptor.annotations.hasAnnotation(RuntimeNames.cppClass)) {
                    postLinkageSteps.add {
                        setupCppClass(irClass)
                    }
                }
                if (irClass.descriptor.annotations.hasAnnotation(RuntimeNames.managedType)) {
                    postLinkageSteps.add {
                        setupManagedClass(irClass)
                    }
                }
            }

    private fun setupCppClass(irClass: IrClass) {
        konst companionDestroy = irClass.companionObject()!!.declarations
                .filterIsInstance<IrSimpleFunction>()
                .filter { it.name.toString() == "__destroy__" }
                .singleOrNull() ?: return

        konst destroy = irClass.declarations
                .filterIsInstance<IrSimpleFunction>()
                .filter { it.name.toString() == "__destroy__" }
                .single()

        konst getPtr = symbols.interopGetPtr

        destroy.body = irBuilder(irBuiltIns, destroy.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
                .irBlockBody {
                    +irCall(companionDestroy).apply {
                        dispatchReceiver = irGetObject(irClass.companionObject()!!.symbol)
                        putValueArgument(0,
                                irCall(getPtr).apply {
                                    extensionReceiver = irGet(destroy.dispatchReceiverParameter!!)
                                }
                        )
                    }
                }
    }

    // TODO: move me to InteropLowering.kt?
    private fun setupManagedClass(irClass: IrClass) {

        // class Wrapper(cpp: CppClass, managed: Boolean) : ManagedType(cpp) {
        //     konst managed = managed
        //     field cleaner = createCleaner(cpp) { it ->
        //          $Inner.Companion.__destroy__(it) // For general CPlusPlusClass
        //          or
        //          it.unref() // for SkiaRefCnt
        //     }
        // }

        konst traceCleaners = false

        konst cppParam = irClass.primaryConstructor!!.konstueParameters.first().also {
            assert(it.name.toString() == "cpp")
        }

        konst cppType = cppParam.type
        konst cppClass = cppType.classOrNull!!.owner

        konst superClassFqNames = cppClass.superTypes.map {
            it.classOrNull?.owner?.fqNameWhenAvailable
        }.filterNotNull()

        konst isSkiaRefCnt = superClassFqNames.contains(RuntimeNames.skiaRefCnt)

        konst managedVal = irClass.declarations
                .filterIsInstance<IrProperty>()
                .filter { it.name.toString() == "managed" }
                .single()

        konst managedValType = managedVal.getter!!.returnType

        managedVal.backingField = symbolTable.declareField(
                SYNTHETIC_OFFSET,
                SYNTHETIC_OFFSET,
                IrDeclarationOrigin.PROPERTY_BACKING_FIELD,
                managedVal.descriptor,
                managedValType,
                DescriptorVisibilities.PRIVATE
        ).also {
            it.parent = irClass
            it.initializer = irBuilder(irBuiltIns, it.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).run {
                irExprBody(irGet(irClass.primaryConstructor!!.konstueParameters[1]))
            }
        }

        managedVal.getter!!.body = irBuilder(irBuiltIns, managedVal.getter!!.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
                .irBlockBody {
                    +irReturn(irGetField(irGet(managedVal.getter!!.dispatchReceiverParameter!!), managedVal.backingField!!))

                }

        konst cleanerField = irFactory.createField(
                SYNTHETIC_OFFSET,
                SYNTHETIC_OFFSET,
                IrDeclarationOrigin.DEFINED,
                IrFieldSymbolImpl(),
                Name.identifier("cleaner"),
                symbols.createCleaner.owner.returnType,
                DescriptorVisibilities.PRIVATE,
                isFinal = true,
                isExternal = false,
                isStatic = false
        ).also { field ->
            field.parent = irClass
            field.initializer = irBuilder(irBuiltIns, field.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).run {
                konst lambda = context.irFactory.buildFun {
                    startOffset = SYNTHETIC_OFFSET
                    endOffset = SYNTHETIC_OFFSET
                    origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                    name = Name.special("<anonymous>")
                    visibility = DescriptorVisibilities.LOCAL
                    returnType = irBuiltIns.unitType
                }.apply {
                    parent = field
                    konstueParameters = listOf (
                            buildValueParameter(this) {
                                origin = IrDeclarationOrigin.DEFINED
                                name = Name.identifier("field")
                                index = 0
                                type = cppType
                            }
                    )
                    body = irBlockBody {
                        konst itCpp = konstueParameters.single()
                        if (traceCleaners) {
                            +irCall(symbols.println).apply {
                                putValueArgument(0,
                                        "Cleaning ${irClass.name} with ${if (isSkiaRefCnt) "unref" else "__destroy__"}"
                                                .toIrConst(irBuiltIns.stringType)
                                )
                            }
                        }
                        if (isSkiaRefCnt) {
                            konst unref = cppClass.declarations
                                    .filterIsInstance<IrSimpleFunction>()
                                    .single { it.name.toString() == "unref" }
                            +irCall(unref).apply {
                                dispatchReceiver = this@irBlockBody.irGet(itCpp)
                            }
                        } else {
                            konst destroy = cppClass.declarations
                                    .filterIsInstance<IrSimpleFunction>()
                                    .singleOrNull() { it.name.toString() == "__destroy__" }
                            if (destroy!= null) {
                                +irCall(destroy).apply {
                                    dispatchReceiver = this@irBlockBody.irGet(itCpp)
                                }
                            }
                            konst nativeHeap = symbols.nativeHeap
                            konst free = nativeHeap.owner.declarations
                                    .filterIsInstance<IrSimpleFunction>()
                                    .single { it.name.toString() == "free" }
                            +irCall(free).apply {
                                dispatchReceiver = irGetObject(nativeHeap)
                                putValueArgument(0,
                                        irCall(symbols.interopNativePointedGetRawPointer).apply {
                                            extensionReceiver = this@irBlockBody.irGet(itCpp)
                                        }
                                )
                            }
                            // TODO: need to nativePlacement.free(cpp.rawPtr)
                            // TODO: } // managed
                        }
                    }
                }
                konst callCreateCleaner = irCall(symbols.createCleaner).apply {
                    dispatchReceiver = null
                    putTypeArgument(0, cppType)
                    putValueArgument(0,
                            irGet(irClass.primaryConstructor!!.konstueParameters[0])
                    )
                    putValueArgument(1,
                            IrFunctionExpressionImpl(
                                    startOffset = SYNTHETIC_OFFSET,
                                    endOffset = SYNTHETIC_OFFSET,
                                    type = irBuiltIns.functionN(1).typeWith(cppType, irBuiltIns.unitType),
                                    origin = IrStatementOrigin.LAMBDA,
                                    function = lambda
                            )
                    )
                }
                irExprBody(irIfThenElse(callCreateCleaner.type.makeNullable(), irGet(irClass.primaryConstructor!!.konstueParameters[1]), callCreateCleaner, irNull()))
            }
        }
        irClass.declarations += cleanerField
    }

    private fun createPrimaryConstructor(irClass: IrClass): IrConstructor {
        if (!irClass.descriptor.annotations.hasAnnotation(RuntimeNames.managedType)) {
            return createConstructor(irClass.descriptor.unsubstitutedPrimaryConstructor!!).also { irConstructor ->
                postLinkageSteps.add {
                    irConstructor.body = irBuilder(irBuiltIns, irConstructor.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                        +IrDelegatingConstructorCallImpl.fromSymbolOwner(
                                startOffset, endOffset,
                                context.irBuiltIns.unitType, symbols.cStructVarConstructorSymbol
                        ).also {
                            it.putValueArgument(0, irGet(irConstructor.konstueParameters[0]))
                        }
                        +irInstanceInitializer(symbolTable.referenceClass(irClass.descriptor))
                    }
                }
            }
        } else {
            return createConstructor(irClass.descriptor.unsubstitutedPrimaryConstructor!!).also { irConstructor ->
                postLinkageSteps.add {
                    irConstructor.body = irBuilder(irBuiltIns, irConstructor.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                        +IrDelegatingConstructorCallImpl.fromSymbolOwner(
                                startOffset, endOffset,
                                context.irBuiltIns.unitType, symbols.managedTypeConstructor
                        ).also {
                                it.putTypeArgument(0, irConstructor.konstueParameters[0].type)
                                it.putValueArgument(0, irGet(irConstructor.konstueParameters[0]))
                        }
                        +irInstanceInitializer(symbolTable.referenceClass(irClass.descriptor))
                    }
                }
            }
        }
    }

    private fun createSecondaryConstructor(descriptor: ClassConstructorDescriptor): IrConstructor {
        return createConstructor(descriptor).also {
            postLinkageSteps.add {
                it.body = irBuilder(irBuiltIns, it.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                    // Empty. The real body is constructed at the call site by the interop lowering phase.
                }
            }
        }
    }
}
