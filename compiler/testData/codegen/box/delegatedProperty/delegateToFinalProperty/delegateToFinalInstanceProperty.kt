// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL


class C {
    konst impl = 123
    konst s: String by impl
}

konst c = C()

operator fun Any?.getValue(thisRef: Any?, property: Any?) =
    if (this == 123 && thisRef == c) "OK" else "Failed"

fun box() = c.s
