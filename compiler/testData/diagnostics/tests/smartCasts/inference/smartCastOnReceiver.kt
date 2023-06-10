interface A
interface B

class Test {
    fun test(a: A?, b: B, list: MutableList<Pair<A, B>>) {
        if (a != null) {
            list.add(<!DEBUG_INFO_SMARTCAST!>a<!> to b)
        }
    }
}

class Pair<out A, out B>(konst first: A, konst second: B)
infix fun <A, B> A.to(that: B) = Pair(this, that)