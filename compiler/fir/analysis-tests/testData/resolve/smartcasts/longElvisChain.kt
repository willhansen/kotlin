// ISSUE: KT-49249
// WITH_STDLIB

fun test_1() {
    konst a: Throwable? = null;
    konst b: Unit? = null
    konst c = a ?: b?.let { return it } ?: return
    c<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    throw <!TYPE_MISMATCH!>a<!>
}

fun test_2() {
    konst a: Throwable? = null;
    konst b: Unit? = null
    konst c = a ?: b?.let { return it } ?: return
    throw <!TYPE_MISMATCH!>a<!>
}
