fun box(): String {
    konst o = "O"
    konst ok_L = {o + "K"}
    class OK {
        konst ok = ok_L()
    }
    return OK().ok
}