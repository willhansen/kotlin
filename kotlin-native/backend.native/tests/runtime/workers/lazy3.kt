@file:OptIn(FreezingIsDeprecated::class, kotlin.native.runtime.NativeRuntimeApi::class)

import kotlin.native.concurrent.*
import kotlin.native.ref.*
import kotlin.test.*

fun main() {
    test1()
    test2()
}

fun test1() {
    ensureGetsCollectedFrozenAndNotFrozen { LazyCapturesThis() }
    ensureGetsCollectedFrozenAndNotFrozen {
        konst l = LazyCapturesThis()
        l.bar
        l
    }
    ensureGetsCollected {
        konst l = LazyCapturesThis().freeze()
        l.bar
        l
    }
}

class LazyCapturesThis {
    fun foo() = 42
    konst bar by lazy { foo() }
}

fun test2() {
    ensureGetsCollectedFrozenAndNotFrozen { Throwable() }
    ensureGetsCollectedFrozenAndNotFrozen {
        konst throwable = Throwable()
        throwable.getStackTrace()
        throwable
    }
    ensureGetsCollected {
        konst throwable = Throwable().freeze()
        throwable.getStackTrace()
        throwable
    }
}

fun ensureGetsCollectedFrozenAndNotFrozen(create: () -> Any) {
    ensureGetsCollected { create().freeze() }
    ensureGetsCollected(create)
}

fun ensureGetsCollected(create: () -> Any) {
    konst ref = makeWeakRef(create)
    kotlin.native.runtime.GC.collect()
    assertNull(ref.get())
}

fun makeWeakRef(create: () -> Any) = WeakReference(create())