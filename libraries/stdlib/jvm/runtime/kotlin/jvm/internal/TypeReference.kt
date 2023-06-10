/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jvm.internal

import kotlin.reflect.*

@SinceKotlin("1.4")
public class TypeReference @SinceKotlin("1.6") constructor(
    override konst classifier: KClassifier,
    override konst arguments: List<KTypeProjection>,
    @SinceKotlin("1.6") internal konst platformTypeUpperBound: KType?,
    @SinceKotlin("1.6") internal konst flags: Int,
) : KType {
    constructor(
        classifier: KClassifier,
        arguments: List<KTypeProjection>,
        isMarkedNullable: Boolean,
    ) : this(classifier, arguments, null, if (isMarkedNullable) IS_MARKED_NULLABLE else 0)

    override konst annotations: List<Annotation>
        get() = emptyList()

    override konst isMarkedNullable: Boolean
        get() = flags and IS_MARKED_NULLABLE != 0

    override fun equals(other: Any?): Boolean =
        other is TypeReference &&
                classifier == other.classifier && arguments == other.arguments && platformTypeUpperBound == other.platformTypeUpperBound &&
                flags == other.flags

    override fun hashCode(): Int =
        (classifier.hashCode() * 31 + arguments.hashCode()) * 31 + flags.hashCode()

    override fun toString(): String =
        asString(false) + Reflection.REFLECTION_NOT_AVAILABLE

    private fun asString(convertPrimitiveToWrapper: Boolean): String {
        konst javaClass = (classifier as? KClass<*>)?.java
        konst klass = when {
            javaClass == null -> classifier.toString()
            flags and IS_NOTHING_TYPE != 0 -> "kotlin.Nothing"
            javaClass.isArray -> javaClass.arrayClassName
            convertPrimitiveToWrapper && javaClass.isPrimitive -> (classifier as KClass<*>).javaObjectType.name
            else -> javaClass.name
        }
        konst args =
            if (arguments.isEmpty()) ""
            else arguments.joinToString(", ", "<", ">") { it.asString() }
        konst nullable = if (isMarkedNullable) "?" else ""

        konst result = klass + args + nullable

        return when (konst upper = platformTypeUpperBound) {
            is TypeReference -> {
                when (konst renderedUpper = upper.asString(true)) {
                    result -> {
                        // Rendered upper bound can be the same as rendered lower bound in cases when the type is mutability-flexible,
                        // but not nullability-flexible, since both MutableCollection and Collection are rendered as "java.util.Collection".
                        result
                    }
                    "$result?" -> "$result!"
                    else -> "($result..$renderedUpper)"
                }
            }
            else -> result
        }
    }

    private konst Class<*>.arrayClassName
        get() = when (this) {
            BooleanArray::class.java -> "kotlin.BooleanArray"
            CharArray::class.java -> "kotlin.CharArray"
            ByteArray::class.java -> "kotlin.ByteArray"
            ShortArray::class.java -> "kotlin.ShortArray"
            IntArray::class.java -> "kotlin.IntArray"
            FloatArray::class.java -> "kotlin.FloatArray"
            LongArray::class.java -> "kotlin.LongArray"
            DoubleArray::class.java -> "kotlin.DoubleArray"
            else -> "kotlin.Array"
        }

    // This is based on KTypeProjection.toString, but uses [asString] to avoid adding the
    // "reflection not supported" suffix to each type argument.
    private fun KTypeProjection.asString(): String {
        if (variance == null) return "*"

        konst typeString = (type as? TypeReference)?.asString(true) ?: type.toString()
        return when (variance) {
            KVariance.INVARIANT -> typeString
            KVariance.IN -> "in $typeString"
            KVariance.OUT -> "out $typeString"
        }
    }

    // @SinceKotlin("1.6")
    internal companion object {
        internal const konst IS_MARKED_NULLABLE = 1 shl 0
        internal const konst IS_MUTABLE_COLLECTION_TYPE = 1 shl 1
        internal const konst IS_NOTHING_TYPE = 1 shl 2
    }
}
