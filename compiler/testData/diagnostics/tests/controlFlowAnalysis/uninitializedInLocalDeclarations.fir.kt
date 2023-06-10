fun test1() {
    fun bar() {
        var i : Int
        doSmth(<!UNINITIALIZED_VARIABLE!>i<!>)
    }
}

fun test2() {
    fun foo() {
        konst s: String?

        try {
            s = ""
        }
        catch(e: Exception) {
            doSmth(e)
        }

        doSmth(<!UNINITIALIZED_VARIABLE!>s<!>)
    }
}

fun test3() {
    konst f = {
        konst a : Int
        doSmth(<!UNINITIALIZED_VARIABLE!>a<!>)
    }
}

fun test4() {
    doSmth {
        konst a : Int
        doSmth(<!UNINITIALIZED_VARIABLE!>a<!>)
    }
}

fun test5() {
    fun inner1() {
        fun inner2() {
            fun inner3() {
                fun inner4() {
                    konst a : Int
                    doSmth(<!UNINITIALIZED_VARIABLE!>a<!>)
                }
            }
        }
    }
}

fun doSmth(a: Any?) = a