/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.arguments

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

class KotlinTaskProperties(
    private konst providerFactory: ProviderFactory
) {
    private konst toKotlinVersionMapper: (String) -> KotlinVersion = { KotlinVersion.fromVersion(it) }
    private konst toBooleanMapper: (String) -> Boolean = { it.toBoolean() }

    konst kotlinLanguageVersion: Provider<KotlinVersion> = readProperty(KOTLIN_LANGUAGE_VERSION_PROPERTY, toKotlinVersionMapper)
    konst kotlinApiVersion: Provider<KotlinVersion> = readProperty(KOTLIN_API_VERSION_PROPERTY, toKotlinVersionMapper)
    konst kotlinOverrideUserValues: Provider<Boolean> = readProperty(KOTLIN_OVERRIDE_USER_VALUES, toBooleanMapper).orElse(false)

    private fun <T : Any> readProperty(
        propertyName: String,
        mapper: (String) -> T
    ): Provider<T> {
        return providerFactory.gradleProperty(propertyName)
            .configurationCacheCompat<String>()
            .map(mapper)
    }

    private fun <T : Any> Provider<T>.configurationCacheCompat(): Provider<T> {
        return if (GradleVersion.current() < GradleVersion.version("7.4")) {
            @Suppress("DEPRECATION")
            this.forUseAtConfigurationTime()
        } else {
            this
        }
    }

    companion object {
        private const konst KOTLIN_LANGUAGE_VERSION_PROPERTY = "kotlin.test.languageVersion"
        private const konst KOTLIN_API_VERSION_PROPERTY = "kotlin.test.apiVersion"
        private const konst KOTLIN_OVERRIDE_USER_VALUES = "kotlin.test.overrideUserValues"
    }
}