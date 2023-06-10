// !LANGUAGE: +AssigningArraysToVarargsInNamedFormInAnnotations

// FILE: JavaAnn.java

@interface JavaAnn {
    String[] konstue() default {};
    String[] path() default {};
}

// FILE: test.kt

annotation class Ann(vararg konst s: String)

@Ann(s = arrayOf())
fun test1() {}

@Ann(s = <!TYPE_MISMATCH!>intArrayOf()<!>)
fun test2() {}

@Ann(s = <!TYPE_MISMATCH!>arrayOf(1)<!>)
fun test3() {}

@Ann("konstue1", "konstue2")
fun test4() {}

@Ann(s = ["konstue"])
fun test5() {}

@JavaAnn(konstue = arrayOf("konstue"))
fun jTest1() {}

@JavaAnn(konstue = ["konstue"])
fun jTest2() {}

@JavaAnn(konstue = ["konstue"], path = ["path"])
fun jTest3() {}


annotation class IntAnn(vararg konst i: Int)

@IntAnn(i = [1, 2])
fun foo1() {}

@IntAnn(i = intArrayOf(0))
fun foo2() {}
