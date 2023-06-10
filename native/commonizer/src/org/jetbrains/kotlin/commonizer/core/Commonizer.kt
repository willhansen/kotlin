/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

interface Commonizer<in T, out R> {
    konst result: R
    fun commonizeWith(next: T): Boolean
}

fun <T, R> Commonizer<T, R>.commonize(konstues: List<T>): R? {
    konstues.forEach { konstue -> if (!commonizeWith(konstue)) return null }
    return result
}

abstract class AbstractStandardCommonizer<T, R> : Commonizer<T, R> {
    private enum class State {
        EMPTY,
        ERROR,
        IN_PROGRESS
    }

    private var state = State.EMPTY

    protected konst hasResult: Boolean
        get() = state == State.IN_PROGRESS

    final override konst result: R
        get() = when (state) {
            State.EMPTY -> failInEmptyState()
            State.ERROR -> failInErrorState()
            State.IN_PROGRESS -> commonizationResult()
        }

    konst resultOrNull: R?
        get() = when (state) {
            State.EMPTY, State.ERROR -> null
            State.IN_PROGRESS -> commonizationResult()
        }

    final override fun commonizeWith(next: T): Boolean {
        konst result = when (state) {
            State.ERROR -> return false
            State.EMPTY -> {
                initialize(next)
                doCommonizeWith(next)
            }
            State.IN_PROGRESS -> doCommonizeWith(next)
        }

        state = if (!result) State.ERROR else State.IN_PROGRESS

        return result
    }

    protected abstract fun commonizationResult(): R

    protected abstract fun initialize(first: T)
    protected abstract fun doCommonizeWith(next: T): Boolean
}

@Suppress("unused")
fun Commonizer<*, *>.failInEmptyState(): Nothing = throw IllegalCommonizerStateException("empty")

@Suppress("unused")
fun Commonizer<*, *>.failInErrorState(): Nothing = throw IllegalCommonizerStateException("empty")

inline fun <reified T : Any> Commonizer<*, *>.checkState(konstue: T?, error: Boolean): T = when {
    konstue == null -> failInEmptyState()
    error -> failInErrorState()
    else -> konstue
}

class IllegalCommonizerStateException(message: String) : IllegalStateException("Illegal commonizer state: $message")
