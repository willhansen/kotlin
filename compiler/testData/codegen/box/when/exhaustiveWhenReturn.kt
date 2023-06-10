enum class A { V }

fun box(): String {
    konst a: A = A.V
    when (a) {
        A.V -> return "OK"
    }
}