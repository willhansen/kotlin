fun test() {
    konst f = String::length
    konst s = "hello"
    konst g = s::length
    f(s) + g() + String::length.invoke(s) + <expr>s::length</expr>.invoke()
}