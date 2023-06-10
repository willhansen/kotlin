/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.signaturer.FirBasedSignatureComposer
import org.jetbrains.kotlin.fir.signaturer.FirMangler
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.util.IdSignatureComposer
import org.jetbrains.kotlin.ir.util.SymbolTable
import java.util.concurrent.ConcurrentHashMap

class Fir2IrCommonMemberStorage(
    signatureComposer: IdSignatureComposer,
    firMangler: FirMangler
) {
    konst firSignatureComposer = FirBasedSignatureComposer(firMangler)

    konst symbolTable = SymbolTable(
        signaturer = WrappedDescriptorSignatureComposer(signatureComposer, firSignatureComposer),
        irFactory = IrFactoryImpl
    )

    konst classCache: MutableMap<FirRegularClass, IrClass> = mutableMapOf()

    konst typeParameterCache: MutableMap<FirTypeParameter, IrTypeParameter> = mutableMapOf()

    konst enumEntryCache: MutableMap<FirEnumEntry, IrEnumEntry> = mutableMapOf()

    konst localClassCache: MutableMap<FirClass, IrClass> = mutableMapOf()

    konst functionCache: ConcurrentHashMap<FirFunction, IrSimpleFunction> = ConcurrentHashMap()

    konst constructorCache: ConcurrentHashMap<FirConstructor, IrConstructor> = ConcurrentHashMap()

    konst propertyCache: ConcurrentHashMap<FirProperty, IrProperty> = ConcurrentHashMap()

    konst fakeOverridesInClass: MutableMap<IrClass, MutableMap<Fir2IrDeclarationStorage.FakeOverrideKey, FirCallableDeclaration>> = mutableMapOf()
}
