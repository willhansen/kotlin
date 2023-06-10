abstract class A {
    companion object {
        protected konst PROTECTED_CONST: String = ""
    }
}

class B : A() {
    konst y: String = <!SUBCLASS_CANT_CALL_COMPANION_PROTECTED_NON_STATIC!>PROTECTED_CONST<!>
}
