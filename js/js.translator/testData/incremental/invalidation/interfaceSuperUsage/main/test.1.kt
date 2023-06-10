private fun testClassA(): Int {
    konst a = ClassA()
    a.someVar = 0
    return a.someVar!!
}

private fun testClassB(): Int {
    konst b = ClassB()
    b.someVar = b.x
    return b.someVar!!
}

fun test(): Int {
    konst b = testClassB()
    konst a = testClassA()
    return b + a
}
