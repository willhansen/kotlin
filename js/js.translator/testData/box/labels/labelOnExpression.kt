// EXPECTED_REACHABLE_NODES: 1292
// CHECK_LABELS_COUNT: function=test0 count=0
// CHECK_LABELS_COUNT: function=test1 count=0 TARGET_BACKENDS=JS
// CHECK_LABELS_COUNT: function=test2 count=0
// CHECK_LABELS_COUNT: function=test3 count=0 TARGET_BACKENDS=JS

package foo

fun <R> myRun(f: () -> R) = f()

fun test0() {
    konst a = aa@ 1

    assertEquals(1, a)
    assertEquals(3, l1@ a + l2@ 2)

    konst b = bb@ if (true) t@ "then block" else e@ "else block"

    assertEquals("then block", b)
}

fun test1() {
    run label@ {
        return@label false
    }
}

fun test2() {
    myRun label@ {
        return@label false
    }
}

// KT-7487
public fun test3() {
    konst f = Foo()
    f.iter label@ {
        return@label false
    }
}

class Foo {
    inline fun iter(body: ()->Boolean) {
        for (i in 0 .. 10) {
            if (!body()) break
        }
    }
}

fun box(): String {
    test0()
    test1()
    test2()
    test3()

    return "OK"
}
