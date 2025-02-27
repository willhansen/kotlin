@file:OptIn(kotlin.native.runtime.NativeRuntimeApi::class)

import kotlin.native.ref.WeakReference
import kotlinx.cinterop.*
import kotlin.test.*
import objcTests.*

// Note: these tests rely on GC assertions: without the fix and the assertions it won't actually crash.
// GC should fire an assertion if it obtains a reference to Kotlin object that is being (or has been) deallocated.

@Test
fun testKT41811() {
    // Attempt to make the state predictable:
    kotlin.native.runtime.GC.collect()

    deallocRetainReleaseDeallocated = false
    assertFalse(deallocRetainReleaseDeallocated)

    createGarbageDeallocRetainRelease()

    // Runs [DeallocRetainRelease dealloc]:
    kotlin.native.runtime.GC.collect()

    assertTrue(deallocRetainReleaseDeallocated)

    // Might crash due to double-dispose if the dealloc applied addRef/releaseRef to reclaimed Kotlin object:
    kotlin.native.runtime.GC.collect()
}

private fun createGarbageDeallocRetainRelease() {
    autoreleasepool {
        object : DeallocRetainRelease() {}
    }
}

@Test
fun testKT41811LoadWeak() {
    testKT41811LoadWeak(ObjCWeakReference())
}

@Test
fun testKT41811LoadKotlinWeak() {
    konst kotlinWeak = object : NSObject(), WeakReferenceProtocol {
        lateinit var weakReference: WeakReference<Any>

        override fun referent(): Any? {
            return weakReference.konstue
        }

        override fun setReferent(referent: Any?) {
            weakReference = WeakReference(referent!!)
        }
    }

    testKT41811LoadWeak(kotlinWeak)
}

private fun testKT41811LoadWeak(weakRef: WeakReferenceProtocol) {
    // Attempt to make the state predictable:
    kotlin.native.runtime.GC.collect()

    deallocLoadWeakDeallocated = false
    assertFalse(deallocLoadWeakDeallocated)

    createGarbageDeallocLoadWeak(weakRef)

    // Runs [DeallocLoadWeak dealloc]:
    kotlin.native.runtime.GC.collect()

    assertTrue(deallocLoadWeakDeallocated)

    // Might crash due to double-dispose if the dealloc applied addRef/releaseRef to reclaimed Kotlin object:
    kotlin.native.runtime.GC.collect()

    weakDeallocLoadWeak = null
}

private fun createGarbageDeallocLoadWeak(weakRef: WeakReferenceProtocol) {
    autoreleasepool {
        konst obj = object : DeallocLoadWeak() {}
        weakDeallocLoadWeak = weakRef.apply { referent = obj }
        obj.checkWeak()
        assertSame(obj, weakDeallocLoadWeak!!.referent)
    }
}

@Test
fun testKT41811WithGlobal() {
    // Attempt to make the state predictable:
    kotlin.native.runtime.GC.collect()

    deallocRetainReleaseDeallocated = false
    assertFalse(deallocRetainReleaseDeallocated)

    autoreleasepool {
        {
            globalDeallocRetainRelease = object: DeallocRetainRelease() {}
        }()
    }

    assertFalse(deallocRetainReleaseDeallocated)

    // Clean up local DeallocRetainRelease on Kotlin side
    kotlin.native.runtime.GC.collect()

    assertFalse(deallocRetainReleaseDeallocated)

    // And drop the last reference to DeallocRetainRelease from ObjC global scope.
    globalDeallocRetainRelease = null

    assertFalse(deallocRetainReleaseDeallocated)

    // This will dispose `DeallocRetainRelease` on Kotlin side, which will cause `dealloc`
    // on ObjC side, which triggers `retain` and `release` of `self`. If these messages
    // were to reach Kotlin side, the `release` would have immediately scheduled the
    // second disposal of Kotlin object.
    kotlin.native.runtime.GC.collect()

    assertTrue(deallocRetainReleaseDeallocated)
}

var localDeallocRetainAndAccessDeallocated = false

@Test
fun testKT41811WithAccess() {
    // Legacy MM crashes with an assertion failure.
    @OptIn(kotlin.ExperimentalStdlibApi::class)
    if (!isExperimentalMM())
        return

    // Attempt to make the state predictable:
    kotlin.native.internal.GC.collect()

    deallocRetainAndAccessDeallocated = false
    localDeallocRetainAndAccessDeallocated = false

    createGarbageDeallocRetainAndAccess()

    // Runs [DeallocRetainAndAccess dealloc]:
    kotlin.native.internal.GC.collect()

    assertTrue(deallocRetainAndAccessDeallocated)
    assertTrue(localDeallocRetainAndAccessDeallocated)

    // Might crash due to double-dispose if the dealloc applied addRef/releaseRef to reclaimed Kotlin object:
    kotlin.native.internal.GC.collect()
}

private fun createGarbageDeallocRetainAndAccess() {
    autoreleasepool {
        object : DeallocRetainAndAccess() {
            init {
                onDealloc = {
                    assertNull(it)
                    assertFalse(localDeallocRetainAndAccessDeallocated)
                    localDeallocRetainAndAccessDeallocated = true
                }
            }
        }
    }
}
