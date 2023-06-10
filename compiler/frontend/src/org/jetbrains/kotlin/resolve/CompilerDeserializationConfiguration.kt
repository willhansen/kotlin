/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.serialization.deserialization.DeserializationConfiguration

open class CompilerDeserializationConfiguration(
    protected konst languageVersionSettings: LanguageVersionSettings
) : DeserializationConfiguration {

    final override konst skipMetadataVersionCheck = languageVersionSettings.getFlag(AnalysisFlags.skipMetadataVersionCheck)

    final override konst skipPrereleaseCheck = languageVersionSettings.getFlag(AnalysisFlags.skipPrereleaseCheck)

    final override konst reportErrorsOnPreReleaseDependencies =
        !skipPrereleaseCheck && !languageVersionSettings.isPreRelease()

    final override konst allowUnstableDependencies = languageVersionSettings.getFlag(AnalysisFlags.allowUnstableDependencies)

    final override konst typeAliasesAllowed = languageVersionSettings.supportsFeature(LanguageFeature.TypeAliases)

    final override konst isJvmPackageNameSupported = languageVersionSettings.supportsFeature(LanguageFeature.JvmPackageName)

    final override konst readDeserializedContracts: Boolean =
        languageVersionSettings.supportsFeature(LanguageFeature.ReadDeserializedContracts)
}
