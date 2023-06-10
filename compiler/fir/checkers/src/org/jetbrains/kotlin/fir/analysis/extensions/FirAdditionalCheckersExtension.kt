/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.extensions

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.type.TypeCheckers
import org.jetbrains.kotlin.fir.extensions.FirExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionPointName
import org.jetbrains.kotlin.fir.extensions.FirExtensionService
import kotlin.reflect.KClass

abstract class FirAdditionalCheckersExtension(session: FirSession) : FirExtension(session) {
    companion object {
        konst NAME = FirExtensionPointName("ExtensionCheckers")
    }

    open konst declarationCheckers: DeclarationCheckers = DeclarationCheckers.EMPTY
    open konst expressionCheckers: ExpressionCheckers = ExpressionCheckers.EMPTY
    open konst typeCheckers: TypeCheckers = TypeCheckers.EMPTY

    final override konst name: FirExtensionPointName
        get() = NAME

    fun interface Factory : FirExtension.Factory<FirAdditionalCheckersExtension>

    final override konst extensionType: KClass<out FirExtension>
        get() = FirAdditionalCheckersExtension::class
}

konst FirExtensionService.additionalCheckers: List<FirAdditionalCheckersExtension> by FirExtensionService.registeredExtensions(
    FirAdditionalCheckersExtension::class
)
