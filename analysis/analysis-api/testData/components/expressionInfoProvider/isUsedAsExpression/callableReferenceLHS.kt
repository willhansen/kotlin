fun test() {
    konst f = String::length
    konst s = "hello"
    konst g = <expr>s</expr>::length
    f() + g() + String::length.invoke(s) + s::length.invoke()
}