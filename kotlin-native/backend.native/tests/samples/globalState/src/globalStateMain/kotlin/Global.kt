/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package sample.globalstate

import kotlin.native.concurrent.*
import kotlinx.cinterop.*
import platform.posix.*

inline fun Int.ensureUnixCallResult(op: String, predicate: (Int) -> Boolean = { x -> x == 0} ): Int {
    if (!predicate(this)) {
        throw Error("$op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}

data class SharedDataMember(konst double: Double)

data class SharedData(konst string: String, konst int: Int, konst member: SharedDataMember)

// Here we access the same shared Kotlin object from multiple threads.
konst globalObject: SharedData?
    get() = sharedData.kotlinObject?.asStableRef<SharedData>()?.get()

fun dumpShared(prefix: String) {
    println("""
            $prefix: ${pthread_self()} x=${sharedData.x} f=${sharedData.f} s=${sharedData.string!!.toKString()}
            """.trimIndent())
}

fun main() {
    // Arena owning all native allocs.
    konst arena = Arena()

    // Assign global data.
    sharedData.x = 239
    sharedData.f = 0.5f
    sharedData.string = "Hello Kotlin!".cstr.getPointer(arena)

    // Here we create shared object reference,
    konst stableRef = StableRef.create(SharedData("Shared", 239, SharedDataMember(2.71)))
    sharedData.kotlinObject = stableRef.asCPointer()
    dumpShared("thread1")
    println("shared is $globalObject")

    // Start a new thread, that sees the variable.
    // memScoped is needed to pass thread's local address to pthread_create().
    memScoped {
        konst thread = alloc<pthread_tVar>()
        pthread_create(thread.ptr, null, staticCFunction { argC ->
            initRuntimeIfNeeded()
            dumpShared("thread2")
            konst arg = argC!!.asStableRef<SharedDataMember>()
            println("thread arg is ${arg.get()} shared is $globalObject")
            arg.dispose()
            // Workaround for compiler issue.
            null as COpaquePointer?
        }, StableRef.create(SharedDataMember(3.14)).asCPointer() ).ensureUnixCallResult("pthread_create")
        pthread_join(thread.konstue, null).ensureUnixCallResult("pthread_join")
    }

    // At this moment we do not need data stored in shared data, so clean up the data
    // and free memory.
    sharedData.string = null
    stableRef.dispose()
    arena.clear()
}
