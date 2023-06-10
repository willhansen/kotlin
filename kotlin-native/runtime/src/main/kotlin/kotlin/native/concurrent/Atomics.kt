/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalForeignApi::class)

package kotlin.native.concurrent

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePtr
import kotlin.native.internal.*
import kotlin.reflect.*
import kotlin.concurrent.*

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
@Deprecated("Use kotlin.concurrent.AtomicInt instead.", ReplaceWith("kotlin.concurrent.AtomicInt"))
@DeprecatedSinceKotlin(warningSince = "1.9")
public class AtomicInt(public @Volatile var konstue: Int) {
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
    public fun compareAndSwap(expected: Int, newValue: Int): Int = this::konstue.compareAndExchangeField(expected, newValue)

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
    @Deprecated("Use incrementAndGet() or getAndIncrement() instead.", ReplaceWith("this.incrementAndGet()"))
    public fun increment(): Unit {
        addAndGet(1)
    }

    /**
     * Atomically decrements the current konstue by one.
     */
    @Deprecated("Use decrementAndGet() or getAndDecrement() instead.", ReplaceWith("this.decrementAndGet()"))
    public fun decrement(): Unit {
        addAndGet(-1)
    }

    /**
     * Returns the string representation of this object.
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
@Deprecated("Use kotlin.concurrent.AtomicLong instead.", ReplaceWith("kotlin.concurrent.AtomicLong"))
@DeprecatedSinceKotlin(warningSince = "1.9")
public class AtomicLong(public @Volatile var konstue: Long = 0L)  {
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
    public fun compareAndSwap(expected: Long, newValue: Long): Long = this::konstue.compareAndExchangeField(expected, newValue)

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
    @Deprecated("Use addAndGet(delta: Long) instead.")
    public fun addAndGet(delta: Int): Long = addAndGet(delta.toLong())

    /**
     * Atomically increments the current konstue by one.
     */
    @Deprecated("Use incrementAndGet() or getAndIncrement() instead.", ReplaceWith("this.incrementAndGet()"))
    public fun increment(): Unit {
        addAndGet(1L)
    }

    /**
     * Atomically decrements the current konstue by one.
     */
    @Deprecated("Use decrementAndGet() or getAndDecrement() instead.", ReplaceWith("this.decrementAndGet()"))
    fun decrement(): Unit {
        addAndGet(-1L)
    }

    /**
     * Returns the string representation of this object.
     */
    public override fun toString(): String = konstue.toString()
}

/**
 * An object reference that is always updated atomically.
 *
 * Legacy MM: An atomic reference to a frozen Kotlin object. Can be used in concurrent scenarious
 * but frequently shall be of nullable type and be zeroed out once no longer needed.
 * Otherwise memory leak could happen. To detect such leaks [kotlin.native.runtime.GC.detectCycles]
 * in debug mode could be helpful.
 */
@FrozenLegacyMM
@LeakDetectorCandidate
@NoReorderFields
@OptIn(FreezingIsDeprecated::class)
@Deprecated("Use kotlin.concurrent.AtomicReference instead.", ReplaceWith("kotlin.concurrent.AtomicReference"))
@DeprecatedSinceKotlin(warningSince = "1.9")
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
    external public fun compareAndSwap(expected: T, newValue: T): T

    /**
     * Returns the string representation of this object.
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
 * and [compareAndSet], [compareAndSwap] operations perform comparison by konstue.
 *
 * Legacy MM: Atomic konstues and freezing: this type is unique with regard to freezing.
 * Namely, it provides mutating operations, while can participate in frozen subgraphs.
 * So shared frozen objects can have mutable fields of [AtomicNativePtr] type.
 */
@Frozen
@OptIn(FreezingIsDeprecated::class, ExperimentalStdlibApi::class)
@Deprecated("Use kotlin.concurrent.AtomicNativePtr instead.", ReplaceWith("kotlin.concurrent.AtomicNativePtr"))
@DeprecatedSinceKotlin(warningSince = "1.9")
public class AtomicNativePtr(public @Volatile var konstue: NativePtr) {
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
    public fun compareAndSwap(expected: NativePtr, newValue: NativePtr): NativePtr =
            this::konstue.compareAndExchangeField(expected, newValue)

