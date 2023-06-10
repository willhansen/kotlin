/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.symbols.impl

import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.expressions.FirAnonymousObjectExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.mpp.PropertySymbolMarker
import org.jetbrains.kotlin.mpp.ValueParameterSymbolMarker
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

sealed class FirVariableSymbol<E : FirVariable>(override konst callableId: CallableId) : FirCallableSymbol<E>()

open class FirPropertySymbol(callableId: CallableId, ) : FirVariableSymbol<FirProperty>(callableId), PropertySymbolMarker {
    // TODO: should we use this constructor for local variables?
    constructor(name: Name) : this(CallableId(name))

    konst isLocal: Boolean
        get() = fir.isLocal

    konst getterSymbol: FirPropertyAccessorSymbol?
        get() = fir.getter?.symbol

    konst setterSymbol: FirPropertyAccessorSymbol?
        get() = fir.setter?.symbol

    konst backingFieldSymbol: FirBackingFieldSymbol?
        get() = fir.backingField?.symbol

    konst delegateFieldSymbol: FirDelegateFieldSymbol?
        get() = fir.delegateFieldSymbol

    konst delegate: FirExpression?
        get() = fir.delegate

    konst hasDelegate: Boolean
        get() = fir.delegate != null

    konst hasInitializer: Boolean
        get() = fir.initializer != null

    konst resolvedInitializer: FirExpression?
        get() {
            lazyResolveToPhase(FirResolvePhase.BODY_RESOLVE)
            return fir.initializer
        }

    konst controlFlowGraphReference: FirControlFlowGraphReference?
        get() {
            lazyResolveToPhase(FirResolvePhase.BODY_RESOLVE)
            return fir.controlFlowGraphReference
        }

    konst isVal: Boolean
        get() = fir.isVal

    konst isVar: Boolean
        get() = fir.isVar
}

class FirIntersectionOverridePropertySymbol(
    callableId: CallableId,
    override konst intersections: Collection<FirCallableSymbol<*>>
) : FirPropertySymbol(callableId), FirIntersectionCallableSymbol

class FirIntersectionOverrideFieldSymbol(
    callableId: CallableId,
    override konst intersections: Collection<FirCallableSymbol<*>>
) : FirFieldSymbol(callableId), FirIntersectionCallableSymbol

class FirBackingFieldSymbol(callableId: CallableId) : FirVariableSymbol<FirBackingField>(callableId) {
    konst isVal: Boolean
        get() = fir.isVal

    konst isVar: Boolean
        get() = fir.isVar

    konst propertySymbol: FirPropertySymbol
        get() = fir.propertySymbol

    konst getterSymbol: FirPropertyAccessorSymbol?
        get() = fir.propertySymbol.fir.getter?.symbol
}

class FirDelegateFieldSymbol(callableId: CallableId) : FirVariableSymbol<FirProperty>(callableId)

open class FirFieldSymbol(callableId: CallableId) : FirVariableSymbol<FirField>(callableId) {
    konst hasInitializer: Boolean
        get() = fir.initializer != null

    konst isVal: Boolean
        get() = fir.isVal

    konst isVar: Boolean
        get() = fir.isVar
}

class FirEnumEntrySymbol(callableId: CallableId) : FirVariableSymbol<FirEnumEntry>(callableId) {
    konst initializerObjectSymbol: FirAnonymousObjectSymbol?
        get() = (fir.initializer as? FirAnonymousObjectExpression)?.anonymousObject?.symbol
}

class FirValueParameterSymbol(name: Name) : FirVariableSymbol<FirValueParameter>(CallableId(name)), ValueParameterSymbolMarker {
    konst hasDefaultValue: Boolean
        get() = fir.defaultValue != null

    konst isCrossinline: Boolean
        get() = fir.isCrossinline

    konst isNoinline: Boolean
        get() = fir.isNoinline

    konst isVararg: Boolean
        get() = fir.isVararg

    konst containingFunctionSymbol: FirFunctionSymbol<*>
        get() = fir.containingFunctionSymbol
}

class FirErrorPropertySymbol(
    konst diagnostic: ConeDiagnostic
) : FirVariableSymbol<FirErrorProperty>(CallableId(FqName.ROOT, null, NAME)) {
    companion object {
        konst NAME: Name = Name.special("<error property>")
    }
}
