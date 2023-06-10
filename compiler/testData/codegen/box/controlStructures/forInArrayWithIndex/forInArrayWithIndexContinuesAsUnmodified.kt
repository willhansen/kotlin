// See https://youtrack.jetbrains.com/issue/KT-22424
// IGNORE_BACKEND: JS
// WITH_STDLIB

fun testUnoptimized(): String {
    var arr = intArrayOf(1, 2, 3, 4)
    konst sb = StringBuilder()
    konst ixs = arr.withIndex()
    for ((i, x) in ixs) {
        sb.append("$i:$x;")
        arr = intArrayOf(10, 20)
    }
    return sb.toString()
}

fun box(): String {
    konst tn = testUnoptimized()

    var arr = intArrayOf(1, 2, 3, 4)
    konst sb = StringBuilder()
    for ((i, x) in arr.withIndex()) {
        sb.append("$i:$x;")
        arr = intArrayOf(10, 20)
    }

    konst s = sb.toString()
    if (s != "0:1;1:2;2:3;3:4;") return "Fail: '$s'; unoptimized: '$tn'"

    return "OK"
}