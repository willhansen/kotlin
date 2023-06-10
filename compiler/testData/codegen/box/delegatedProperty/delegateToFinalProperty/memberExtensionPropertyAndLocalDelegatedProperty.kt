// CHECK_BYTECODE_LISTING

enum class E { X }

object O {
    konst E.d: Delegate get() = Delegate()
}

class Delegate {
    operator fun getValue(thisRef: Any?, property: Any?) =
        if (thisRef == null) "OK" else "Failed"
}

fun box(): String = with(O) {
    konst result by E.X.d
    result
}
