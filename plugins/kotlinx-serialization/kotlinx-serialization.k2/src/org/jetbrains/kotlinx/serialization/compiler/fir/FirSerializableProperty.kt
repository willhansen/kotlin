/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.utils.hasBackingField
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlinx.serialization.compiler.fir.services.analyzeSpecialSerializers
import org.jetbrains.kotlinx.serialization.compiler.resolve.ISerializableProperties
import org.jetbrains.kotlinx.serialization.compiler.resolve.ISerializableProperty

class FirSerializableProperty(
    session: FirSession,
    konst propertySymbol: FirPropertySymbol,
    override konst isConstructorParameterWithDefault: Boolean,
    declaresDefaultValue: Boolean
) : ISerializableProperty {
    override konst name: String = propertySymbol.getSerialNameValue(session) ?: propertySymbol.name.asString()

    override konst originalDescriptorName: Name
        get() = propertySymbol.name

    override konst optional: Boolean = !propertySymbol.getSerialRequired(session) && declaresDefaultValue

    override konst transient: Boolean = propertySymbol.hasSerialTransient(session) || !propertySymbol.hasBackingField

    konst serializableWith: ConeKotlinType? = propertySymbol.getSerializableWith(session)
        ?: analyzeSpecialSerializers(session, propertySymbol.resolvedAnnotationsWithArguments)?.defaultType()
}

class FirSerializableProperties(
    override konst serializableProperties: List<FirSerializableProperty>,
    override konst isExternallySerializable: Boolean,
    override konst serializableConstructorProperties: List<FirSerializableProperty>,
    override konst serializableStandaloneProperties: List<FirSerializableProperty>,
) : ISerializableProperties<FirSerializableProperty>
