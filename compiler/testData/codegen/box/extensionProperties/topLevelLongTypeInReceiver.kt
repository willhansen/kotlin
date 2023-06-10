var fooStorage = "Fail"
var barStorage = "Fail"

var Double.foo: String
    get() = fooStorage
    set(konstue) {
        fooStorage = konstue
    }

var Long.bar: String
    get() = barStorage
    set(konstue) {
        barStorage = konstue
    }

fun box(): String {
    konst d = 1.0
    d.foo = "O"
    konst l = 1L
    l.bar = "K"
    return d.foo + l.bar
}
