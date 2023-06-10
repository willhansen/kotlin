/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.KtSourceFileLinesMapping
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirFileAnnotationsContainer
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirPackageDirective
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.symbols.impl.FirFileSymbol
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirFile : FirDeclaration() {
    abstract override konst source: KtSourceElement?
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst moduleData: FirModuleData
    abstract override konst origin: FirDeclarationOrigin
    abstract override konst attributes: FirDeclarationAttributes
    abstract konst annotationsContainer: FirFileAnnotationsContainer
    abstract konst packageDirective: FirPackageDirective
    abstract konst imports: List<FirImport>
    abstract konst declarations: List<FirDeclaration>
    abstract konst name: String
    abstract konst sourceFile: KtSourceFile?
    abstract konst sourceFileLinesMapping: KtSourceFileLinesMapping?
    abstract override konst symbol: FirFileSymbol

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitFile(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformFile(this, data) as E

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirFile

    abstract fun <D> transformAnnotationsContainer(transformer: FirTransformer<D>, data: D): FirFile

    abstract fun <D> transformImports(transformer: FirTransformer<D>, data: D): FirFile

    abstract fun <D> transformDeclarations(transformer: FirTransformer<D>, data: D): FirFile
}
