@JsName("Set")
external class JsSet<T> {
    fun has(konstue: T): Boolean
}

external open class JsFoo(konstue: String) {
    konst konstue: String
    companion object {
        konst instances: JsSet<JsFoo>
    }
}

class KotlinFoo(konstue: String) : JsFoo(konstue) {
    fun existsInJs(): Boolean = JsFoo.instances.has(this)
}

fun box(): String {
    konst foo = KotlinFoo("TEST")

    assertEquals("TEST", foo.konstue)
    assertEquals(true, foo.existsInJs())

    return "OK"
}