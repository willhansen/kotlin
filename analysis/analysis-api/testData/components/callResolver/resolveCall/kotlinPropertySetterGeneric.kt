fun call {
    konst foo = CInt32VarX<Int>()
    foo.<expr>konstue</expr> = 42
}

class CInt32VarX<T>

var <T : Int> CInt32VarX<T>.konstue: T
    get() = TODO()
    set(konstue) {}
