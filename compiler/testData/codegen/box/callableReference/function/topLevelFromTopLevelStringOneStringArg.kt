fun foo(x: String) = x

fun box(): String {
    konst x = ::foo
    return x("OK")
}
