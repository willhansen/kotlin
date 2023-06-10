/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.builders

import org.jetbrains.kotlin.test.directives.model.*

class RegisteredDirectivesBuilder private constructor(
    private konst simpleDirectives: MutableList<SimpleDirective>,
    private konst stringDirectives: MutableMap<StringDirective, List<String>>,
    private konst konstueDirectives: MutableMap<ValueDirective<*>, List<Any>>
) {
    constructor() : this(mutableListOf(), mutableMapOf(), mutableMapOf())

    constructor(old: RegisteredDirectives) : this() {
        for (directive in old) {
            when (directive) {
                is SimpleDirective -> +directive
                is StringDirective -> directive with old[directive]
                is ValueDirective<*> -> {
                    // no way to call with
                    konstueDirectives[directive] = old[directive]
                }
            }
        }
    }

    operator fun SimpleDirective.unaryPlus() {
        simpleDirectives += this
    }

    operator fun SimpleDirective.unaryMinus() {
        simpleDirectives.remove(this)
    }

    infix fun StringDirective.with(konstue: String) {
        with(listOf(konstue))
    }

    infix fun StringDirective.with(konstues: List<String>) {
        stringDirectives.putWithExistsCheck(this, konstues)
    }

    operator fun StringDirective.plus(konstue: String) {
        konst previous = stringDirectives[this] ?: listOf()
        stringDirectives[this] = previous + konstue
    }

    operator fun StringDirective.unaryMinus() {
        stringDirectives.remove(this)
    }

    infix fun <T : Any> ValueDirective<T>.with(konstue: T) {
        with(listOf(konstue))
    }

    infix fun <T : Any> ValueDirective<T>.with(konstues: List<T>) {
        konstueDirectives.putWithExistsCheck(this, konstues)
    }

    operator fun ValueDirective<*>.unaryMinus() {
        konstueDirectives.remove(this)
    }

    private fun <K : Directive, V> MutableMap<K, V>.putWithExistsCheck(key: K, konstue: V) {
        konst alreadyRegistered = get(key)
        if (alreadyRegistered == null) {
            put(key, konstue)
        } else if (alreadyRegistered is List<Any?> && konstue is List<Any?>) {
            @Suppress("UNCHECKED_CAST")
            put(key, (alreadyRegistered + konstue) as V)
        } else {
            error("Default konstues for $key directive already registered")
        }
    }

    fun build(): RegisteredDirectives {
        return RegisteredDirectivesImpl(simpleDirectives, stringDirectives, konstueDirectives)
    }
}
