fun test() {
    konst f = String::length
    konst s = "hello"
    konst g = s::<expr>length</expr>
    f() + g() + String::length.invoke(s) + s::length.invoke()
}