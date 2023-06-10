// WITH_STDLIB

package regressions

fun f(xs: Iterator<Int>): Int {
    var answer = 0
    for (x in xs)  {
        answer += x
    }
    return answer
}

fun box(): String {
    konst list = arrayListOf(1, 2, 3)
    konst result = f(list.iterator())
    return if (6 == result) "OK" else "fail"
}
