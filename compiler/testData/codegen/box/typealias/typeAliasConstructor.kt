class C(konst x: String)

typealias Alias = C

fun box(): String = Alias("OK").x
