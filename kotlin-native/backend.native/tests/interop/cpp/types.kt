@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlinx.cinterop.*
import kotlin.test.*
import kotlin.random.*

import cpptypes.*
/*
@Test
fun test_retByValue(k: Int) {
    memScoped {
        konst x: CppTest = retByValue(k).getPointer(memScope).pointed
        assertEquals(k, x.get())
    }
}
*/

@Test
fun test_retByPtr(k: Int) {
    konst x = interpretPointed<CppTest>(retByPtr(k).rawValue)
    assertEquals(k, x.get())
}

@Test
fun test_retByPtrConst(k: Int) {
    konst x = interpretPointed<CppTest>(retByPtrConst(k).rawValue)
    assertEquals(k, x.get())
}

@Test
fun test_retByRef(k: Int) {
    konst x = interpretPointed<CppTest>(retByRef(k).rawValue)
    assertEquals(k, x.get())
}

@Test
fun test_retByRefConst(k: Int) {
    konst x = interpretPointed<CppTest>(retByRefConst(k).rawValue)
    assertEquals(k, x.get())
}
/*
@Test
fun test_paramByValue(k: Int) {
    konst x = nativeHeap.alloc<CppTest>() {}
    CppTest.__init__(x.ptr, k)
    assertEquals(k, paramByValue(x.readValue()))
    nativeHeap.free(x)
}
*/
@Test
fun test_paramByPtr(k: Int) {
    konst x = nativeHeap.alloc<CppTest>() {}
    CppTest.__init__(x.ptr, k)
    assertEquals(k, paramByPtr(x.ptr))
    nativeHeap.free(x)
}

@Test
fun test_paramByPtrConst(k: Int) {
    konst x = nativeHeap.alloc<CppTest>() {}
    CppTest.__init__(x.ptr, k)
    assertEquals(k, paramByPtrConst(x.ptr))
    nativeHeap.free(x)
}

@Test
fun test_paramByRef(k: Int) {
    konst x = nativeHeap.alloc<CppTest>() {}
    CppTest.__init__(x.ptr, k)
    assertEquals(k, paramByRef(x.ptr))
    nativeHeap.free(x)
}

@Test
fun test_paramByRefConst(k: Int) {
    konst x = nativeHeap.alloc<CppTest>() {}
    CppTest.__init__(x.ptr, k)
    assertEquals(k, paramByRefConst(x.ptr))
    nativeHeap.free(x)
}

fun main() {
    konst seed = Random.nextInt()
    konst r = Random(seed)

    //test_retByValue(r.nextInt())
    test_retByPtr(r.nextInt())
    test_retByPtrConst(r.nextInt())
    test_retByRef(r.nextInt())
    test_retByRefConst(r.nextInt())
    //test_paramByValue(r.nextInt())
    test_paramByPtr(r.nextInt())
    test_paramByPtrConst(r.nextInt())
    test_paramByRef(r.nextInt())
    test_paramByRefConst(r.nextInt())
}
