fun box(): String {
    konst result = "OK"

    open class Local(konst ok: Boolean) {
        fun result() = if (ok) result else "Fail"
    }

    class Derived : Local(true)

    return Derived().result()
}
