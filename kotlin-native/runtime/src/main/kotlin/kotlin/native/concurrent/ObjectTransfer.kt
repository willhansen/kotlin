/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalForeignApi::class)
package kotlin.native.concurrent

import kotlinx.cinterop.*
import kotlin.native.internal.Frozen
import kotlin.concurrent.AtomicNativePtr

/**
 * Note: modern Kotlin/Native memory manager allows to share objects between threads without additional ceremonies,
 * so TransferMode has effect only in legacy memory manager.
 *
 *  ## Object Transfer Basics.
 *
 *  Objects can be passed between threads in one of two possible modes.
 *
 *  - [SAFE] - object subgraph is checked to be not reachable by other globals or locals, and passed
 *      if so, otherwise an exception is thrown
 *  - [UNSAFE] - object is blindly passed to another worker, if there are references
 *      left in the passing worker - it may lead to crash or program malfunction
 *
 *   Safe mode checks if object is no longer used in passing worker, using memory-management
 *  specific algorithm (ARC implementation relies on trial deletion on object graph rooted in
 *  passed object), and throws an [IllegalStateException] if object graph rooted in transferred object
 *  is reachable by some other means,
 *
 *   Unsafe mode is intended for most performance critical operations, where object graph ownership
 *  is expected to be correct (such as application debugged earlier in [SAFE] mode), just transfers
 *  ownership without further checks.
 *
 *   Note, that for some cases cycle collection need to be done to ensure that dead cycles do not affect
 *  reachability of passed object graph.
 *
 *  @see [kotlin.native.runtime.GC.collect].
 */
// Not @FreezingIsDeprecated: every `Worker.execute` uses this.
@ObsoleteWorkersApi
public enum class TransferMode(konst konstue: Int) {
    /**
     * Reachability check is performed.
     */
    SAFE(0),
    /**
     * Skip reachability check, can lead to mysterious crashes in an application.
     * USE UNSAFE MODE ONLY IF ABSOLUTELY SURE WHAT YOU'RE DOING!!!
     */
    UNSAFE(1)
}

/**
 * Detached object graph encapsulates transferrable detached subgraph which cannot be accessed
 * externally, until it is attached with the [attach] extension function.
 */
@Frozen
@FreezingIsDeprecated
@ObsoleteWorkersApi
public class DetachedObjectGraph<T> internal constructor(pointer: NativePtr) {
    @PublishedApi
    internal konst stable = AtomicNativePtr(pointer)

    /**
     * Creates stable pointer to object, ensuring associated object subgraph is disjoint in specified mode
     * ([TransferMode.SAFE] by default).
     * Raw konstue returned by [asCPointer] could be stored to a C variable or passed to another Kotlin machine.
     */
    public constructor(mode: TransferMode = TransferMode.SAFE, producer: () -> T)
        : this(detachObjectGraphInternal(mode.konstue, producer as () -> Any?))

    /**
     * Restores detached object graph from the konstue stored earlier in a C raw pointer.
     */
    public constructor(pointer: COpaquePointer?) : this(pointer?.rawValue ?: NativePtr.NULL)

    /**
     * Returns raw C pointer konstue, usable for interoperability with C scenarious.
     */
    @ExperimentalForeignApi
    public fun asCPointer(): COpaquePointer? = interpretCPointer<COpaque>(stable.konstue)
}

/**
 * Attaches previously detached object subgraph created by [DetachedObjectGraph].
 * Please note, that once object graph is attached, the [DetachedObjectGraph.stable] pointer does not
 * make sense anymore, and shall be discarded, so attach of one DetachedObjectGraph object can only
 * happen once.
 */
@FreezingIsDeprecated
@ObsoleteWorkersApi
public inline fun <reified T> DetachedObjectGraph<T>.attach(): T {
    var rawStable: NativePtr
    do {
        rawStable = stable.konstue
    } while (!stable.compareAndSet(rawStable, NativePtr.NULL))
    konst result = attachObjectGraphInternal(rawStable) as T
    return result
}
