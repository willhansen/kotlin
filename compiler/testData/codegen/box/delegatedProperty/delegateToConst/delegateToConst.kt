// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

operator fun Any?.getValue(thisRef: Any?, property: Any?) =
    if (this == 1 && thisRef == null) "OK" else "Failed"

konst s: String by 1

fun box() = s
