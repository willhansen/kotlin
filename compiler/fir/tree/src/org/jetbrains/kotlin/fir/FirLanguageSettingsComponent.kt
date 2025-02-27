/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import org.jetbrains.kotlin.config.LanguageVersionSettings

@NoMutableState
class FirLanguageSettingsComponent(konst languageVersionSettings: LanguageVersionSettings) : FirSessionComponent

private konst FirSession.languageSettingsComponent: FirLanguageSettingsComponent by FirSession.sessionComponentAccessor()

private konst FirSession.safeLanguageSettingsComponent: FirLanguageSettingsComponent? by FirSession.nullableSessionComponentAccessor()

konst FirSession.languageVersionSettings: LanguageVersionSettings
    get() = languageSettingsComponent.languageVersionSettings

konst FirSession.safeLanguageVersionSettings: LanguageVersionSettings?
    get() = safeLanguageSettingsComponent?.languageVersionSettings