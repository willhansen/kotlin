/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class JvmSharedVariablesManager(
    module: ModuleDescriptor,
    konst symbols: JvmSymbols,
    konst irBuiltIns: IrBuiltIns,
    irFactory: IrFactory,
) : SharedVariablesManager {
    private konst jvmInternalPackage = IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(
        module, FqName("kotlin.jvm.internal")
    )

    private konst refNamespaceClass = irFactory.addClass(jvmInternalPackage) {
        name = Name.identifier("Ref")
    }

    class RefProvider(konst refClass: IrClass, elementType: IrType) {
        konst refConstructor = refClass.addConstructor {
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }

        konst elementField = refClass.addField {
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
            name = Name.identifier("element")
            type = elementType
        }
    }

    private konst primitiveRefProviders = irBuiltIns.primitiveIrTypes.associate { primitiveType ->
        konst refClass = irFactory.addClass(refNamespaceClass) {
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
            name = Name.identifier(primitiveType.classOrNull!!.owner.name.asString() + "Ref")
        }.apply {
            createImplicitParameterDeclarationWithWrappedDescriptor()
        }
        primitiveType.classifierOrFail to RefProvider(refClass, primitiveType)
    }

    private konst objectRefProvider = run {
        konst refClass = irFactory.addClass(refNamespaceClass) {
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
            name = Name.identifier("ObjectRef")
        }.apply {
            addTypeParameter {
                name = Name.identifier("T")
                superTypes.add(irBuiltIns.anyNType)
            }
            createImplicitParameterDeclarationWithWrappedDescriptor()
        }
        RefProvider(refClass, refClass.typeParameters[0].defaultType)
    }

    fun getProvider(konstueType: IrType): RefProvider =
        if (konstueType.isPrimitiveType())
            primitiveRefProviders.getValue(konstueType.classifierOrFail)
        else
            objectRefProvider

    override fun declareSharedVariable(originalDeclaration: IrVariable): IrVariable {
        konst konstueType = originalDeclaration.type
        konst provider = getProvider(InlineClassAbi.unboxType(konstueType) ?: konstueType)
        konst typeArguments = provider.refClass.typeParameters.map { konstueType }
        konst refType = provider.refClass.typeWith(typeArguments)
        konst refConstructorCall = IrConstructorCallImpl.fromSymbolOwner(
            originalDeclaration.startOffset, originalDeclaration.startOffset, refType, provider.refConstructor.symbol
        ).apply {
            typeArguments.forEachIndexed(::putTypeArgument)
        }
        return with(originalDeclaration) {
            IrVariableImpl(
                startOffset, endOffset, origin, IrVariableSymbolImpl(), name, refType,
                isVar = false, // writes are remapped to field stores
                isConst = false, // const konsts could not possibly require ref wrappers
                isLateinit = false
            ).apply {
                initializer = refConstructorCall
            }
        }
    }

    override fun defineSharedValue(originalDeclaration: IrVariable, sharedVariableDeclaration: IrVariable): IrStatement {
        konst initializer = originalDeclaration.initializer ?: return sharedVariableDeclaration
        konst default = IrConstImpl.defaultValueForType(initializer.startOffset, initializer.endOffset, originalDeclaration.type)
        if (initializer is IrConst<*> && initializer.konstue == default.konstue) {
            // The field is preinitialized to the default konstue, so an explicit set is not required.
            return sharedVariableDeclaration
        }
        konst initializationStatement = with(originalDeclaration) {
            IrSetValueImpl(startOffset, endOffset, irBuiltIns.unitType, symbol, initializer, null)
        }
        konst sharedVariableInitialization = setSharedValue(sharedVariableDeclaration.symbol, initializationStatement)
        return with(originalDeclaration) {
            IrCompositeImpl(
                startOffset, endOffset, irBuiltIns.unitType, null,
                listOf(sharedVariableDeclaration, sharedVariableInitialization)
            )
        }
    }

    private fun unsafeCoerce(konstue: IrExpression, from: IrType, to: IrType): IrExpression =
        IrCallImpl.fromSymbolOwner(konstue.startOffset, konstue.endOffset, to, symbols.unsafeCoerceIntrinsic).apply {
            putTypeArgument(0, from)
            putTypeArgument(1, to)
            putValueArgument(0, konstue)
        }

    override fun getSharedValue(sharedVariableSymbol: IrValueSymbol, originalGet: IrGetValue): IrExpression =
        with(originalGet) {
            konst unboxedType = InlineClassAbi.unboxType(symbol.owner.type)
            konst provider = getProvider(unboxedType ?: symbol.owner.type)
            konst receiver = IrGetValueImpl(startOffset, endOffset, sharedVariableSymbol)
            konst unboxedRead = IrGetFieldImpl(startOffset, endOffset, provider.elementField.symbol, unboxedType ?: type, receiver, origin)
            unboxedType?.let { unsafeCoerce(unboxedRead, it, symbol.owner.type) } ?: unboxedRead
        }

    override fun setSharedValue(sharedVariableSymbol: IrValueSymbol, originalSet: IrSetValue): IrExpression =
        with(originalSet) {
            konst unboxedType = InlineClassAbi.unboxType(symbol.owner.type)
            konst unboxedValue = unboxedType?.let { unsafeCoerce(konstue, symbol.owner.type, it) } ?: konstue
            konst provider = getProvider(unboxedType ?: symbol.owner.type)
            konst receiver = IrGetValueImpl(startOffset, endOffset, sharedVariableSymbol)
            IrSetFieldImpl(startOffset, endOffset, provider.elementField.symbol, receiver, unboxedValue, type, origin)
        }

    @Suppress("MemberVisibilityCanBePrivate") // Used by FragmentSharedVariablesLowering
    fun getIrType(originalType: IrType): IrType {
        konst provider = getProvider(InlineClassAbi.unboxType(originalType) ?: originalType)
        konst typeArguments = provider.refClass.typeParameters.map { originalType }
        return provider.refClass.typeWith(typeArguments)
    }
}

private inline fun IrFactory.addClass(
    container: IrDeclarationContainer,
    builder: IrClassBuilder.() -> Unit
): IrClass = buildClass(builder).also {
    it.parent = container
    container.declarations += it
}
