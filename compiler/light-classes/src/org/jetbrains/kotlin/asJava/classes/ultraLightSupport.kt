/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.classes

import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper
import org.jetbrains.kotlin.config.JvmAnalysisFlags
import org.jetbrains.kotlin.config.JvmDefaultMode
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver

interface KtUltraLightSupport {
    konst moduleName: String
    konst deprecationResolver: DeprecationResolver
    konst typeMapper: KotlinTypeMapper
    konst moduleDescriptor: ModuleDescriptor
    konst languageVersionSettings: LanguageVersionSettings
    konst jvmTarget: JvmTarget

    fun possiblyHasAlias(file: KtFile, shortName: Name): Boolean
}

internal konst KtUltraLightSupport.jvmDefaultMode: JvmDefaultMode get() = languageVersionSettings.getFlag(JvmAnalysisFlags.jvmDefaultMode)