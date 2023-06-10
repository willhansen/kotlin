// !LANGUAGE: +ProhibitTypeParametersInAnonymousObjects
// !DIAGNOSTICS: -UNUSED_VARIABLE
// ISSUE: KT-28999

fun case_1() {
    konst x = object<!TYPE_PARAMETERS_IN_OBJECT!><T><!> { } // type of x is <anonymous object><T>
}

fun case_2() {
    konst x = object<!TYPE_PARAMETERS_IN_OBJECT!><T : Number, K: Comparable<K>><!> { }
}

fun case_3() {
    konst x = object<!TYPE_PARAMETERS_IN_OBJECT!><T><!> <!SYNTAX!>where <!DEBUG_INFO_MISSING_UNRESOLVED!>T<!> : <!DEBUG_INFO_MISSING_UNRESOLVED!>Comparable<!><<!DEBUG_INFO_MISSING_UNRESOLVED!>T<!>><!> { } // ERROR: Where clause is not allowed for objects
}

konst x = object<!TYPE_PARAMETERS_IN_OBJECT!><T, K: Comparable<K>><!> {
    fun test() = 10 <!UNCHECKED_CAST!>as T<!> // OK
}

fun case_4() {
    konst x = object<!TYPE_PARAMETERS_IN_OBJECT!><T><!> {
        fun test() = 10 <!UNCHECKED_CAST!>as T<!>
    }

    konst y = x.test() // type y is T
}

inline fun <reified T> case_5() {
    konst x = object<!TYPE_PARAMETERS_IN_OBJECT!><T><!> {
        fun test() = 10 <!UNCHECKED_CAST!>as T<!>
    }

    konst z = x.test()

    if (z is T) {
        // z is {T!! & T!!} (smart cast from T)
        <!UNRESOLVED_REFERENCE!>println<!>(z)
    }

    konst a = object<!TYPE_PARAMETERS_IN_OBJECT!><A><!> {
        fun test() = 42 <!UNCHECKED_CAST!>as A<!>
    }

    konst b = a.test()

    if (a is T) {
        <!UNRESOLVED_REFERENCE!>println<!>(a)
    }
}
