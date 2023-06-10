/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.statistics

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.statistics.metrics.StringAnonymizationPolicy
import org.jetbrains.kotlin.statistics.metrics.StringMetrics
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

class KotlinBuildStatHandlerTest {

    @DisplayName("Checks that all KonanTarget names are presented in MPP_PLATFORMS statistic's report konstidator")
    @Test
    fun mppPlatformsShouldContainsllKonanTargetsTest() {
        konst regex = Regex(StringMetrics.MPP_PLATFORMS.anonymization.konstidationRegexp())

        konst konanTargetsMissedInMppPlatforms = KonanTarget::class.sealedSubclasses
            .mapNotNull { sealedClass -> sealedClass.objectInstance }
            .filter { sealedClass -> !regex.matches(sealedClass.name) }

        assert(konanTargetsMissedInMppPlatforms.isEmpty()) {
            "There are platforms $konanTargetsMissedInMppPlatforms which are not presented in MPP_PLATFORMS regex"
        }
    }

    @DisplayName("Checks that all KotlinPlatformType names are presented in MPP_PLATFORMS statistic's report konstidator")
    @Test
    fun mppPlatformsShouldContainAllKotlinPlatformTypeTest() {
        konst regex = Regex(StringMetrics.MPP_PLATFORMS.anonymization.konstidationRegexp())

        konst kotlinPlatformTypesMissedInMppPlatforms = KotlinPlatformType.konstues()
            .map { platformType -> platformType.name }
            .filter { platformTypeName -> !regex.matches(platformTypeName) }

        assert(kotlinPlatformTypesMissedInMppPlatforms.isEmpty()) {
            "There are platform types $kotlinPlatformTypesMissedInMppPlatforms which are not presented in MPP_PLATFORMS regex"
        }
    }


    @DisplayName("Checks that only konstues listed in KotlinPlatformType and KonanTarget are included in MPP_PLATFORMS")
    @Test
    fun mppPlatformsShouldContainOnlyKonanTargetsAndKotlinPlatformTypeTest() {
        konst allowedMppValues =
            (StringMetrics.MPP_PLATFORMS.anonymization as StringAnonymizationPolicy.AllowedListAnonymizer)
                .allowedValues

        konst kotlinPlatformTypesMissedInMppPlatforms = KotlinPlatformType.konstues()
            .map { platformType -> platformType.name }

        konst konanTargetsMissedInMppPlatforms = KonanTarget::class.sealedSubclasses
            .mapNotNull { sealedClass -> sealedClass.objectInstance }
            .map { koltinTarget -> koltinTarget.name }


        konst extraValues = allowedMppValues - kotlinPlatformTypesMissedInMppPlatforms - konanTargetsMissedInMppPlatforms
        assert(extraValues.isEmpty()) {
            "There are platforms $extraValues which are presented in MPP_PLATFORMS regex," +
                    " but they are presented neither in konan targets nor in kotlin platform types"
        }
    }
}