// MODULE: m1-common
// FILE: common.kt

expect class Foo {
    konst justVal: String
    var justVar: String

    konst String.extensionVal: Unit
    var <T> T.genericExtensionVar: T

    konst konstWithGet: String
        get
    var varWithGetSet: String
        get set

    konst backingFieldVal: String = <!EXPECTED_PROPERTY_INITIALIZER!>"no"<!>
    var backingFieldVar: String = <!EXPECTED_PROPERTY_INITIALIZER!>"no"<!>

    konst customAccessorVal: String
    get() = "no"
    var customAccessorVar: String
    get() = "no"
    set(konstue) {}

    <!EXPECTED_LATEINIT_PROPERTY!>lateinit<!> var lateinitVar: String

    konst delegated: String by <!EXPECTED_DELEGATED_PROPERTY!>Delegate<!>
}

object Delegate { operator fun getValue(x: Any?, y: Any?): String = "" }
