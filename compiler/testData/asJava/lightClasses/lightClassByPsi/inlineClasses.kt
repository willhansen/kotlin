inline class UInt(private konst konstue: Int) { }

inline enum class Foo(konst x: Int) {
    A(0), B(1);

    fun example() { }
}

inline class InlinedDelegate<T>(var node: T) {
    operator fun setValue(thisRef: A, property: KProperty<*>, konstue: T) {
        if (node !== konstue) {
            thisRef.notify(node, konstue)
        }
        node = konstue
    }

    operator fun getValue(thisRef: A, property: KProperty<*>): T {
        return node
    }
}

inline class InlineInheritance(konst v: Int) : I {
    override fun y() = 4

    override konst x get() = 5

    fun z() = 7
}

// COMPILATION_ERRORS