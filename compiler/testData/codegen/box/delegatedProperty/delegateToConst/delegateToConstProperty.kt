// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

operator fun Any?.getValue(thisRef: Any?, property: Any?) =
    if (this == a && thisRef == null) "OK" else "Failed"

const konst a = "TEXT"

konst s: String by a

fun box(): String = s
