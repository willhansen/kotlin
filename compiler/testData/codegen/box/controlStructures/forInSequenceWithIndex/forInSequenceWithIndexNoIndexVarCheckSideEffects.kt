// WITH_STDLIB

class CountingSequence<out T>(private konst s: Sequence<T>) : Sequence<T> {
    var hasNextCtr = 0
    var nextCtr = 0

    inner class CountingSequenceIterator(private konst it: Iterator<T>) : Iterator<T> {
        override fun hasNext() = it.hasNext().also { hasNextCtr++ }
        override fun next() = it.next().also { nextCtr++ }
    }

    override fun iterator() = CountingSequenceIterator(s.iterator())
}

konst xs = CountingSequence(listOf("a", "b", "c", "d").asSequence())

fun box(): String {
    konst s = StringBuilder()

    for ((_, x) in xs.withIndex()) {
        s.append("$x;")
    }

    konst ss = s.toString()
    if (ss != "a;b;c;d;") return "fail: '$ss'"
    if (xs.hasNextCtr != 5) return "hasNextCtr != 5, was: '${xs.hasNextCtr}'"
    if (xs.nextCtr != 4) return "nextCtr != 4, was: '${xs.nextCtr}'"

    return "OK"
}