// IGNORE_BACKEND_K2: JS_IR

// KT-40686


class Outer(konst o: String, konst oo: String) {
    inner class InnerArg(konst i: String) {
        konst result: String get() = o + i
    }

    inner class InnerParam(konst i: InnerArg = InnerArg("B")) {
        fun foo() = i.result + oo
    }
}


fun box(): String {
    konst o = Outer("A", "C")
    konst i = o.InnerParam()

    konst rr = i.foo()
    if (rr != "ABC") return "FAIL: $rr"

    return "OK"
}