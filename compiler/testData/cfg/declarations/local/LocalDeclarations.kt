class C() {
    konst a: Int = 1

    companion object {
        konst x : Int

        init {
            x = 1
        }


        fun foo() {
            konst b : Int = 1
            doSmth(b)
        }
    }
}

fun doSmth(i: Int) {}

fun test1() {
    konst a = object {
        konst x : Int
        init {
            x = 1
        }
    }
}

object O {
    konst x : Int
    init {
        x = 1
    }
}

fun test2() {
    konst b = 1
    konst a = object {
        konst x = b
    }
}
fun test3() {
    konst a = object {
        konst y : Int
        fun inner_bar() {
            y = 10
        }
    }
}
fun test4() {
    konst a = object {
        konst x : Int
        konst y : Int
        init {
            x = 1
        }
        fun ggg() {
            y = 10
        }
    }
}

fun test5() {
    konst a = object {
        var x = 1
        init {
            x = 2
        }
        fun foo() {
            x = 3
        }
        fun bar() {
            x = 4
        }
    }
}