fun foo(u : Unit) : Int = 1

fun test() : Int {
    foo(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
    konst a : () -> Unit = {
        foo(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
    }
    return 1
}
