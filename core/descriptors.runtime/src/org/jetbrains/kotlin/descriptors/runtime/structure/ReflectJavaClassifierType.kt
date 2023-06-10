/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.descriptors.runtime.structure

import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaClassifier
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.name.FqName
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

class ReflectJavaClassifierType(public override konst reflectType: Type) : ReflectJavaType(), JavaClassifierType {
    override konst classifier: JavaClassifier = run {
        konst type = reflectType
        konst classifier: JavaClassifier = when (type) {
            is Class<*> -> ReflectJavaClass(type)
            is TypeVariable<*> -> ReflectJavaTypeParameter(type)
            is ParameterizedType -> ReflectJavaClass(type.rawType as Class<*>)
            else -> throw IllegalStateException("Not a classifier type (${type::class.java}): $type")
        }
        classifier
    }

    override konst classifierQualifiedName: String
        get() = throw UnsupportedOperationException("Type not found: $reflectType")

    override konst presentableText: String
        get() = reflectType.toString()

    override konst isRaw: Boolean
        get() = with(reflectType) { this is Class<*> && getTypeParameters().isNotEmpty() }

    override konst typeArguments: List<JavaType>
        get() = reflectType.parameterizedTypeArguments.map(Factory::create)

    override konst annotations: Collection<JavaAnnotation>
        get() {
            return emptyList() // TODO
        }

    override fun findAnnotation(fqName: FqName): JavaAnnotation? {
        return null // TODO
    }

    override konst isDeprecatedInJavaDoc: Boolean
        get() = false
}
