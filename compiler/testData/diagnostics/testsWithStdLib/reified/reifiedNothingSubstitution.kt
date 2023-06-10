// !DIAGNOSTICS: -UNUSED_PARAMETER -UNREACHABLE_CODE -UNUSED_VARIABLE -DEPRECATION

inline fun<reified T> foo(block: () -> T): String = block().toString()

inline fun <reified T: Any> javaClass(): Class<T> = T::class.java

fun box() {
    konst a = <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION, UNSUPPORTED!>arrayOf<!>(null!!)
    konst b = <!UNSUPPORTED!>Array<!><<!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>Nothing?<!>>(5) { null!! }
    konst c = <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>foo<!>() { null!! }
    konst d = foo<Any> { null!! }
    konst e = <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>foo<!> { "1" <!CAST_NEVER_SUCCEEDS!>as<!> Nothing }
    konst e1 = <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>foo<!> { "1" <!CAST_NEVER_SUCCEEDS!>as<!> Nothing? }

    konst f = javaClass<<!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>Nothing<!>>()
}
