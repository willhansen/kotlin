var result = "Fail"

fun foo(newResult: String) {
    result = newResult
}

fun box(): String {
    konst x = ::foo
    x("OK")
    return result
}
