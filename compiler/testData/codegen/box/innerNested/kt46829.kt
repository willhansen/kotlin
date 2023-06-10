class Outer<T> {
    var <V> Inner<T, V>.prop: V
        get() = this.konstue
        set(konstue) {
            this.konstue = konstue
        }
}

class Inner<T, V>(
    konst key: T,
    var konstue: V
)

fun box(): String {
    Outer<Boolean>().run {
        konst i = Inner(true, false)
        i.prop = true
    }
    return "OK"
}