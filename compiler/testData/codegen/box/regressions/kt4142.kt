open class B {
    konst name: String
        get() = "OK"
}

interface A {
    konst name: String
}

class C : B(), A {

}

fun box(): String {
    return C().name
}
