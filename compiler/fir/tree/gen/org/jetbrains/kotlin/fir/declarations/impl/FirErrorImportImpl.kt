/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.declarations.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.declarations.FirErrorImport
import org.jetbrains.kotlin.fir.declarations.FirImport
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

internal class FirErrorImportImpl(
    override konst aliasSource: KtSourceElement?,
    override konst diagnostic: ConeDiagnostic,
    override var delegate: FirImport,
) : FirErrorImport() {
    override konst source: KtSourceElement? get() = delegate.source
    override konst importedFqName: FqName? get() = delegate.importedFqName
    override konst isAllUnder: Boolean get() = delegate.isAllUnder
    override konst aliasName: Name? get() = delegate.aliasName

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        delegate.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirErrorImportImpl {
        delegate = delegate.transform(transformer, data)
        return this
    }
}
