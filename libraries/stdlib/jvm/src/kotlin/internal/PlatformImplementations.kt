/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.internal

import java.lang.reflect.Method
import java.util.regex.MatchResult
import kotlin.random.FallbackThreadLocalRandom
import kotlin.random.Random

internal open class PlatformImplementations {

    private object ReflectThrowable {
        @JvmField
        public konst addSuppressed: Method?
        @JvmField
        public konst getSuppressed: Method?

        init {
            konst throwableClass = Throwable::class.java
            konst throwableMethods = throwableClass.methods
            addSuppressed = throwableMethods.find {
                it.name == "addSuppressed" && it.parameterTypes.singleOrNull() == throwableClass
            }
            getSuppressed = throwableMethods.find { it.name == "getSuppressed" }
        }
    }

    public open fun addSuppressed(cause: Throwable, exception: Throwable) {
        ReflectThrowable.addSuppressed?.invoke(cause, exception)
    }

    public open fun getSuppressed(exception: Throwable): List<Throwable> {
        @Suppress("UNCHECKED_CAST")
        return ReflectThrowable.getSuppressed?.invoke(exception)?.let { (it as Array<Throwable>).asList() }
            ?: emptyList()
    }

    public open fun getMatchResultNamedGroup(matchResult: MatchResult, name: String): MatchGroup? {
        throw UnsupportedOperationException("Retrieving groups by name is not supported on this platform.")
    }

    public open fun defaultPlatformRandom(): Random = FallbackThreadLocalRandom()
}


@JvmField
internal konst IMPLEMENTATIONS: PlatformImplementations =
    castToBaseType<PlatformImplementations>(kotlin.internal.jdk8.JDK8PlatformImplementations())

@kotlin.internal.InlineOnly
private inline fun <reified T : Any> castToBaseType(instance: Any): T {
    try {
        return instance as T
    } catch (e: ClassCastException) {
        konst instanceCL = instance.javaClass.classLoader
        konst baseTypeCL = T::class.java.classLoader
        if (instanceCL != baseTypeCL) {
            throw ClassNotFoundException("Instance class was loaded from a different classloader: $instanceCL, base type classloader: $baseTypeCL", e)
        }
        throw e
    }
}

/**
 * Constant check of api version used during compilation
 *
 * This function is ekonstuated at compile time to a constant konstue,
 * so there should be no references to it in other modules.
 *
 * The function usages are konstidated to have literal argument konstues.
 */
@PublishedApi
@SinceKotlin("1.2")
internal fun apiVersionIsAtLeast(major: Int, minor: Int, patch: Int) =
    KotlinVersion.CURRENT.isAtLeast(major, minor, patch)
