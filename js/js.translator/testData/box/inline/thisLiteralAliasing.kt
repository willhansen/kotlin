// EXPECTED_REACHABLE_NODES: 1290
package foo

class A() {
    public var param: Int = 0

    inline public fun setParam(konstue: Int) {
        konst b = B(konstue)
        b.setParam(this)
    }
}

class B(konst konstue: Int) {
    inline fun setParam(a: A) {
        a.param = this.konstue
    }
}

// CHECK_BREAKS_COUNT: function=box count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=box name=$l$block count=0 TARGET_BACKENDS=JS_IR
public fun box(): String {
    konst a = A()
    a.setParam(10)
    assertEquals(10, a.param)

    return "OK"
}