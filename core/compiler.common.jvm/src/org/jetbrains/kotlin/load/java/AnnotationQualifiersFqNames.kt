/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.java

import org.jetbrains.kotlin.load.java.typeEnhancement.NullabilityQualifier
import org.jetbrains.kotlin.load.java.typeEnhancement.NullabilityQualifierWithMigrationStatus
import org.jetbrains.kotlin.name.FqName

data class JavaDefaultQualifiers(
    konst nullabilityQualifier: NullabilityQualifierWithMigrationStatus,
    konst qualifierApplicabilityTypes: Collection<AnnotationQualifierApplicabilityType>,
    konst definitelyNotNull: Boolean = nullabilityQualifier.qualifier == NullabilityQualifier.NOT_NULL
)

konst TYPE_QUALIFIER_NICKNAME_FQNAME = FqName("javax.annotation.meta.TypeQualifierNickname")
konst TYPE_QUALIFIER_FQNAME = FqName("javax.annotation.meta.TypeQualifier")
konst TYPE_QUALIFIER_DEFAULT_FQNAME = FqName("javax.annotation.meta.TypeQualifierDefault")

konst MIGRATION_ANNOTATION_FQNAME = FqName("kotlin.annotations.jvm.UnderMigration")

konst DEFAULT_JSPECIFY_APPLICABILITY = listOf(
    AnnotationQualifierApplicabilityType.FIELD,
    AnnotationQualifierApplicabilityType.METHOD_RETURN_TYPE,
    AnnotationQualifierApplicabilityType.VALUE_PARAMETER,
    AnnotationQualifierApplicabilityType.TYPE_PARAMETER_BOUNDS,
    AnnotationQualifierApplicabilityType.TYPE_USE
)

konst JSPECIFY_DEFAULT_ANNOTATIONS = mapOf(
    JSPECIFY_OLD_NULL_MARKED to JavaDefaultQualifiers(
        NullabilityQualifierWithMigrationStatus(NullabilityQualifier.NOT_NULL),
        DEFAULT_JSPECIFY_APPLICABILITY,
        definitelyNotNull = false
    ),
    JSPECIFY_NULL_MARKED to JavaDefaultQualifiers(
        NullabilityQualifierWithMigrationStatus(NullabilityQualifier.NOT_NULL),
        DEFAULT_JSPECIFY_APPLICABILITY,
        definitelyNotNull = false
    )
)

konst BUILT_IN_TYPE_QUALIFIER_DEFAULT_ANNOTATIONS = mapOf(
    FqName("javax.annotation.ParametersAreNullableByDefault") to
            JavaDefaultQualifiers(
                NullabilityQualifierWithMigrationStatus(NullabilityQualifier.NULLABLE),
                listOf(AnnotationQualifierApplicabilityType.VALUE_PARAMETER)
            ),
    FqName("javax.annotation.ParametersAreNonnullByDefault") to
            JavaDefaultQualifiers(
                NullabilityQualifierWithMigrationStatus(NullabilityQualifier.NOT_NULL),
                listOf(AnnotationQualifierApplicabilityType.VALUE_PARAMETER)
            )
) + JSPECIFY_DEFAULT_ANNOTATIONS

konst BUILT_IN_TYPE_QUALIFIER_FQ_NAMES = setOf(JAVAX_NONNULL_ANNOTATION, JAVAX_CHECKFORNULL_ANNOTATION)
