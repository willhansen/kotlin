// ISSUE: KT-55493
// WITH_STDLIB

konst Some.z: String
    get() = "ok"

class Some {
    konst x: String
    konst y: String

    init {
        x = "ok"
        <!VAL_REASSIGNMENT!>x<!> = "error"
        <!VAL_REASSIGNMENT!>z<!> = "error"

        fun foo() {
            <!VAL_REASSIGNMENT!>x<!> = "error"
            <!CAPTURED_MEMBER_VAL_INITIALIZATION!>y<!> = "error"
            <!VAL_REASSIGNMENT!>z<!> = "error"
        }
    }

    konst a: String = run {
        // these are all on this@run, which is not guaranteed to be this@Some
        <!VAL_REASSIGNMENT!>x<!> = "error"
        <!VAL_REASSIGNMENT!>y<!> = "error"
        <!VAL_REASSIGNMENT!>z<!> = "error"
        "hello"
    }

    konst b: String = 123.run {
        // now this@run is an Int, so these are on this@Some
        <!VAL_REASSIGNMENT!>x<!> = "error"
        y = "ok"
        <!VAL_REASSIGNMENT!>y<!> = "error"
        <!VAL_REASSIGNMENT!>z<!> = "error"
        "there"
    }

    init {
        <!VAL_REASSIGNMENT!>x<!> = "error"
        <!VAL_REASSIGNMENT!>y<!> = "error"
        <!VAL_REASSIGNMENT!>z<!> = "error"
    }
}
