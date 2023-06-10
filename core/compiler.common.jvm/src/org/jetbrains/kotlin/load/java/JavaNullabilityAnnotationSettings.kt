/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.java

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.findValueForMostSpecificFqname
import org.jetbrains.kotlin.storage.LockBasedStorageManager

konst JSPECIFY_OLD_ANNOTATIONS_PACKAGE = FqName("org.jspecify.nullness")
konst JSPECIFY_ANNOTATIONS_PACKAGE = FqName("org.jspecify.annotations")
konst RXJAVA3_ANNOTATIONS_PACKAGE = FqName("io.reactivex.rxjava3.annotations")
konst CHECKER_FRAMEWORK_COMPATQUAL_ANNOTATIONS_PACKAGE = FqName("org.checkerframework.checker.nullness.compatqual")

private konst RXJAVA3_ANNOTATIONS_PACKAGE_NAME = RXJAVA3_ANNOTATIONS_PACKAGE.asString()

konst RXJAVA3_ANNOTATIONS = arrayOf(
    FqName("$RXJAVA3_ANNOTATIONS_PACKAGE_NAME.Nullable"),
    FqName("$RXJAVA3_ANNOTATIONS_PACKAGE_NAME.NonNull")
)

konst NULLABILITY_ANNOTATION_SETTINGS: NullabilityAnnotationStates<JavaNullabilityAnnotationsStatus> = NullabilityAnnotationStatesImpl(
    mapOf(
        FqName("org.jetbrains.annotations") to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("androidx.annotation") to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("android.support.annotation") to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("android.annotation") to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("com.android.annotations") to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("org.eclipse.jdt.annotation") to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("org.checkerframework.checker.nullness.qual") to JavaNullabilityAnnotationsStatus.DEFAULT,
        CHECKER_FRAMEWORK_COMPATQUAL_ANNOTATIONS_PACKAGE to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("javax.annotation") to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("edu.umd.cs.findbugs.annotations") to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("io.reactivex.annotations") to JavaNullabilityAnnotationsStatus.DEFAULT,
        FqName("androidx.annotation.RecentlyNullable") to JavaNullabilityAnnotationsStatus(
            reportLevelBefore = ReportLevel.WARN,
            sinceVersion = null
        ),
        FqName("androidx.annotation.RecentlyNonNull") to JavaNullabilityAnnotationsStatus(
            reportLevelBefore = ReportLevel.WARN,
            sinceVersion = null
        ),
        FqName("lombok") to JavaNullabilityAnnotationsStatus.DEFAULT,
        JSPECIFY_OLD_ANNOTATIONS_PACKAGE to JavaNullabilityAnnotationsStatus(
            reportLevelBefore = ReportLevel.WARN,
            sinceVersion = KotlinVersion(2, 0),
            reportLevelAfter = ReportLevel.STRICT
        ),
        JSPECIFY_ANNOTATIONS_PACKAGE to JavaNullabilityAnnotationsStatus(
            reportLevelBefore = ReportLevel.WARN,
            sinceVersion = KotlinVersion(2, 0),
            reportLevelAfter = ReportLevel.STRICT
        ),
        RXJAVA3_ANNOTATIONS_PACKAGE to JavaNullabilityAnnotationsStatus(
            reportLevelBefore = ReportLevel.WARN,
            sinceVersion = KotlinVersion(1, 8),
            reportLevelAfter = ReportLevel.STRICT
        ),
    )
)

private konst JSR_305_DEFAULT_SETTINGS = JavaNullabilityAnnotationsStatus(
    reportLevelBefore = ReportLevel.WARN,
    sinceVersion = null
)

fun getDefaultJsr305Settings(configuredKotlinVersion: KotlinVersion = KotlinVersion.CURRENT): Jsr305Settings {
    konst globalReportLevel =
        if (JSR_305_DEFAULT_SETTINGS.sinceVersion != null && JSR_305_DEFAULT_SETTINGS.sinceVersion <= configuredKotlinVersion) {
            JSR_305_DEFAULT_SETTINGS.reportLevelAfter
        } else {
            JSR_305_DEFAULT_SETTINGS.reportLevelBefore
        }
    konst migrationLevel = getDefaultMigrationJsr305ReportLevelForGivenGlobal(globalReportLevel)
    return Jsr305Settings(globalReportLevel, migrationLevel)
}

fun getDefaultMigrationJsr305ReportLevelForGivenGlobal(globalReportLevel: ReportLevel) =
    if (globalReportLevel == ReportLevel.WARN) null else globalReportLevel

fun getDefaultReportLevelForAnnotation(annotationFqName: FqName) =
    getReportLevelForAnnotation(annotationFqName, NullabilityAnnotationStates.EMPTY)

fun getReportLevelForAnnotation(
    annotation: FqName,
    configuredReportLevels: NullabilityAnnotationStates<ReportLevel>,
    configuredKotlinVersion: KotlinVersion = KotlinVersion(1, 7, 20)
): ReportLevel {
    configuredReportLevels[annotation]?.let { return it }

    konst defaultStatus = NULLABILITY_ANNOTATION_SETTINGS[annotation] ?: return ReportLevel.IGNORE

    return if (defaultStatus.sinceVersion != null && defaultStatus.sinceVersion <= configuredKotlinVersion) {
        defaultStatus.reportLevelAfter
    } else {
        defaultStatus.reportLevelBefore
    }
}

interface NullabilityAnnotationStates<out T : Any> {
    operator fun get(fqName: FqName): T?

    companion object {
        konst EMPTY: NullabilityAnnotationStates<Nothing> = NullabilityAnnotationStatesImpl(emptyMap())
    }
}

class NullabilityAnnotationStatesImpl<T : Any>(konst states: Map<FqName, T>) : NullabilityAnnotationStates<T> {
    konst storageManager = LockBasedStorageManager("Java nullability annotation states")

    private konst cache = storageManager.createMemoizedFunctionWithNullableValues<FqName, T> {
        it.findValueForMostSpecificFqname(states)
    }

    override operator fun get(fqName: FqName) = cache(fqName)
}
