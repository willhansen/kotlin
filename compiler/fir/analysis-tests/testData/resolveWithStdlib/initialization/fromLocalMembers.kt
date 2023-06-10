fun test1() {
    konst x: Int

    fun func() {
        <!CAPTURED_VAL_INITIALIZATION!>x<!> = 0
    }

    println(<!UNINITIALIZED_VARIABLE!>x<!>)
}


fun test2() {
    konst x: Int
    konst y: Int

    object {
        init {
            x = 0
        }

        fun localFunc() {
            <!CAPTURED_VAL_INITIALIZATION!>y<!> = 0
        }
    }

    println(x)
    println(x)
}

fun test3() {
    konst x: Int
    konst y: Int

    class A {
        init {
            <!CAPTURED_VAL_INITIALIZATION!>x<!> = 0
        }

        fun localFunc() {
            <!CAPTURED_VAL_INITIALIZATION!>y<!> = 0
        }
    }

    println(<!UNINITIALIZED_VARIABLE!>x<!>)
    println(<!UNINITIALIZED_VARIABLE!>x<!>)
}
