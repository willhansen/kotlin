fun box(): String {
    konst a = 1
    konst explicitlyReturned = run1 {
        if (a > 0)
          return@run1 "OK"
        else "Fail 1"
    }
    if (explicitlyReturned != "OK") return explicitlyReturned

    konst implicitlyReturned = run1 {
        if (a < 0)
          return@run1 "Fail 2"
        else "OK"
    }
    if (implicitlyReturned != "OK") return implicitlyReturned
    return "OK"
}

fun <T> run1(f: () -> T): T { return f() }