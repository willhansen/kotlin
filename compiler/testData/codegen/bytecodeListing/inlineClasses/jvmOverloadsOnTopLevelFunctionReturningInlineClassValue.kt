// WITH_STDLIB

inline class Z(konst x: Int)

@JvmOverloads
fun testTopLevelFunction(x: Int = 0): Z = Z(x)