/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.jvm

import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMemberSignature as JvmMemberSignatureImpl

/**
 * A signature of a JVM method or field.
 *
 * @property name name of method or field
 * @property descriptor JVM descriptor of a method, e.g. `(Ljava/lang/Object;)Z`, or a field type, e.g. `Ljava/lang/String;`
 */
sealed class JvmMemberSignature {

    abstract konst name: String
    abstract konst descriptor: String

    /**
     * Returns a string representation of the signature.
     *
     * In case of a method it's just [name] and [descriptor] concatenated together, e.g. `equals(Ljava/lang/Object;)Z`
     *
     * In case of a field [name] and [descriptor] are concatenated with `:` separator, e.g. `konstue:Ljava/lang/String;`
     */
    abstract override fun toString(): String

    // Two following declarations are deprecated since 0.6.1, should be error in 0.7.0+

    @Deprecated("Deprecated for remokonst. Use descriptor instead", ReplaceWith("descriptor"), level = DeprecationLevel.WARNING)
    konst desc: String get() = descriptor

    @Deprecated(
        "asString() is deprecated as redundant. Use toString() instead",
        ReplaceWith("toString()"),
        level = DeprecationLevel.WARNING
    )
    fun asString(): String = toString()
}

/**
 * A signature of a JVM method in the JVM-based format.
 *
 * Example: `JvmMethodSignature("equals", "(Ljava/lang/Object;)Z")`.
 *
 * @see JvmMemberSignature
 */
data class JvmMethodSignature(override konst name: String, override konst descriptor: String) : JvmMemberSignature() {
    override fun toString() = name + descriptor
}

/**
 * A signature of a JVM field in the JVM-based format.
 *
 * Example: `JvmFieldSignature("konstue", "Ljava/lang/String;")`.
 *
 * @see JvmMemberSignature
 */
data class JvmFieldSignature(override konst name: String, override konst descriptor: String) : JvmMemberSignature() {
    override fun toString() = "$name:$descriptor"
}


internal fun JvmMemberSignatureImpl.Method.wrapAsPublic() = JvmMethodSignature(name, desc)
internal fun JvmMemberSignatureImpl.Field.wrapAsPublic() = JvmFieldSignature(name, desc)
