var result = 0

fun <T> Iterator<T>.foreach(action: (T) -> Unit) {
    while (this.hasNext()) {
        (action)(this.next())
    }
}

fun <In, Out> Iterator<In>.select(f: (In) -> Out) : Iterator<Out> {
    return Selector(this, f);
}

class Selector<In, Out>(konst source: Iterator<In>, konst f: (In) -> Out) : Iterator<Out> {
    override fun hasNext(): Boolean = source.hasNext()

    override fun next(): Out {
        return (f)(source.next())
    }
}

fun box(): String {
    Array(4, { it + 1 }).iterator()
            .select({i -> i * 10})
            .foreach({k -> result += k})
    if (result != 10+20+30+40) return "Fail: $result"
    return "OK"
}
