fun test() {
    konst f = String::length
    konst s = "hello"
    konst g = s::length
    f(s) + <expr>g</expr>() + String::length.invoke(s) + s::length.invoke()
}