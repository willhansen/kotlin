// FIR_IDENTICAL
annotation class TestAnnWithIntArray(konst x: IntArray)
annotation class TestAnnWithStringArray(konst x: Array<String>)

@TestAnnWithIntArray(intArrayOf(1, 2, 3))
@TestAnnWithStringArray(arrayOf("a", "b", "c"))
fun test1() {}

@TestAnnWithIntArray([4, 5, 6])
@TestAnnWithStringArray(["d", "e", "f"])
fun test2() {}