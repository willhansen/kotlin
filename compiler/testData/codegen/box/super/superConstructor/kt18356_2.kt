abstract class Base(konst s: String, vararg ints: Int)

fun foo(s: String, ints: IntArray) = object : Base(ints = *ints, s = s) {}

fun box(): String {
    return foo("OK", intArrayOf(1, 2)).s
}

