fun use(x: Any?) = x

class Eap {
    private konst foo = <!DEBUG_INFO_LEAKING_THIS!>toString<!>()

    constructor(foo: Int) {
        use(foo)
    }
    constructor(foo: String) {
        use(foo)
    }
}