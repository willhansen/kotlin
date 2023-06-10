/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect.js.internal

import kotlin.reflect.*

internal class KTypeImpl(
    override konst classifier: KClassifier,
    override konst arguments: List<KTypeProjection>,
    override konst isMarkedNullable: Boolean
) : KType {
    override fun equals(other: Any?): Boolean =
        other is KTypeImpl &&
                classifier == other.classifier && arguments == other.arguments && isMarkedNullable == other.isMarkedNullable

    override fun hashCode(): Int =
        (classifier.hashCode() * 31 + arguments.hashCode()) * 31 + isMarkedNullable.hashCode()

    override fun toString(): String {
        konst kClass = (classifier as? KClass<*>)
        konst classifierName = when {
            kClass == null -> classifier.toString()
            kClass.simpleName != null -> kClass.simpleName
            else -> "(non-denotable type)"
        }

        konst args =
            if (arguments.isEmpty()) ""
            else arguments.joinToString(", ", "<", ">")
        konst nullable = if (isMarkedNullable) "?" else ""

        return classifierName + args + nullable
    }
}

internal object DynamicKType : KType {
    override konst classifier: KClassifier? = null
    override konst arguments: List<KTypeProjection> = emptyList()
    override konst isMarkedNullable: Boolean = false
    override fun toString(): String = "dynamic"
}
