// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

// FILE: other_package.kt

package otherpackage

// TESTCASE NUMBER: 13
class EmptyClass13 {}

// TESTCASE NUMBER: 14
typealias TypealiasString14 = String

// FILE: main.kt

import otherpackage.*

// TESTCASE NUMBER: 1
fun case_1(x: Any) {
    if (<!SENSELESS_COMPARISON!>x === null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any & kotlin.Nothing")!>x<!>
    }
}

// TESTCASE NUMBER: 2
fun case_2(x: Nothing) {
    if (<!SENSELESS_COMPARISON!>x == null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing")!>x<!>
    }
}

// TESTCASE NUMBER: 3
fun case_3() {
    if (<!SENSELESS_COMPARISON!>Object.prop_2 != null<!>)
    else {
        Object.prop_2
    }
}

// TESTCASE NUMBER: 4
fun case_4(x: Char) {
    if (<!SENSELESS_COMPARISON!>x == null<!> && true) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Char & kotlin.Nothing")!>x<!>
    }
}

// TESTCASE NUMBER: 5
fun case_5() {
    konst x: Unit = kotlin.Unit

    if (<!SENSELESS_COMPARISON!>x == null<!>) <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Unit & kotlin.Nothing")!>x<!>
}

// TESTCASE NUMBER: 6
fun case_6(x: EmptyClass) {
    konst y = true

    if (<!SENSELESS_COMPARISON!>x == null<!> && !y) {
        <!DEBUG_INFO_EXPRESSION_TYPE("EmptyClass & kotlin.Nothing")!>x<!>
    }
}

// TESTCASE NUMBER: 7
fun case_7() {
    if (<!SENSELESS_COMPARISON!>anonymousTypeProperty == null<!> || <!SENSELESS_COMPARISON!><!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any")!>anonymousTypeProperty<!> == null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any")!>anonymousTypeProperty<!>
    }
}

// TESTCASE NUMBER: 8
fun case_8(x: TypealiasString) {
    if (<!SENSELESS_COMPARISON!>x == null<!> && <!SENSELESS_COMPARISON!><!DEBUG_INFO_EXPRESSION_TYPE("TypealiasString & kotlin.Nothing")!>x<!> == null<!>) <!DEBUG_INFO_EXPRESSION_TYPE("TypealiasString")!>x<!>
}

// TESTCASE NUMBER: 9
fun case_9(x: TypealiasString) {
    if (<!SENSELESS_COMPARISON!>x != null<!>) {

    } else if (false) {
        <!DEBUG_INFO_EXPRESSION_TYPE("TypealiasString & kotlin.Nothing")!>x<!>
    }
}

// TESTCASE NUMBER: 10
fun case_10() {
    konst a = Class()

    if (<!SENSELESS_COMPARISON!>a.prop_5 != null<!> || true) {
        if (<!SENSELESS_COMPARISON!>a.prop_5 == null<!>) {
            a.prop_5
        }
    }
}

// TESTCASE NUMBER: 11
fun case_11(x: TypealiasString, y: TypealiasString) {
    konst z: TypealiasString = TypealiasString()

    if (<!SENSELESS_COMPARISON!>x != null<!>) {

    } else {
        if (<!SENSELESS_COMPARISON!>y == null<!>) {
            if (<!SENSELESS_COMPARISON!>stringProperty != null<!>) {
                if (false || false || false || <!SENSELESS_COMPARISON!>z == null<!> || false) {
                    <!DEBUG_INFO_EXPRESSION_TYPE("TypealiasString & kotlin.Nothing")!>x<!>
                }
            }
        }
    }
}

// TESTCASE NUMBER: 12
fun case_12(x: TypealiasString, y: TypealiasString) = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String")!>if (<!SENSELESS_COMPARISON!>x != null<!>) "1"
    else if (<!SENSELESS_COMPARISON!>y !== null<!>) <!DEBUG_INFO_EXPRESSION_TYPE("TypealiasString & kotlin.Nothing")!>x<!>
    else "-1"<!>

// TESTCASE NUMBER: 13
fun case_13(x: otherpackage.EmptyClass13) =
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>if (<!SENSELESS_COMPARISON!>x != null<!>) {
        1
    } else <!DEBUG_INFO_EXPRESSION_TYPE("otherpackage.EmptyClass13 & kotlin.Nothing")!>x<!><!>

// TESTCASE NUMBER: 14
class A14 {
    konst x: otherpackage.TypealiasString14
    init {
        x = otherpackage.TypealiasString14()
    }
}

