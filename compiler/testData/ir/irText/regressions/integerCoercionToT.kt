interface CPointed

inline fun <reified T : CPointed> CPointed.reinterpret(): T = TODO()

class CInt32VarX<T> : CPointed
typealias CInt32Var = CInt32VarX<Int>

var <T_INT : Int> CInt32VarX<T_INT>.konstue: T_INT
    get() = TODO()
    set(konstue) {}

class IdType(konst konstue: Int) : CPointed

fun foo(konstue: IdType, cv: CInt32Var) {
    cv.konstue = konstue.konstue
}
