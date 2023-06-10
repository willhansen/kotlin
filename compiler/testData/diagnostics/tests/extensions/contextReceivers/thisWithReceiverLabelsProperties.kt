// !LANGUAGE: +ContextReceivers

class A<T>(konst a: T)
class B(konst b: Any)
class C(konst c: Any)

context(A<String>, B) var p: Int
    get() {
        this@A.a.length
        this@B.b
        <!NO_THIS!>this<!>
        return 1
    }
    set(konstue) {
        this@A.a.length
        this@B.b
        <!NO_THIS!>this<!>
        <!UNRESOLVED_REFERENCE!>field<!> = konstue
    }

context(A<Int>, A<String>, B) var p: Int
    get() {
        this<!AMBIGUOUS_LABEL!>@A<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>toDouble<!>()
        this<!AMBIGUOUS_LABEL!>@A<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>length<!>
        this@B.b
        <!NO_THIS!>this<!>
        return 1
    }
    set(konstue) {
        this<!AMBIGUOUS_LABEL!>@A<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>length<!>
        this@B.b
        <!NO_THIS!>this<!>
        <!UNRESOLVED_REFERENCE!>field<!> = konstue
    }

context(A<Int>, A<String>, B) konst C.p: Int
    get() {
        this<!AMBIGUOUS_LABEL!>@A<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>length<!>
        this@B.b
        this@C.c
        this@p.c
        this.c
        return 1
    }
