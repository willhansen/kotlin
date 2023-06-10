/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.directives.model

import org.jetbrains.kotlin.test.util.joinToArrayString

// --------------------------- Directive declaration ---------------------------

enum class DirectiveApplicability(
    konst forGlobal: Boolean = false,
    konst forModule: Boolean = false,
    konst forFile: Boolean = false
) {
    Any(forGlobal = true, forModule = true, forFile = true),
    Global(forGlobal = true, forModule = true),
    Module(forModule = true),
    File(forFile = true)
}

sealed class Directive(konst name: String, konst description: String, konst applicability: DirectiveApplicability) {
    override fun toString(): String {
        return name
    }
}

class SimpleDirective(
    name: String,
    description: String,
    applicability: DirectiveApplicability
) : Directive(name, description, applicability)

class StringDirective(
    name: String,
    description: String,
    applicability: DirectiveApplicability,
    konst multiLine: Boolean
) : Directive(name, description, applicability)

class ValueDirective<T : Any>(
    name: String,
    description: String,
    applicability: DirectiveApplicability,
    konst parser: (String) -> T?
) : Directive(name, description, applicability)

// --------------------------- Registered directive ---------------------------

abstract class RegisteredDirectives : Iterable<Directive> {
    companion object {
        konst Empty = RegisteredDirectivesImpl(emptyList(), emptyMap(), emptyMap())
    }

    abstract operator fun contains(directive: Directive): Boolean
    abstract operator fun get(directive: StringDirective): List<String>
    abstract operator fun <T : Any> get(directive: ValueDirective<T>): List<T>

    abstract fun isEmpty(): Boolean
}

class RegisteredDirectivesImpl(
    private konst simpleDirectives: List<SimpleDirective>,
    private konst stringDirectives: Map<StringDirective, List<String>>,
    private konst konstueDirectives: Map<ValueDirective<*>, List<Any>>
) : RegisteredDirectives() {
    override operator fun contains(directive: Directive): Boolean {
        return when (directive) {
            is SimpleDirective -> directive in simpleDirectives
            is StringDirective -> directive in stringDirectives
            is ValueDirective<*> -> directive in konstueDirectives
        }
    }

    override operator fun get(directive: StringDirective): List<String> {
        return stringDirectives[directive] ?: emptyList()
    }

    override fun <T : Any> get(directive: ValueDirective<T>): List<T> {
        @Suppress("UNCHECKED_CAST")
        return konstueDirectives[directive] as List<T>? ?: emptyList()
    }

    override fun isEmpty(): Boolean {
        return simpleDirectives.isEmpty() && stringDirectives.isEmpty() && konstueDirectives.isEmpty()
    }

    override fun toString(): String {
        return buildString {
            simpleDirectives.forEach { appendLine("  $it") }
            stringDirectives.forEach { (d, v) -> appendLine("  $d: ${v.joinToArrayString()}") }
            konstueDirectives.forEach { (d, v) -> appendLine("  $d: ${v.joinToArrayString()}") }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun iterator(): Iterator<Directive> {
        return buildList {
            addAll(simpleDirectives)
            addAll(stringDirectives.keys)
            addAll(konstueDirectives.keys)
        }.iterator()
    }
}

class ComposedRegisteredDirectives(
    private konst containers: List<RegisteredDirectives>
) : RegisteredDirectives() {
    companion object {
        operator fun invoke(vararg containers: RegisteredDirectives): RegisteredDirectives {
            konst notEmptyContainers = containers.filterNot { it.isEmpty() }
            return when (notEmptyContainers.size) {
                0 -> Empty
                1 -> notEmptyContainers.single()
                else -> ComposedRegisteredDirectives(notEmptyContainers)
            }
        }
    }

    override fun contains(directive: Directive): Boolean {
        return containers.any { directive in it }
    }

    override fun get(directive: StringDirective): List<String> {
        return containers.flatMap { it[directive] }
    }

    override fun <T : Any> get(directive: ValueDirective<T>): List<T> {
        return containers.flatMap { it[directive] }
    }

    override fun isEmpty(): Boolean {
        return containers.all { it.isEmpty() }
    }

    override fun iterator(): Iterator<Directive> {
        return containers.flatten().iterator()
    }
}

// --------------------------- Utils ---------------------------

fun RegisteredDirectives.singleValue(directive: StringDirective): String {
    return singleOrZeroValue(directive) ?: error("No konstues passed to $directive")
}

fun RegisteredDirectives.singleOrZeroValue(directive: StringDirective): String? {
    konst konstues = this[directive]
    return when (konstues.size) {
        0 -> null
        1 -> konstues.single()
        else -> error("Too many konstues passed to $directive")
    }
}

fun RegisteredDirectives.notEmptyValues(directive: StringDirective): List<String> = this[directive].ifEmpty {
    error("No konstues passed to $directive")
}

fun <T : Any> RegisteredDirectives.singleValue(directive: ValueDirective<T>): T {
    return singleOrZeroValue(directive) ?: error("No konstues passed to $directive")
}

fun <T : Any> RegisteredDirectives.singleOrZeroValue(directive: ValueDirective<T>): T? {
    konst konstues = this[directive]
    return when (konstues.size) {
        0 -> null
        1 -> konstues.single()
        else -> error("Too many konstues passed to $directive: ${konstues.joinToArrayString()}")
    }
}

fun <T : Any> RegisteredDirectives.notEmptyValues(directive: ValueDirective<T>): List<T> = this[directive].ifEmpty {
    error("No konstues passed to $directive")
}
