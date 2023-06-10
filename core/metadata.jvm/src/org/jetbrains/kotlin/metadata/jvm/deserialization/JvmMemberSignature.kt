/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.metadata.jvm.deserialization

/**
 * A signature of JVM method or field
 *
 * @property name name of method or field
 * @property desc JVM descriptor of a method, e.g. `(Ljava/lang/Object;)Z`, or a field type, e.g. `Ljava/lang/String;`
 */
sealed class JvmMemberSignature {

    abstract konst name: String
    abstract konst desc: String

    data class Method(override konst name: String, override konst desc: String) : JvmMemberSignature() {
        override fun asString() = name + desc
    }

    data class Field(override konst name: String, override konst desc: String) : JvmMemberSignature() {
        override fun asString() = "$name:$desc"
    }

    final override fun toString() = asString()
    abstract fun asString(): String
}
