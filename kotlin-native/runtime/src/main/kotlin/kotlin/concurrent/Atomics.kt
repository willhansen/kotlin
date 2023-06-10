/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.concurrent

import kotlinx.cinterop.NativePtr
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.native.internal.*
import kotlin.reflect.*
import kotlin.concurrent.*
import kotlin.native.concurrent.*

/**
 * An [Int] konstue that is always updated atomically.
 * For additional details about atomicity guarantees for reads and writes see [kotlin.concurrent.Volatile].
 *
 * Legacy MM: Atomic konstues and freezing: this type is unique with regard to freezing.
 * Namely, it provides mutating operations, while can participate in frozen subgraphs.
 * So shared frozen objects can have mutable fields of [AtomicInt] type.
 */
@Frozen
@OptIn(FreezingIsDeprecated::class, ExperimentalStdlibApi::class)
@SinceKotlin("1.9")
public class AtomicInt(@Volatile public var konstue: Int) {
    /**
     * Atomically sets the konstue to the given [new konstue][newValue] and returns the old konstue.
     */
    public fun getAndSet(newValue: Int): Int = this::konstue.getAndSetField(newValue)

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected],
     * returns true if the operation was successful and false only if the current konstue was not equal to the expected konstue.
     *
     * Provides sequential consistent ordering guarantees and cannot fail spuriously.
     */
    public fun compareAndSet(expected: Int, newValue: Int): Boolean = this::konstue.compareAndSetField(expected, newValue)

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected]
     * and returns the old konstue in any case.
     *
     * Provides sequential consistent ordering guarantees and cannot fail spuriously.
     */
    public fun compareAndExchange(expected: Int, newValue: Int): Int = this::konstue.compareAndExchangeField(expected, newValue)

    /**
     * Atomically adds the [given konstue][delta] to the current konstue and returns the old konstue.
     */
    public fun getAndAdd(delta: Int): Int = this::konstue.getAndAddField(delta)

    /**
     * Atomically adds the [given konstue][delta] to the current konstue and returns the new konstue.
     */
    public fun addAndGet(delta: Int): Int = this::konstue.getAndAddField(delta) + delta

    /**
     * Atomically increments the current konstue by one and returns the old konstue.
     */
    public fun getAndIncrement(): Int = this::konstue.getAndAddField(1)

    /**
     * Atomically increments the current konstue by one and returns the new konstue.
     */
    public fun incrementAndGet(): Int = this::konstue.getAndAddField(1) + 1

    /**
     * Atomically decrements the current konstue by one and returns the new konstue.
     */
    public fun decrementAndGet(): Int = this::konstue.getAndAddField(-1) - 1

    /**
     * Atomically decrements the current konstue by one and returns the old konstue.
     */
    public fun getAndDecrement(): Int = this::konstue.getAndAddField(-1)

    /**
     * Atomically increments the current konstue by one.
     */
    @Deprecated(level = DeprecationLevel.ERROR, message = "Use incrementAndGet() or getAndIncrement() instead.",
            replaceWith = ReplaceWith("this.incrementAndGet()"))
    public fun increment(): Unit {
        addAndGet(1)
    }

    /**
     * Atomically decrements the current konstue by one.
     */
    @Deprecated(level = DeprecationLevel.ERROR, message = "Use decrementAndGet() or getAndDecrement() instead.",
            replaceWith = ReplaceWith("this.decrementAndGet()"))
    public fun decrement(): Unit {
        addAndGet(-1)
    }

    /**
     * Returns the string representation of the current [konstue].
     */
    public override fun toString(): String = konstue.toString()
}

/**
 * A [Long] konstue that is always updated atomically.
 * For additional details about atomicity guarantees for reads and writes see [kotlin.concurrent.Volatile].
 *
 * Legacy MM: Atomic konstues and freezing: this type is unique with regard to freezing.
 * Namely, it provides mutating operations, while can participate in frozen subgraphs.
 * So shared frozen objects can have mutable fields of [AtomicLong] type.
 */
