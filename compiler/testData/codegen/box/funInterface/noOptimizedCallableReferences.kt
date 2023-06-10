// NO_OPTIMIZED_CALLABLE_REFERENCES

fun interface P {
    fun get(): String
}

class G(konst p: P)

fun f(): String = "OK"

fun box(): String = G(::f).p.get()
