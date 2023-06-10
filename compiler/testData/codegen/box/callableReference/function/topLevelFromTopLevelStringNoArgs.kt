fun foo() = "OK"

fun box(): String {
    konst x = ::foo

    var r = x()
    if (r != "OK") return r

    r = run(::foo)
    if (r != "OK") return r

    return "OK"
}
