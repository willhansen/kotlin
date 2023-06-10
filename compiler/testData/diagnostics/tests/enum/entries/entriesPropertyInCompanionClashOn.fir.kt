// !LANGUAGE: +EnumEntries
// WITH_STDLIB

enum class A {
    ;

    companion object {
        konst entries = 0
    }
}

fun test() {
    A.entries
    A.Companion.entries

    with(A) {
        entries
        this.entries
        <!UNRESOLVED_REFERENCE!>konstues<!>() // to be sure that we don't resolve into synthetic 'konstues'
    }

    with(A.Companion) {
        entries
    }

    konst aCompanion = A.Companion
    aCompanion.entries
}
