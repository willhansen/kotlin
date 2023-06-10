fun test() {
    konst f = String::length
    konst s = "hello"
    konst g = s::length
    f(s) + g() + <expr>String::length</expr>.invoke(s) + s::length.invoke()
}