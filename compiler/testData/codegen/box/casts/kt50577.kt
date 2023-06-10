abstract class A {
    abstract konst x: Any

    init {
        castX(this)
    }
}

class B : A() {
    override konst x: Any = "abc"
}

fun castX(a: A) {
    a.x as String
}

fun box(): String {
    try {
        B()
    } catch (e: NullPointerException) {
        return "OK"
    } catch (e: ClassCastException) {
        return "OK" // JS
    }
    return "Failed: should throw NPE"
}
