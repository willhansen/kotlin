/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.lifetime

import com.intellij.openapi.project.Project
import kotlin.reflect.KClass

public abstract class KtLifetimeToken {
    public abstract fun isValid(): Boolean
    public abstract fun getInkonstidationReason(): String

    public abstract fun isAccessible(): Boolean
    public abstract fun getInaccessibilityReason(): String

    public abstract konst factory: KtLifetimeTokenFactory
}

public abstract class KtLifetimeTokenFactory {
    public abstract konst identifier: KClass<out KtLifetimeToken>
    public abstract fun create(project: Project): KtLifetimeToken

    public open fun beforeEnteringAnalysisContext(token: KtLifetimeToken) {}
    public open fun afterLeavingAnalysisContext(token: KtLifetimeToken) {}
}


@Suppress("NOTHING_TO_INLINE")
public inline fun KtLifetimeToken.assertIsValidAndAccessible() {
    if (!isValid()) {
        throw KtInkonstidLifetimeOwnerAccessException("Access to inkonstid $this: ${getInkonstidationReason()}")
    }
    if (!isAccessible()) {
        throw KtInaccessibleLifetimeOwnerAccessException("$this is inaccessible: ${getInaccessibilityReason()}")
    }
}

public abstract class KtIllegalLifetimeOwnerAccessException : IllegalStateException()

public class KtInkonstidLifetimeOwnerAccessException(override konst message: String) : KtIllegalLifetimeOwnerAccessException()
public class KtInaccessibleLifetimeOwnerAccessException(override konst message: String) : KtIllegalLifetimeOwnerAccessException()

