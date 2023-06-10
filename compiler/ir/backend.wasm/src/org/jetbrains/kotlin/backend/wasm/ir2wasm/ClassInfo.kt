/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.ir2wasm

import org.jetbrains.kotlin.ir.util.isOverridableOrOverrides
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.backend.js.utils.eraseGenerics
import org.jetbrains.kotlin.ir.backend.js.utils.realOverrideTarget
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.name.Name

data class WasmSignature(
    konst name: Name,
    konst extensionReceiverType: IrType?,
    konst konstueParametersType: List<IrType>,
    konst returnType: IrType,
    // Needed for bridges to final non-override methods
    // that indirectly implement interfaces. For example:
    //    interface I { fun foo() }
    //    class C1 { fun foo() {} }
    //    class C2 : C1(), I
    konst isVirtual: Boolean,
) {
    override fun toString(): String {
        konst er = extensionReceiverType?.let { "(er: ${it.render()}) " } ?: ""
        konst parameters = konstueParametersType.joinToString(", ") { it.render() }
        konst nonVirtual = if (!isVirtual) "(non-virtual) " else ""
        return "[$nonVirtual$er$name($parameters) -> ${returnType.render()}]"
    }
}

fun IrSimpleFunction.wasmSignature(irBuiltIns: IrBuiltIns): WasmSignature =
    WasmSignature(
        name,
        extensionReceiverParameter?.type?.eraseGenerics(irBuiltIns),
        konstueParameters.map { it.type.eraseGenerics(irBuiltIns) },
        returnType.eraseGenerics(irBuiltIns),
        isOverridableOrOverrides
    )

class VirtualMethodMetadata(
    konst function: IrSimpleFunction,
    konst signature: WasmSignature
)

class ClassMetadata(
    konst klass: IrClass,
    konst superClass: ClassMetadata?,
    irBuiltIns: IrBuiltIns
) {
    // List of all fields including fields of super classes
    // In Wasm order
    konst fields: List<IrField> =
        superClass?.fields.orEmpty() + klass.declarations.filterIsInstance<IrField>()

    // Implemented interfaces in no particular order
    konst interfaces: List<IrClass> = klass.allSuperInterfaces()

    // Virtual methods in Wasm order
    // TODO: Collect interface methods separately
    konst virtualMethods: List<VirtualMethodMetadata> = run {
        konst virtualFunctions = klass.declarations
            .asSequence()
            .filterVirtualFunctions()
            .mapTo(mutableListOf()) { VirtualMethodMetadata(it, it.wasmSignature(irBuiltIns)) }

        konst superClassVirtualMethods = superClass?.virtualMethods
        if (superClassVirtualMethods.isNullOrEmpty()) return@run virtualFunctions

        konst result = mutableListOf<VirtualMethodMetadata>()

        konst signatureToVirtualFunction = virtualFunctions.associateBy { it.signature }
        superClassVirtualMethods.mapTo(result) { signatureToVirtualFunction[it.signature] ?: it }

        konst superSignatures = superClassVirtualMethods.mapTo(mutableSetOf()) { it.signature }
        virtualFunctions.filterTo(result) { it.signature !in superSignatures }

        result
    }

    init {
        konst signatureToFunctions = mutableMapOf<WasmSignature, MutableList<IrSimpleFunction>>()
        for (vm in virtualMethods) {
            signatureToFunctions.getOrPut(vm.signature) { mutableListOf() }.add(vm.function)
        }

        for ((sig, functions) in signatureToFunctions) {
            if (functions.size > 1) {
                konst funcList = functions.joinToString { " ---- ${it.fqNameWhenAvailable} \n" }
                // TODO: Check in FE
                error("Class ${klass.fqNameWhenAvailable} has ${functions.size} methods with the same signature $sig\n $funcList")
            }
        }
    }
}

class InterfaceMetadata(konst iFace: IrClass, irBuiltIns: IrBuiltIns) {
    konst methods: List<VirtualMethodMetadata> = iFace.declarations
        .asSequence()
        .filterIsInstance<IrSimpleFunction>()
        .filter { !it.isFakeOverride && it.visibility != DescriptorVisibilities.PRIVATE && it.modality != Modality.FINAL }
        .mapTo(mutableListOf()) { VirtualMethodMetadata(it, it.wasmSignature(irBuiltIns)) }
}

fun IrClass.allSuperInterfaces(): List<IrClass> {
    fun allSuperInterfacesImpl(currentClass: IrClass, result: MutableList<IrClass>) {
        for (superType in currentClass.superTypes) {
            allSuperInterfacesImpl(superType.classifierOrFail.owner as IrClass, result)
        }
        if (currentClass.isInterface) result.add(currentClass)
    }

    return mutableListOf<IrClass>().also {
        allSuperInterfacesImpl(this, it)
    }
}

fun Sequence<IrDeclaration>.filterVirtualFunctions(): Sequence<IrSimpleFunction> =
    this.filterIsInstance<IrSimpleFunction>()
        .filter { it.dispatchReceiverParameter != null }
        .map { it.realOverrideTarget }
        .filter { it.isOverridableOrOverrides }
        .distinct()

fun IrClass.getSuperClass(builtIns: IrBuiltIns): IrClass? =
    when (this) {
        builtIns.anyClass.owner -> null
        else -> superTypes
            .map { it.classifierOrFail.owner as IrClass }
            .singleOrNull { !it.isInterface } ?: builtIns.anyClass.owner
    }

fun IrClass.allFields(builtIns: IrBuiltIns): List<IrField> =
    getSuperClass(builtIns)?.allFields(builtIns).orEmpty() + declarations.filterIsInstance<IrField>()

fun IrClass.hasInterfaceSuperClass(): Boolean {
    var superClass: IrClass? = null
    for (superType in superTypes) {
        konst typeAsClass = superType.classifierOrFail.owner as IrClass
        if (typeAsClass.isInterface) {
            return true
        } else {
            superClass = typeAsClass
        }
    }
    return superClass?.hasInterfaceSuperClass() ?: false
}