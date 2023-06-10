// FIR_IDENTICAL
<!WRONG_MODIFIER_TARGET!>external<!> class A

<!WRONG_MODIFIER_TARGET!>external<!> konst foo: Int = 23

class B {
    <!WRONG_MODIFIER_TARGET!>external<!> class A

    <!WRONG_MODIFIER_TARGET!>external<!> konst foo: Int = 23
}
