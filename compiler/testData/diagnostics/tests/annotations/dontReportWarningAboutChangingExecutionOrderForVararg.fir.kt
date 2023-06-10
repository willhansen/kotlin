@Retention(AnnotationRetention.RUNTIME)
annotation class Anno(vararg konst x: String, konst y: String)

@Anno(x = [["a", "b"], ["a", "b"]], y = "a")
fun foo1() {}

@Anno(x = ["a", "b"], y = "a")
fun foo2() {}

@Anno(x = <!ARGUMENT_TYPE_MISMATCH!>arrayOf(arrayOf("a"), arrayOf("b"))<!>, y = "a")
fun foo3() {}

@Anno(x = arrayOf("a", "b"), y = "a")
fun foo4() {}

@Retention(AnnotationRetention.RUNTIME)
annotation class Anno1(konst x: Array<in String>, konst y: String)

@Retention(AnnotationRetention.RUNTIME)
annotation class Anno2(vararg konst x: String, konst y: String)

@Anno1(x = ["", Anno2(x = [""], y = "")], y = "")
fun foo5() {}
