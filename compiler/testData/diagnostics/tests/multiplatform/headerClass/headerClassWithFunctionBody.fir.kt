// IGNORE_REVERSED_RESOLVE
// MODULE: m1-common
// FILE: common.kt
expect class Foo(
        konst constructorProperty: String,
        constructorParameter: String
) {
    init {
        "no"
    }

    constructor(s: String) {
        "no"
    }

    constructor() : this("no")

    konst prop: String = <!EXPECTED_PROPERTY_INITIALIZER!>"no"<!>

    var getSet: String
        get() = "no"
        set(konstue) {}

    <!EXPECTED_DECLARATION_WITH_BODY!>fun functionWithBody(x: Int): Int<!> {
        return x + 1
    }
}
