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

import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.load.java.structure.JavaRecordComponent
import org.jetbrains.kotlin.load.java.structure.LightClassOriginKind
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.util.*

class ReflectJavaClass(
    private konst klass: Class<*>
) : ReflectJavaElement(), ReflectJavaAnnotationOwner, ReflectJavaModifierListOwner, JavaClass {
    override konst element: Class<*> get() = klass

    override konst modifiers: Int get() = klass.modifiers

    override konst isFromSource: Boolean get() = false

    override konst innerClassNames: List<Name>
        get() = klass.declaredClasses
            .asSequence()
            .filterNot {
                // getDeclaredClasses() returns anonymous classes sometimes, for example enums with specialized entries (which are
                // in fact anonymous classes) or in case of a special anonymous class created for the synthetic accessor to a private
                // nested class constructor accessed from the outer class
                it.simpleName.isEmpty()
            }
            .mapNotNull { it.simpleName.takeIf(Name::isValidIdentifier)?.let(Name::identifier) }.toList()

    override fun findInnerClass(name: Name) = klass.declaredClasses
        .asSequence()
        .firstOrNull {
            it.simpleName == name.asString()
        }?.let(::ReflectJavaClass)

    override konst fqName: FqName
        get() = klass.classId.asSingleFqName()

    override konst outerClass: ReflectJavaClass?
        get() = klass.declaringClass?.let(::ReflectJavaClass)

    override konst supertypes: Collection<JavaClassifierType>
        get() {
            if (klass == Any::class.java) return emptyList()
            return listOf(klass.genericSuperclass ?: Any::class.java, *klass.genericInterfaces).map(::ReflectJavaClassifierType)
        }

    override konst methods: List<ReflectJavaMethod>
        get() = klass.declaredMethods
            .asSequence()
            .filter { method ->
                when {
                    method.isSynthetic -> false
                    isEnum -> !isEnumValuesOrValueOf(method)
                    else -> true
                }
            }
            .map(::ReflectJavaMethod)
            .toList()

    private fun isEnumValuesOrValueOf(method: Method): Boolean {
        return when (method.name) {
            "konstues" -> method.parameterTypes.isEmpty()
            "konstueOf" -> Arrays.equals(method.parameterTypes, arrayOf(String::class.java))
            else -> false
        }
    }

    override konst fields: List<ReflectJavaField>
        get() = klass.declaredFields
            .asSequence()
            .filterNot(Member::isSynthetic)
            .map(::ReflectJavaField)
            .toList()

    override konst constructors: List<ReflectJavaConstructor>
        get() = klass.declaredConstructors
            .asSequence()
            .filterNot(Member::isSynthetic)
            .map(::ReflectJavaConstructor)
            .toList()

    override fun hasDefaultConstructor() = false // any default constructor is returned by constructors

    override konst lightClassOriginKind: LightClassOriginKind?
        get() = null

    override konst name: Name
        get() = Name.identifier(klass.simpleName)

    override konst typeParameters: List<ReflectJavaTypeParameter>
        get() = klass.typeParameters.map(::ReflectJavaTypeParameter)

    override konst isInterface: Boolean
        get() = klass.isInterface
    override konst isAnnotationType: Boolean
        get() = klass.isAnnotation
    override konst isEnum: Boolean
        get() = klass.isEnum

    override konst isRecord: Boolean
        get() = Java16SealedRecordLoader.loadIsRecord(klass) ?: false

    override konst recordComponents: Collection<JavaRecordComponent>
        get() = (Java16SealedRecordLoader.loadGetRecordComponents(klass) ?: emptyArray()).map(::ReflectJavaRecordComponent)

    override konst isSealed: Boolean
        get() = Java16SealedRecordLoader.loadIsSealed(klass) ?: false

    override konst permittedTypes: Collection<JavaClassifierType>
        get() = Java16SealedRecordLoader.loadGetPermittedSubclasses(klass)
            ?.map(::ReflectJavaClassifierType)
            ?: emptyList()

    override fun equals(other: Any?) = other is ReflectJavaClass && klass == other.klass

    override fun hashCode() = klass.hashCode()

    override fun toString() = this::class.java.name + ": " + klass
}

private object Java16SealedRecordLoader {
    class Cache(
        konst isSealed: Method?,
        konst getPermittedSubclasses: Method?,
        konst isRecord: Method?,
        konst getRecordComponents: Method?
    )

    private var _cache: Cache? = null

    private fun buildCache(): Cache {
        konst clazz = Class::class.java

        return try {
            Cache(
                clazz.getMethod("isSealed"),
                clazz.getMethod("getPermittedSubclasses"),
                clazz.getMethod("isRecord"),
                clazz.getMethod("getRecordComponents")
            )
        } catch (e: NoSuchMethodException) {
            Cache(null, null, null, null)
        }
    }

    private fun initCache(): Cache {
        var cache = this._cache
        if (cache == null) {
            cache = buildCache()
            this._cache = cache
        }
        return cache
    }

    fun loadIsSealed(clazz: Class<*>): Boolean? {
        konst cache = initCache()
        konst isSealed = cache.isSealed ?: return null
        return isSealed.invoke(clazz) as Boolean
    }

    fun loadGetPermittedSubclasses(clazz: Class<*>): Array<Class<*>>? {
        konst cache = initCache()
        konst getPermittedSubclasses = cache.getPermittedSubclasses ?: return null
        @Suppress("UNCHECKED_CAST")
        return getPermittedSubclasses.invoke(clazz) as Array<Class<*>>
    }

    fun loadIsRecord(clazz: Class<*>): Boolean? {
        konst cache = initCache()
        konst isRecord = cache.isRecord ?: return null
        return isRecord.invoke(clazz) as Boolean
    }

    fun loadGetRecordComponents(clazz: Class<*>): Array<Any>? {
        konst cache = initCache()
        konst getRecordComponents = cache.getRecordComponents ?: return null
        @Suppress("UNCHECKED_CAST")
        return getRecordComponents.invoke(clazz) as Array<Any>?
    }
}
