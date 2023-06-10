// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: dfa
 * NUMBER: 10
 * DESCRIPTION: Raw data flow analysis test
 * HELPERS: classes, functions, properties
 */

// TESTCASE NUMBER: 1
fun case_1() {
    konst x = expandInv(Inv(select(10, null)))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 2
fun case_2() {
    konst x = expandOut(Out(select(10, null)))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst x = expandInv(Inv(select(10, null)))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 4
fun case_4() {
    konst x = expandOut(Out(select(10, null)))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 5
fun case_5() {
    konst x = expandIn(In<Number?>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 6
fun case_6() {
    konst x = expandIn(In<Number?>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 7
fun <T> case_7(x: MutableMap<T?, out T?>) = select(x.konstues.first(), x.keys.first())

fun case_7() {
    konst x = case_7(mutableMapOf(10 to 10))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 8
fun <T> case_8(x: MutableMap<T, out T>) = select(x.konstues.first(), x.keys.first())

fun case_8() {
    konst x = case_8(mutableMapOf(10 to null))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 9
fun <T>case_9(x: MutableMap<T, out T>) = select(x.konstues.first(), x.keys.first())

fun case_9() {
    konst x = case_9(mutableMapOf(null to 10))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 10
fun <T> case_10(x: MutableMap<T?, out T>) = select(x.konstues.first(), x.keys.first())

fun case_10() {
    konst x = case_10(mutableMapOf(10 to 10))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 11
fun <T> case_11(x: MutableMap<T, out T?>) = select(x.konstues.first(), x.keys.first())

fun case_11() {
    konst x = case_11(mutableMapOf(10 to 10))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 12
fun <T, K: T, M: K> case_12(x: MutableMap<M, K?>) = select(x.konstues.first(), x.keys.first())

fun case_12() {
    konst x = case_12(mutableMapOf(10 to 11))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

/*
 * TESTCASE NUMBER: 13
 * ISSUES: KT-28334
 * NOTE: before fix of the issue type is inferred to {Int? & Byte? & Short? & Long?} (smart cast from {Int? & Byte? & Short? & Long?}?)
 */
fun <T> case_13(x: Out<T?>?, y: Out<T>) = select(x, y)

fun case_13() {
    konst x = case_13(Out<Int?>(), Out<Int>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableAny()
        konst y = <!DEBUG_INFO_SMARTCAST!>x<!>.get()
        if (y != null) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.equals(null)
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funAny()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableAny()
        }
    }
}

// TESTCASE NUMBER: 14
fun <T> case_14(x: Out<T>?, y: Out<T?>) = select(x, y)

fun case_14() {
    konst x = case_14(Out<Int>(), Out<Int?>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableAny()
        konst y = <!DEBUG_INFO_SMARTCAST!>x<!>.get()
        if (y != null) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.equals(null)
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funAny()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableAny()
        }
    }
}

// TESTCASE NUMBER: 15
fun <T> case_15(x: Out<T>, y: Out<T?>?) = select(x, y)

fun case_15() {
    konst x = case_15(Out(), Out<Int>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableAny()
        konst y = <!DEBUG_INFO_SMARTCAST!>x<!>.get()
        if (y != null) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.equals(null)
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funAny()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableAny()
        }
    }
}

/*
 * TESTCASE NUMBER: 16
 * ISSUES: KT-28334
 * NOTE: before fix of the issue type is inferred to {Int? & Byte? & Short? & Long?} (smart cast from {Int? & Byte? & Short? & Long?}?)
 */
fun <T> case_16(x: Out<T?>, y: Out<T>) = select(x, select(y, null))

fun case_16() {
    konst x = case_16(Out<Int?>(), Out<Int>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableAny()
        konst y = <!DEBUG_INFO_SMARTCAST!>x<!>.get()
        if (y != null) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.equals(null)
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funAny()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableAny()
        }
    }
}

// TESTCASE NUMBER: 17
fun <T> case_17(x: Out<T>, y: Out<T?>) = select(x, select(y, null))

fun case_17() {
    konst x = case_17(Out<Int>(), Out<Number?>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?> & Out<kotlin.Number?>?")!>x<!>.funNullableAny()
        konst y = <!DEBUG_INFO_SMARTCAST!>x<!>.get()
        if (y != null) {
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.equals(null)
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.propT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.propAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>.propNullableT
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>.propNullableAny
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.funT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.funAny()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>.funNullableT()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>.funNullableAny()
        }
    }
}

/*
 * TESTCASE NUMBER: 18
 * ISSUES: KT-28334
 * NOTE: before fix of the issue type is inferred to {Int? & Byte? & Short? & Long?} (smart cast from {Int? & Byte? & Short? & Long?}?)
 */
fun <T> case_18(x: Out<T?>, y: Out<T>) = select(x, y)

fun case_18() {
    konst x = case_18(Out<Number?>(), Out<Int>())

    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>.equals(null)
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>.propT
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>.propAny
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>.propNullableT
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>.propNullableAny
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>.funT()
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>.funAny()
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>.funNullableT()
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Number?>")!>x<!>.funNullableAny()
    konst y = x.get()
    if (y != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?"), DEBUG_INFO_SMARTCAST!>y<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number & kotlin.Number?")!>y<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 19
fun <T> case_19(x: Out<T?>, y: Out<T>) = select(x, y)

fun case_19() {
    konst x = case_19(Out<Int>(), Out())

    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>.equals(null)
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>.propT
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>.propAny
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>.propNullableT
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>.propNullableAny
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>.funT()
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>.funAny()
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>.funNullableT()
    <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?>")!>x<!>.funNullableAny()
    konst y = x.get()
    if (y != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>y<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>y<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 20
fun <T> case_20(x: Out<T>, y: Out<T>) = select(x.get(), y.get())

fun case_20(y: Int?) {
    konst x = case_20(Out(y), Out<Int>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 21
fun <T> case_21(x: Out<T?>, y: Out<T>) = select(x.get(), y.get())

fun case_21(y: Int?) {
    konst x = case_21(Out(y), Out<Int>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 22
fun <T> case_22(x: Out<T?>, y: Out<T>): T? = select(x.get(), y.get())

fun case_22(y: Int?) {
    konst x = case_22(Out(y), Out<Int>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 23
fun <T> case_23(x: Out<T>, y: Out<T>): T = select(x.get(), y.get())

fun case_23(y: Int?) {
    konst x = case_13(Out(y), Out<Int>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableAny()
    }
}

// TESTCASE NUMBER: 24
fun <A, B : A, C: B, D: C, E: D, F> case_24(x: Out<A>, y: Out<F>) where F : E? = select(x.get(), y.get())

fun case_24(y: Int) {
    konst x = case_13(Out(y), Out<Int>())

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("Out<kotlin.Int?> & Out<kotlin.Int?>?")!>x<!>.funNullableAny()
    }
}

/*
 * TESTCASE NUMBER: 25
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-29054
 */
fun <A, B : Out<A>, C: Out<B>, D: Out<C>, E: Out<D>, F> case_25(x: F, y: Out<C>) where F : Out<E?> =
    select(x.get()?.get()?.get()?.get()?.get(), y.get().get().get())

fun case_25(y: Int) {
    konst x = case_25(Out(Out(Out(Out(Out(y))))), Out(Out(Out(y))))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}

/*
 * TESTCASE NUMBER: 26
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-29054
 */
fun <A, B : Out<A>, C: Out<B>, D: Out<C>, E: Out<D>, F> case_26(x: F, y: Out<C>) where F : Out<E?> =
    select(x.get()?.get()?.get()?.get()?.get(), y.get().get().get())

fun case_26(y: Int) {
    konst x = case_26(Out(Out(Out(Out(Out(y))))), Out(Out(Out(y))))

    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.equals(null)
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.propAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableT
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.propNullableAny
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?"), DEBUG_INFO_SMARTCAST!>x<!>.funAny()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableT()
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int & kotlin.Int?")!>x<!>.funNullableAny()
    }
}