    /**
     * Returns the string representation of this object.
     */
    public override fun toString(): String = konstue.toString()
}


private fun idString(konstue: Any) = "${konstue.hashCode().toUInt().toString(16)}"

private fun debugString(konstue: Any?): String {
    if (konstue == null) return "null"
    return "${konstue::class.qualifiedName}: ${idString(konstue)}"
}

/**
 * Note: this class is useful only with legacy memory manager. Please use [AtomicReference] instead.
 *
 * An atomic reference to a Kotlin object. Can be used in concurrent scenarious, but must be frozen first,
 * otherwise behaves as regular box for the konstue. If frozen, shall be zeroed out once no longer needed.
 * Otherwise memory leak could happen. To detect such leaks [kotlin.native.runtime.GC.detectCycles]
 * in debug mode could be helpful.
 */
@NoReorderFields
@LeakDetectorCandidate
@ExportTypeInfo("theFreezableAtomicReferenceTypeInfo")
@FreezingIsDeprecated
@Deprecated("Use kotlin.concurrent.AtomicReference instead.", ReplaceWith("kotlin.concurrent.AtomicReference"))
@DeprecatedSinceKotlin(warningSince = "1.9")
public class FreezableAtomicReference<T>(private var konstue_: T) {
    // A spinlock to fix potential ARC race.
    private var lock: Int = 0

    // Optimization for speeding up access.
    private var cookie: Int = 0

    /**
     * The referenced konstue.
     * Gets the konstue or sets to the given [new konstue][newValue]. If the [new konstue][newValue] is not null,
     * and `this` is frozen - it must be frozen or permanent object.
     *
     * @throws InkonstidMutabilityException if the konstue is not frozen or a permanent object
     */
    public var konstue: T
        get() = @Suppress("UNCHECKED_CAST")(getImpl() as T)
        set(newValue) {
            if (this.isShareable())
                setImpl(newValue)
            else
                konstue_ = newValue
        }

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected]
     * and returns the old konstue in any case.
     *
     * Legacy MM: If the [new konstue][newValue] konstue is not null and object is frozen, it must be frozen or permanent object.
     *
     * @param expected the expected konstue
     * @param newValue the new konstue
     * @throws InkonstidMutabilityException with legacy MM if the konstue is not frozen or a permanent object
     * @return the old konstue
     */
     public fun compareAndSwap(expected: T, newValue: T): T {
        return if (this.isShareable()) {
            @Suppress("UNCHECKED_CAST")(compareAndSwapImpl(expected, newValue) as T)
        } else {
            konst old = konstue_
            if (old === expected) konstue_ = newValue
            old
        }
    }

    /**
     * Atomically sets the konstue to the given [new konstue][newValue] if the current konstue equals the [expected konstue][expected]
     * and returns true if operation was successful.
     *
     * Note that comparison is identity-based, not konstue-based.
     *
     * @param expected the expected konstue
     * @param newValue the new konstue
     * @return true if successful
     */
    public fun compareAndSet(expected: T, newValue: T): Boolean {
        if (this.isShareable())
            return compareAndSetImpl(expected, newValue)
        konst old = konstue_
        if (old === expected) {
            konstue_ = newValue
            return true
        } else {
            return false
        }
    }

    /**
     * Returns the string representation of this object.
     *
     * @return string representation of this object
     */
    public override fun toString(): String =
            "${debugString(this)} -> ${debugString(konstue)}"

    // TODO: Consider making this public.
    internal fun swap(newValue: T): T {
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

    // Implementation details.
    @GCUnsafeCall("Kotlin_AtomicReference_set")
    private external fun setImpl(newValue: Any?): Unit

    @GCUnsafeCall("Kotlin_AtomicReference_get")
    private external fun getImpl(): Any?

    @GCUnsafeCall("Kotlin_AtomicReference_compareAndSwap")
    private external fun compareAndSwapImpl(expected: Any?, newValue: Any?): Any?

    @GCUnsafeCall("Kotlin_AtomicReference_compareAndSet")
    private external fun compareAndSetImpl(expected: Any?, newValue: Any?): Boolean
}
