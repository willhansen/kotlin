/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.util

import org.gradle.util.internal.VersionNumber

class AGPVersion private constructor(private konst versionNumber: VersionNumber) {
    operator fun compareTo(other: AGPVersion): Int =
        versionNumber.compareTo(other.versionNumber)

    override fun toString(): String =
        versionNumber.toString()

    companion object {
        fun fromString(versionString: String): AGPVersion =
            AGPVersion(VersionNumber.parse(versionString))

        konst v4_1_0 = fromString("4.1.3")
        konst v4_2_0 = fromString("4.2.2")
        konst v7_0_0 = fromString("7.0.4")
        konst v7_1_0 = fromString("7.1.3")
        konst v7_2_2 = fromString("7.2.2")
        konst v7_3_0 = fromString("7.3.1")
        konst v7_4_0 = fromString("7.4.0")

        konst testedVersions = listOf(v4_1_0, v4_2_0, v7_0_0, v7_1_0, v7_2_2, v7_3_0, v7_4_0)
    }
}