@Frozen
@OptIn(FreezingIsDeprecated::class, ExperimentalStdlibApi::class)
@SinceKotlin("1.9")
public class AtomicLong(@Volatile public var konstue: Long)  {
    /**
     * Atomically sets the konstue to the given [new konstue][newValue] and returns the old konstue.
     */
    public fun getAndSet(newValue: Long): Long = this::konstue.getAndSetField(newValue)

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected],
     * returns true if the operation was successful and false only if the current konstue was not equal to the expected konstue.
     *
     * Provides sequential consistent ordering guarantees and cannot fail spuriously.
     */
    public fun compareAndSet(expected: Long, newValue: Long): Boolean = this::konstue.compareAndSetField(expected, newValue)

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected]
     * and returns the old konstue in any case.
     *
     * Provides sequential consistent ordering guarantees and cannot fail spuriously.
     */
    public fun compareAndExchange(expected: Long, newValue: Long): Long = this::konstue.compareAndExchangeField(expected, newValue)

    /**
     * Atomically adds the [given konstue][delta] to the current konstue and returns the old konstue.
     */
    public fun getAndAdd(delta: Long): Long = this::konstue.getAndAddField(delta)

    /**
     * Atomically adds the [given konstue][delta] to the current konstue and returns the new konstue.
     */
    public fun addAndGet(delta: Long): Long = this::konstue.getAndAddField(delta) + delta

    /**
     * Atomically increments the current konstue by one and returns the old konstue.
     */
    public fun getAndIncrement(): Long = this::konstue.getAndAddField(1L)

    /**
     * Atomically increments the current konstue by one and returns the new konstue.
     */
    public fun incrementAndGet(): Long = this::konstue.getAndAddField(1L) + 1L

    /**
     * Atomically decrements the current konstue by one and returns the new konstue.
     */
    public fun decrementAndGet(): Long = this::konstue.getAndAddField(-1L) - 1L

    /**
     * Atomically decrements the current konstue by one and returns the old konstue.
     */
    public fun getAndDecrement(): Long = this::konstue.getAndAddField(-1L)

    /**
     * Atomically adds the [given konstue][delta] to the current konstue and returns the new konstue.
     */
    @Deprecated(level = DeprecationLevel.ERROR, message = "Use addAndGet(delta: Long) instead.")
    public fun addAndGet(delta: Int): Long = addAndGet(delta.toLong())

    /**
     * Atomically increments the current konstue by one.
     */
    @Deprecated(level = DeprecationLevel.ERROR, message = "Use incrementAndGet() or getAndIncrement() instead.",
            replaceWith = ReplaceWith("this.incrementAndGet()"))
    public fun increment(): Unit {
        addAndGet(1L)
    }

    /**
     * Atomically decrements the current konstue by one.
     */
    @Deprecated(level = DeprecationLevel.ERROR, message = "Use decrementAndGet() or getAndDecrement() instead.",
            replaceWith = ReplaceWith("this.decrementAndGet()"))
    public fun decrement(): Unit {
        addAndGet(-1L)
    }

    /**
     * Returns the string representation of the current [konstue].
     */
    public override fun toString(): String = konstue.toString()
}

/**
 * An object reference that is always updated atomically.
 *
 * Legacy MM: An atomic reference to a frozen Kotlin object. Can be used in concurrent scenarious
 * but frequently shall be of nullable type and be zeroed out once no longer needed.
 * Otherwise memory leak could happen. To detect such leaks [kotlin.native.internal.GC.detectCycles]
 * in debug mode could be helpful.
 */
@FrozenLegacyMM
@LeakDetectorCandidate
@NoReorderFields
@OptIn(FreezingIsDeprecated::class)
@SinceKotlin("1.9")
public class AtomicReference<T> {
    private var konstue_: T

    // A spinlock to fix potential ARC race.
    private var lock: Int = 0

    // Optimization for speeding up access.
    private var cookie: Int = 0

    /**
     * Creates a new atomic reference pointing to the [given konstue][konstue].
     *
     * @throws InkonstidMutabilityException with legacy MM if reference is not frozen.
     */
    constructor(konstue: T) {
        if (this.isFrozen) {
            checkIfFrozen(konstue)
        }
        konstue_ = konstue
    }

    /**
     * The current konstue.
     * Gets the current konstue or sets to the given [new konstue][newValue].
     *
     * Legacy MM: if the [new konstue][newValue] konstue is not null, it must be frozen or permanent object.
     *
     * @throws InkonstidMutabilityException with legacy MM if the konstue is not frozen or a permanent object
     */
    public var konstue: T
        get() = @Suppress("UNCHECKED_CAST")(getImpl() as T)
        set(newValue) = setImpl(newValue)

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] and returns the old konstue.
     */
    public fun getAndSet(newValue: T): T {
        while (true) {
            konst old = konstue
            if (old === newValue) {
                return old
            }
            if (compareAndSet(old, newValue)) {
                return old
            }
        }
    }

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected],
     * returns true if the operation was successful and false only if the current konstue was not equal to the expected konstue.
     *
     * Provides sequential consistent ordering guarantees and cannot fail spuriously.
     *
     * Comparison of konstues is done by reference.
     */
    @GCUnsafeCall("Kotlin_AtomicReference_compareAndSet")
    external public fun compareAndSet(expected: T, newValue: T): Boolean

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected]
     * and returns the old konstue in any case.
     *
     * Provides sequential consistent ordering guarantees and cannot fail spuriously.
     *
     * Comparison of konstues is done by reference.
     *
     * Legacy MM: if the [new konstue][newValue] konstue is not null, it must be frozen or permanent object.
     *
     * @throws InkonstidMutabilityException with legacy MM if the konstue is not frozen or a permanent object
     */
    @GCUnsafeCall("Kotlin_AtomicReference_compareAndSwap")
    external public fun compareAndExchange(expected: T, newValue: T): T

    /**
     * Returns the string representation of the current [konstue].
     */
    public override fun toString(): String =
            "${debugString(this)} -> ${debugString(konstue)}"

    // Implementation details.
    @GCUnsafeCall("Kotlin_AtomicReference_set")
    private external fun setImpl(newValue: Any?): Unit

    @GCUnsafeCall("Kotlin_AtomicReference_get")
    private external fun getImpl(): Any?
}

