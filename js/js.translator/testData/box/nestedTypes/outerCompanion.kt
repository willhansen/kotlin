// EXPECTED_REACHABLE_NODES: 1292
package foo

class A {
    inner class B {
        konst x = foo();
    }

    class C {
        konst x = foo();
    }

    companion object {
        fun foo(): String {
            return "foo_result";
        }
    }
}

fun box(): String {
    var result = A().B().x
    if (result != "foo_result") {
        return "fail1_" + result
    }
    result = A.C().x
    if (result != "foo_result") {
        return "fail2_" + result
    }
    return "OK"
}