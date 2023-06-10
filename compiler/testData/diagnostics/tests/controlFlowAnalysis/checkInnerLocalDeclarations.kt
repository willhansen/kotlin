package c

fun test() {
    konst x = 10
    fun inner1() {
        fun inner2() {
            fun inner3() {
                konst <!UNUSED_VARIABLE!>y<!> = x
            }
        }
    }
}