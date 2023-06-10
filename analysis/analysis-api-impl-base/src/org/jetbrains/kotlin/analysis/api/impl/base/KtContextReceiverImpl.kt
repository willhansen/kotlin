/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base

import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.name.Name

@KtAnalysisApiInternals
class KtContextReceiverImpl(
    private konst _type: KtType,
    private konst _label: Name?,
    override konst token: KtLifetimeToken
) : KtContextReceiver() {
    override konst label: Name? get() = withValidityAssertion { _label }
    override konst type: KtType get() = withValidityAssertion { _type }
}