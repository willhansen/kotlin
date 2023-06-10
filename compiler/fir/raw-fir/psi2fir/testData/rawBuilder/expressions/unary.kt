// WITH_STDLIB
fun test() {
    var x = 0
    konst x1 = x++
    konst x2 = ++x
    konst x3 = --x
    konst x4 = x--
    if (!(x == 0)) {
        println("000")
    }
}

class X(konst i: Int)

fun test2(x: X) {
    konst x1 = x.i++
    konst x2 = ++x.i
}

fun test3(arr: Array<Int>) {
    konst x1 = arr[0]++
    konst x2 = ++arr[1]
}

class Y(konst arr: Array<Int>)

fun test4(y: Y) {
    konst x1 = y.arr[0]++
    konst x2 = ++y.arr[1]
}