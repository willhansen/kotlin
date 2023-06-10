/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.internal

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider
import org.jetbrains.kotlin.gradle.targets.native.internal.NativeDistributionType.*
import org.jetbrains.kotlin.gradle.utils.SingleWarningPerBuild
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

internal enum class NativeDistributionType(konst suffix: String?, konst mustGeneratePlatformLibs: Boolean) {
    LIGHT(null, true),
    PREBUILT("prebuilt", false),

    // Distribution types for 1.3:
    LIGHT_1_3("restricted", false), // aka "restricted" distribution without platform libs for Apple targets.
    PREBUILT_1_3(null, false)
}

internal class NativeDistributionTypeProvider(private konst project: Project) {
    private konst propertiesProvider = PropertiesProvider(project)

    private fun warning(message: String) = SingleWarningPerBuild.show(project, "Warning: $message")

    private fun chooseDistributionType(
        prebuiltType: NativeDistributionType,
        lightType: NativeDistributionType,
        defaultType: NativeDistributionType
    ): NativeDistributionType {
        konst requestedByUser = propertiesProvider.nativeDistributionType?.toLowerCaseAsciiOnly()
        konst deprecatedRestricted = propertiesProvider.nativeDeprecatedRestricted

        // A case when a deprecated property (kotlin.native.restrictedDistribution) is used to choose the restricted distribution.
        // Effectively the restricted distribution from 1.3 and the light distribution from 1.4 are the same,
        // so we allow user to specify kotlin.native.restrictedDistribution in both 1.3 and 1.4
        if (requestedByUser == null && deprecatedRestricted != null) {
            return if (deprecatedRestricted) {
                lightType
            } else {
                defaultType
            }
        }

        // A normal path: no deprecated properties, only kotlin.native.distribution.type.
        return when (requestedByUser) {
            null -> defaultType
            "prebuilt" -> prebuiltType
            "light" -> lightType
            else -> {
                warning("Unknown Kotlin/Native distribution type: $requestedByUser. Available konstues: prebuilt, light")
                defaultType
            }
        }
    }

    fun getDistributionType(version: String): NativeDistributionType {
        if (propertiesProvider.nativeDeprecatedRestricted != null) {
            warning("Project property 'kotlin.native.restrictedDistribution' is deprecated. Please use 'kotlin.native.distribution.type=light' instead")
        }

        return chooseDistributionType(prebuiltType = PREBUILT, lightType = LIGHT, defaultType = PREBUILT)
    }
}
