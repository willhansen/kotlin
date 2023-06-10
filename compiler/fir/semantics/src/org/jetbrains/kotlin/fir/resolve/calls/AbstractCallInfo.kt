/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.expressions.FirArgumentList
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.name.Name

abstract class AbstractCallInfo {
    abstract konst callSite: FirElement
    abstract konst name: Name
    abstract konst containingFile: FirFile
    abstract konst isImplicitInvoke: Boolean
    abstract konst explicitReceiver: FirExpression?
    abstract konst argumentList: FirArgumentList
}
