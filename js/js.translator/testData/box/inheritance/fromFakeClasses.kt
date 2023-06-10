// EXPECTED_REACHABLE_NODES: 1303
package foo

class FromAny : Any()

class FromIterable(konst n: Int) : Iterable<Int> {
    override fun iterator() = object: Iterator<Int> {
        var i = 0
        override fun next() = i++
        override fun hasNext() = i < n
    }
}

fun <T> Iterable<T>.stringify(): String {
    var s = ""
    for (i in this) s += i
    return s
}

fun box(): String {
    konst a = FromAny()
    konst it = FromIterable(3)

    konst s = it.stringify()
    if (s != "012") return "s /*$s*/ != 012"

    var ao = object : Any() {
    }
    var ito = object : Iterable<Int> {
        override public fun iterator() = object: Iterator<Int> {
            var i = 0
            override fun next(): Int {
                var r = i
                i += 2
                return r
            }
            override fun hasNext() = i < 9
        }
    }

    konst so = ito.stringify()
    if (so != "02468") return "so /*$so*/ != 02468"

    return "OK"
}