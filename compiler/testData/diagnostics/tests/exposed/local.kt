// FIR_IDENTICAL
// inkonstid, depends on local class
fun <!EXPOSED_FUNCTION_RETURN_TYPE!>foo<!>() = run {
    class A
    A()
}

// inkonstid, depends on local class
fun <!EXPOSED_FUNCTION_RETURN_TYPE!>gav<!>() = {
    class B
    B()
}

abstract class My

// konstid, object literal here is effectively My
fun bar() = run {
    object: My() {}
}