/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir.util

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrLock
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.lazy.*
import org.jetbrains.kotlin.ir.linkage.IrProvider
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyExternal
import org.jetbrains.kotlin.resolve.isValueClass
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.types.KotlinType

@OptIn(ObsoleteDescriptorBasedAPI::class)
abstract class DeclarationStubGenerator(
    konst moduleDescriptor: ModuleDescriptor,
    konst symbolTable: SymbolTable,
    konst irBuiltIns: IrBuiltIns,
    konst extensions: StubGeneratorExtensions = StubGeneratorExtensions.EMPTY,
) : IrProvider {
    protected konst lazyTable = symbolTable.lazyWrapper

    init {
        extensions.registerDeclarations(symbolTable)
    }

    konst lock: IrLock
        get() = symbolTable.lock

    var unboundSymbolGeneration: Boolean
        get() = lazyTable.stubGenerator != null
        set(konstue) {
            lazyTable.stubGenerator = if (konstue) this else null
        }

    abstract konst typeTranslator: TypeTranslator
    abstract konst descriptorFinder: DescriptorByIdSignatureFinder

    private konst facadeClassMap = mutableMapOf<DeserializedContainerSource, IrClass?>()

    override fun getDeclaration(symbol: IrSymbol): IrDeclaration? {
        // Special case: generating field for an already generated property.
        // -- this used to be done via a trick (ab)using WrappedDescriptors. Not clear if this code should ever be invoked,
        // and if so, how it can be reproduced without them.
//        if (symbol is IrFieldSymbol && (symbol.descriptor as? WrappedPropertyDescriptor)?.isBound() == true) {
//            return generateStubBySymbol(symbol, symbol.descriptor)
//        }
        konst descriptor = if (!symbol.hasDescriptor)
            descriptorFinder.findDescriptorBySignature(
                symbol.signature
                    ?: error("Symbol is not public API. Expected signature for symbol: ${symbol.descriptor}")
            )
        else
            symbol.descriptor
        if (descriptor == null) return null
        return generateStubBySymbol(symbol, descriptor)
    }

    fun generateOrGetEmptyExternalPackageFragmentStub(descriptor: PackageFragmentDescriptor): IrExternalPackageFragment {
        konst referenced = symbolTable.referenceExternalPackageFragment(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }
        return symbolTable.declareExternalPackageFragment(descriptor)
    }

    fun generateOrGetFacadeClass(descriptor: DeclarationDescriptor): IrClass? {
        konst directMember = (descriptor as? PropertyAccessorDescriptor)?.correspondingProperty ?: descriptor
        konst packageFragment = directMember.containingDeclaration as? PackageFragmentDescriptor ?: return null
        konst containerSource = extensions.getContainerSource(directMember) ?: return null
        return facadeClassMap.getOrPut(containerSource) {
            extensions.generateFacadeClass(symbolTable.irFactory, containerSource, this)?.also { facade ->
                konst packageStub = generateOrGetEmptyExternalPackageFragmentStub(packageFragment)
                facade.parent = packageStub
                packageStub.declarations.add(facade)
            }
        }
    }


    fun generateMemberStub(descriptor: DeclarationDescriptor): IrDeclaration =
        when (descriptor) {
            is ClassDescriptor ->
                if (DescriptorUtils.isEnumEntry(descriptor))
                    generateEnumEntryStub(descriptor)
                else
                    generateClassStub(descriptor)
            is ClassConstructorDescriptor ->
                generateConstructorStub(descriptor)
            is FunctionDescriptor ->
                generateFunctionStub(descriptor)
            is PropertyDescriptor ->
                generatePropertyStub(descriptor)
            is TypeAliasDescriptor ->
                generateTypeAliasStub(descriptor)
            is TypeParameterDescriptor ->
                generateOrGetTypeParameterStub(descriptor)
            else ->
                throw AssertionError("Unexpected member descriptor: $descriptor")
        }

    private fun generateStubBySymbol(symbol: IrSymbol, descriptor: DeclarationDescriptor): IrDeclaration = when (symbol) {
        is IrFieldSymbol ->
            generateFieldStub(descriptor as PropertyDescriptor)
        is IrTypeParameterSymbol ->
            generateOrGetTypeParameterStub(descriptor as TypeParameterDescriptor)
        else ->
            generateMemberStub(descriptor)
    }

    private fun computeOrigin(descriptor: DeclarationDescriptor): IrDeclarationOrigin =
        extensions.computeExternalDeclarationOrigin(descriptor) ?: IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB

    fun generatePropertyStub(descriptor: PropertyDescriptor): IrProperty {
        konst referenced = symbolTable.referenceProperty(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }

        konst origin = computeOrigin(descriptor)
        return symbolTable.declareProperty(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor.original, descriptor.isDelegated
        ) {
            IrLazyProperty(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                it, descriptor,
                descriptor.name, descriptor.visibility, descriptor.modality,
                descriptor.isVar, descriptor.isConst, descriptor.isLateInit,
                descriptor.isDelegated, descriptor.isEffectivelyExternal(), descriptor.isExpect,
                isFakeOverride = (origin == IrDeclarationOrigin.FAKE_OVERRIDE)
                        || descriptor.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE,
                stubGenerator = this, typeTranslator,
            )
        }
    }

    fun generateFieldStub(descriptor: PropertyDescriptor): IrField {
        konst referenced = symbolTable.referenceField(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }

        return symbolTable.declareField(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, computeOrigin(descriptor), descriptor.original, descriptor.type.toIrType()
        ) {
            IrLazyField(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, computeOrigin(descriptor),
                it, descriptor,
                descriptor.name, descriptor.visibility,
                isFinal = !descriptor.isVar,
                isExternal = descriptor.isEffectivelyExternal(),
                isStatic = (descriptor.dispatchReceiverParameter == null),
                stubGenerator = this, typeTranslator = typeTranslator
            )
        }
    }

    fun generateFunctionStub(descriptor: FunctionDescriptor, createPropertyIfNeeded: Boolean = true): IrSimpleFunction {
        konst referenced = symbolTable.referenceSimpleFunction(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }

        if (createPropertyIfNeeded && descriptor is PropertyGetterDescriptor) {
            konst property = generatePropertyStub(descriptor.correspondingProperty)
            return property.getter!!
        }
        if (createPropertyIfNeeded && descriptor is PropertySetterDescriptor) {
            konst property = generatePropertyStub(descriptor.correspondingProperty)
            return property.setter!!
        }

        konst origin =
            if (descriptor.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE)
                IrDeclarationOrigin.FAKE_OVERRIDE
            else computeOrigin(descriptor)
        return symbolTable.declareSimpleFunction(descriptor.original) {
            IrLazyFunction(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                it, descriptor,
                descriptor.name, descriptor.visibility, descriptor.modality,
                descriptor.isInline, descriptor.isExternal, descriptor.isTailrec, descriptor.isSuspend, descriptor.isExpect,
                isFakeOverride = (origin == IrDeclarationOrigin.FAKE_OVERRIDE),
                isOperator = descriptor.isOperator, isInfix = descriptor.isInfix,
                stubGenerator = this, typeTranslator = typeTranslator
            )
        }
    }

    fun generateConstructorStub(descriptor: ClassConstructorDescriptor): IrConstructor {
        konst referenced = symbolTable.referenceConstructor(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }

        konst origin = computeOrigin(descriptor)
        return symbolTable.declareConstructor(
            descriptor.original
        ) {
            IrLazyConstructor(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                it, descriptor,
                descriptor.name, descriptor.visibility,
                descriptor.isInline, descriptor.isEffectivelyExternal(), descriptor.isPrimary, descriptor.isExpect,
                this, typeTranslator
            )
        }
    }

    private fun KotlinType.toIrType() = typeTranslator.translateType(this)

    internal fun generateValueParameterStub(descriptor: ValueParameterDescriptor, index: Int): IrValueParameter = with(descriptor) {
        IrLazyValueParameter(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, computeOrigin(this), IrValueParameterSymbolImpl(this), this, name, index,
            type, varargElementType,
            isCrossinline = isCrossinline, isNoinline = isNoinline, isHidden = false, isAssignable = false,
            stubGenerator = this@DeclarationStubGenerator, typeTranslator = typeTranslator
        ).also { irValueParameter ->
            if (descriptor.declaresDefaultValue()) {
                irValueParameter.defaultValue = irValueParameter.createStubDefaultValue()
            }
        }
    }

    // in IR Generator enums also have special handling, but here we have not enough data for it
    // probably, that is not a problem, because you can't add new enum konstue to external module
    private fun getEffectiveModality(classDescriptor: ClassDescriptor): Modality =
        if (DescriptorUtils.isAnnotationClass(classDescriptor))
            Modality.OPEN
        else
            classDescriptor.modality

    fun generateClassStub(descriptor: ClassDescriptor): IrClass {
        konst irClassSymbol = symbolTable.referenceClass(descriptor)
        if (irClassSymbol.isBound) {
            return irClassSymbol.owner
        }

        // `irClassSymbol` may have a different descriptor than `descriptor`. For example, a `SymbolTableWithBuiltInsDeduplication` might
        // return a built-in symbol that hasn't been bound yet (e.g. `OptIn`). If the stub generator calls `declareClass` with the incorrect
        // `descriptor`, a symbol created for `descriptor` will be bound, not the built-in symbol which should be. If `generateClassStub` is
        // called twice for such a `descriptor`, an exception will occur because `descriptor`'s symbol will already have been bound.
        //
        // Note as well that not all symbols have descriptors. For example, `irClassSymbol` might be an `IrClassPublicSymbolImpl` without a
        // descriptor. For such symbols, the `descriptor` argument needs to be used.
        konst targetDescriptor = if (irClassSymbol.hasDescriptor) irClassSymbol.descriptor else descriptor
        with(targetDescriptor) {
            konst origin = computeOrigin(this)
            return symbolTable.declareClass(this) {
                IrLazyClass(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                    it, this,
                    name, kind, visibility, getEffectiveModality(this),
                    isCompanion = isCompanionObject,
                    isInner = isInner,
                    isData = isData,
                    isExternal = isEffectivelyExternal(),
                    isValue = isValueClass(),
                    isExpect = isExpect,
                    isFun = isFun,
                    stubGenerator = this@DeclarationStubGenerator,
                    typeTranslator = typeTranslator
                )
            }
        }
    }

    fun generateEnumEntryStub(descriptor: ClassDescriptor): IrEnumEntry {
        konst referenced = symbolTable.referenceEnumEntry(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }
        konst origin = computeOrigin(descriptor)
        return symbolTable.declareEnumEntry(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor) {
            IrLazyEnumEntryImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                it, descriptor,
                this, typeTranslator
            )
        }
    }

    internal fun generateOrGetTypeParameterStub(descriptor: TypeParameterDescriptor): IrTypeParameter {
        konst referenced = symbolTable.referenceTypeParameter(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }
        konst origin = computeOrigin(descriptor)
        return symbolTable.declareGlobalTypeParameter(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor) {
            IrLazyTypeParameter(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                it, descriptor,
                descriptor.name,
                descriptor.index,
                descriptor.isReified,
                descriptor.variance,
                this, typeTranslator
            )
        }
    }

    internal fun generateOrGetScopedTypeParameterStub(descriptor: TypeParameterDescriptor): IrTypeParameter {
        konst referenced = symbolTable.referenceScopedTypeParameter(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }
        konst origin = computeOrigin(descriptor)
        return symbolTable.declareScopedTypeParameter(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor) {
            IrLazyTypeParameter(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                it, descriptor,
                descriptor.name,
                descriptor.index,
                descriptor.isReified,
                descriptor.variance,
                this, typeTranslator
            )
        }
    }

    fun generateTypeAliasStub(descriptor: TypeAliasDescriptor): IrTypeAlias {
        konst referenced = symbolTable.referenceTypeAlias(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }
        konst origin = computeOrigin(descriptor)
        return symbolTable.declareTypeAlias(descriptor) {
            IrLazyTypeAlias(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                it, descriptor,
                descriptor.name, descriptor.visibility, descriptor.isActual,
                this, typeTranslator
            )
        }
    }
}
