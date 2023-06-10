/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator

import org.jetbrains.kotlin.ir.generator.Packages.declarations
import org.jetbrains.kotlin.ir.generator.Packages.exprs
import org.jetbrains.kotlin.ir.generator.Packages.symbols
import org.jetbrains.kotlin.ir.generator.Packages.tree
import org.jetbrains.kotlin.ir.generator.Packages.types
import org.jetbrains.kotlin.ir.generator.Packages.visitors
import org.jetbrains.kotlin.ir.generator.util.TypeKind
import org.jetbrains.kotlin.ir.generator.util.type

object Packages {
    const konst tree = "org.jetbrains.kotlin.ir"
    const konst exprs = "org.jetbrains.kotlin.ir.expressions"
    const konst symbols = "org.jetbrains.kotlin.ir.symbols"
    const konst declarations = "org.jetbrains.kotlin.ir.declarations"
    const konst types = "org.jetbrains.kotlin.ir.types"
    const konst visitors = "org.jetbrains.kotlin.ir.visitors"
    const konst descriptors = "org.jetbrains.kotlin.descriptors"
}

konst elementBaseType = type(tree, "IrElementBase", TypeKind.Class)
konst statementOriginType = type(exprs, "IrStatementOrigin")
konst elementVisitorType = type(visitors, "IrElementVisitor")
konst elementTransformerType = type(visitors, "IrElementTransformer")
konst mutableAnnotationContainerType = type(declarations, "IrMutableAnnotationContainer")
konst irTypeType = type(types, "IrType")

konst symbolType = type(symbols, "IrSymbol")
konst packageFragmentSymbolType = type(symbols, "IrPackageFragmentSymbol")
konst fileSymbolType = type(symbols, "IrFileSymbol")
konst externalPackageFragmentSymbolType = type(symbols, "IrExternalPackageFragmentSymbol")
konst anonymousInitializerSymbolType = type(symbols, "IrAnonymousInitializerSymbol")
konst enumEntrySymbolType = type(symbols, "IrEnumEntrySymbol")
konst fieldSymbolType = type(symbols, "IrFieldSymbol")
konst classifierSymbolType = type(symbols, "IrClassifierSymbol")
konst classSymbolType = type(symbols, "IrClassSymbol")
konst scriptSymbolType = type(symbols, "IrScriptSymbol")
konst typeParameterSymbolType = type(symbols, "IrTypeParameterSymbol")
konst konstueSymbolType = type(symbols, "IrValueSymbol")
konst konstueParameterSymbolType = type(symbols, "IrValueParameterSymbol")
konst variableSymbolType = type(symbols, "IrVariableSymbol")
konst returnTargetSymbolType = type(symbols, "IrReturnTargetSymbol")
konst functionSymbolType = type(symbols, "IrFunctionSymbol")
konst constructorSymbolType = type(symbols, "IrConstructorSymbol")
konst simpleFunctionSymbolType = type(symbols, "IrSimpleFunctionSymbol")
konst returnableBlockSymbolType = type(symbols, "IrReturnableBlockSymbol")
konst propertySymbolType = type(symbols, "IrPropertySymbol")
konst localDelegatedPropertySymbolType = type(symbols, "IrLocalDelegatedPropertySymbol")
konst typeAliasSymbolType = type(symbols, "IrTypeAliasSymbol")
