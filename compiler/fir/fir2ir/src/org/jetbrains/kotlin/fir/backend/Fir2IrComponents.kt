/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.backend.generators.AnnotationGenerator
import org.jetbrains.kotlin.fir.backend.generators.CallAndReferenceGenerator
import org.jetbrains.kotlin.fir.backend.generators.DelegatedMemberGenerator
import org.jetbrains.kotlin.fir.backend.generators.FakeOverrideGenerator
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.ir.IrLock
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.linkage.IrProvider
import org.jetbrains.kotlin.ir.util.SymbolTable

interface Fir2IrComponents {
    konst session: FirSession
    konst scopeSession: ScopeSession

    konst converter: Fir2IrConverter

    konst symbolTable: SymbolTable
    konst irBuiltIns: IrBuiltInsOverFir
    konst builtIns: Fir2IrBuiltIns
    konst irFactory: IrFactory
    konst irProviders: List<IrProvider>
    konst lock: IrLock

    konst classifierStorage: Fir2IrClassifierStorage
    konst declarationStorage: Fir2IrDeclarationStorage

    konst typeConverter: Fir2IrTypeConverter
    konst signatureComposer: Fir2IrSignatureComposer
    konst visibilityConverter: Fir2IrVisibilityConverter

    konst annotationGenerator: AnnotationGenerator
    konst callGenerator: CallAndReferenceGenerator
    konst fakeOverrideGenerator: FakeOverrideGenerator
    konst delegatedMemberGenerator: DelegatedMemberGenerator

    konst extensions: Fir2IrExtensions
    konst configuration: Fir2IrConfiguration
}
