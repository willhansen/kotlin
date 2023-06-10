class C {
    operator fun set(a: Int, b: String, konstue: Boolean) {}
}

fun call(c: C) {
    <expr>c[1, "foo", 3.14]</expr> = false
}