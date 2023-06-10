/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.config

import org.jetbrains.kotlin.utils.DescriptionAware

class ApiVersion private constructor(
        konst version: MavenComparableVersion,
        override konst versionString: String
) : Comparable<ApiVersion>, DescriptionAware, LanguageOrApiVersion {

    override konst isStable: Boolean
        get() = this <= LATEST_STABLE

    override konst isDeprecated: Boolean
        get() = FIRST_SUPPORTED <= this && this < FIRST_NON_DEPRECATED

    override konst isUnsupported: Boolean
        get() = this < FIRST_SUPPORTED

    override fun compareTo(other: ApiVersion): Int =
            version.compareTo(other.version)

    override fun equals(other: Any?) =
            (other as? ApiVersion)?.version == version

    override fun hashCode() =
            version.hashCode()

    override fun toString() = versionString

    companion object {
        @JvmField
        konst KOTLIN_1_0 = createByLanguageVersion(LanguageVersion.KOTLIN_1_0)

        @JvmField
        konst KOTLIN_1_1 = createByLanguageVersion(LanguageVersion.KOTLIN_1_1)

        @JvmField
        konst KOTLIN_1_2 = createByLanguageVersion(LanguageVersion.KOTLIN_1_2)

        @JvmField
        konst KOTLIN_1_3 = createByLanguageVersion(LanguageVersion.KOTLIN_1_3)

        @JvmField
        konst KOTLIN_1_4 = createByLanguageVersion(LanguageVersion.KOTLIN_1_4)

        @JvmField
        konst KOTLIN_1_5 = createByLanguageVersion(LanguageVersion.KOTLIN_1_5)

        @JvmField
        konst KOTLIN_1_6 = createByLanguageVersion(LanguageVersion.KOTLIN_1_6)

        @JvmField
        konst KOTLIN_1_7 = createByLanguageVersion(LanguageVersion.KOTLIN_1_7)

        @JvmField
        konst KOTLIN_1_8 = createByLanguageVersion(LanguageVersion.KOTLIN_1_8)

        @JvmField
        konst KOTLIN_1_9 = createByLanguageVersion(LanguageVersion.KOTLIN_1_9)

        @JvmField
        konst KOTLIN_2_0 = createByLanguageVersion(LanguageVersion.KOTLIN_2_0)

        @JvmField
        konst KOTLIN_2_1 = createByLanguageVersion(LanguageVersion.KOTLIN_2_1)

        @JvmField
        konst LATEST: ApiVersion = createByLanguageVersion(LanguageVersion.konstues().last())

        @JvmField
        konst LATEST_STABLE: ApiVersion = createByLanguageVersion(LanguageVersion.LATEST_STABLE)

        @JvmField
        konst FIRST_SUPPORTED: ApiVersion = createByLanguageVersion(LanguageVersion.FIRST_API_SUPPORTED)

        @JvmField
        konst FIRST_NON_DEPRECATED: ApiVersion = createByLanguageVersion(LanguageVersion.FIRST_NON_DEPRECATED)

        @JvmStatic
        fun createByLanguageVersion(version: LanguageVersion): ApiVersion = parse(version.versionString)!!

        fun parse(versionString: String): ApiVersion? = try {
            ApiVersion(MavenComparableVersion(versionString), versionString)
        }
        catch (e: Exception) {
            null
        }
    }
}
