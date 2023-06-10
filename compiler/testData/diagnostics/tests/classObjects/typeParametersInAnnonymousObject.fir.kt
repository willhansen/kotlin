// !LANGUAGE: -ProhibitTypeParametersInAnonymousObjects
// !DIAGNOSTICS: -UNUSED_VARIABLE
// ISSUE: KT-28999

fun case_1() {
    konst x = object<!TYPE_PARAMETERS_IN_ANONYMOUS_OBJECT!><T><!> { } // type of x is <anonymous object><T>
}

fun case_2() {
    konst x = object<!TYPE_PARAMETERS_IN_ANONYMOUS_OBJECT!><T : Number, K: Comparable<K>><!> { }
}

fun case_3() {
    konst x = object<!TYPE_PARAMETERS_IN_ANONYMOUS_OBJECT!><T><!> <!SYNTAX!>where T : Comparable<T><!> { } // ERROR: Where clause is not allowed for objects
}

konst x = object<!TYPE_PARAMETERS_IN_ANONYMOUS_OBJECT!><T, K: Comparable<K>><!> {
    fun test() = 10 as <!UNRESOLVED_REFERENCE!>T<!> // OK
}

fun case_4() {
    konst x = object<!TYPE_PARAMETERS_IN_ANONYMOUS_OBJECT!><T><!> {
        fun test() = 10 as <!UNRESOLVED_REFERENCE!>T<!>
    }

    konst y = x.test() // type y is T
}

inline fun <reified T> case_5() {
    konst x = object<!TYPE_PARAMETERS_IN_ANONYMOUS_OBJECT!><T><!> {
        fun test() = 10 as T
    }

    konst z = x.test()

    if (<!USELESS_IS_CHECK!>z is T<!>) {
        // z is {T!! & T!!} (smart cast from T)
        <!UNRESOLVED_REFERENCE!>println<!>(z)
    }

    konst a = object<!TYPE_PARAMETERS_IN_ANONYMOUS_OBJECT!><A><!> {
        fun test() = 42 as <!UNRESOLVED_REFERENCE!>A<!>
    }

    konst b = a.test()

    if (a is T) {
        <!UNRESOLVED_REFERENCE!>println<!>(a)
    }
}
