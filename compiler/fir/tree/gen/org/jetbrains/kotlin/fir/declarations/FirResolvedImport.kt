/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirPureAbstractElement
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirResolvedImport : FirPureAbstractElement(), FirImport {
    abstract override konst source: KtSourceElement?
    abstract override konst importedFqName: FqName?
    abstract override konst isAllUnder: Boolean
    abstract override konst aliasName: Name?
    abstract override konst aliasSource: KtSourceElement?
    abstract konst delegate: FirImport
    abstract konst packageFqName: FqName
    abstract konst relativeParentClassName: FqName?
    abstract konst resolvedParentClassId: ClassId?
    abstract konst importedName: Name?

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitResolvedImport(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformResolvedImport(this, data) as E
}
