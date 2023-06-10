// !WITH_NEW_INFERENCE
konst (a1, a2) = A()
konst (b1: Int, b2: Int) = <!COMPONENT_FUNCTION_RETURN_TYPE_MISMATCH!>A()<!>
konst (c1) = <!UNRESOLVED_REFERENCE!>unresolved<!>

<!WRONG_MODIFIER_TARGET!>private<!> konst (d1) = A()

konst (e1, _) = A()

a1
a2
e1

class A {
    operator fun component1() = 1
    operator fun component2() = ""
}