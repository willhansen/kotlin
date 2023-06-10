// KT-5159

object Test {
    konst a = "OK"
}

fun box(): String? = Test?.a
