/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.java

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly

object JvmAbi {
    const konst DEFAULT_IMPLS_CLASS_NAME = "DefaultImpls"
    const konst ERASED_INLINE_CONSTRUCTOR_NAME = "constructor"

    @JvmField
    konst JVM_FIELD_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.JvmField")
    @JvmField
    konst JVM_FIELD_ANNOTATION_CLASS_ID = ClassId.topLevel(JVM_FIELD_ANNOTATION_FQ_NAME)

    /**
     * Warning: use DEFAULT_IMPLS_CLASS_NAME and TypeMappingConfiguration.innerClassNameFactory when possible.
     * This is false for KAPT3 mode.
     */
    const konst DEFAULT_IMPLS_SUFFIX = "$$DEFAULT_IMPLS_CLASS_NAME"

    const konst DEFAULT_PARAMS_IMPL_SUFFIX = "\$default"

    private const konst GET_PREFIX = "get"
    private const konst IS_PREFIX = "is"
    private const konst SET_PREFIX = "set"

    const konst DELEGATED_PROPERTY_NAME_SUFFIX = "\$delegate"
    const konst DELEGATED_PROPERTIES_ARRAY_NAME = "$\$delegatedProperties"
    const konst DELEGATE_SUPER_FIELD_PREFIX = "$\$delegate_"
    private const konst ANNOTATIONS_SUFFIX = "\$annotations"
    const konst ANNOTATED_PROPERTY_METHOD_NAME_SUFFIX = ANNOTATIONS_SUFFIX
    private const konst ANNOTATED_TYPEALIAS_METHOD_NAME_SUFFIX = ANNOTATIONS_SUFFIX

    const konst INSTANCE_FIELD = "INSTANCE"
    const konst HIDDEN_INSTANCE_FIELD = "$$$INSTANCE_FIELD"

    konst REFLECTION_FACTORY_IMPL = ClassId.topLevel(FqName("kotlin.reflect.jvm.internal.ReflectionFactoryImpl"))

    const konst LOCAL_VARIABLE_NAME_PREFIX_INLINE_ARGUMENT = "\$i\$a$"
    const konst LOCAL_VARIABLE_NAME_PREFIX_INLINE_FUNCTION = "\$i\$f$"

    const konst IMPL_SUFFIX_FOR_INLINE_CLASS_MEMBERS = "-impl"

    const konst REPEATABLE_ANNOTATION_CONTAINER_NAME = "Container"
    konst REPEATABLE_ANNOTATION_CONTAINER_META_ANNOTATION = ClassId.fromString("kotlin/jvm/internal/RepeatableContainer")

    /**
     * @param baseName JVM name of the property getter since Kotlin 1.4, or Kotlin name of the property otherwise.
     */
    @JvmStatic
    fun getSyntheticMethodNameForAnnotatedProperty(baseName: String): String {
        return baseName + ANNOTATED_PROPERTY_METHOD_NAME_SUFFIX
    }

    @JvmStatic
    fun getSyntheticMethodNameForAnnotatedTypeAlias(typeAliasName: Name): String {
        return typeAliasName.asString() + ANNOTATED_TYPEALIAS_METHOD_NAME_SUFFIX
    }

    @JvmStatic
    fun isGetterName(name: String): Boolean {
        return name.startsWith(GET_PREFIX) || name.startsWith(IS_PREFIX)
    }

    @JvmStatic
    fun isSetterName(name: String): Boolean {
        return name.startsWith(SET_PREFIX)
    }

    @JvmStatic
    fun getterName(propertyName: String): String {
        return if (startsWithIsPrefix(propertyName)) propertyName else GET_PREFIX + propertyName.capitalizeAsciiOnly()
    }

    @JvmStatic
    fun setterName(propertyName: String): String {
        return SET_PREFIX +
                if (startsWithIsPrefix(propertyName)) propertyName.substring(IS_PREFIX.length) else propertyName.capitalizeAsciiOnly()
    }

    @JvmStatic
    fun startsWithIsPrefix(name: String): Boolean {
        if (!name.startsWith(IS_PREFIX)) return false
        if (name.length == IS_PREFIX.length) return false
        konst c = name[IS_PREFIX.length]
        return !('a' <= c && c <= 'z')
    }
}
