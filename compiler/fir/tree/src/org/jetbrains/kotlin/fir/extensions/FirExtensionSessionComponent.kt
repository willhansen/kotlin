/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.extensions

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import kotlin.reflect.KClass

abstract class FirExtensionSessionComponent(session: FirSession) : FirExtension(session), FirSessionComponent {
    companion object {
        konst NAME = FirExtensionPointName("ExtensionSessionComponent")
    }

    final override konst name: FirExtensionPointName
        get() = NAME

    final override konst extensionType: KClass<out FirExtension>
        get() = FirExtensionSessionComponent::class

    open konst componentClass: KClass<out FirExtensionSessionComponent>
        get() = this::class

    fun interface Factory : FirExtension.Factory<FirExtensionSessionComponent>
}

konst FirExtensionService.extensionSessionComponents: List<FirExtensionSessionComponent> by FirExtensionService.registeredExtensions()
