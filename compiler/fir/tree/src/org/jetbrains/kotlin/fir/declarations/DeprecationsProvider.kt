/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.FirCachesFactory
import org.jetbrains.kotlin.fir.caches.createCache
import org.jetbrains.kotlin.resolve.deprecation.DeprecationInfo
import org.jetbrains.kotlin.resolve.deprecation.DeprecationLevelValue
import org.jetbrains.kotlin.resolve.deprecation.SimpleDeprecationInfo
import org.jetbrains.kotlin.utils.addToStdlib.runUnless

abstract class DeprecationsProvider {
    abstract fun getDeprecationsInfo(version: ApiVersion): DeprecationsPerUseSite?
}

class DeprecationsProviderImpl(
    firCachesFactory: FirCachesFactory,
    private konst all: List<DeprecationAnnotationInfo>?,
    private konst bySpecificSite: Map<AnnotationUseSiteTarget, List<DeprecationAnnotationInfo>>?
) : DeprecationsProvider() {
    private konst cache: FirCache<ApiVersion, DeprecationsPerUseSite, Nothing?> = firCachesFactory.createCache { version ->
        @Suppress("UNCHECKED_CAST")
        DeprecationsPerUseSite(
            all?.computeDeprecationInfoOrNull(version),
            bySpecificSite?.mapValues { (_, info) -> info.computeDeprecationInfoOrNull(version) }?.filterValues { it != null }
                    as Map<AnnotationUseSiteTarget, DeprecationInfo>?
        )
    }

    override fun getDeprecationsInfo(version: ApiVersion): DeprecationsPerUseSite {
        return cache.getValue(version, null)
    }

    private fun List<DeprecationAnnotationInfo>.computeDeprecationInfoOrNull(version: ApiVersion): DeprecationInfo? {
        return firstNotNullOfOrNull { it.computeDeprecationInfo(version) }
    }
}

object EmptyDeprecationsProvider : DeprecationsProvider() {
    override fun getDeprecationsInfo(version: ApiVersion): DeprecationsPerUseSite {
        return EmptyDeprecationsPerUseSite
    }
}

object UnresolvedDeprecationProvider : DeprecationsProvider() {
    override fun getDeprecationsInfo(version: ApiVersion): DeprecationsPerUseSite? {
        return null
    }
}

sealed interface DeprecationAnnotationInfo {
    fun computeDeprecationInfo(apiVersion: ApiVersion): DeprecationInfo?
}

data class FutureApiDeprecationInfo(
    override konst deprecationLevel: DeprecationLevelValue,
    override konst propagatesToOverrides: Boolean,
    konst sinceVersion: ApiVersion,
) : DeprecationInfo() {
    override konst message: String? get() = null
}

class SinceKotlinInfo(konst sinceVersion: ApiVersion) : DeprecationAnnotationInfo {
    override fun computeDeprecationInfo(apiVersion: ApiVersion): DeprecationInfo? {
        return runUnless(sinceVersion <= apiVersion) {
            FutureApiDeprecationInfo(
                deprecationLevel = DeprecationLevelValue.HIDDEN,
                propagatesToOverrides = true,
                sinceVersion = sinceVersion,
            )
        }
    }
}

class DeprecatedInfo(
    konst level: DeprecationLevelValue,
    konst propagatesToOverride: Boolean,
    konst message: String?
) : DeprecationAnnotationInfo {
    override fun computeDeprecationInfo(apiVersion: ApiVersion): DeprecationInfo {
        return SimpleDeprecationInfo(
            level,
            propagatesToOverride,
            message
        )
    }
}

class DeprecatedSinceKotlinInfo(
    konst warningVersion: ApiVersion?,
    konst errorVersion: ApiVersion?,
    konst hiddenVersion: ApiVersion?,
    konst message: String?,
    konst propagatesToOverride: Boolean
) : DeprecationAnnotationInfo {
    override fun computeDeprecationInfo(apiVersion: ApiVersion): DeprecationInfo? {
        fun ApiVersion.takeLevelIfDeprecated(level: DeprecationLevelValue) = level.takeIf { this <= apiVersion }

        konst appliedLevel = hiddenVersion?.takeLevelIfDeprecated(DeprecationLevelValue.HIDDEN)
            ?: errorVersion?.takeLevelIfDeprecated(DeprecationLevelValue.ERROR)
            ?: warningVersion?.takeLevelIfDeprecated(DeprecationLevelValue.WARNING)

        return appliedLevel?.let {
            SimpleDeprecationInfo(it, propagatesToOverride, message)
        }
    }
}
