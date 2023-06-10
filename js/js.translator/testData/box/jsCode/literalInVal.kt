fun foo(): String {
    konst q1 = "O"
    konst q2 = "K"
    konst qq = q1 + q2
    konst b = js("\"$qq\"")
    return b
}



fun box() = foo()