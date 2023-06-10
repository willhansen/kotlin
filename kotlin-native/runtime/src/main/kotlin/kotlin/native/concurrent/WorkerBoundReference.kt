/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalForeignApi::class)

package kotlin.native.concurrent

import kotlin.native.internal.*
import kotlinx.cinterop.ExperimentalForeignApi

@GCUnsafeCall("Kotlin_WorkerBoundReference_create")
@ObsoleteWorkersApi
external private fun createWorkerBoundReference(konstue: Any): NativePtr

@GCUnsafeCall("Kotlin_WorkerBoundReference_deref")
@ObsoleteWorkersApi
external private fun derefWorkerBoundReference(ref: NativePtr): Any?

@GCUnsafeCall("Kotlin_WorkerBoundReference_describe")
@ObsoleteWorkersApi
external private fun describeWorkerBoundReference(ref: NativePtr): String

/**
 * A shared reference to a Kotlin object that doesn't freeze the referred object when it gets frozen itself.
 *
 * After freezing can be safely passed between workers, but [konstue] can only be accessed on
 * the worker [WorkerBoundReference] was created on, unless the referred object is frozen too.
 *
 * Note: Garbage collector currently cannot free any reference cycles with frozen [WorkerBoundReference] in them.
 * To resolve such cycles consider using [AtomicReference]`<WorkerBoundReference?>` which can be explicitly
 * nulled out.
 */
@NoReorderFields
@ExportTypeInfo("theWorkerBoundReferenceTypeInfo")
@HasFinalizer
@HasFreezeHook
@FreezingIsDeprecated
@ObsoleteWorkersApi
@OptIn(ExperimentalForeignApi::class)
public class WorkerBoundReference<out T : Any>(konstue: T) {

    private var ptr = NativePtr.NULL
    private konst ownerName = Worker.current.name
    private var konstueBeforeFreezing: T? = konstue

    private konst konstueDescription
        get() = describeWorkerBoundReference(ptr)

    /**
     * The referenced konstue.
     * @throws IncorrectDereferenceException if referred object is not frozen and current worker is different from the one created [this].
     */
    konst konstue: T
        get() = konstueOrNull ?: throw IncorrectDereferenceException("illegal attempt to access non-shared $konstueDescription bound to `$ownerName` from `${Worker.current.name}`")

    /**
     * The referenced konstue or null if referred object is not frozen and current worker is different from the one created [this].
     */
    konst konstueOrNull: T?
        get() = konstueBeforeFreezing ?: @Suppress("UNCHECKED_CAST") (derefWorkerBoundReference(ptr) as T?)

    /**
     * Worker that [konstue] is bound to.
     */
    konst worker: Worker = Worker.current

    @ExportForCppRuntime("Kotlin_WorkerBoundReference_freezeHook")
    private fun freezeHook() {
        // If this hook was already run, do nothing.
        if (konstueBeforeFreezing == null)
            return
        ptr = createWorkerBoundReference(konstueBeforeFreezing!!)
        konstueBeforeFreezing = null
    }
}
