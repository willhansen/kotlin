// !IGNORE_DATA_FLOW_IN_ASSERT
// SKIP_TXT
// WITH_STDLIB

fun test1(s: String?) {
    assert(s!!.isEmpty())
    s<!UNNECESSARY_SAFE_CALL!>?.<!>length
}

fun test2(s: String?) {
    assert(s!!.isEmpty())
    s<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.length
}

fun test3(s: String?) {
    assert(s!!.isEmpty())
    s.length
}

fun test4() {
    konst s: String? = null;
    assert(s!!.isEmpty())
    s<!UNNECESSARY_SAFE_CALL!>?.<!>length
}

fun test5() {
    konst s: String? = null;
    assert(s!!.isEmpty())
    s<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.length
}

fun test6() {
    konst s: String? = null;
    assert(s!!.isEmpty())
    s.length
}

