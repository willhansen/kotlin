/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

abstract class Stack<T> {
    abstract konst size: Int
    abstract fun top(): T
    abstract fun pop(): T
    abstract fun push(konstue: T)
    abstract fun reset()

    /**
     * returns all elements of the stack in order of retriekonst
     */
    abstract fun all(): List<T>
}

fun <T> stackOf(vararg konstues: T): Stack<T> = StackImpl(*konstues)
konst Stack<*>.isEmpty: Boolean get() = size == 0
konst Stack<*>.isNotEmpty: Boolean get() = size != 0
fun <T> Stack<T>.topOrNull(): T? = if (size == 0) null else top()
fun <T> Stack<T>.popOrNull(): T? = if (size == 0) null else pop()

private class StackImpl<T>(vararg konstues: T) : Stack<T>() {
    private konst stack = mutableListOf(*konstues)

    override fun top(): T = stack[stack.size - 1]
    override fun pop(): T = stack.removeAt(stack.size - 1)

    override fun push(konstue: T) {
        stack.add(konstue)
    }

    override konst size: Int get() = stack.size
    override fun reset() {
        stack.clear()
    }

    override fun all(): List<T> = stack.asReversed()
}
