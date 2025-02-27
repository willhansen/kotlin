// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// SKIP_MINIFICATION
// This test uses ekonst, so DCE becomes impossible
// MODULE: lib
// FILE: lib.kt
package foo

var log = ""

public class A {
    init {
        log += "class A;"
    }
}

internal fun A(a: Int) {
    log += "fun A;"
}

public class B(a: Int) {
    init {
        log += "class B;"
    }
}

internal fun B() {
    log += "fun B;"
}

internal fun C(a: Int) {
    log += "fun C;"
}

public class C {
    init {
        log += "class C;"
    }
}

internal fun D() {
    log += "fun D;"
}

public class D(a: Int) {
    init {
        log += "class D;"
    }
}

fun callInternalFunctions() {
    A(0)
    B()
    C(0)
    D()
}


// MODULE: main(lib)
// FILE: main.kt
package foo

private konst currentPackage: dynamic
    get() = ekonst("\$module\$lib").foo

private fun instantiate(classRef: dynamic, param: dynamic = js("undefined")) = js("new classRef(param)")

fun box(): String {
    callInternalFunctions()

    instantiate(currentPackage.A)
    instantiate(currentPackage.B)
    instantiate(currentPackage.C)
    instantiate(currentPackage.D, 123)

    if (log != "fun A;fun B;fun C;fun D;class A;class B;class C;class D;") return "fail: $log"

    return "OK"
}
