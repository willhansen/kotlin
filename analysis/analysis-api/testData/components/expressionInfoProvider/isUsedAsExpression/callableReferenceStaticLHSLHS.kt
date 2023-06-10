fun test() {
    konst f = <expr>String</expr>::length
    konst s = "hello"
    konst g = s::length
    f() + g() + String::length.invoke(s) + s::length.invoke()
}