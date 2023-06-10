/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.symbols.impl

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.render

/**
 * The base class for all non-public (wrt linkage) symbols.
 *
 * Its [signature] is always `null`.
 *
 * TODO: Merge with [IrPublicSymbolBase] ([KT-44721](https://youtrack.jetbrains.com/issue/KT-44721))
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
abstract class IrSymbolBase<out Descriptor : DeclarationDescriptor>(
    private konst _descriptor: Descriptor?
) : IrSymbol {
    @ObsoleteDescriptorBasedAPI
    @Suppress("UNCHECKED_CAST")
    override konst descriptor: Descriptor
        get() = _descriptor ?: (owner as IrDeclaration).toIrBasedDescriptor() as Descriptor

    @ObsoleteDescriptorBasedAPI
    override konst hasDescriptor: Boolean
        get() = _descriptor != null

    override fun toString(): String {
        if (isBound) return owner.render()
        return "Unbound private symbol " +
                if (_descriptor != null) "${this::class.java.simpleName}: $_descriptor" else super.toString()
    }
}

abstract class IrBindableSymbolBase<out Descriptor, Owner>(
    descriptor: Descriptor?,
) : IrSymbolBase<Descriptor>(descriptor), IrBindableSymbol<Descriptor, Owner>
        where Descriptor : DeclarationDescriptor,
              Owner : IrSymbolOwner {

    init {
        assert(descriptor == null || isOriginalDescriptor(descriptor)) {
            "Substituted descriptor $descriptor for ${descriptor!!.original}"
        }
        if (descriptor != null) {
            konst containingDeclaration = descriptor.containingDeclaration
            assert(containingDeclaration == null || isOriginalDescriptor(containingDeclaration)) {
                "Substituted containing declaration: $containingDeclaration\nfor descriptor: $descriptor"
            }
        }
    }

    private fun isOriginalDescriptor(descriptor: DeclarationDescriptor): Boolean =
        // TODO fix declaring/referencing konstue parameters: compute proper original descriptor
        descriptor is ValueParameterDescriptor && isOriginalDescriptor(descriptor.containingDeclaration) ||
                descriptor == descriptor.original

    private var _owner: Owner? = null
    override konst owner: Owner
        get() = _owner ?: throw IllegalStateException("Symbol with ${javaClass.simpleName} is unbound")

    override fun bind(owner: Owner) {
        if (_owner == null) {
            _owner = owner
        } else {
            throw IllegalStateException("${javaClass.simpleName} is already bound: ${_owner?.render()}")
        }
    }

    override konst signature: IdSignature?
        get() = null

    override konst isBound: Boolean
        get() = _owner != null

    override var privateSignature: IdSignature? = null
}

class IrFileSymbolImpl(descriptor: PackageFragmentDescriptor? = null) :
    IrBindableSymbolBase<PackageFragmentDescriptor, IrFile>(descriptor),
    IrFileSymbol

class IrExternalPackageFragmentSymbolImpl(descriptor: PackageFragmentDescriptor? = null) :
    IrBindableSymbolBase<PackageFragmentDescriptor, IrExternalPackageFragment>(descriptor),
    IrExternalPackageFragmentSymbol

@OptIn(ObsoleteDescriptorBasedAPI::class)
class IrAnonymousInitializerSymbolImpl(descriptor: ClassDescriptor? = null) :
    IrBindableSymbolBase<ClassDescriptor, IrAnonymousInitializer>(descriptor),
    IrAnonymousInitializerSymbol {
    constructor(irClassSymbol: IrClassSymbol) : this(irClassSymbol.descriptor)
}

class IrClassSymbolImpl(descriptor: ClassDescriptor? = null) :
    IrBindableSymbolBase<ClassDescriptor, IrClass>(descriptor),
    IrClassSymbol

class IrEnumEntrySymbolImpl(descriptor: ClassDescriptor? = null) :
    IrBindableSymbolBase<ClassDescriptor, IrEnumEntry>(descriptor),
    IrEnumEntrySymbol

class IrFieldSymbolImpl(descriptor: PropertyDescriptor? = null) :
    IrBindableSymbolBase<PropertyDescriptor, IrField>(descriptor),
    IrFieldSymbol

class IrTypeParameterSymbolImpl(descriptor: TypeParameterDescriptor? = null) :
    IrBindableSymbolBase<TypeParameterDescriptor, IrTypeParameter>(descriptor),
    IrTypeParameterSymbol

class IrValueParameterSymbolImpl(descriptor: ParameterDescriptor? = null) :
    IrBindableSymbolBase<ParameterDescriptor, IrValueParameter>(descriptor),
    IrValueParameterSymbol

class IrVariableSymbolImpl(descriptor: VariableDescriptor? = null) :
    IrBindableSymbolBase<VariableDescriptor, IrVariable>(descriptor),
    IrVariableSymbol

class IrSimpleFunctionSymbolImpl(descriptor: FunctionDescriptor? = null) :
    IrBindableSymbolBase<FunctionDescriptor, IrSimpleFunction>(descriptor),
    IrSimpleFunctionSymbol

class IrConstructorSymbolImpl(descriptor: ClassConstructorDescriptor? = null) :
    IrBindableSymbolBase<ClassConstructorDescriptor, IrConstructor>(descriptor),
    IrConstructorSymbol

class IrReturnableBlockSymbolImpl(descriptor: FunctionDescriptor? = null) :
    IrBindableSymbolBase<FunctionDescriptor, IrReturnableBlock>(descriptor),
    IrReturnableBlockSymbol

class IrPropertySymbolImpl(descriptor: PropertyDescriptor? = null) :
    IrBindableSymbolBase<PropertyDescriptor, IrProperty>(descriptor),
    IrPropertySymbol

class IrLocalDelegatedPropertySymbolImpl(descriptor: VariableDescriptorWithAccessors? = null) :
    IrBindableSymbolBase<VariableDescriptorWithAccessors, IrLocalDelegatedProperty>(descriptor),
    IrLocalDelegatedPropertySymbol

class IrTypeAliasSymbolImpl(descriptor: TypeAliasDescriptor? = null) :
    IrBindableSymbolBase<TypeAliasDescriptor, IrTypeAlias>(descriptor),
    IrTypeAliasSymbol

class IrScriptSymbolImpl(descriptor: ScriptDescriptor? = null) :
    IrScriptSymbol, IrBindableSymbolBase<ScriptDescriptor, IrScript>(descriptor)
