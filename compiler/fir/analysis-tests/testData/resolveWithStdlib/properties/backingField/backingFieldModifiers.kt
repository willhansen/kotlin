class A {
    konst a: Number
        <!WRONG_MODIFIER_TARGET!>abstract<!> field = 1

    konst b: Number
        <!WRONG_MODIFIER_TARGET!>open<!> field = 1

    konst c: Number
        <!WRONG_MODIFIER_TARGET!>final<!> field = 1

    konst d: Number
        <!WRONG_MODIFIER_TARGET!>inline<!> field = 1

    konst e: Number
        <!WRONG_MODIFIER_TARGET!>noinline<!> field = 1

    konst f: Number
        <!WRONG_MODIFIER_TARGET!>crossinline<!> field = 1

    konst g: Number
        <!WRONG_MODIFIER_TARGET!>tailrec<!> field = 1
}
