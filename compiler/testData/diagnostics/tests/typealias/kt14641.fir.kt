// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

class A {
    public inner class B { }
    public typealias BAlias = B
}

fun f() {
    konst a = A()
    a.<!UNRESOLVED_REFERENCE!>BAlias<!>
}