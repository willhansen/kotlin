class A(konst next: A? = null) {
    konst x: String
    init {
        next?.<!VAL_REASSIGNMENT!>x<!> = "a"
        x = "b"
        this.<!VAL_REASSIGNMENT!>x<!> = "c"
        <!VAL_REASSIGNMENT!>x<!> = "d" // don't repeat the same diagnostic again with this receiver
        this.<!VAL_REASSIGNMENT!>x<!> = "e"

        next?.<!VAL_REASSIGNMENT!>x<!> = "f"
    }
}
