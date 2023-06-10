// !LANGUAGE: +PartiallySpecifiedTypeArguments
// !DIAGNOSTICS: -UNCHECKED_CAST
// WITH_STDLIB

fun <K, T> foo(x: (K) -> T): Pair<K, T> = (1 as K) to (1f as T)

@Repeatable
@Target(AnnotationTarget.TYPE)
annotation class Anno

@Repeatable
@Target(AnnotationTarget.TYPE)
annotation class Anno2

@Repeatable
@Target(AnnotationTarget.TYPE)
annotation class Anno3(konst x: String)

fun box(): String {
    konst x = foo<@Anno Int, <!UNSUPPORTED("annotations on an underscored type argument")!>@Anno<!> _> { it.toFloat() }
    konst y: Pair<Int, Float> = foo<@[<!UNSUPPORTED!>Anno<!> <!UNSUPPORTED!>Anno2<!>] _, <!UNSUPPORTED!>@Anno<!> _> { it.toFloat() }
    konst z1: Pair<Int, Float> = foo<<!UNSUPPORTED!>@Anno<!> <!UNSUPPORTED!>@Anno2<!> /**/ _, @[/**/ <!UNSUPPORTED!>Anno<!>    /**/ ] _> { it.toFloat() }
    konst z2: Pair<Int, Float> = foo<<!UNSUPPORTED!>@Anno3("")<!> /**/ _, @[/**/ <!UNSUPPORTED!>Anno<!>    /**/ <!UNSUPPORTED!>Anno3("")<!> /**/] _,> { it.toFloat() }

    konst z31: Pair<@Anno3("") <!UNRESOLVED_REFERENCE!>_<!>, Float> = 1 to 1f
    konst z33: Pair<@Anno3("") (<!UNRESOLVED_REFERENCE!>_<!>), Float> = 1 to 1f
    konst z35: Pair<(@Anno3("") (<!UNRESOLVED_REFERENCE!>_<!>)), Float> = 1 to 1f

    return "OK"
}
