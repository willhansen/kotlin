/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import org.jetbrains.kotlin.backend.common.DefaultDelegateFactory
import org.jetbrains.kotlin.backend.common.DefaultMapping
import org.jetbrains.kotlin.ir.backend.js.utils.MutableReference
import org.jetbrains.kotlin.ir.declarations.*

class JsMapping : DefaultMapping() {
    konst esClassWhichNeedBoxParameters = DefaultDelegateFactory.newDeclarationToValueMapping<IrClass, Boolean>()
    konst esClassToPossibilityForOptimization = DefaultDelegateFactory.newDeclarationToValueMapping<IrClass, MutableReference<Boolean>>()

    konst outerThisFieldSymbols = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrField>()
    konst innerClassConstructors = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrConstructor, IrConstructor>()
    konst originalInnerClassPrimaryConstructorByClass = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrConstructor>()
    konst secondaryConstructorToDelegate = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrConstructor, IrSimpleFunction>()
    konst secondaryConstructorToFactory = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrConstructor, IrSimpleFunction>()
    konst objectToGetInstanceFunction = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrSimpleFunction>()
    konst objectToInstanceField = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrField>()
    konst classToSyntheticPrimaryConstructor = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrConstructor>()
    konst privateMemberToCorrespondingStatic = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrFunction, IrSimpleFunction>()

    konst enumEntryToGetInstanceFun = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrEnumEntry, IrSimpleFunction>()
    konst enumEntryToInstanceField = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrEnumEntry, IrField>()
    konst enumConstructorToNewConstructor = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrConstructor, IrConstructor>()
    konst enumClassToCorrespondingEnumEntry = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrEnumEntry>()
    konst enumConstructorOldToNewValueParameters = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrValueDeclaration, IrValueParameter>()
    konst enumEntryToCorrespondingField = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrEnumEntry, IrField>()
    konst fieldToEnumEntry = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrField, IrEnumEntry>()
    konst enumClassToInitEntryInstancesFun = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrSimpleFunction>()

    konst suspendArityStore = DefaultDelegateFactory.newDeclarationToDeclarationCollectionMapping<IrClass, Collection<IrSimpleFunction>>()

    konst objectsWithPureInitialization = DefaultDelegateFactory.newDeclarationToValueMapping<IrClass, Boolean>()

    konst inlineFunctionsBeforeInlining = DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrFunction, IrFunction>()

    // Wasm mappings
    konst wasmJsInteropFunctionToWrapper =
        DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrSimpleFunction, IrSimpleFunction>()

    konst wasmNestedExternalToNewTopLevelFunction =
        DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrFunction, IrSimpleFunction>()

    konst wasmExternalObjectToGetInstanceFunction =
        DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrSimpleFunction>()

    konst wasmExternalClassToInstanceCheck =
        DefaultDelegateFactory.newDeclarationToDeclarationMapping<IrClass, IrSimpleFunction>()
}
