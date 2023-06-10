/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.diagnostics

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeOwner
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import kotlin.reflect.KClass

public interface KtDiagnostic : KtLifetimeOwner {
    public konst severity: Severity
    public konst factoryName: String?
    public konst defaultMessage: String
}

public interface KtDiagnosticWithPsi<out PSI : PsiElement> : KtDiagnostic {
    public konst psi: PSI
    public konst textRanges: Collection<TextRange>
    public konst diagnosticClass: KClass<out KtDiagnosticWithPsi<PSI>>
}

public class KtNonBoundToPsiErrorDiagnostic(
    override konst factoryName: String?,
    override konst defaultMessage: String,
    override konst token: KtLifetimeToken,
) : KtDiagnostic {
    override konst severity: Severity get() = withValidityAssertion { Severity.ERROR }
}

public fun KtDiagnostic.getDefaultMessageWithFactoryName(): String =
    if (factoryName == null) defaultMessage
    else "[$factoryName] $defaultMessage"