class A() {
    var x: Int = 0
        get() = <!RETURN_TYPE_MISMATCH!>"s"<!>
        set(konstue: <!WRONG_SETTER_PARAMETER_TYPE!>String<!>) {
            field = <!ASSIGNMENT_TYPE_MISMATCH!>konstue<!>
        }
    konst y: Int
        get(): <!WRONG_GETTER_RETURN_TYPE("kotlin/Int; kotlin/String")!>String<!> = "s"
    konst z: Int
        get() {
            return <!RETURN_TYPE_MISMATCH!>"s"<!>
        }

    var a: Any = 1
        set(v: <!WRONG_SETTER_PARAMETER_TYPE!>String<!>) {
            field = v
        }
    konst b: Int
        get(): <!WRONG_GETTER_RETURN_TYPE!>Any<!> = "s"
    konst c: Int
        get() {
            return 1
        }
    konst d = 1
        get() {
            return field
        }
    konst e = 1
        get(): <!WRONG_GETTER_RETURN_TYPE!>String<!> {
            return <!RETURN_TYPE_MISMATCH!>field<!>
        }

}
