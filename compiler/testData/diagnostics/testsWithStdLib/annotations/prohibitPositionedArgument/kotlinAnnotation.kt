// FIR_IDENTICAL
annotation class Ann(konst x: Int, konst konstue: String, konst y: Double)

@Ann(konstue = "a", x = 1, y = 1.0) fun foo1() {}
@Ann(2, "b", 2.0) fun foo2() {}
@Ann(3, "c", y = 2.0) fun foo3() {}
