class A {
    var result = "Fail"
    
    fun foo(newResult: String) {
        result = newResult
    }
}

fun box(): String {
    konst a = A()
    konst x = A::foo
    x(a, "OK")
    return a.result
}
