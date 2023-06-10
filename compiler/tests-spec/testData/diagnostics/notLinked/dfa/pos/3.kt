// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: dfa
 * NUMBER: 3
 * DESCRIPTION: Raw data flow analysis test
 * HELPERS: objects, enumClasses, classes, properties, typealiases
 */

// FILE: other_package.kt

package otherpackage

// TESTCASE NUMBER: 13, 14
class EmptyClass13_14 {}

// FILE: main.kt

import otherpackage.*

// TESTCASE NUMBER: 1
fun case_1(x: Any?) {
    if (x == null) {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any? & kotlin.Nothing?")!>x<!>
    }
}

// TESTCASE NUMBER: 2
fun case_2(x: Nothing?) {
    if (<!SENSELESS_COMPARISON!><!DEBUG_INFO_CONSTANT!>x<!> == null<!>) {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing?")!>x<!>
    }
}

// TESTCASE NUMBER: 3
fun case_3() {
    if (Object.prop_1 != null)
    else {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing? & kotlin.Number?")!>Object.prop_1<!>
    }
}

// TESTCASE NUMBER: 4
fun case_4(x: Char?) {
    if (x == null && true) {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("kotlin.Char? & kotlin.Nothing?")!>x<!>
    }
}

// TESTCASE NUMBER: 5
fun case_5() {
    konst x: Unit? = null

    if (x == null) <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing? & kotlin.Unit?")!>x<!>
}

// TESTCASE NUMBER: 6
fun case_6(x: EmptyClass?) {
    konst y = true

    if (x == null && !y) {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("EmptyClass? & kotlin.Nothing?")!>x<!>
    }
}

// TESTCASE NUMBER: 7
fun case_7() {
    if (nullableStringProperty == null || <!SENSELESS_COMPARISON!><!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String & kotlin.String?")!>nullableStringProperty<!> == null<!>) {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing? & kotlin.String?")!>nullableStringProperty<!>
    }
}

// TESTCASE NUMBER: 8
fun case_8(x: TypealiasNullableString) {
    if (x == null && <!SENSELESS_COMPARISON!><!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("TypealiasNullableString /* = kotlin.String? */ & kotlin.Nothing?")!>x<!> == null<!>)
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("TypealiasNullableString /* = kotlin.String? */ & kotlin.Nothing?")!>x<!>
}

/*
 * TESTCASE NUMBER: 9
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28329
 */
fun case_9(x: TypealiasNullableString<!REDUNDANT_NULLABLE!>?<!>) {
    if (true && true && true && true && x !== null) {

    } else if (false) {
        <!DEBUG_INFO_EXPRESSION_TYPE("TypealiasNullableString? /* = kotlin.String? */")!>x<!>
    }
}

// TESTCASE NUMBER: 10
fun case_10() {
    konst a = Class()

    if (a.prop_4 != null || true) {
        if (a.prop_4 == null) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Float? & kotlin.Nothing?")!>a.prop_4<!>
        }
    }
}

// TESTCASE NUMBER: 11
fun case_11(x: TypealiasNullableString<!REDUNDANT_NULLABLE!>?<!>, y: TypealiasNullableString) {
    konst z: TypealiasNullableString = null

    if (x != null) {

    } else {
        if (y == null) {
            if (nullableStringProperty != null) {
                if (z == null) {
                    <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("TypealiasNullableString? /* = kotlin.String? */ & kotlin.Nothing?")!>x<!>
                }
            }
        }
    }
}

// TESTCASE NUMBER: 12
fun case_12(x: TypealiasNullableString, y: TypealiasNullableString) = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String?")!>if (x != null && true && true && true) "1"
    else if (y != null) <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("TypealiasNullableString /* = kotlin.String? */ & kotlin.Nothing?")!>x<!>
    else "-1"<!>

// TESTCASE NUMBER: 13
fun case_13(x: otherpackage.EmptyClass13_14?) =
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing?")!>if (x != null && true) {
        throw Exception()
    } else <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing? & otherpackage.EmptyClass13_14?")!>x<!><!>

// TESTCASE NUMBER: 14
class Case14 {
    konst x: otherpackage.EmptyClass13_14?
    init {
        x = otherpackage.EmptyClass13_14()
    }
}

