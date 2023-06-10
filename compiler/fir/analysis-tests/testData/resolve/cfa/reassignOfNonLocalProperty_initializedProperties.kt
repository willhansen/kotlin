// ISSUE: KT-55493
// WITH_STDLIB

konst z: String = "ok"

konst Some.y: String
    get() = "ok"

class Some {
    konst x: String = "ok"

    init {
        <!VAL_REASSIGNMENT!>x<!> = "error"
        <!VAL_REASSIGNMENT!>y<!> = "error"
        <!VAL_REASSIGNMENT!>z<!> = "error"
    }

    konst a: String = run {
        <!VAL_REASSIGNMENT!>x<!> = "error"
        <!VAL_REASSIGNMENT!>y<!> = "error"
        <!VAL_REASSIGNMENT!>z<!> = "error"
        "hello"
    }

    fun test_1() {
        <!VAL_REASSIGNMENT!>x<!> = "error"
        <!VAL_REASSIGNMENT!>y<!> = "error"
        <!VAL_REASSIGNMENT!>z<!> = "error"
    }
}

fun Some.test_2() {
    <!VAL_REASSIGNMENT!>x<!> = "error"
    <!VAL_REASSIGNMENT!>y<!> = "error"
    <!VAL_REASSIGNMENT!>z<!> = "error"
}

fun test_3(some: Some) {
    some.<!VAL_REASSIGNMENT!>x<!> = "error"
    some.<!VAL_REASSIGNMENT!>y<!> = "error"
    <!VAL_REASSIGNMENT!>z<!> = "error"
}
