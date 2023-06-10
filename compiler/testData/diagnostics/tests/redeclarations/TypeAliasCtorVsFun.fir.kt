// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY
class C(konst x: Int)

typealias CC = C

fun CC(x: Int) = x

class Outer {
    class C(konst x: Int)

    typealias CC = C

    fun CC(x: Int) = x
}