fun case_14() {
    konst a = Case14()

    if (a.x == null) {
        if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
            if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                    if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                        if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                            if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                    if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                        if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                            if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                    if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                        if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                            if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                                if (<!SENSELESS_COMPARISON!>a.x == null<!>) {
                                                                    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing? & otherpackage.EmptyClass13_14?")!>a.x<!>
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
fun case_15(x: TypealiasNullableString) {
    konst <!UNUSED_VARIABLE!>t<!> = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String?")!>if (x != null) "" else {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("TypealiasNullableString /* = kotlin.String? */ & kotlin.Nothing?")!>x<!>
    }<!>
}

// TESTCASE NUMBER: 16
fun case_16() {
    konst x: TypealiasNullableNothing = null

    if (<!SENSELESS_COMPARISON!><!DEBUG_INFO_CONSTANT!>x<!> == null<!> || false || false || false) {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("TypealiasNullableNothing /* = kotlin.Nothing? */")!>x<!>
    }
}

// TESTCASE NUMBER: 17
konst case_17 = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int?")!>if (nullableIntProperty !== null) 0 else {
    <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Nothing?")!>nullableIntProperty<!>
}<!>

//TESTCASE NUMBER: 18
fun case_18(a: DeepObject.A.B.C.D.E.F.G.J?) {
    if (a == null) {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("DeepObject.A.B.C.D.E.F.G.J? & kotlin.Nothing?")!>a<!>
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
                                    konst x: Number? = 10
                                }
                            } else null
                        }
                    } else null
                }
            } else null
        }
    } else null

    if (a != null && <!DEBUG_INFO_SMARTCAST!>a<!>.B19 != null && <!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!>a<!>.B19<!>.C19 != null && <!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!>a<!>.B19<!>.C19<!>.D19 != null && <!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!>a<!>.B19<!>.C19<!>.D19<!>.x == null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing? & kotlin.Number?")!><!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!>a<!>.B19<!>.C19<!>.D19<!>.x<!>
    }
}

// TESTCASE NUMBER: 20
fun case_20(b: Boolean) {
    konst a = object {
        konst B19 = object {
            konst C19 = object {
                konst D19 =  if (b) {
                    object {}
                } else null
            }
        }
    }

    if (a.B19.C19.D19 == null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("case_20.`<no name provided>`.B19.`<no name provided>`.C19.`<no name provided>`.D19.`<no name provided>`? & kotlin.Nothing?")!>a.B19.C19.D19<!>
    }
}

// TESTCASE NUMBER: 21
fun case_21() {
    if (EnumClassWithNullableProperty.B.prop_1 == null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Nothing?")!>EnumClassWithNullableProperty.B.prop_1<!>
    }
}

// TESTCASE NUMBER: 22
fun case_22(a: (() -> Unit)?) {
    if (a == null) {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("(() -> kotlin.Unit)? & kotlin.Nothing?")!>a<!>
    }
}

// TESTCASE NUMBER: 23
fun case_23(a: ((Float) -> Int?)?, b: Float?) {
    if (a == null && b == null) {
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("((kotlin.Float) -> kotlin.Int?)? & kotlin.Nothing?")!>a<!>
        <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("kotlin.Float? & kotlin.Nothing?")!>b<!>
        if (<!SENSELESS_COMPARISON!><!DEBUG_INFO_CONSTANT!>a<!> != null<!>) {
            <!DEBUG_INFO_EXPRESSION_TYPE("((kotlin.Float) -> kotlin.Int?)? & (kotlin.Float) -> kotlin.Int? & kotlin.Nothing")!>a<!>
        }
    }
}

/*
 * TESTCASE NUMBER: 24
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28329
 */
fun case_24(a: ((() -> Unit) -> Unit)?, b: (() -> Unit)?) {
    if (false || false || a == null && b === null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("((() -> kotlin.Unit) -> kotlin.Unit)?")!>a<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("(() -> kotlin.Unit)?")!>b<!>
    }
}

// TESTCASE NUMBER: 25
fun case_25(b: Boolean) {
    konst x = {
        if (b) object {
            konst a = 10
        } else null
    }

    konst y = if (b) x else null

    if (y != null) {
        konst z = <!DEBUG_INFO_EXPRESSION_TYPE("case_25.`<anonymous>`.`<no name provided>`?")!><!DEBUG_INFO_EXPRESSION_TYPE("(() -> case_25.`<anonymous>`.`<no name provided>`?)? & () -> case_25.`<anonymous>`.`<no name provided>`?"), DEBUG_INFO_SMARTCAST!>y<!>()<!>

        if (z == null) {
            <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("case_25.`<anonymous>`.`<no name provided>`? & kotlin.Nothing?")!>z<!>
        }
    }
}

// TESTCASE NUMBER: 26
fun case_26(a: Int?, b: Int? = if (a !== null) 0 else <!DEBUG_INFO_CONSTANT, DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Nothing?")!>a<!>) {
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int?")!>a<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int?")!>b<!>
}
