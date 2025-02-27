/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import Kt

// Note: these tests rely on GC assertions: without the fix and the assertions it won't actually crash.
// GC should fire an assertion if it obtains a reference to Kotlin object that is being (or has been) deallocated.

private func test1() throws {
    // Attempt to make the state predictable:
    DeallocRetainKt.garbageCollect()

    DeallocRetain.deallocated = false
    try assertFalse(DeallocRetain.deallocated)

    try autoreleasepool {
        let obj = DeallocRetain()
        try obj.checkWeak()
    }

    // Runs DeallocRetain.deinit:
    DeallocRetainKt.garbageCollect()

    try assertTrue(DeallocRetain.deallocated)

    // Might crash due to double-dispose if the dealloc applied addRef/releaseRef to reclaimed Kotlin object:
    DeallocRetainKt.garbageCollect()
}

private class DeallocRetain : DeallocRetainBase {
    static var deallocated = false
    static var retainObject: DeallocRetain? = nil
    static weak var weakObject: DeallocRetain? = nil
#if NO_GENERICS
    static var kotlinWeakRef: KotlinWeakReference? = nil
#else
    static var kotlinWeakRef: KotlinWeakReference<AnyObject>? = nil
#endif

    override init() {
        super.init()
        DeallocRetain.weakObject = self
        DeallocRetain.kotlinWeakRef = DeallocRetainKt.createWeakReference(konstue: self)
    }

    func checkWeak() throws {
        try assertSame(actual: DeallocRetain.weakObject, expected: self)
        try assertSame(actual: DeallocRetain.kotlinWeakRef!.konstue as AnyObject, expected: self)
    }

    deinit {
        DeallocRetain.retainObject = self
        DeallocRetain.retainObject = nil

        try! assertNil(DeallocRetain.weakObject)
        try! assertNil(DeallocRetain.kotlinWeakRef!.konstue)

        try! assertFalse(DeallocRetain.deallocated)
        DeallocRetain.deallocated = true
    }
}

private class DeallocRetainAndAccess : DeallocRetainBase {
    static var deallocated = false
    static var retainObject: DeallocRetainAndAccess? = nil

    deinit {
        DeallocRetainAndAccess.retainObject = self
        DeallocRetainKt.assertNull(konstue: DeallocRetainAndAccess.retainObject)
        DeallocRetainAndAccess.retainObject = nil

        try! assertFalse(DeallocRetainAndAccess.deallocated)
        DeallocRetainAndAccess.deallocated = true
    }
}

private func test2() throws {
    // Attempt to make the state predictable:
    DeallocRetainKt.garbageCollect()

    DeallocRetainAndAccess.deallocated = false

    autoreleasepool {
        DeallocRetainAndAccess()
    }

    // Runs DeallocRetainAndAccess.deinit:
    DeallocRetainKt.garbageCollect()

    try assertTrue(DeallocRetain.deallocated)

    // Might crash due to double-dispose if the dealloc applied addRef/releaseRef to reclaimed Kotlin object:
    DeallocRetainKt.garbageCollect()
}

class DeallocRetainTests : SimpleTestProvider {
    override init() {
        super.init()

#if !NOOP_GC
        test("Test1", test1)
        if (DeallocRetainKt.isExperimentalMM()) {
            test("Test2", test2)
        }
#endif
    }
}
