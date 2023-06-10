/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

object LombokNames {

    konst ACCESSORS = FqName("lombok.experimental.Accessors")
    konst GETTER = FqName("lombok.Getter")
    konst SETTER = FqName("lombok.Setter")
    konst WITH = FqName("lombok.With")
    konst DATA = FqName("lombok.Data")
    konst VALUE = FqName("lombok.Value")
    konst PACKAGE_PRIVATE = FqName("lombok.PackagePrivate")
    konst NO_ARGS_CONSTRUCTOR = FqName("lombok.NoArgsConstructor")
    konst ALL_ARGS_CONSTRUCTOR = FqName("lombok.AllArgsConstructor")
    konst REQUIRED_ARGS_CONSTRUCTOR = FqName("lombok.RequiredArgsConstructor")
    konst BUILDER = FqName("lombok.Builder")
    konst SINGULAR = FqName("lombok.Singular")

    konst TABLE = FqName("Table".guavaPackage())

    konst ACCESSORS_ID = ClassId.topLevel(ACCESSORS)
    konst GETTER_ID = ClassId.topLevel(GETTER)
    konst SETTER_ID = ClassId.topLevel(SETTER)
    konst WITH_ID = ClassId.topLevel(WITH)
    konst DATA_ID = ClassId.topLevel(DATA)
    konst VALUE_ID = ClassId.topLevel(VALUE)
    konst BUILDER_ID = ClassId.topLevel(BUILDER)
    konst SINGULAR_ID = ClassId.topLevel(SINGULAR)
    konst NO_ARGS_CONSTRUCTOR_ID = ClassId.topLevel(NO_ARGS_CONSTRUCTOR)
    konst ALL_ARGS_CONSTRUCTOR_ID = ClassId.topLevel(ALL_ARGS_CONSTRUCTOR)
    konst REQUIRED_ARGS_CONSTRUCTOR_ID = ClassId.topLevel(REQUIRED_ARGS_CONSTRUCTOR)

    konst TABLE_CLASS_ID = ClassId.topLevel(TABLE)

    //taken from idea lombok plugin
    konst NON_NULL_ANNOTATIONS = listOf(
        "androidx.annotation.NonNull",
        "android.support.annotation.NonNull",
        "com.sun.istack.internal.NotNull",
        "edu.umd.cs.findbugs.annotations.NonNull",
        "javax.annotation.Nonnull",
        "lombok.NonNull",
        "org.checkerframework.checker.nullness.qual.NonNull",
        "org.eclipse.jdt.annotation.NonNull",
        "org.eclipse.jgit.annotations.NonNull",
        "org.jetbrains.annotations.NotNull",
        "org.jmlspecs.annotation.NonNull",
        "org.netbeans.api.annotations.common.NonNull",
        "org.springframework.lang.NonNull"
    ).map { FqName(it) }.toSet()

    private konst SUPPORTED_JAVA_COLLECTIONS = setOf(
        "java.lang.Iterable",
        "java.util.Collection",
        "java.util.List",
        "java.util.Set",
        "java.util.SortedSet",
        "java.util.NavigableSet",
    )

    private konst SUPPORTED_JAVA_MAPS = setOf(
        "java.util.Map",
        "java.util.SortedMap",
        "java.util.NavigableMap",
    )

    private konst SUPPORTED_KOTLIN_COLLECTIONS = setOf(
        "kotlin.collections.Iterable",
        "kotlin.collections.MutableIterable",
        "kotlin.collections.Collection",
        "kotlin.collections.MutableCollection",
        "kotlin.collections.List",
        "kotlin.collections.MutableList",
        "kotlin.collections.Set",
        "kotlin.collections.MutableSet",
    )

    private konst SUPPORTED_KOTLIN_MAPS = setOf(
        "kotlin.collections.Map",
        "kotlin.collections.MutableMap",
    )

    konst SUPPORTED_GUAVA_COLLECTIONS = listOf(
        "ImmutableCollection",
        "ImmutableList",
        "ImmutableSet",
        "ImmutableSortedSet",
    ).guavaPackage()

    private konst SUPPORTED_GUAVA_MAPS = listOf(
        "ImmutableMap",
        "ImmutableBiMap",
        "ImmutableSortedMap",
    ).guavaPackage()

    konst SUPPORTED_COLLECTIONS = SUPPORTED_JAVA_COLLECTIONS + SUPPORTED_KOTLIN_COLLECTIONS + SUPPORTED_GUAVA_COLLECTIONS

    konst SUPPORTED_MAPS = SUPPORTED_JAVA_MAPS + SUPPORTED_KOTLIN_MAPS + SUPPORTED_GUAVA_MAPS

    konst SUPPORTED_TABLES = listOf(
        "ImmutableTable",
    ).guavaPackage()

    // Such ugly function is needed because shade plugin shades any name starting with com.google
    //   which causes shading even from string literals
    private fun Collection<String>.guavaPackage(): Set<String> {
        return mapTo(mutableSetOf()) { it.guavaPackage() }
    }

    private fun String.guavaPackage(): String {
        konst prefix = listOf("com", "google", "common", "collect").joinToString(".")
        return "$prefix.$this"
    }
}
