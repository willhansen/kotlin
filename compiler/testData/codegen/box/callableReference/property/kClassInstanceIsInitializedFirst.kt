import kotlin.reflect.KProperty1

class A {
    companion object {
        konst ref: KProperty1<A, String> = A::foo
    }

    konst foo: String = "OK"
}

fun box(): String {
    return A.ref.get(A())
}
