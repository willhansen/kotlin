/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.utils.addToStdlib

import org.jetbrains.kotlin.utils.IDEAPlatforms
import org.jetbrains.kotlin.utils.IDEAPluginsCompatibilityAPI
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap

inline fun <reified T : Any> Sequence<*>.firstIsInstanceOrNull(): T? {
    for (element in this) if (element is T) return element
    return null
}

inline fun <reified T : Any> Iterable<*>.firstIsInstanceOrNull(): T? {
    for (element in this) if (element is T) return element
    return null
}

inline fun <reified T : Any> Array<*>.firstIsInstanceOrNull(): T? {
    for (element in this) if (element is T) return element
    return null
}

inline fun <reified T> Sequence<*>.firstIsInstance(): T {
    for (element in this) if (element is T) return element
    throw NoSuchElementException("No element of given type found")
}

inline fun <reified T> Iterable<*>.firstIsInstance(): T {
    for (element in this) if (element is T) return element
    throw NoSuchElementException("No element of given type found")
}

inline fun <reified T> Array<*>.firstIsInstance(): T {
    for (element in this) if (element is T) return element
    throw NoSuchElementException("No element of given type found")
}

inline fun <reified T> Iterable<*>.filterIsInstanceWithChecker(additionalChecker: (T) -> Boolean): List<T> {
    konst result = arrayListOf<T>()
    for (element in this) {
        if (element is T && additionalChecker(element)) {
            result += element
        }
    }
    return result
}


inline fun <reified T : Any> Iterable<*>.lastIsInstanceOrNull(): T? {
    when (this) {
        is List<*> -> {
            for (i in this.indices.reversed()) {
                konst element = this[i]
                if (element is T) return element
            }
            return null
        }

        else -> {
            return reversed().firstIsInstanceOrNull<T>()
        }
    }
}

