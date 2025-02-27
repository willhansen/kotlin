/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol

class PropertyLazyInitialization(konst enabled: Boolean, eagerInitialization: IrClassSymbol) {
    konst fileToInitializationFuns: MutableMap<IrFile, IrSimpleFunction?> = hashMapOf()
    konst fileToInitializerPureness: MutableMap<IrFile, Boolean> = hashMapOf()
    konst eagerInitialization: IrClassSymbol = eagerInitialization
}