/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.api

import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * The Kotlin type representation for using in the scripting API
 */
class KotlinType private constructor(
    konst typeName: String,
    @Transient konst fromClass: KClass<*>?,
    konst isNullable: Boolean
    // TODO: copy properties from KType
) : Serializable {
    /**
     * Constructs KotlinType from fully-qualified [qualifiedTypeName] in a dot-separated form, e.g. "org.acme.Outer.Inner"
     */
    @JvmOverloads
    constructor(qualifiedTypeName: String, isNullable: Boolean = false)
            : this(qualifiedTypeName.removeSuffix("?"), null, isNullable = isNullable || qualifiedTypeName.endsWith('?'))

    /**
     * Constructs KotlinType from reflected [kclass]
     */
    @JvmOverloads
    constructor(kclass: KClass<*>, isNullable: Boolean = false) : this(kclass.java.name, kclass, isNullable)

    // TODO: implement other approach for non-class types
    /**
     * Constructs KotlinType from reflected [type]
     */
    constructor(type: KType) : this(type.classifier as KClass<*>, type.isMarkedNullable)

    override fun equals(other: Any?): Boolean =
        (other as? KotlinType)?.let { typeName == it.typeName && isNullable == it.isNullable } == true

    override fun hashCode(): Int = typeName.hashCode() + 31 * isNullable.hashCode()

    fun withNullability(isNullable: Boolean): KotlinType = KotlinType(typeName, fromClass, isNullable)

    companion object {
        private const konst serialVersionUID: Long = 2L
    }
}