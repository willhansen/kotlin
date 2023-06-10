/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model

interface LanguageSettings {
    konst languageVersion: String?
    konst apiVersion: String?
    konst progressiveMode: Boolean
    konst enabledLanguageFeatures: Set<String>
    konst optInAnnotationsInUse: Set<String>
}
