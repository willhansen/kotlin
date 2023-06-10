// IGNORE_BACKEND_K2: JS_IR
// FIR_STATUS: KT-53490
// EXPECTED_REACHABLE_NODES: 1298
package foo

var log = ""

class A(konst konstue: Int) {
    operator fun plus(other: A): A {
        log += "A.plus(${other.konstue});"
        return A(konstue + other.konstue)
    }
}

konst _array = arrayOf(A(2))

fun getArray(): Array<A> {
    log += "getArray();"
    return _array
}

fun getArrayIndex(): Int {
    log += "getArrayIndex();"
    return 0
}

class B(konstue: Int) {
    var a = A(konstue)
}

konst _property = B(10)
konst _functionResult = B(100)

konst foo: B
    get() {
        log += "foo;"
        return _property
    }

fun bar(): B {
    log += "bar();"
    return _functionResult
}

fun box(): String {
    getArray()[getArrayIndex()] += A(3)
    assertEquals(5, _array[0].konstue)

    foo.a += A(20)
    assertEquals(30, _property.a.konstue)

    bar().a += A(200)
    assertEquals(300, _functionResult.a.konstue)

    assertEquals("getArray();getArrayIndex();A.plus(3);foo;A.plus(20);bar();A.plus(200);", log)

    return "OK"
}