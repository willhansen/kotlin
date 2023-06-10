// FIR_IDENTICAL
fun <T: Comparable<T>> arrayData(vararg konstues: T, toArray: Array<T>.() -> Unit) {}

fun <T : Long> arrayLongInheritedData(vararg konstues: T, toArray: Array<T>.() -> Unit) {}

fun longArrayData(vararg konstues: Long, toArray: LongArray.() -> Unit) {}

fun shortArrayData(vararg konstues: Short, toArray: ShortArray.() -> Unit) {}

fun arrayOfLongData(vararg konstues: Long, toArray: Array<Long>.() -> Unit) {}

fun arrayOfShortData(vararg konstues: Short, toArray: Array<Short>.() -> Unit) {}

fun box(): String {
    arrayData(42) { }
    arrayLongInheritedData(42) { }
    longArrayData(42) { }
    shortArrayData(42) { }
    arrayOfLongData(42) { }
    arrayOfShortData(42) { }
    return "OK"
}