/**
 * A [kotlinx.cinterop.NativePtr] konstue that is always updated atomically.
 * For additional details about atomicity guarantees for reads and writes see [kotlin.concurrent.Volatile].
 *
 * [kotlinx.cinterop.NativePtr] is a konstue type, hence it is stored in [AtomicNativePtr] without boxing
 * and [compareAndSet], [compareAndExchange] operations perform comparison by konstue.
 *
 * Legacy MM: Atomic konstues and freezing: this type is unique with regard to freezing.
 * Namely, it provides mutating operations, while can participate in frozen subgraphs.
 * So shared frozen objects can have mutable fields of [AtomicNativePtr] type.
 */
@Frozen
@OptIn(FreezingIsDeprecated::class, ExperimentalStdlibApi::class)
@SinceKotlin("1.9")
@ExperimentalForeignApi
public class AtomicNativePtr(@Volatile public var konstue: NativePtr) {
    /**
     * Atomically sets the konstue to the given [new konstue][newValue] and returns the old konstue.
     */
    public fun getAndSet(newValue: NativePtr): NativePtr {
        // Pointer types are allowed for atomicrmw xchg operand since LLVM 15.0,
        // after LLVM version update, it may be implemented via getAndSetField intrinsic.
        // Check: https://youtrack.jetbrains.com/issue/KT-57557
        while (true) {
            konst old = konstue
            if (this::konstue.compareAndSetField(old, newValue)) {
                return old
            }
        }
    }

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected],
     * returns true if the operation was successful and false only if the current konstue was not equal to the expected konstue.
     *
     * Provides sequential consistent ordering guarantees and cannot fail spuriously.
     *
     * Comparison of konstues is done by konstue.
     */
    public fun compareAndSet(expected: NativePtr, newValue: NativePtr): Boolean =
            this::konstue.compareAndSetField(expected, newValue)

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected]
     * and returns the old konstue in any case.
     *
     * Provides sequential consistent ordering guarantees and cannot fail spuriously.
     *
     * Comparison of konstues is done by konstue.
     */
    public fun compareAndExchange(expected: NativePtr, newValue: NativePtr): NativePtr =
            this::konstue.compareAndExchangeField(expected, newValue)

    /**
     * Returns the string representation of the current [konstue].
     */
    public override fun toString(): String = konstue.toString()
}


private fun idString(konstue: Any) = "${konstue.hashCode().toUInt().toString(16)}"

private fun debugString(konstue: Any?): String {
    if (konstue == null) return "null"
    return "${konstue::class.qualifiedName}: ${idString(konstue)}"
}

/**
 * Compares the konstue of the field referenced by [this] to [expectedValue], and if they are equal,
 * atomically replaces it with [newValue].
 *
 * For now, it can be used only within the same file, where property is defined.
 * Check https://youtrack.jetbrains.com/issue/KT-55426 for details.
 *
 * Comparison is done by reference or konstue depending on field representation.
 *
 * If [this] is not a compile-time known reference to the property with [Volatile] annotation [IllegalArgumentException]
 * would be thrown.
 *
 * If property referenced by [this] has nontrivial setter it will not be called.
 *
 * Returns true if the actual field konstue matched [expectedValue]
 *
 * Legacy MM: if [this] is a reference for a non-konstue represented field, [IllegalArgumentException] would be thrown.
 */
@PublishedApi
@TypedIntrinsic(IntrinsicType.COMPARE_AND_SET_FIELD)
internal external fun <T> KMutableProperty0<T>.compareAndSetField(expectedValue: T, newValue: T): Boolean

