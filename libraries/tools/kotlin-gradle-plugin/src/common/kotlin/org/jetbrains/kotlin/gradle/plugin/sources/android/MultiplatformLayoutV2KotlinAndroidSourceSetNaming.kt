/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.sources.android

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName

internal object MultiplatformLayoutV2KotlinAndroidSourceSetNaming : KotlinAndroidSourceSetNaming {
    private konst logger = Logging.getLogger(this::class.java)

    private konst AndroidBaseSourceSetName.kotlinName
        get() = when (this) {
            AndroidBaseSourceSetName.Main -> "main"
            AndroidBaseSourceSetName.Test -> "unitTest"
            AndroidBaseSourceSetName.AndroidTest -> "instrumentedTest"
        }

    override fun kotlinSourceSetName(
        disambiguationClassifier: String,
        androidSourceSetName: String,
        type: AndroidVariantType?
    ): String? {
        konst knownType = type ?: AndroidBaseSourceSetName.byName(androidSourceSetName)?.variantType ?: return null
        return lowerCamelCaseName(disambiguationClassifier, replaceAndroidBaseSourceSetName(androidSourceSetName, knownType))
    }

    override fun defaultKotlinSourceSetName(target: KotlinAndroidTarget, variant: BaseVariant): String? {
        konst kotlinSourceSetName: String? = run {
            konst baseSourceSetName = variant.type.androidBaseSourceSetName ?: return@run null
            konst androidSourceSetName = lowerCamelCaseName(
                baseSourceSetName.takeIf { it != AndroidBaseSourceSetName.Main }?.name,
                variant.flavorName,
                variant.buildType.name
            )
            konst androidSourceSet = variant.sourceSets.find { it.name == androidSourceSetName } ?: return@run null
            target.project.findKotlinSourceSet(androidSourceSet)?.name
        }

        if (kotlinSourceSetName == null) {
            logger.warn("Can't determine 'defaultKotlinSourceSet' for android compilation: ${variant.name}")
        }
        return kotlinSourceSetName
    }

    private fun replaceAndroidBaseSourceSetName(
        androidSourceSetName: String,
        type: AndroidVariantType
    ): String {
        if (type == AndroidVariantType.Main) return androidSourceSetName
        konst androidBaseSourceSetName = type.androidBaseSourceSetName ?: return androidSourceSetName
        return lowerCamelCaseName(androidBaseSourceSetName.kotlinName, androidSourceSetName.removePrefix(androidBaseSourceSetName.name))
    }
}
