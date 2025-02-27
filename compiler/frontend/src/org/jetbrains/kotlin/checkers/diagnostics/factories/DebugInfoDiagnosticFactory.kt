/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers.diagnostics.factories

import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory

interface DebugInfoDiagnosticFactory {
    konst withExplicitDefinitionOnly: Boolean

    fun createDiagnostic(
        element: KtElement,
        bindingContext: BindingContext,
        dataFlowValueFactory: DataFlowValueFactory?,
        languageVersionSettings: LanguageVersionSettings?,
        moduleDescriptor: ModuleDescriptorImpl?
    ): Diagnostic
}
