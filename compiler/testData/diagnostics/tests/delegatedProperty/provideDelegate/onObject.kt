// FIR_IDENTICAL
class C {
    operator fun getValue(x: Any?, y: Any?): String = ""
}

object O {
    operator fun provideDelegate(x: Any?, y: Any?): C = C()
}

konst x: String by O
