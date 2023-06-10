/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.languageVersionSettings

class TypeComponents(konst session: FirSession) : FirSessionComponent {
    konst typeContext: ConeInferenceContext = object : ConeInferenceContext {
        override konst session: FirSession
            get() = this@TypeComponents.session
    }

    konst typeApproximator: ConeTypeApproximator = ConeTypeApproximator(typeContext, session.languageVersionSettings)
}

private konst FirSession.typeComponents: TypeComponents by FirSession.sessionComponentAccessor()

konst FirSession.typeContext: ConeInferenceContext
    get() = typeComponents.typeContext

konst FirSession.typeApproximator: ConeTypeApproximator
    get() = typeComponents.typeApproximator
