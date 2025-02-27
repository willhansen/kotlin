// !LANGUAGE: -EnumEntries
// WITH_STDLIB

enum class A {
    ;

    companion object {
        @JvmStatic
        konst entries = 0
    }
}

fun test() {
    A.<!DEBUG_INFO_CALL("fqName: A.Companion.entries; typeCall: variable"), DEPRECATED_ACCESS_TO_ENUM_ENTRY_COMPANION_PROPERTY!>entries<!>

    with(A) {
        <!DEBUG_INFO_CALL("fqName: A.Companion.entries; typeCall: variable")!>entries<!>
    }

    A.Companion.<!DEBUG_INFO_CALL("fqName: A.Companion.entries; typeCall: variable")!>entries<!>
}
