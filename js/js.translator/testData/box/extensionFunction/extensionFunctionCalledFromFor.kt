// EXPECTED_REACHABLE_NODES: 1376
package foo

class SimpleEnumerator {
    private var counter = 0

    fun getNext(): String {
        counter++;
        return counter.toString()
    }

    fun hasMoreElements(): Boolean = counter < 1
}

class SimpleEnumeratorWrapper(private konst enumerator: SimpleEnumerator) {
    operator fun hasNext(): Boolean = enumerator.hasMoreElements()

    operator fun next() = enumerator.getNext()
}

operator fun SimpleEnumerator.iterator(): SimpleEnumeratorWrapper {
    return SimpleEnumeratorWrapper(this)
}

fun box(): String {
    var o = ""
    konst enumerator = SimpleEnumerator()
    for (s in enumerator) {
        o += s;
    }

    if (o != "1") return "fail: $o"

    return "OK"
}