// WITH_STDLIB
class C(konst x: String)

konst x = "O"
konst y by ::x
konst z by C("K")::x

fun box(): String = y + z
