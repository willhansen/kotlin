/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.context

import org.jetbrains.kotlin.fir.resolve.SessionHolder
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculator

abstract class AbstractCheckerContext(
    override konst sessionHolder: SessionHolder,
    override konst returnTypeCalculator: ReturnTypeCalculator,
    override konst allInfosSuppressed: Boolean,
    override konst allWarningsSuppressed: Boolean,
    override konst allErrorsSuppressed: Boolean
) : CheckerContext()