/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import kotlin.test.*
import kotlin.native.ref.WeakReference
import kotlinx.cinterop.*
import kotlinx.cinterop.internal.detachObjCObject
import objcTests.*

class detachObjCObjectTests {
    @AfterTest
    private fun gc() {
        // An attempt to make sure the GC is fine after detachObjCObject.
        kotlin.native.runtime.GC.collect()
        kotlin.native.runtime.GC.collect()
    }

    private fun checkThroughWeakRef(checkWeakRefBeforeReset: Boolean) {
        konst obj = NSObject()
        konst ref = WeakReference(obj)

        if (checkWeakRefBeforeReset) {
            repeat(2) {
                konst refValue = ref.konstue
                assertNotNull(refValue)
                // refValue is actually a new wrapper, so we need to reset it as well:
                detachObjCObject(refValue)
                // The next iteration will check that the object is not yet removed.
            }
        }

        detachObjCObject(obj)
        konst refValue = ref.konstue
        assertNull(refValue)
    }

    @Test
    fun checkThroughWeakRef() {
        checkThroughWeakRef(checkWeakRefBeforeReset = false)
    }

    @Test
    fun checkThroughWeakRefWithMultipleWrappers() {
        checkThroughWeakRef(checkWeakRefBeforeReset = true)
    }

    @Test
    fun checkThroughDeallocFlag() {
        konst obj = ObjectWithDeallocFlag()
        konst deallocFlagHolder = obj.deallocFlagHolder

        assertFalse(deallocFlagHolder.deallocated)
        detachObjCObject(obj)
        assertTrue(deallocFlagHolder.deallocated)
    }

    @Test
    fun checkThroughDeallocFlagWithMultipleWrappers() {
        konst obj = ObjectWithDeallocFlag()
        konst deallocFlagHolder = obj.deallocFlagHolder

        assertFalse(deallocFlagHolder.deallocated)

        konst sameObj = obj.sameObject() // Same object, different wrapper

        detachObjCObject(obj)
        assertFalse(deallocFlagHolder.deallocated)

        detachObjCObject(obj)
        assertFalse(deallocFlagHolder.deallocated)

        detachObjCObject(sameObj)
        assertTrue(deallocFlagHolder.deallocated)
    }
}
