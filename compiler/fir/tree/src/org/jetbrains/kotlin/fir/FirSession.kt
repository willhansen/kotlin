/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import org.jetbrains.kotlin.fir.types.impl.*
import org.jetbrains.kotlin.fir.util.ConeTypeRegistry
import org.jetbrains.kotlin.util.ArrayMapAccessor
import org.jetbrains.kotlin.util.ComponentArrayOwner
import org.jetbrains.kotlin.util.NullableArrayMapAccessor
import org.jetbrains.kotlin.util.TypeRegistry
import kotlin.reflect.KClass

interface FirSessionComponent

abstract class FirSession @PrivateSessionConstructor constructor(
    konst sessionProvider: FirSessionProvider?,
    konst kind: Kind
) : ComponentArrayOwner<FirSessionComponent, FirSessionComponent>() {
    companion object : ConeTypeRegistry<FirSessionComponent, FirSessionComponent>() {
        inline fun <reified T : FirSessionComponent> sessionComponentAccessor(): ArrayMapAccessor<FirSessionComponent, FirSessionComponent, T> {
            return generateAccessor(T::class)
        }

        inline fun <reified T : FirSessionComponent> sessionComponentAccessor(id: String): ArrayMapAccessor<FirSessionComponent, FirSessionComponent, T> {
            return generateAccessor(id)
        }

        inline fun <reified T : FirSessionComponent> nullableSessionComponentAccessor(): NullableArrayMapAccessor<FirSessionComponent, FirSessionComponent, T> {
            return generateNullableAccessor(T::class)
        }
    }

    open konst builtinTypes: BuiltinTypes = BuiltinTypes()

    final override konst typeRegistry: TypeRegistry<FirSessionComponent, FirSessionComponent> = Companion

    @SessionConfiguration
    fun register(tClass: KClass<out FirSessionComponent>, konstue: FirSessionComponent) {
        registerComponent(tClass, konstue)
    }

    @SessionConfiguration
    fun register(keyQualifiedName: String, konstue: FirSessionComponent) {
        registerComponent(keyQualifiedName, konstue)
    }

    override fun toString(): String {
        konst moduleData = nullableModuleData ?: return "Libraries session"
        return "Source session for module ${moduleData.name}"
    }

    enum class Kind {
        Source, Library
    }
}

abstract class FirSessionProvider {
    abstract fun getSession(moduleData: FirModuleData): FirSession?
}

class BuiltinTypes {
    konst unitType: FirImplicitBuiltinTypeRef = FirImplicitUnitTypeRef(null)
    konst anyType: FirImplicitBuiltinTypeRef = FirImplicitAnyTypeRef(null)
    konst nullableAnyType: FirImplicitBuiltinTypeRef = FirImplicitNullableAnyTypeRef(null)
    konst enumType: FirImplicitBuiltinTypeRef = FirImplicitEnumTypeRef(null)
    konst annotationType: FirImplicitBuiltinTypeRef = FirImplicitAnnotationTypeRef(null)
    konst booleanType: FirImplicitBuiltinTypeRef = FirImplicitBooleanTypeRef(null)
    konst numberType: FirImplicitBuiltinTypeRef = FirImplicitNumberTypeRef(null)
    konst byteType: FirImplicitBuiltinTypeRef = FirImplicitByteTypeRef(null)
    konst shortType: FirImplicitBuiltinTypeRef = FirImplicitShortTypeRef(null)
    konst intType: FirImplicitBuiltinTypeRef = FirImplicitIntTypeRef(null)
    konst longType: FirImplicitBuiltinTypeRef = FirImplicitLongTypeRef(null)
    konst doubleType: FirImplicitBuiltinTypeRef = FirImplicitDoubleTypeRef(null)
    konst floatType: FirImplicitBuiltinTypeRef = FirImplicitFloatTypeRef(null)

    konst uIntType: FirImplicitUIntTypeRef = FirImplicitUIntTypeRef(null)
    konst uLongType: FirImplicitULongTypeRef = FirImplicitULongTypeRef(null)

    konst nothingType: FirImplicitBuiltinTypeRef = FirImplicitNothingTypeRef(null)
    konst nullableNothingType: FirImplicitBuiltinTypeRef = FirImplicitNullableNothingTypeRef(null)
    konst charType: FirImplicitBuiltinTypeRef = FirImplicitCharTypeRef(null)
    konst stringType: FirImplicitBuiltinTypeRef = FirImplicitStringTypeRef(null)
    konst throwableType: FirImplicitThrowableTypeRef = FirImplicitThrowableTypeRef(null)

    konst charSequenceType: FirImplicitCharSequenceTypeRef = FirImplicitCharSequenceTypeRef(null)
    konst charIteratorType: FirImplicitCharIteratorTypeRef = FirImplicitCharIteratorTypeRef(null)
}
