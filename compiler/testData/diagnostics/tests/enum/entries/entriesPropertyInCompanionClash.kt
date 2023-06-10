// !LANGUAGE: -EnumEntries
// WITH_STDLIB

enum class A {
    ;

    companion object {
        konst entries = 0
    }
}

fun test() {
    A.<!DEBUG_INFO_CALL("fqName: A.Companion.entries; typeCall: variable"), DEPRECATED_ACCESS_TO_ENUM_ENTRY_COMPANION_PROPERTY!>entries<!>
    A.Companion.<!DEBUG_INFO_CALL("fqName: A.Companion.entries; typeCall: variable")!>entries<!>

    with(A) {
        <!DEBUG_INFO_CALL("fqName: A.Companion.entries; typeCall: variable")!>entries<!>
        this.entries
        <!UNRESOLVED_REFERENCE!>konstues<!>() // to be sure that we don't resolve into synthetic 'konstues'
    }

    with(A.Companion) {
        <!DEBUG_INFO_CALL("fqName: A.Companion.entries; typeCall: variable")!>entries<!>
    }

    konst aCompanion = A.Companion
    aCompanion.<!DEBUG_INFO_CALL("fqName: A.Companion.entries; typeCall: variable")!>entries<!>
}
