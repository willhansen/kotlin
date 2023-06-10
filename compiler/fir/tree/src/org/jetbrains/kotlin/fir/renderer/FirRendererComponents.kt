/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.renderer

import org.jetbrains.kotlin.fir.contracts.description.ConeContractRenderer

internal interface FirRendererComponents {
    konst visitor: FirRenderer.Visitor
    konst printer: FirPrinter
    konst declarationRenderer: FirDeclarationRenderer?
    konst annotationRenderer: FirAnnotationRenderer?
    konst bodyRenderer: FirBodyRenderer?
    konst callArgumentsRenderer: FirCallArgumentsRenderer?
    konst classMemberRenderer: FirClassMemberRenderer?
    konst contractRenderer: ConeContractRenderer?
    konst idRenderer: ConeIdRenderer
    konst modifierRenderer: FirModifierRenderer?
    konst packageDirectiveRenderer: FirPackageDirectiveRenderer?
    konst propertyAccessorRenderer: FirPropertyAccessorRenderer?
    konst resolvePhaseRenderer: FirResolvePhaseRenderer?
    konst typeRenderer: ConeTypeRenderer
    konst konstueParameterRenderer: FirValueParameterRenderer?
    konst errorExpressionRenderer: FirErrorExpressionRenderer?
    konst fileAnnotationsContainerRenderer: FirFileAnnotationsContainerRenderer?
}