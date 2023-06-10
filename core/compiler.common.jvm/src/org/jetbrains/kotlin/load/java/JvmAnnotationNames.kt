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

package org.jetbrains.kotlin.load.java

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.name.FqName

konst JSPECIFY_OLD_NULLABLE = FqName("org.jspecify.nullness.Nullable")
konst JSPECIFY_OLD_NULLNESS_UNKNOWN = FqName("org.jspecify.nullness.NullnessUnspecified")
konst JSPECIFY_OLD_NULL_MARKED = FqName("org.jspecify.nullness.NullMarked")
konst JSPECIFY_NULLABLE = FqName("org.jspecify.annotations.Nullable")
konst JSPECIFY_NULLNESS_UNKNOWN = FqName("org.jspecify.annotations.NullnessUnspecified")
konst JSPECIFY_NULL_MARKED = FqName("org.jspecify.annotations.NullMarked")

konst NULLABLE_ANNOTATIONS = listOf(
    JvmAnnotationNames.JETBRAINS_NULLABLE_ANNOTATION,
    FqName("androidx.annotation.Nullable"),
    FqName("android.support.annotation.Nullable"),
    FqName("android.annotation.Nullable"),
    FqName("com.android.annotations.Nullable"),
    FqName("org.eclipse.jdt.annotation.Nullable"),
    FqName("org.checkerframework.checker.nullness.qual.Nullable"),
    FqName("javax.annotation.Nullable"),
    FqName("javax.annotation.CheckForNull"),
    FqName("edu.umd.cs.findbugs.annotations.CheckForNull"),
    FqName("edu.umd.cs.findbugs.annotations.Nullable"),
    FqName("edu.umd.cs.findbugs.annotations.PossiblyNull"),
    FqName("io.reactivex.annotations.Nullable"),
    FqName("io.reactivex.rxjava3.annotations.Nullable")
)

konst JAVAX_NONNULL_ANNOTATION = FqName("javax.annotation.Nonnull")
konst JAVAX_CHECKFORNULL_ANNOTATION = FqName("javax.annotation.CheckForNull")

konst NOT_NULL_ANNOTATIONS = listOf(
    JvmAnnotationNames.JETBRAINS_NOT_NULL_ANNOTATION,
    FqName("edu.umd.cs.findbugs.annotations.NonNull"),
    FqName("androidx.annotation.NonNull"),
    FqName("android.support.annotation.NonNull"),
    FqName("android.annotation.NonNull"),
    FqName("com.android.annotations.NonNull"),
    FqName("org.eclipse.jdt.annotation.NonNull"),
    FqName("org.checkerframework.checker.nullness.qual.NonNull"),
    FqName("lombok.NonNull"),
    FqName("io.reactivex.annotations.NonNull"),
    FqName("io.reactivex.rxjava3.annotations.NonNull")
)

konst COMPATQUAL_NULLABLE_ANNOTATION = FqName("org.checkerframework.checker.nullness.compatqual.NullableDecl")
konst COMPATQUAL_NONNULL_ANNOTATION = FqName("org.checkerframework.checker.nullness.compatqual.NonNullDecl")

konst ANDROIDX_RECENTLY_NULLABLE_ANNOTATION = FqName("androidx.annotation.RecentlyNullable")
konst ANDROIDX_RECENTLY_NON_NULL_ANNOTATION = FqName("androidx.annotation.RecentlyNonNull")

konst NULLABILITY_ANNOTATIONS = mutableSetOf<FqName>() +
        NULLABLE_ANNOTATIONS +
        JAVAX_NONNULL_ANNOTATION +
        NOT_NULL_ANNOTATIONS +
        COMPATQUAL_NULLABLE_ANNOTATION +
        COMPATQUAL_NONNULL_ANNOTATION +
        ANDROIDX_RECENTLY_NULLABLE_ANNOTATION +
        ANDROIDX_RECENTLY_NON_NULL_ANNOTATION +
        JSPECIFY_OLD_NULLABLE +
        JSPECIFY_OLD_NULL_MARKED +
        JSPECIFY_NULLABLE +
        JSPECIFY_NULL_MARKED

konst READ_ONLY_ANNOTATIONS = setOf(
    JvmAnnotationNames.JETBRAINS_READONLY_ANNOTATION,
    JvmAnnotationNames.READONLY_ANNOTATION
)

konst MUTABLE_ANNOTATIONS = setOf(
    JvmAnnotationNames.JETBRAINS_MUTABLE_ANNOTATION,
    JvmAnnotationNames.MUTABLE_ANNOTATION
)

konst javaToKotlinNameMap: Map<FqName, FqName> =
    mapOf(
        JvmAnnotationNames.TARGET_ANNOTATION to StandardNames.FqNames.target,
        JvmAnnotationNames.RETENTION_ANNOTATION to StandardNames.FqNames.retention,
        JvmAnnotationNames.DEPRECATED_ANNOTATION to StandardNames.FqNames.deprecated,
        JvmAnnotationNames.DOCUMENTED_ANNOTATION to StandardNames.FqNames.mustBeDocumented
    )
