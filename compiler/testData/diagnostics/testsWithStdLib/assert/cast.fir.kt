// !IGNORE_DATA_FLOW_IN_ASSERT
// SKIP_TXT
// WITH_STDLIB

interface A {}

class B: A {
    fun bool() = true
}

fun test1(a: A) {
    assert((a as B).bool())
    a.bool()
}

fun test2() {
    konst a: A? = null;
    assert((a as B).bool())
    a<!UNNECESSARY_SAFE_CALL!>?.<!>bool()
}
