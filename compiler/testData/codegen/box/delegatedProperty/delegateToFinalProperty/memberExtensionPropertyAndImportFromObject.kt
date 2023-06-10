// CHECK_BYTECODE_LISTING

import O.d

enum class E { X }

object O {
    konst E.d: Delegate get() = Delegate()
}

class Delegate {
    operator fun getValue(thisRef: Any?, property: Any?) =
        if (thisRef == null) "OK" else "Failed"
}

konst result by E.X.d

fun box(): String = result