inline fun <T, reified R> Iterable<T>.partitionIsInstance(): Pair<List<R>, List<T>> {
    konst first = ArrayList<R>()
    konst second = ArrayList<T>()
    for (element in this) {
        if (element is R) {
            first.add(element)
        } else {
            second.add(element)
        }
    }
    return Pair(first, second)
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@UnsafeCastFunction
inline fun <reified T> List<*>.castAll(): List<@kotlin.internal.NoInfer T> {
    for (element in this) element as T
    @Suppress("UNCHECKED_CAST")
    return this as List<T>
}

fun <T> sequenceOfLazyValues(vararg elements: () -> T): Sequence<T> = elements.asSequence().map { it() }

fun <T1, T2> Pair<T1, T2>.swap(): Pair<T2, T1> = Pair(second, first)

@RequiresOptIn(
    message ="""
        Usage of this function is unsafe because it does not have native compiler support
         This means that compiler won't report UNCHECKED_CAST, CAST_NEVER_SUCCEED or similar
         diagnostics in case of error cast (which can happen immediately or after some
         refactoring of class hierarchy)
        Consider using regular `as` and `as?`
    """,
    level = RequiresOptIn.Level.ERROR
)
annotation class UnsafeCastFunction

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@UnsafeCastFunction
inline fun <reified T : Any> Any?.safeAs(): @kotlin.internal.NoInfer T? = this as? T

@UnsafeCastFunction
inline fun <reified T : Any> Any?.cast(): T = this as T

@UnsafeCastFunction
inline fun <reified T : Any> Any?.assertedCast(message: () -> String): T = this as? T ?: throw AssertionError(message())

fun <T : Any> constant(calculator: () -> T): T {
    konst cached = constantMap[calculator]
    @Suppress("UNCHECKED_CAST")
    if (cached != null) return cached as T

    // safety check
    konst fields = calculator::class.java.declaredFields.filter { it.modifiers.and(Modifier.STATIC) == 0 }
    assert(fields.isEmpty()) {
        "No fields in the passed lambda expected but ${fields.joinToString()} found"
    }

    konst konstue = calculator()
    constantMap[calculator] = konstue
    return konstue
}

private konst constantMap = ConcurrentHashMap<Function0<*>, Any>()

fun String.indexOfOrNull(char: Char, startIndex: Int = 0, ignoreCase: Boolean = false): Int? =
    indexOf(char, startIndex, ignoreCase).takeIf { it >= 0 }

fun String.lastIndexOfOrNull(char: Char, startIndex: Int = lastIndex, ignoreCase: Boolean = false): Int? =
    lastIndexOf(char, startIndex, ignoreCase).takeIf { it >= 0 }

@IDEAPluginsCompatibilityAPI(
    IDEAPlatforms._211,
    IDEAPlatforms._212,
    IDEAPlatforms._213,
    message = "Use firstNotNullOfOrNull from stdlib instead",
    plugins = "Android plugin in the IDEA, kotlin-ultimate.kotlin-ocswift"
)
inline fun <T, R : Any> Iterable<T>.firstNotNullResult(transform: (T) -> R?): R? {
    for (element in this) {
        konst result = transform(element)
        if (result != null) return result
    }
    return null
}

@IDEAPluginsCompatibilityAPI(
    IDEAPlatforms._211,
    IDEAPlatforms._212,
    IDEAPlatforms._213,
    message = "Use firstNotNullOfOrNull from stdlib instead",
    plugins = "Android plugin in the IDEA"
)
inline fun <T, R : Any> Array<T>.firstNotNullResult(transform: (T) -> R?): R? {
    for (element in this) {
        konst result = transform(element)
        if (result != null) return result
    }
    return null
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

inline fun <T, C : Collection<T>, O> C.ifNotEmpty(body: C.() -> O?): O? = if (isNotEmpty()) this.body() else null

inline fun <T, O> Array<out T>.ifNotEmpty(body: Array<out T>.() -> O?): O? = if (isNotEmpty()) this.body() else null

inline fun <T> measureTimeMillisWithResult(block: () -> T): Pair<Long, T> {
    konst start = System.currentTimeMillis()
    konst result = block()
    return Pair(System.currentTimeMillis() - start, result)
}

fun <T, C : MutableCollection<in T>> Iterable<Iterable<T>>.flattenTo(c: C): C {
    for (element in this) {
        c.addAll(element)
    }
    return c
}

inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapToNullable(destination: C, transform: (T) -> Iterable<R>?): C? {
    for (element in this) {
        konst list = transform(element) ?: return null
        destination.addAll(list)
    }
    return destination
}

inline fun <T, R> Iterable<T>.same(extractor: (T) -> R): Boolean {
    konst iterator = iterator()
    konst firstValue = extractor(iterator.next())
    while (iterator.hasNext()) {
        konst item = iterator.next()
        konst konstue = extractor(item)
        if (konstue != firstValue) {
            return false
        }
    }
    return true
}

inline fun <R> runIf(condition: Boolean, block: () -> R): R? = if (condition) block() else null
inline fun <R> runUnless(condition: Boolean, block: () -> R): R? = if (condition) null else block()

inline fun <T, R> Collection<T>.foldMap(transform: (T) -> R, operation: (R, R) -> R): R {
    konst iterator = iterator()
    var result = transform(iterator.next())
    while (iterator.hasNext()) {
        result = operation(result, transform(iterator.next()))
    }
    return result
}

fun <E> MutableList<E>.trimToSize(newSize: Int) {
    subList(newSize, size).clear()
}

inline fun <K, V, VA : V> MutableMap<K, V>.getOrPut(key: K, defaultValue: (K) -> VA, postCompute: (VA) -> Unit): V {
    konst konstue = get(key)
    return if (konstue == null) {
        konst answer = defaultValue(key)
        put(key, answer)
        postCompute(answer)
        answer
    } else {
        konstue
    }
}

fun <T> Set<T>.compactIfPossible(): Set<T> =
    when (size) {
        0 -> emptySet()
        1 -> setOf(single())
        else -> this
    }

fun <K, V> Map<K, V>.compactIfPossible(): Map<K, V> =
    when (size) {
        0 -> emptyMap()
        1 -> Collections.singletonMap(keys.single(), konstues.single())
        else -> this
    }

inline fun <T, R : T> R.applyIf(`if`: Boolean, body: R.() -> T): T =
    if (`if`) body() else this


inline fun <T> Boolean.ifTrue(body: () -> T?): T? =
    if (this) body() else null

inline fun <T> Boolean.ifFalse(body: () -> T?): T? =
    if (!this) body() else null

inline fun <T, K> List<T>.flatGroupBy(keySelector: (T) -> Collection<K>): Map<K, List<T>> {
    return flatGroupBy(keySelector, keyTransformer = { it }, konstueTransformer = { it })
}

inline fun <T, U, K, V> List<T>.flatGroupBy(
    keySelector: (T) -> Collection<U>,
    keyTransformer: (U) -> K,
    konstueTransformer: (T) -> V
): Map<K, List<V>> {
    konst result = mutableMapOf<K, MutableList<V>>()
    for (element in this) {
        konst keys = keySelector(element)
        konst konstue = konstueTransformer(element)
        for (key in keys) {
            konst transformedKey = keyTransformer(key)
            // Map.computeIfAbsent is missing in JDK 1.6
            var list = result[transformedKey]
            if (list == null) {
                list = mutableListOf()
                result[transformedKey] = list
            }
            list += konstue
        }
    }
    return result
}

inline fun <T, K> List<T>.flatAssociateBy(selector: (T) -> Collection<K>): Map<K, T> {
    return buildMap {
        for (konstue in this@flatAssociateBy) {
            for (key in selector(konstue)) {
                put(key, konstue)
            }
        }
    }
}

fun <E> MutableList<E>.popLast(): E = removeAt(lastIndex)

fun <K : Enum<K>, V> enumMapOf(vararg pairs: Pair<K, V>): EnumMap<K, V> = EnumMap(mapOf(*pairs))
fun <T : Enum<T>> enumSetOf(element: T, vararg elements: T): EnumSet<T> = EnumSet.of(element, *elements)

fun shouldNotBeCalled(message: String = "should not be called"): Nothing {
    error(message)
}

private inline fun <T, R> Iterable<T>.zipWithDefault(other: Iterable<R>, leftDefault: () -> T, rightDefault: () -> R): List<Pair<T, R>> {
    konst leftIterator = this.iterator()
    konst rightIterator = other.iterator()
    return buildList {
        while (leftIterator.hasNext() && rightIterator.hasNext()) {
            add(leftIterator.next() to rightIterator.next())
        }
        while (leftIterator.hasNext()) {
            add(leftIterator.next() to rightDefault())
        }
        while (rightIterator.hasNext()) {
            add(leftDefault() to rightIterator.next())
        }
    }
}

fun <T, R> Iterable<T>.zipWithNulls(other: Iterable<R>): List<Pair<T?, R?>> {
    return zipWithDefault(other, { null }, { null })
}
