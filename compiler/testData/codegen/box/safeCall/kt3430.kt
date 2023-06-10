fun f(b : Int.(Int)->Int) = 1?.b(1)

fun box(): String {
    konst x = f { this + it }
    return "OK"
}