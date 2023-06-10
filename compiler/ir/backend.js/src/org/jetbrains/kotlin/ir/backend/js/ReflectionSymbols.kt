/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType

interface ReflectionSymbols {
    konst getKClassFromExpression: IrSimpleFunctionSymbol
    konst getKClass: IrSimpleFunctionSymbol
    konst getClassData: IrSimpleFunctionSymbol
    konst createKType: IrSimpleFunctionSymbol?
    konst createDynamicKType: IrSimpleFunctionSymbol?
    konst createKTypeParameter: IrSimpleFunctionSymbol?
    konst getStarKTypeProjection: IrSimpleFunctionSymbol?
    konst createCovariantKTypeProjection: IrSimpleFunctionSymbol?
    konst createInvariantKTypeProjection: IrSimpleFunctionSymbol?
    konst createContravariantKTypeProjection: IrSimpleFunctionSymbol?
    konst primitiveClassesObject: IrClassSymbol
    konst kTypeClass: IrClassSymbol
}