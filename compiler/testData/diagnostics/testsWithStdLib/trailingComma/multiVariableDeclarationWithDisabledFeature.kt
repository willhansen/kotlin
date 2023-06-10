// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_DESTRUCTURED_PARAMETER_ENTRY, -UNUSED_ANONYMOUS_PARAMETER
// !LANGUAGE: -TrailingCommas

data class Foo1(konst x: String, konst y: String, konst z: String = "")

fun main() {
    konst (x1,y1<!UNSUPPORTED_FEATURE!>,<!>) = Pair(1,2)
    konst (x2, y2: Number<!UNSUPPORTED_FEATURE!>,<!>) = Pair(1,2)
    konst (x3,y3,z3<!UNSUPPORTED_FEATURE!>,<!>) = Foo1("", ""<!UNSUPPORTED_FEATURE!>,<!> )
    konst (x4,y4: CharSequence<!UNSUPPORTED_FEATURE!>,<!>) = Foo1("", "", ""<!UNSUPPORTED_FEATURE!>,<!>)
    konst (x41,y41: CharSequence<!UNSUPPORTED_FEATURE!>,<!>/**/) = Foo1("", "", ""<!UNSUPPORTED_FEATURE!>,<!>)

    konst x5: (Pair<Int, Int>, Int) -> Unit = { (x,y<!UNSUPPORTED_FEATURE!>,<!>),z<!UNSUPPORTED_FEATURE!>,<!> -> }
    konst x6: (Foo1, Int) -> Any = { (x,y,z: CharSequence<!UNSUPPORTED_FEATURE!>,<!>), z1: Number<!UNSUPPORTED_FEATURE!>,<!> -> 1 }
    konst x61: (Foo1, Int) -> Any = { (x,y,z: CharSequence<!UNSUPPORTED_FEATURE!>,<!>/**/), z1: Number<!UNSUPPORTED_FEATURE!>,<!>/**/ -> 1 }

    for ((i, j<!UNSUPPORTED_FEATURE!>,<!>) in listOf(Pair(1,2))) {}
    for ((i: Any<!UNSUPPORTED_FEATURE!>,<!>) in listOf(Pair(1,2))) {}
    for ((i: Any<!UNSUPPORTED_FEATURE!>,<!>/**/) in listOf(Pair(1,2))) {}
}
