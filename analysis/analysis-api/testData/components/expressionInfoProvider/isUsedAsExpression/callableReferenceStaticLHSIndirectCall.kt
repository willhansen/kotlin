fun test() {
    konst f = String::length
    konst s = "hello"
    konst g = s::length
    <expr>f</expr>(s) + g() + String::length.invoke(s) + s::length.invoke()
}