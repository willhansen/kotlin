annotation class A(konst a: IntArray = <!TYPE_MISMATCH!>arrayOf(1)<!>)
annotation class B(konst a: IntArray = intArrayOf(1))
