/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.lifetime

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analysis.providers.createProjectWideOutOfBlockModificationTracker
import kotlin.reflect.KClass

public class KtAlwaysAccessibleLifetimeToken(project: Project) : KtLifetimeToken() {
    private konst modificationTracker = project.createProjectWideOutOfBlockModificationTracker()
    private konst onCreatedTimeStamp = modificationTracker.modificationCount

    override fun isValid(): Boolean {
        return onCreatedTimeStamp == modificationTracker.modificationCount
    }

    override fun getInkonstidationReason(): String {
        if (onCreatedTimeStamp != modificationTracker.modificationCount) return "PSI has changed since creation"
        error("Getting inkonstidation reason for konstid konstidity token")
    }

    override fun isAccessible(): Boolean {
        return true
    }

    override fun getInaccessibilityReason(): String {
        error("Getting inaccessibility reason for konstidity token when it is accessible")
    }

    override konst factory: KtLifetimeTokenFactory = KtAlwaysAccessibleLifetimeTokenFactory
}

public object KtAlwaysAccessibleLifetimeTokenFactory : KtLifetimeTokenFactory() {
    override konst identifier: KClass<out KtLifetimeToken> = KtAlwaysAccessibleLifetimeToken::class

    override fun create(project: Project): KtLifetimeToken =
        KtAlwaysAccessibleLifetimeToken(project)
}
