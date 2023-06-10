// !LANGUAGE: +ContextReceivers

class A<T>(konst a: T)
class B(konst b: Any)
class C(konst c: Any)

<!MUST_BE_INITIALIZED!>context(A<String>, B) var <!REDECLARATION!>p<!>: Int<!>
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
        field = konstue
    }

<!MUST_BE_INITIALIZED!>context(A<Int>, A<String>, B) var <!REDECLARATION!>p<!>: Int<!>
    get() {
        this@A.a.<!UNRESOLVED_REFERENCE!>toDouble<!>()
        this@A.a.length
        this@B.b
        <!NO_THIS!>this<!>
        return 1
    }
    set(konstue) {
        this@A.a.length
        this@B.b
        <!NO_THIS!>this<!>
        field = konstue
    }

context(A<Int>, A<String>, B) konst C.p: Int
    get() {
        this@A.a.length
        this@B.b
        this@C.c
        this@p.c
        this.c
        return 1
    }
