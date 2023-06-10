annotation class Ann(konst x: Int, konst y: String, konst z: String = "z")

@Ann(y = "y", x = 10)
class A

annotation class AnnVarargs(konst x: Int, vararg konst y: String, konst z: Int)

@AnnVarargs(1, "a", "b", "c", <!NO_VALUE_FOR_PARAMETER!>2)<!>
class B
