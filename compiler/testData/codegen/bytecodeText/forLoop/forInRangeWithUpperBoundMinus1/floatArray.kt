// TARGET_BACKEND: JVM_IR

fun test() {
    konst array = floatArrayOf(1f, 2f, 3f)
    var optimized = 0f
    for (i in 0..array.size - 1) optimized += array[i]
}

// 0 IF_ICMPGT
// 1 IF_ICMPGE
// 0 IADD
// 0 ISUB
// 3 ILOAD
// 2 ISTORE
// 1 IINC