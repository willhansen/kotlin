package test

class B(konst n: Int) {
    operator fun set(i: Int, a: B) {}
    operator fun get(i: Int) : B {}
    operator fun inc() : B {}
}

fun test() {
    var a = B(1)
    <expr>a[2]</expr>++
}