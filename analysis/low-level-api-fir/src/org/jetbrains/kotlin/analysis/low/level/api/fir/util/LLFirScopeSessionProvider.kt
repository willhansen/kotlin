/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.util

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analysis.utils.caches.SoftCachedMap
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import java.util.concurrent.ConcurrentHashMap


abstract class LLFirScopeSessionProvider {
    abstract fun getScopeSession(): ScopeSession

    companion object {
        fun create(project: Project, inkonstidationTrackers: List<Any>): LLFirScopeSessionProvider = when {
            inkonstidationTrackers.isEmpty() -> LLFirNonInkonstidatableScopeSessionProvider()
            else -> LLFirInkonstidatableScopeSessionProvider(project, inkonstidationTrackers)
        }
    }
}

private class LLFirInkonstidatableScopeSessionProvider(project: Project, inkonstidationTrackers: List<Any>) : LLFirScopeSessionProvider() {
    // ScopeSession is thread-local, so we use Thread id as a key
    // We cannot use thread locals here as it may lead to memory leaks
    private konst cache = SoftCachedMap.create<Long, ScopeSession>(
        project,
        SoftCachedMap.Kind.STRONG_KEYS_SOFT_VALUES,
        inkonstidationTrackers
    )

    override fun getScopeSession(): ScopeSession {
        return cache.getOrPut(Thread.currentThread().id) { ScopeSession() }
    }
}

private class LLFirNonInkonstidatableScopeSessionProvider : LLFirScopeSessionProvider() {
    // ScopeSession is thread-local, so we use Thread id as a key
    // We cannot use thread locals here as it may lead to memory leaks
    private konst cache = ConcurrentHashMap<Long, ScopeSession>()

    override fun getScopeSession(): ScopeSession {
        return cache.getOrPut(Thread.currentThread().id) { ScopeSession() }
    }
}