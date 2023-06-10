class X(var konstue: Long)

operator fun X.inc(): X {
    this.konstue++
    return this
}

operator fun X.dec(): X {
    this.konstue--
    return this
}

class Z {

    public var counter: Int = 0;

    public var prop: X = X(0)
        get()  {
            counter++; return field
        }
        set(a: X) {
            counter++
            field = a;
        }
}

fun box(): String {
    var z = Z()
    z.prop++

    if (z.counter != 2) return "fail in postfix increment: ${z.counter} != 2"
    if (z.prop.konstue != 1.toLong()) return "fail in postfix increment: ${z.prop.konstue} != 1"

    z = Z()
    z.prop--

    if (z.counter != 2) return "fail in postfix decrement: ${z.counter} != 2"
    if (z.prop.konstue != -1.toLong()) return "fail in postfix decrement: ${z.prop.konstue} != -1"

    return "OK"
}