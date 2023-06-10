// !LANGUAGE: -TrailingCommas
// !DIAGNOSTICS: -UNUSED_PARAMETER

import java.util.ArrayList

fun <T> foo(a : T, b : Collection<T>, c : Int) {
}

fun <T> arrayListOf(vararg konstues: T): ArrayList<T> = throw Exception("$konstues")

konst bar = foo("", <!NO_VALUE_FOR_PARAMETER!>arrayListOf(), )<!>
konst bar2 = foo<String>("", <!NO_VALUE_FOR_PARAMETER!>arrayListOf(), )<!>