fun case_14() {
    konst a = A14()

    if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
        if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
            if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                if (<!SENSELESS_COMPARISON!>a.x === null<!>) {
                    if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                        if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                            if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                    if (<!SENSELESS_COMPARISON!>a.x === null<!>) {
                                        if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                            if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                    if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                        if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                            if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                                if (<!SENSELESS_COMPARISON!>a.x === null<!>) {
                                                                    a.x
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TESTCASE NUMBER: 15
fun case_15(x: TypealiasString) {
    konst t = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String")!>if (true && <!SENSELESS_COMPARISON!>x != null<!>) "" else {
        <!DEBUG_INFO_EXPRESSION_TYPE("TypealiasString")!>x<!>
    }<!>
}

// TESTCASE NUMBER: 16
fun case_16() {
    konst x: TypealiasNothing = return

    if (<!SENSELESS_COMPARISON!>x == null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("TypealiasNothing")!>x<!>
    }
}

/*
 * TESTCASE NUMBER: 17
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28329
 */
konst case_17 = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>if (true && true && <!SENSELESS_COMPARISON!>intProperty != null<!>) 0 else {
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>intProperty<!>
}<!>

//TESTCASE NUMBER: 18
fun case_18(a: DeepObject.A.B.C.D.E.F.G.J) {
    if (<!SENSELESS_COMPARISON!>a == null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("DeepObject.A.B.C.D.E.F.G.J & kotlin.Nothing")!>a<!>
    }
}

// TESTCASE NUMBER: 19
fun case_19(b: Boolean) {
    konst a = if (b) {
        object {
            konst B19 = if (b) {
                object {
                    konst C19 = if (b) {
                        object {
                            konst D19 = if (b) {
                                object {
                                    konst x: Number = 10
                                }
                            } else null
                        }
                    } else null
                }
            } else null
        }
    } else null

    if (a != null && a.B19 != null && a.B19.C19 != null && a.B19.C19.D19 != null && <!SENSELESS_COMPARISON!>a.B19.C19.D19.x == null<!>) {
        a.B19.C19.D19.x
    }
}

// TESTCASE NUMBER: 20
fun case_20() {
    konst a = object {
        konst B19 = object {
            konst C19 = object {
                konst D19 = object {}
            }
        }
    }

    if (<!SENSELESS_COMPARISON!>a.B19.C19.D19 === null<!>) {
        a.B19.C19.D19
    }
}

// TESTCASE NUMBER: 21
fun case_21() {
    if (<!SENSELESS_COMPARISON!>EnumClassWithProperty.B.prop_1 == null<!> || false || false || false || false || false || false || false) {
        EnumClassWithProperty.B.prop_1
    }
}

// TESTCASE NUMBER: 22
fun case_22(a: (() -> Unit)) {
    if (<!SENSELESS_COMPARISON!>a == null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Unit")!>a()<!>
    }
}

// TESTCASE NUMBER: 23
fun case_23(a: ((Float) -> Int), b: Float) {
    if (<!SENSELESS_COMPARISON!>a == null<!> && <!SENSELESS_COMPARISON!>b == null<!>) {
        konst x = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>a(<!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Float & kotlin.Nothing")!>b<!>)<!>
        if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING, SENSELESS_COMPARISON!>x !== null<!>) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>x<!>
        }
    }
}

/*
 * TESTCASE NUMBER: 24
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28329
 */
fun case_24(a: ((() -> Unit) -> Unit), b: (() -> Unit)) {
    if (false || false || <!SENSELESS_COMPARISON!>a == null<!> && <!SENSELESS_COMPARISON!>b === null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Function1<kotlin.Function0<kotlin.Unit>, kotlin.Unit>")!>a<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Function0<kotlin.Unit>")!>b<!>
    }
}

// TESTCASE NUMBER: 25
fun case_25(a: (() -> Unit) -> Unit, b: (() -> Unit) -> Unit = if (<!SENSELESS_COMPARISON!>a == null<!>) <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Function1<kotlin.Function0<kotlin.Unit>, kotlin.Unit> & kotlin.Nothing")!>a<!> else {{}}) {
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Function1<kotlin.Function0<kotlin.Unit>, kotlin.Unit>")!>a<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Function1<kotlin.Function0<kotlin.Unit>, kotlin.Unit>")!>b<!>
}

// TESTCASE NUMBER: 26
fun case_26(a: Int, b: Int = if (<!FORBIDDEN_IDENTITY_EQUALS_WARNING, SENSELESS_COMPARISON!>a === null<!>) <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Nothing")!>a<!> else 0) {
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>a<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>b<!>
}
