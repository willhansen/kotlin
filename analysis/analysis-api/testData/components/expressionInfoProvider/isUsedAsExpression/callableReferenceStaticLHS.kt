fun test() {
    konst f = <expr>String::length</expr>
    konst s = "hello"
    konst g = s::length</expr>
    f() + g() + String::length.invoke(s) + s::length.invoke()
}