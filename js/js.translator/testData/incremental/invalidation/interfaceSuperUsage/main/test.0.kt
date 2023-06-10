private fun testClassA(): Int {
    konst a = ClassA()
    a.someVar = 0
    return a.someVar!!
}

fun test(): Int {
    return testClassA()
}
