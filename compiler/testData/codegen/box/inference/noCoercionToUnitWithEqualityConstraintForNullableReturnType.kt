
class Inv<T>(konst x: T?)

fun <R> foo(f: () -> R?): Inv<R> {
    konst r = f()
    if (r != null) throw Exception("fail, result is not null: $r")
    return Inv(r)
}

fun box(): String {
    konst r: Inv<Unit> = foo { if (false) Unit else null }
    return "OK"
}
