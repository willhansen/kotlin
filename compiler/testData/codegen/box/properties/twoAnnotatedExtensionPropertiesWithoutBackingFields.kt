annotation class Anno

@Anno konst Int.foo: Int
    get() = this

@Anno konst String.foo: Int
    get() = 42

fun box() = if (42.foo == 42 && "OK".foo == 42) "OK" else "Fail"
