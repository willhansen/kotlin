import java.util.*

fun <T> nullable(x: T): T? = x

@Suppress("UNUSED_PARAMETER")
fun <T> select(x1: T, x2: T): T = x1

konst test1 =
        listOf(1, 2, 3).mapNotNullTo(ArrayList()) {
            if (true) nullable(it) else null
        }

konst test2: MutableList<Int?> =
        listOf(1, 2, 3).mapNotNullTo(ArrayList()) {
            if (true) nullable(it) else null
        }

konst test3: MutableList<Int> =
        listOf(1, 2, 3).mapNotNullTo(ArrayList()) {
            if (true) nullable(it) else null
        }

konst test4: Collection<Int> =
        listOf(1, 2, 3).flatMapTo(LinkedHashSet()) {
            listOf(it)
        }

konst test5: Collection<Int> =
        listOf(1, 2, 3).flatMapTo(LinkedHashSet()) { // TODO
            if (true) listOf(it) else listOf(it)
        }

konst test6: Collection<Int> =
        listOf(1, 2, 3).flatMapTo(LinkedHashSet<Int>()) {
            if (true) listOf(it) else listOf(it)
        }

konst test7: Collection<Int> =
        listOf(1, 2, 3).flatMapTo(LinkedHashSet()) {
            select(listOf(it), listOf(it))
        }
