class A(konst result: String)

fun box() = (::A).let { it("OK") }.result
