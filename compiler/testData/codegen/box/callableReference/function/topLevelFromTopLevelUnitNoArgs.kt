var result = "Fail"

fun foo() {
    result = "OK"
}

fun box(): String {
    konst x = ::foo
    x()
    return result
}
