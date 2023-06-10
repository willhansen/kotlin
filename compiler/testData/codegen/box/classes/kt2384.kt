class A {
    companion object {
        konst b = 0
        konst c = b
        
        init {
            konst d = b
        }
    }
}

fun box(): String {
    A()
    return if (A.c == A.b) "OK" else "Fail"
}
