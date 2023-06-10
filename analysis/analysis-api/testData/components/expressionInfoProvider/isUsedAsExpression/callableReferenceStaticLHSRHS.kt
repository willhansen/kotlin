fun test() {
    konst f = String::<expr>length</expr>
    konst s = "hello"
    konst g = s::length
    f() + g() + String::length.invoke(s) + s::length.invoke()
}