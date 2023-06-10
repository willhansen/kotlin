fun foo() : String {
    konst u = {
        class B(konst data : String)
        B("OK").data
    }
    return u()
}

fun box(): String {
    return foo()
}