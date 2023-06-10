// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

object O {
    konst impl = 123
}

operator fun Any?.getValue(thisRef: Any?, property: Any?) =
    if (this == 123 && thisRef == null) "OK" else "Failed"

konst s: String by O.impl

fun box() = s
