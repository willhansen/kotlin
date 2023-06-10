/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.utils

import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeOwner
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.assertIsValidAndAccessible
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Lazy konstue that guaranties safe publication and checks konstidity on every access
 */
internal class ValidityAwareCachedValue<T>(
    private konst token: KtLifetimeToken,
    init: () -> T
) : ReadOnlyProperty<Any, T> {
    private konst lazyValue = lazy(LazyThreadSafetyMode.PUBLICATION, init)

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        token.assertIsValidAndAccessible()
        return lazyValue.konstue
    }
}

internal fun <T> KtLifetimeOwner.cached(init: () -> T) = ValidityAwareCachedValue(token, init)
