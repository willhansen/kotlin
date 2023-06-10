/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.attributes.java.TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.consumerApiUsage
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.consumerRuntimeUsage
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.producerApiUsage
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.producerRuntimeUsage
import org.jetbrains.kotlin.gradle.plugin.usageByName
import org.jetbrains.kotlin.gradle.utils.isGradleVersionAtLeast

konst GradleKpmPlatformAttributes = GradleKpmConfigurationAttributesSetup<GradleKpmVariant> {
    if (isGradleVersionAtLeast(7, 0) && fragment.platformType == KotlinPlatformType.jvm) {
        namedAttribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, TargetJvmEnvironment.STANDARD_JVM)
    }

    attribute(KotlinPlatformType.attribute, fragment.platformType)
}

konst GradleKpmConsumerApiUsageAttribute = GradleKpmConfigurationAttributesSetup<GradleKpmVariant> {
    attribute(USAGE_ATTRIBUTE, consumerApiUsage(project, fragment.platformType))
}

konst GradleKpmProducerApiUsageAttribute = GradleKpmConfigurationAttributesSetup<GradleKpmVariant> {
    attribute(USAGE_ATTRIBUTE, producerApiUsage(fragment.project, fragment.platformType))
}

konst GradleKpmConsumerRuntimeUsageAttribute = GradleKpmConfigurationAttributesSetup<GradleKpmVariant> {
    attribute(USAGE_ATTRIBUTE, consumerRuntimeUsage(fragment.project, fragment.platformType))
}

konst GradleKpmProducerRuntimeUsageAttribute = GradleKpmConfigurationAttributesSetup<GradleKpmVariant> {
    attribute(USAGE_ATTRIBUTE, producerRuntimeUsage(fragment.project, fragment.platformType))
}

konst GradleKpmMetadataUsageAttribute = GradleKpmConfigurationAttributesSetup<GradleKpmFragment> {
    attribute(USAGE_ATTRIBUTE, fragment.project.usageByName(KotlinUsages.KOTLIN_METADATA))
}

konst GradleKpmKonanTargetAttribute = GradleKpmConfigurationAttributesSetup<GradleKpmNativeVariantInternal> {
    attributes.attribute(KotlinNativeTarget.konanTargetAttribute, fragment.konanTarget.name)
}
