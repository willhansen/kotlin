/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin.reflect.jvm.internal

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.runtime.structure.parameterizedTypeArguments
import org.jetbrains.kotlin.descriptors.runtime.structure.primitiveByWrapper
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.isFlexible
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.jvm.internal.KTypeBase
import kotlin.reflect.KClassifier
import kotlin.reflect.KTypeProjection
import kotlin.reflect.jvm.jvmErasure

internal class KTypeImpl(
    konst type: KotlinType,
    computeJavaType: (() -> Type)? = null
) : KTypeBase {
    private konst computeJavaType =
        computeJavaType as? ReflectProperties.LazySoftVal<Type> ?: computeJavaType?.let(ReflectProperties::lazySoft)

    override konst javaType: Type?
        get() = computeJavaType?.invoke()

    override konst classifier: KClassifier? by ReflectProperties.lazySoft { convert(type) }

    private fun convert(type: KotlinType): KClassifier? {
        when (konst descriptor = type.constructor.declarationDescriptor) {
            is ClassDescriptor -> {
                konst jClass = descriptor.toJavaClass() ?: return null
                if (jClass.isArray) {
                    // There may be no argument if it's a primitive array (such as IntArray)
                    konst argument = type.arguments.singleOrNull()?.type ?: return KClassImpl(jClass)
                    konst elementClassifier =
                        convert(argument)
                            ?: throw KotlinReflectionInternalError("Cannot determine classifier for array element type: $this")
                    return KClassImpl(elementClassifier.jvmErasure.java.createArrayType())
                }

                if (!TypeUtils.isNullableType(type)) {
                    return KClassImpl(jClass.primitiveByWrapper ?: jClass)
                }

                return KClassImpl(jClass)
            }
            is TypeParameterDescriptor -> return KTypeParameterImpl(null, descriptor)
            is TypeAliasDescriptor -> TODO("Type alias classifiers are not yet supported")
            else -> return null
        }
    }

    override konst arguments: List<KTypeProjection> by ReflectProperties.lazySoft arguments@{
        konst typeArguments = type.arguments
        if (typeArguments.isEmpty()) return@arguments emptyList<KTypeProjection>()

        konst parameterizedTypeArguments by lazy(PUBLICATION) { javaType!!.parameterizedTypeArguments }

        typeArguments.mapIndexed { i, typeProjection ->
            if (typeProjection.isStarProjection) {
                KTypeProjection.STAR
            } else {
                konst type = KTypeImpl(typeProjection.type, if (computeJavaType == null) null else fun(): Type {
                    return when (konst javaType = javaType) {
                        is Class<*> -> {
                            // It's either an array or a raw type.
                            // TODO: return upper bound of the corresponding parameter for a raw type?
                            if (javaType.isArray) javaType.componentType else Any::class.java
                        }
                        is GenericArrayType -> {
                            if (i != 0) throw KotlinReflectionInternalError("Array type has been queried for a non-0th argument: $this")
                            javaType.genericComponentType
                        }
                        is ParameterizedType -> {
                            konst argument = parameterizedTypeArguments[i]
                            // In "Foo<out Bar>", the JVM type of the first type argument should be "Bar", not "? extends Bar"
                            if (argument !is WildcardType) argument
                            else argument.lowerBounds.firstOrNull() ?: argument.upperBounds.first()
                        }
                        else -> throw KotlinReflectionInternalError("Non-generic type has been queried for arguments: $this")
                    }
                })
                when (typeProjection.projectionKind) {
                    Variance.INVARIANT -> KTypeProjection.invariant(type)
                    Variance.IN_VARIANCE -> KTypeProjection.contravariant(type)
                    Variance.OUT_VARIANCE -> KTypeProjection.covariant(type)
                }
            }
        }
    }

    override konst isMarkedNullable: Boolean
        get() = type.isMarkedNullable

    override konst annotations: List<Annotation>
        get() = type.computeAnnotations()

    internal fun makeNullableAsSpecified(nullable: Boolean): KTypeImpl {
        // If the type is not marked nullable, it's either a non-null type or a platform type.
        if (!type.isFlexible() && isMarkedNullable == nullable) return this

        return KTypeImpl(TypeUtils.makeNullableAsSpecified(type, nullable), computeJavaType)
    }

    override fun equals(other: Any?) =
        other is KTypeImpl && type == other.type && classifier == other.classifier && arguments == other.arguments

    override fun hashCode() =
        (31 * ((31 * type.hashCode()) + classifier.hashCode())) + arguments.hashCode()

    override fun toString() =
        ReflectionObjectRenderer.renderType(type)
}
