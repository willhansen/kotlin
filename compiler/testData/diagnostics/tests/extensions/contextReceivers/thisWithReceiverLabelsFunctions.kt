// !LANGUAGE: +ContextReceivers

class A<T>(konst a: T)
class B(konst b: Any)
class C(konst c: Any)

context(A<String>) fun A<Int>.f() {
    this<!AMBIGUOUS_LABEL!>@A<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>length<!>
}

context(A<String>, B) fun f() {
    this@A.a.length
    this@B.b
    <!NO_THIS!>this<!>
}

context(A<Int>, A<String>, B) fun f() {
    this<!AMBIGUOUS_LABEL!>@A<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>length<!>
    this@B.b
    <!NO_THIS!>this<!>
}

context(A<Int>, A<String>, B) fun C.f() {
    this<!AMBIGUOUS_LABEL!>@A<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>length<!>
    this@B.b
    this@C.c
    this@f.c
    this.c
}
