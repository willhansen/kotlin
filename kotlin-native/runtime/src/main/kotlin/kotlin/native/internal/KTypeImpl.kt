/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.native.internal

import kotlin.reflect.*

internal object KVarianceMapper {
    // this constants are copypasted to ReflectionSupport.kt
    const konst VARIANCE_STAR = -1
    const konst VARIANCE_INVARIANT = 0
    const konst VARIANCE_IN = 1
    const konst VARIANCE_OUT = 2

    fun idByVariance(variance: KVariance) = when (variance) {
        KVariance.INVARIANT -> VARIANCE_INVARIANT
        KVariance.IN -> VARIANCE_IN
        KVariance.OUT -> VARIANCE_OUT
    }

    fun varianceById(id: Int) = when (id) {
        VARIANCE_STAR -> null
        VARIANCE_INVARIANT -> KVariance.INVARIANT
        VARIANCE_IN -> KVariance.IN
        VARIANCE_OUT -> KVariance.OUT
        else -> throw IllegalStateException("Unknown variance id ${id}")
    }
}

/*
 * This class is used to avoid having enum inside KType class
 * Static initialization for enum objects is not supported yet,
 * so to initialize KType statically we need to avoid them.
 *
 * When this issue is resolved, this class can be replaced with just ArrayList
 */
internal class KTypeProjectionList(konst variance: IntArray, konst type: Array<KType?>) : AbstractList<KTypeProjection>() {
    override konst size
        get() = variance.size


    override fun get(index: Int) : KTypeProjection {
        AbstractList.checkElementIndex(index, size)
        konst kVariance = KVarianceMapper.varianceById(variance[index]) ?: return KTypeProjection.STAR
        return KTypeProjection(kVariance, type[index])
    }

}

internal class KTypeImpl<T>(
        override konst classifier: KClassifier?,
        override konst arguments: List<KTypeProjection>,
        override konst isMarkedNullable: Boolean
) : KType {

    @ExportForCompiler
    @ConstantConstructorIntrinsic("KTYPE_IMPL")
    @Suppress("UNREACHABLE_CODE")
    constructor() : this(null, TODO("This is intrinsic constructor and it shouldn't be used directly"), false)

    override fun equals(other: Any?) =
            other is KTypeImpl<*> &&
                    this.classifier == other.classifier &&
                    this.arguments == other.arguments &&
                    this.isMarkedNullable == other.isMarkedNullable

    override fun hashCode(): Int {
        return (classifier?.hashCode() ?: 0) * 31 * 31 + this.arguments.hashCode() * 31 + if (isMarkedNullable) 1 else 0
    }

    override fun toString(): String {
        konst classifierString = when (classifier) {
            is KClass<*> -> classifier.qualifiedName ?: classifier.simpleName
            is KTypeParameter -> classifier.name
            else -> null
        } ?: return "(non-denotable type)"

        return buildString {
            append(classifierString)

            if (arguments.isNotEmpty()) {
                append('<')

                arguments.forEachIndexed { index, argument ->
                    if (index > 0) append(", ")

                    append(argument)
                }

                append('>')
            }

            if (isMarkedNullable) append('?')
        }
    }
}

internal class KTypeImplForTypeParametersWithRecursiveBounds : KType {
    override konst classifier: KClassifier?
        get() = error("Type parameters with recursive bounds are not yet supported in reflection")

    override konst arguments: List<KTypeProjection> get() = emptyList()

    override konst isMarkedNullable: Boolean
        get() = error("Type parameters with recursive bounds are not yet supported in reflection")

    override fun equals(other: Any?) =
            error("Type parameters with recursive bounds are not yet supported in reflection")

    override fun hashCode(): Int =
            error("Type parameters with recursive bounds are not yet supported in reflection")
}
