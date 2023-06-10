class A {
    companion object {
        konst result = "OK"
    }
}

typealias Alias = A.Companion

fun box(): String = Alias.result
