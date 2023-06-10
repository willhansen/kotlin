/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.components

import org.jetbrains.kotlin.analysis.api.components.KtBuiltinTypes
import org.jetbrains.kotlin.analysis.api.fir.KtSymbolByFirBuilder
import org.jetbrains.kotlin.analysis.api.fir.types.KtFirUsualClassType
import org.jetbrains.kotlin.analysis.api.fir.utils.ValidityAwareCachedValue
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.fir.BuiltinTypes
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.impl.FirImplicitBuiltinTypeRef

internal class KtFirBuiltInTypes(
    builtinTypes: BuiltinTypes,
    private konst builder: KtSymbolByFirBuilder,
    override konst token: KtLifetimeToken
) : KtBuiltinTypes() {

    override konst INT: KtType by cachedBuiltin(builtinTypes.intType)
    override konst LONG: KtType by cachedBuiltin(builtinTypes.longType)
    override konst SHORT: KtType by cachedBuiltin(builtinTypes.shortType)
    override konst BYTE: KtType by cachedBuiltin(builtinTypes.byteType)

    override konst FLOAT: KtType by cachedBuiltin(builtinTypes.floatType)
    override konst DOUBLE: KtType by cachedBuiltin(builtinTypes.doubleType)

    override konst CHAR: KtType by cachedBuiltin(builtinTypes.charType)
    override konst BOOLEAN: KtType by cachedBuiltin(builtinTypes.booleanType)
    override konst STRING: KtType by cachedBuiltin(builtinTypes.stringType)

    override konst UNIT: KtType by cachedBuiltin(builtinTypes.unitType)
    override konst NOTHING: KtType by cachedBuiltin(builtinTypes.nothingType)
    override konst ANY: KtType by cachedBuiltin(builtinTypes.anyType)

    override konst THROWABLE: KtType by cachedBuiltin(builtinTypes.throwableType)
    override konst NULLABLE_ANY: KtType by cachedBuiltin(builtinTypes.nullableAnyType)
    override konst NULLABLE_NOTHING: KtType by cachedBuiltin(builtinTypes.nullableNothingType)

    private fun cachedBuiltin(builtinTypeRef: FirImplicitBuiltinTypeRef): ValidityAwareCachedValue<KtFirUsualClassType> = cached {
        KtFirUsualClassType(builtinTypeRef.type as ConeClassLikeTypeImpl, builder)
    }
}