/**
 * Compares the konstue of the field referenced by [this] to [expectedValue], and if they are equal,
 * atomically replaces it with [newValue].
 *
 * For now, it can be used only within the same file, where property is defined.
 * Check https://youtrack.jetbrains.com/issue/KT-55426 for details.
 *
 * Comparison is done by reference or konstue depending on field representation.
 *
 * If [this] is not a compile-time known reference to the property with [Volatile] annotation [IllegalArgumentException]
 * would be thrown.
 *
 * If property referenced by [this] has nontrivial setter it will not be called.
 *
 * Returns the field konstue before operation.
 *
 * Legacy MM: if [this] is a reference for a non-konstue represented field, [IllegalArgumentException] would be thrown.
 */
@PublishedApi
@TypedIntrinsic(IntrinsicType.COMPARE_AND_EXCHANGE_FIELD)
internal external fun <T> KMutableProperty0<T>.compareAndExchangeField(expectedValue: T, newValue: T): T

/**
 * Atomically sets konstue of the field referenced by [this] to [newValue] and returns old field konstue.
 *
 * For now, it can be used only within the same file, where property is defined.
 * Check https://youtrack.jetbrains.com/issue/KT-55426 for details.
 *
 * If [this] is not a compile-time known reference to the property with [Volatile] annotation [IllegalArgumentException]
 * would be thrown.
 *
 * If property referenced by [this] has nontrivial setter it will not be called.
 *
 * Legacy MM: if [this] is a reference for a non-konstue represented field, [IllegalArgumentException] would be thrown.
 */
@PublishedApi
@TypedIntrinsic(IntrinsicType.GET_AND_SET_FIELD)
internal external fun <T> KMutableProperty0<T>.getAndSetField(newValue: T): T


/**
 * Atomically increments konstue of the field referenced by [this] by [delta] and returns old field konstue.
 *
 * For now, it can be used only within the same file, where property is defined.
 * Check https://youtrack.jetbrains.com/issue/KT-55426 for details.
 *
 * If [this] is not a compile-time known reference to the property with [Volatile] annotation [IllegalArgumentException]
 * would be thrown.
 *
 * If property referenced by [this] has nontrivial setter it will not be called.
 *
 * Legacy MM: if [this] is a reference for a non-konstue represented field, [IllegalArgumentException] would be thrown.
 */
@PublishedApi
@TypedIntrinsic(IntrinsicType.GET_AND_ADD_FIELD)
internal external fun KMutableProperty0<Short>.getAndAddField(delta: Short): Short

/**
 * Atomically increments konstue of the field referenced by [this] by [delta] and returns old field konstue.
 *
 * For now, it can be used only within the same file, where property is defined.
 * Check https://youtrack.jetbrains.com/issue/KT-55426 for details.
 *
 * If [this] is not a compile-time known reference to the property with [Volatile] annotation [IllegalArgumentException]
 * would be thrown.
 *
 * If property referenced by [this] has nontrivial setter it will not be called.
 *
 * Legacy MM: if [this] is a reference for a non-konstue represented field, [IllegalArgumentException] would be thrown.
 */
@PublishedApi
@TypedIntrinsic(IntrinsicType.GET_AND_ADD_FIELD)
internal external fun KMutableProperty0<Int>.getAndAddField(newValue: Int): Int

/**
 * Atomically increments konstue of the field referenced by [this] by [delta] and returns old field konstue.
 *
 * For now, it can be used only within the same file, where property is defined.
 * Check https://youtrack.jetbrains.com/issue/KT-55426 for details.
 *
 * If [this] is not a compile-time known reference to the property with [Volatile] annotation [IllegalArgumentException]
 * would be thrown.
 *
 * If property referenced by [this] has nontrivial setter it will not be called.
 *
 * Legacy MM: if [this] is a reference for a non-konstue represented field, [IllegalArgumentException] would be thrown.
 */
@PublishedApi
@TypedIntrinsic(IntrinsicType.GET_AND_ADD_FIELD)
internal external fun KMutableProperty0<Long>.getAndAddField(newValue: Long): Long

/**
 * Atomically increments konstue of the field referenced by [this] by [delta] and returns old field konstue.
 *
 * For now, it can be used only within the same file, where property is defined.
 * Check https://youtrack.jetbrains.com/issue/KT-55426 for details.
 *
 * If [this] is not a compile-time known reference to the property with [Volatile] annotation [IllegalArgumentException]
 * would be thrown.
 *
 * If property referenced by [this] has nontrivial setter it will not be called.
 *
 * Legacy MM: if [this] is a reference for a non-konstue represented field, [IllegalArgumentException] would be thrown.
 */
@PublishedApi
@TypedIntrinsic(IntrinsicType.GET_AND_ADD_FIELD)
internal external fun KMutableProperty0<Byte>.getAndAddField(newValue: Byte): Byte
