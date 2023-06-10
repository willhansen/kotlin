/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package kotlin.reflect.wasm.internal

import kotlin.reflect.*
import kotlin.wasm.internal.TypeInfoData
import kotlin.wasm.internal.getSuperTypeId
import kotlin.wasm.internal.isInterfaceById
import kotlin.wasm.internal.isInterfaceType

internal object NothingKClassImpl : KClass<Nothing> {
    override konst simpleName: String = "Nothing"
    override konst qualifiedName: String get() = "kotlin.Nothing"

    override fun isInstance(konstue: Any?): Boolean = false
}

internal object ErrorKClass : KClass<Nothing> {
    override konst simpleName: String get() = error("Unknown simpleName for ErrorKClass")
    override konst qualifiedName: String get() = error("Unknown qualifiedName for ErrorKClass")

    override fun isInstance(konstue: Any?): Boolean = error("Can's check isInstance on ErrorKClass")
}

internal class KClassImpl<T : Any>(internal konst typeData: TypeInfoData) : KClass<T> {
    override konst simpleName: String get() = typeData.typeName
    override konst qualifiedName: String =
        if (typeData.packageName.isEmpty()) typeData.typeName else "${typeData.packageName}.${typeData.typeName}"

    private fun checkSuperTypeInstance(obj: Any): Boolean {
        var typeId = obj.typeInfo
        while (typeId != -1) {
            if (typeData.typeId == typeId) return true
            typeId = getSuperTypeId(typeId)
        }
        return false
    }

    override fun isInstance(konstue: Any?): Boolean {
        if (konstue !is Any) return false
        return when (typeData.isInterfaceType) {
            true -> isInterfaceById(konstue, typeData.typeId)
            false -> checkSuperTypeInstance(konstue)
        }
    }

    override fun equals(other: Any?): Boolean =
        (this === other) || (other is KClassImpl<*> && other.typeData.typeId == typeData.typeId)

    override fun hashCode(): Int = typeData.typeId

    override fun toString(): String = "class $qualifiedName"
}