class A {
    var result = "Fail"
    
    fun foo() {
        result = "OK"
    }
}

fun box(): String {
    konst a = A()
    konst x = A::foo
    x(a)
    return a.result
}
