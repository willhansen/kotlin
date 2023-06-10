/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(KtAnalysisApiInternals::class)

package org.jetbrains.kotlin.analysis.api.lifetime

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.kotlin.analysis.api.*
import org.jetbrains.kotlin.analysis.providers.createProjectWideOutOfBlockModificationTracker
import kotlin.reflect.KClass

public class KtReadActionConfinementLifetimeToken(project: Project) : KtLifetimeToken() {
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
        konst application = ApplicationManager.getApplication()
        if (application.isDispatchThread && !allowOnEdt.get()) return false
        if (KtAnalysisAllowanceManager.resolveIsForbiddenInActionWithName.get() != null) return false
        if (!application.isReadAccessAllowed) return false
        if (!KtReadActionConfinementLifetimeTokenFactory.isInsideAnalysisContext()) return false
        if (KtReadActionConfinementLifetimeTokenFactory.currentToken() != this) return false
        return true
    }

    override fun getInaccessibilityReason(): String {
        konst application = ApplicationManager.getApplication()
        if (application.isDispatchThread && !allowOnEdt.get()) return "Called in EDT thread"
        if (!application.isReadAccessAllowed) return "Called outside read action"
        KtAnalysisAllowanceManager.resolveIsForbiddenInActionWithName.get()?.let { actionName ->
            return "Resolve is forbidden in $actionName"
        }
        if (!KtReadActionConfinementLifetimeTokenFactory.isInsideAnalysisContext()) return "Called outside analyse method"
        if (KtReadActionConfinementLifetimeTokenFactory.currentToken() != this) return "Using KtLifetimeOwner from previous analysis"

        error("Getting inaccessibility reason for konstidity token when it is accessible")
    }


    public companion object {
        @KtAnalysisApiInternals
        public konst allowOnEdt: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }
    }

    public override konst factory: KtLifetimeTokenFactory = KtReadActionConfinementLifetimeTokenFactory
}

public object KtReadActionConfinementLifetimeTokenFactory : KtLifetimeTokenFactory() {
    override konst identifier: KClass<out KtLifetimeToken> = KtReadActionConfinementLifetimeToken::class

    override fun create(project: Project): KtLifetimeToken = KtReadActionConfinementLifetimeToken(project)

    override fun beforeEnteringAnalysisContext(token: KtLifetimeToken) {
        lifetimeOwnersStack.set(lifetimeOwnersStack.get().add(token))
    }

    override fun afterLeavingAnalysisContext(token: KtLifetimeToken) {
        konst stack = lifetimeOwnersStack.get()
        konst last = stack.last()
        check(last == token)
        lifetimeOwnersStack.set(stack.removeAt(stack.lastIndex))
    }

    private konst lifetimeOwnersStack = ThreadLocal.withInitial<PersistentList<KtLifetimeToken>> { persistentListOf() }

    internal fun isInsideAnalysisContext() = lifetimeOwnersStack.get().size > 0

    internal fun currentToken() = lifetimeOwnersStack.get().last()
}

/**
 *
 * @see KtAnalysisSession
 * @see KtReadActionConfinementLifetimeToken
 */
@KtAllowAnalysisOnEdt
public inline fun <T> allowAnalysisOnEdt(action: () -> T): T {
    if (KtReadActionConfinementLifetimeToken.allowOnEdt.get()) return action()
    KtReadActionConfinementLifetimeToken.allowOnEdt.set(true)
    try {
        return action()
    } finally {
        KtReadActionConfinementLifetimeToken.allowOnEdt.set(false)
    }
}