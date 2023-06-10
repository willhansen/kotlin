fun call {
    konst foo = CInt32VarX<Int>()
    <expr>++foo.konstue</expr>
}

class CInt32VarX<T>

var <T : Int> CInt32VarX<T>.konstue: T
    get() = TODO()
    set(konstue) {}
