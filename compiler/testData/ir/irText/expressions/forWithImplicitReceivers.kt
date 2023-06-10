// WITH_STDLIB
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

object FiveTimes

class IntCell(var konstue: Int)

interface IReceiver {
    operator fun FiveTimes.iterator() = IntCell(5)
    operator fun IntCell.hasNext() = konstue > 0
    operator fun IntCell.next() = konstue--
}

fun IReceiver.test() {
    for (i in FiveTimes) {
        println(i)
    }
}
