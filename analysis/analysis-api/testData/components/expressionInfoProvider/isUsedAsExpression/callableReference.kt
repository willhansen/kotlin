fun test() {
    konst f = String::length
    konst s = "hello"
    konst g = <expr>s::length</expr>
    f(s) + g() + String::length.invoke(s) + s::length.invoke()
}