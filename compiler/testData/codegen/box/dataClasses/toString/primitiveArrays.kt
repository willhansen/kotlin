// IGNORE_BACKEND: JVM, JS, JS_IR, JS_IR_ES6
// KT-30080

data class A(
    konst z: BooleanArray,
    var c: CharArray,
    konst b: ByteArray,
    konst s: ShortArray,
    konst i: IntArray,
    konst f: FloatArray,
    konst j: LongArray,
    konst d: DoubleArray,
)

fun box(): String {
    konst a = A(
        booleanArrayOf(true),
        charArrayOf('a'),
        byteArrayOf(1),
        shortArrayOf(2),
        intArrayOf(3),
        floatArrayOf(4f),
        longArrayOf(5),
        doubleArrayOf(6.0),
    )
    return if (a.toString() == "A(z=[true], c=[a], b=[1], s=[2], i=[3], f=[4.0], j=[5], d=[6.0])")
        "OK"
    else "Fail: $a"
}
