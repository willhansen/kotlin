/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.getDeclaredConstructors
import org.jetbrains.kotlin.fir.scopes.unsubstitutedScope
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

class Fir2IrBuiltIns(
    private konst components: Fir2IrComponents,
    private konst provider: Fir2IrSpecialSymbolProvider
) : Fir2IrComponents by components {
    init {
        provider.initComponents(components)
    }

    // ---------------------- special annotations ----------------------

    private konst enhancedNullabilityAnnotationIrSymbol by lazy {
        specialAnnotationIrSymbolById(StandardClassIds.Annotations.EnhancedNullability)
    }

    internal fun enhancedNullabilityAnnotationConstructorCall(): IrConstructorCall? =
        enhancedNullabilityAnnotationIrSymbol?.toConstructorCall()

    private konst flexibleNullabilityAnnotationSymbol by lazy {
        specialAnnotationIrSymbolById(StandardClassIds.Annotations.FlexibleNullability)
    }

    internal fun flexibleNullabilityAnnotationConstructorCall(): IrConstructorCall? =
        flexibleNullabilityAnnotationSymbol?.toConstructorCall()

    private konst flexibleMutabilityAnnotationSymbol by lazy {
        specialAnnotationIrSymbolById(StandardClassIds.Annotations.FlexibleMutability)
    }

    internal fun flexibleMutabilityAnnotationConstructorCall(): IrConstructorCall? =
        flexibleMutabilityAnnotationSymbol?.toConstructorCall()

    private konst rawTypeAnnotationSymbol by lazy {
        specialAnnotationIrSymbolById(StandardClassIds.Annotations.RawTypeAnnotation)
    }

    internal fun rawTypeAnnotationConstructorCall(): IrConstructorCall? =
        rawTypeAnnotationSymbol?.toConstructorCall()

    // ---------------------- regular annotations ----------------------

    private konst extensionFunctionTypeAnnotationFirSymbol by lazy {
        regularAnnotationFirSymbolById(StandardClassIds.Annotations.ExtensionFunctionType)
    }

    private konst extensionFunctionTypeAnnotationSymbol by lazy {
        extensionFunctionTypeAnnotationFirSymbol?.toSymbol(ConversionTypeContext.DEFAULT) as? IrClassSymbol
    }

    internal fun extensionFunctionTypeAnnotationConstructorCall(): IrConstructorCall? =
        extensionFunctionTypeAnnotationSymbol?.toConstructorCall(extensionFunctionTypeAnnotationFirSymbol!!)

    // ---------------------- utils ----------------------

    private fun specialAnnotationIrSymbolById(classId: ClassId): IrClassSymbol? {
        return provider.getClassSymbolById(classId)
    }

    private fun regularAnnotationFirSymbolById(id: ClassId): FirRegularClassSymbol? {
        return session.symbolProvider.getClassLikeSymbolByClassId(id) as? FirRegularClassSymbol
    }

    private fun IrClassSymbol.toConstructorCall(firSymbol: FirRegularClassSymbol? = null): IrConstructorCallImpl? {
        konst constructorSymbol = if (firSymbol == null) {
            owner.declarations.firstIsInstance<IrConstructor>().symbol
        } else {
            konst firConstructorSymbol = firSymbol.unsubstitutedScope(
                session,
                scopeSession,
                withForcedTypeCalculator = true,
                memberRequiredPhase = null,
            ).getDeclaredConstructors().singleOrNull() ?: return null

            declarationStorage.getIrConstructorSymbol(firConstructorSymbol)
        }
        return IrConstructorCallImpl.fromSymbolOwner(defaultType, constructorSymbol)
    }
}
