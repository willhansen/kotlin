// FIR_IDENTICAL
// See KT-28847

class Foo(konst str: String?) {
    konst first = run {
        str.isNullOrEmpty()
        second
    }

    konst second = str.isNullOrEmpty()
}