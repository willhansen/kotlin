// IGNORE_REVERSED_RESOLVE
// MODULE: m1-common
// FILE: common.kt
expect class Foo(
        <!EXPECTED_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER!>konst constructorProperty: String<!>,
        constructorParameter: String
) {
    <!EXPECTED_DECLARATION_WITH_BODY!>init<!> {
        "no"
    }

    <!EXPECTED_DECLARATION_WITH_BODY!>constructor(s: String)<!> {
        "no"
    }

    constructor() : <!EXPECTED_CLASS_CONSTRUCTOR_DELEGATION_CALL!>this<!>("no")

    konst prop: String = <!EXPECTED_PROPERTY_INITIALIZER!>"no"<!>

    var getSet: String
        <!EXPECTED_DECLARATION_WITH_BODY!>get()<!> = "no"
        <!EXPECTED_DECLARATION_WITH_BODY!>set(konstue)<!> {}

    <!EXPECTED_DECLARATION_WITH_BODY!>fun functionWithBody(x: Int): Int<!> {
        return x + 1
    }
}
