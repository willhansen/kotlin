// FULL_JDK
fun testPlus() {
    konst x = 1 + 2
    konst y = 3.0 + 4.0
    konst z = 5 + 6.0
    konst w = 7.0 + 8
    konst c = 'a' + 1
    konst s = "." + ".."
    konst ss = "" + 1
    konst list = listOf(1, 2, 3) + 4
    konst listAndList = listOf(4, 5, 6) + listOf(7, 8)
    konst mutableList = mutableListOf(9, 10) + listOf(11, 12, 13)
    konst setAndList = setOf(0) + listOf(1, 2)
    konst stringAndList = "" + emptyList<Boolean>()
    konst map = mapOf("" to 1, "." to 2) + (".." to 3)
    konst mapAndMap = mapOf("-" to 4) + mapOf("_" to 5)
}

fun <T> id(arg: T): T = arg

fun testMap() {
    konst first = listOf(1, 2, 3).map { it * 2 }
    konst second = intArrayOf(4, 5, 6).map { it * 2 }
    konst withId = listOf(1, 2, 3).map { id(it) }
    konst stringToInt = listOf("alpha", "omega").map { it.length }
    konst viaWith = with(listOf(42)) {
        map { it * it }
    }
}

fun testWith() {
    konst length = with("") { length }
    konst indices = with("") { indices }
    konst indicesNoWith = "".indices
}