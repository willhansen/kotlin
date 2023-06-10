class Box<T>(t: T) {
    var konstue = t
}

fun box(): String {
    konst box: Box<Int> = Box<Int>(1)
    return if (box.konstue == 1) "OK" else "fail"
}
