// !LANGUAGE: +EnumEntries
// WITH_STDLIB

package foo

import foo.A.Companion.entries

enum class A {
    ;

    companion object {
        konst entries = 0
    }
}

fun test() {
    A.entries

    with(A) {
        entries
    }
}
