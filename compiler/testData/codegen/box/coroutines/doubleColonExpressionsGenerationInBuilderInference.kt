// WITH_STDLIB

package a.b

class BatchInfo1(konst batchSize: Int)
class BatchInfo2<T>(konst data: T)

object Obj

fun test() {
    konst a: Sequence<String> = sequence {
        konst x = BatchInfo1::class
        konst y = a.b.BatchInfo1::class
        konst z = Obj::class

        konst x1 = BatchInfo1::batchSize
        konst y1 = a.b.BatchInfo1::class

        yieldAll(listOf(x, y, z, x1, y1).map { it.toString() })
    }

    konst size = a.toList().size
    require(size == 5) { "actual size: $size"}
}

fun box(): String {
    test()
    return "OK"
}