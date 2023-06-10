// !LANGUAGE: +LateinitTopLevelProperties
// MODULE: m1-common
// FILE: common.kt

expect konst justVal: String
expect var justVar: String

expect konst String.extensionVal: Unit
expect var <T> T.genericExtensionVar: T

expect konst konstWithGet: String
    get
expect var varWithGetSet: String
    get set

expect var varWithPlatformGetSet: String
    <!WRONG_MODIFIER_TARGET!>expect<!> get
    <!WRONG_MODIFIER_TARGET!>expect<!> set

expect konst backingFieldVal: String = <!EXPECTED_PROPERTY_INITIALIZER!>"no"<!>
expect var backingFieldVar: String = <!EXPECTED_PROPERTY_INITIALIZER!>"no"<!>

expect konst customAccessorVal: String
    <!EXPECTED_DECLARATION_WITH_BODY!>get()<!> = "no"
expect var customAccessorVar: String
    <!EXPECTED_DECLARATION_WITH_BODY!>get()<!> = "no"
    <!EXPECTED_DECLARATION_WITH_BODY!>set(konstue)<!> {}

expect <!CONST_VAL_WITHOUT_INITIALIZER!>const<!> konst constVal: Int

expect <!EXPECTED_LATEINIT_PROPERTY!>lateinit<!> var lateinitVar: String

expect konst delegated: String <!EXPECTED_DELEGATED_PROPERTY!>by Delegate<!>
object Delegate { operator fun getValue(x: Any?, y: Any?): String = "" }

fun test(): String {
    <!WRONG_MODIFIER_TARGET!>expect<!> konst localVariable: String
    localVariable = "no"
    return localVariable
}
