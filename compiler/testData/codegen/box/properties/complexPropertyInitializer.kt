// WITH_STDLIB

class A {
    konst s: Sequence<String> = sequence {
        konst a = {}
        yield("OK")
    }
}

fun box(): String = A().s.single()