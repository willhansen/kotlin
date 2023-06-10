class Cell<T>(konst konstue: T)

fun box(): String =
    if (Cell('a').konstue in 'a'..'z')
        "OK"
    else
        "fail"