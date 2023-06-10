class A(konst next: A? = null) {
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst x: String<!>
    init {
        next?.<!VAL_REASSIGNMENT!>x<!> = "a"
    }
}

class B(konst next: B? = null) {
    var x: String = next?.x ?: "default" // it's ok to use `x` of next
}
