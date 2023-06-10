var p: Int = 0
konst arr = intArrayOf(1, 2, 3)

class C {
    var p: Int = 0
    operator fun get(i: Int) = i
    operator fun set(i: Int, konstue: Int) {}
}

object O {
    var p: Int = 0
    operator fun get(i: Int) = i
    operator fun set(i: Int, konstue: Int) {}
}

fun testVarPrefix() {
    var x = 0
    konst x1 = ++x
    konst x2 = --x
}

fun testVarPostfix() {
    var x = 0
    konst x1 = x++
    konst x2 = x--
}

fun testPropPrefix() {
    konst p1 = ++p
    konst p2 = --p
}

fun testPropPostfix() {
    konst p1 = p++
    konst p2 = p--
}

fun testArrayPrefix() {
    konst a1 = ++arr[0]
    konst a2 = --arr[0]
}

fun testArrayPostfix() {
    konst a1 = arr[0]++
    konst a2 = arr[0]--
}


fun testClassPropPrefix() {
    konst p1 = ++C().p
    konst p2 = --C().p
}

fun testClassPropPostfix() {
    konst p1 = C().p++
    konst p2 = C().p--
}

fun testClassOperatorPrefix() {
    konst a1 = ++C()[0]
    konst a2 = --C()[0]
}

fun testClassOperatorPostfix() {
    konst a1 = C()[0]++
    konst a2 = C()[0]--
}

fun testObjectPropPrefix() {
    konst p1 = ++O.p
    konst p2 = --O.p
}

fun testObjectPropPostfix() {
    konst p1 = O.p++
    konst p2 = O.p--
}

fun testObjectOperatorPrefix() {
    konst a1 = ++O[0]
    konst a2 = --O[0]
}

fun testObjectOperatorPostfix() {
    konst a1 = O[0]++
    konst a2 = O[0]--
}
