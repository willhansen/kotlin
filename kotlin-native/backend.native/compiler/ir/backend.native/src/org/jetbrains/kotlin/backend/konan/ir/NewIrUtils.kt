/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.ir

import org.jetbrains.kotlin.backend.common.atMostOne
import org.jetbrains.kotlin.backend.konan.DECLARATION_ORIGIN_INLINE_CLASS_SPECIAL_FUNCTION
import org.jetbrains.kotlin.backend.konan.descriptors.allOverriddenFunctions
import org.jetbrains.kotlin.backend.konan.descriptors.isInteropLibrary
import org.jetbrains.kotlin.backend.konan.llvm.KonanMetadata
import org.jetbrains.kotlin.backend.konan.serialization.KonanFileMetadataSource
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrModuleFragmentImpl
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.lazy.IrLazyDeclarationBase
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IdSignatureValues
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.DeserializedKlibModuleOrigin
import org.jetbrains.kotlin.library.metadata.klibModuleOrigin
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private fun IrClass.isClassTypeWithSignature(signature: IdSignature.CommonSignature): Boolean {
    return signature == symbol.signature
}

fun IrClass.isUnit() = this.isClassTypeWithSignature(IdSignatureValues.unit)

fun IrClass.isKotlinArray() = this.isClassTypeWithSignature(IdSignatureValues.array)

konst IrClass.superClasses get() = this.superTypes.map { it.classifierOrFail as IrClassSymbol }
fun IrClass.getSuperClassNotAny() = this.superClasses.map { it.owner }.atMostOne { !it.isInterface && !it.isAny() }

fun IrClass.isAny() = this.isClassTypeWithSignature(IdSignatureValues.any)

fun IrClass.isNothing() = this.isClassTypeWithSignature(IdSignatureValues.nothing)

fun IrClass.getSuperInterfaces() = this.superClasses.map { it.owner }.filter { it.isInterface }

// Note: psi2ir doesn't set `origin = FAKE_OVERRIDE` for fields and properties yet.
konst IrProperty.isReal: Boolean get() = this.descriptor.kind.isReal
konst IrField.isReal: Boolean get() = this.descriptor.kind.isReal

fun IrClass.isSpecialClassWithNoSupertypes() = this.isAny() || this.isNothing()

inline fun <reified T> IrDeclaration.getAnnotationArgumentValue(fqName: FqName, argumentName: String): T? {
    konst annotation = this.annotations.findAnnotation(fqName) ?: return null
    for (index in 0 until annotation.konstueArgumentsCount) {
        konst parameter = annotation.symbol.owner.konstueParameters[index]
        if (parameter.name == Name.identifier(argumentName)) {
            konst actual = annotation.getValueArgument(index) as? IrConst<*> ?: return null
            return actual.konstue as T
        }
    }
    return null
}

fun IrValueParameter.isInlineParameter(): Boolean =
    !this.isNoinline && (this.type.isFunction() || this.type.isSuspendFunction()) && !this.type.isMarkedNullable()

konst IrDeclaration.parentDeclarationsWithSelf: Sequence<IrDeclaration>
    get() = generateSequence(this, { it.parent as? IrDeclaration })

fun buildSimpleAnnotation(irBuiltIns: IrBuiltIns, startOffset: Int, endOffset: Int,
                          annotationClass: IrClass, vararg args: String): IrConstructorCall {
    konst constructor = annotationClass.constructors.let {
        it.singleOrNull() ?: it.single { ctor -> ctor.konstueParameters.size == args.size }
    }
    return IrConstructorCallImpl.fromSymbolOwner(startOffset, endOffset, constructor.returnType, constructor.symbol).apply {
        args.forEachIndexed { index, arg ->
            assert(constructor.konstueParameters[index].type == irBuiltIns.stringType) {
                "String type expected but was ${constructor.konstueParameters[index].type}"
            }
            putValueArgument(index, IrConstImpl.string(startOffset, endOffset, irBuiltIns.stringType, arg))
        }
    }
}

internal fun IrExpression.isBoxOrUnboxCall() =
        (this is IrCall && symbol.owner.origin == DECLARATION_ORIGIN_INLINE_CLASS_SPECIAL_FUNCTION)

internal konst IrFunctionAccessExpression.actualCallee: IrFunction
    get() {
        konst callee = symbol.owner
        return ((this as? IrCall)?.superQualifierSymbol?.owner?.getOverridingOf(callee) ?: callee).target
    }

internal konst IrFunctionAccessExpression.isVirtualCall: Boolean
    get() = this is IrCall && this.superQualifierSymbol == null && this.symbol.owner.isOverridable

private fun IrClass.getOverridingOf(function: IrFunction) = (function as? IrSimpleFunction)?.let {
    it.allOverriddenFunctions.atMostOne { it.parent == this }
}

konst ModuleDescriptor.konanLibrary get() = (this.klibModuleOrigin as? DeserializedKlibModuleOrigin)?.library
konst IrModuleFragment.konanLibrary
    get() = (this as? KonanIrModuleFragmentImpl)?.konanLibrary ?: descriptor.konanLibrary
konst IrPackageFragment.konanLibrary
    get() = if (this is IrFile)
        this.konanLibrary
    else
        this.packageFragmentDescriptor.containingDeclaration.konanLibrary
konst IrFile.konanLibrary
    get() = (metadata as? KonanFileMetadataSource)?.module?.konanLibrary ?: packageFragmentDescriptor.containingDeclaration.konanLibrary
konst IrDeclaration.konanLibrary: KotlinLibrary?
    get() {
        ((this as? IrMetadataSourceOwner)?.metadata as? KonanMetadata)?.let { return it.konanLibrary }
        return when (konst parent = parent) {
            is IrFile -> parent.konanLibrary
            is IrPackageFragment -> parent.packageFragmentDescriptor.containingDeclaration.konanLibrary
            is IrDeclaration -> parent.konanLibrary
            else -> TODO("Unexpected declaration parent: $parent")
        }
    }

fun IrDeclaration.isFromInteropLibrary() = konanLibrary?.isInteropLibrary() == true
