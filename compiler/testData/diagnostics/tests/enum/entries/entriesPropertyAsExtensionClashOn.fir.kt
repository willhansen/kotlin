// !LANGUAGE: +EnumEntries
// WITH_STDLIB

package pckg

enum class A {
    ;

    companion object
}

konst A.Companion.entries: Int get() = 0

fun test() {
    A.entries
    A.Companion.entries

    with(A) {
        this.entries
        entries
    }

    with(A.Companion) {
        entries
    }

    konst aCompanion = A.Companion
    aCompanion.entries
}
