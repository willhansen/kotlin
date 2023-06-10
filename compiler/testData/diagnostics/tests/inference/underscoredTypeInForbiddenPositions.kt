// IGNORE_LEAKED_INTERNAL_TYPES: KT-54568
// !LANGUAGE: +PartiallySpecifiedTypeArguments
// !DIAGNOSTICS: -UNCHECKED_CAST
// WITH_STDLIB

fun <K, T> foo(x: (K) -> T): Pair<K, T> = (1 as K) to (1f as T)

class Foo<K>

class Bar0<K : <!UNRESOLVED_REFERENCE!>_<!><<!UNRESOLVED_REFERENCE!>_<!>>>
class Bar1<K : Foo<<!UNRESOLVED_REFERENCE!>_<!>>>
class Bar2<K : <!UNRESOLVED_REFERENCE!>_<!>>
class Bar3<K> where K : <!UNRESOLVED_REFERENCE!>_<!>
class Bar4<<!UNDERSCORE_IS_RESERVED!>_<!>>

typealias A1<<!UNDERSCORE_IS_RESERVED!>_<!>> = <!TYPEALIAS_SHOULD_EXPAND_TO_CLASS, UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!>
typealias A2<T> = Foo<<!UNRESOLVED_REFERENCE!>_<!>>
typealias A3<T> = (<!UNRESOLVED_REFERENCE!>_<!>) -> T
typealias A4<T> = (T) -> () -> <!UNRESOLVED_REFERENCE!>_<!>
typealias A5<T> = (T) -> (((<!UNRESOLVED_REFERENCE!>_<!>))) -> T

fun foo1(x: <!UNRESOLVED_REFERENCE!>_<!>) {}
fun foo2(x: Foo<<!UNRESOLVED_REFERENCE!>_<!>>) {}
fun foo3(): <!UNRESOLVED_REFERENCE!>_<!> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun foo5(): Foo<<!UNRESOLVED_REFERENCE!>_<!>> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun <K, <!UNDERSCORE_IS_RESERVED!>_<!>> foo6(): Foo<<!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!>> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun <K : <!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!>, <!UNDERSCORE_IS_RESERVED!>_<!>> foo7(): Foo<<!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!>> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

class AA1 : <!UNRESOLVED_REFERENCE!>_<!>
class AA2 : <!FINAL_SUPERTYPE, SUPERTYPE_NOT_INITIALIZED!>Foo<<!UNRESOLVED_REFERENCE!>_<!>><!>

fun <`_`> bar(): Foo<<!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!>> = TODO()

fun test() {
    konst x1 = foo<Int, (<!UNRESOLVED_REFERENCE!>_<!>) -> Unit> { { <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>it<!> } }
    konst x2 = foo<Int, (Int) -> <!UNRESOLVED_REFERENCE!>_<!>> { { it } }
    konst x3 = foo<Int, ((<!UNRESOLVED_REFERENCE!>_<!>)) -> <!UNRESOLVED_REFERENCE!>_<!>> { { <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>it<!> } }
    konst x4 = <!FUNCTION_CALL_EXPECTED, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NO_VALUE_FOR_PARAMETER!>foo<!><!DEBUG_INFO_MISSING_UNRESOLVED!><<!>Int<!SYNTAX!>, _ -> Float><!> { { <!UNRESOLVED_REFERENCE!>it<!> } }
    konst x5 = foo<Int, Foo<(<!UNRESOLVED_REFERENCE!>_<!>) -> Float>> { <!TYPE_MISMATCH!>{ <!TYPE_MISMATCH!>it<!> }<!> }
    konst x6 = foo<Int, Foo<(<!UNRESOLVED_REFERENCE!>_<!>) -> <!UNRESOLVED_REFERENCE!>_<!>>> { <!TYPE_MISMATCH!>{ <!TYPE_MISMATCH!>it<!> }<!> }
    konst x7 = foo<Int, Foo<(Int) -> <!UNRESOLVED_REFERENCE!>_<!>>> { <!TYPE_MISMATCH!>{ <!TYPE_MISMATCH!>it<!> }<!> }

    konst z32: Pair<<!UNRESOLVED_REFERENCE!>_<!>, Float> = 1 to 1f
    konst z34: Pair<((<!UNRESOLVED_REFERENCE!>_<!>)), Float> = 1 to 1f

    konst x8: (Float) -> Int = { x: <!UNRESOLVED_REFERENCE!>_<!> -> 10 }
    konst x9: (Foo<Float>) -> Int = { x: Foo<<!UNRESOLVED_REFERENCE!>_<!>> -> 10 }

    konst x10 = object : <!UNRESOLVED_REFERENCE!>_<!> {}
    konst x11 = object : <!FINAL_SUPERTYPE!>Foo<<!UNRESOLVED_REFERENCE!>_<!>><!>() {}

    if (x11 is <!UNRESOLVED_REFERENCE!>_<!>) { }
    if (x11 is <!INCOMPATIBLE_TYPES!>Foo<<!UNRESOLVED_REFERENCE!>_<!>><!>) { }

    x10 as <!UNRESOLVED_REFERENCE!>_<!>
    x10 <!CAST_NEVER_SUCCEEDS!>as<!> Foo<<!UNRESOLVED_REFERENCE!>_<!>>

    konst x12: Foo<@<!UNRESOLVED_REFERENCE!>_<!> Int>? = null
    konst x13: Foo<@<!UNRESOLVED_REFERENCE!>_<!>() Int>? = null
    konst x14: Foo<@Anno(<!UNRESOLVED_REFERENCE!>_<!>) Int>? = null

    konst x15: <!UNRESOLVED_REFERENCE!>_<!><<!UNRESOLVED_REFERENCE!>_<!>>? = null
}

@Target(AnnotationTarget.TYPE)
annotation class Anno(konst x: Int)
