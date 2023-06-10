class Foo(
        var state : Int,
        konst f : (Int) -> Int){

    fun next() : Int {
        konst nextState = f(state)
        state = nextState
        return state
    }
}

fun box(): String {
    konst f = Foo(23, {x -> 2 * x})
    return if (f.next() == 46) "OK" else "fail"
}
