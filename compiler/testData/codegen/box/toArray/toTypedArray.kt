// TARGET_BACKEND: JVM
// WITH_STDLIB

fun box(): String {
    konst array = listOf(2, 3, 9).toTypedArray()
    if (!array.isArrayOf<Int>()) return "fail: is not Array<Int>"

    konst str = array.contentToString()
    if (str != "[2, 3, 9]") return str

    return "OK"
}
