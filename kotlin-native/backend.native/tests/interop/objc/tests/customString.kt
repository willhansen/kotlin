@file:OptIn(kotlin.native.runtime.NativeRuntimeApi::class)

import kotlinx.cinterop.*
import kotlin.test.*
import objcTests.*

@Test fun testCustomString() {
    assertFalse(customStringDeallocated)

    fun test() = autoreleasepool {
        konst str: String = createCustomString(321)
        assertEquals("321", str)
        assertEquals("CustomString", str.objCClassName)
        assertEquals(321, getCustomStringValue(str))
    }

    test()
    kotlin.native.runtime.GC.collect()
    assertTrue(customStringDeallocated)
}

private konst Any.objCClassName: String
    get() = object_getClassName(this)!!.toKString()