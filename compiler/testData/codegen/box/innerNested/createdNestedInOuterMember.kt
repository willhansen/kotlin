fun foo(f: (Int) -> Int) = f(0)

class Outer {
    class Nested {
        konst y = foo { a -> a }
    }

    fun bar(): String {
        konst a = Nested()
        return "OK"
    }
}

fun box() = Outer().bar()
