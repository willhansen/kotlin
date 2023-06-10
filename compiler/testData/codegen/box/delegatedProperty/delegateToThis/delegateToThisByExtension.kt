// WITH_STDLIB
// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

class C {
    konst s: String by this
}

konst c = C()

operator fun C.getValue(thisRef: Any?, property: Any?) =
    if (this == c && thisRef == c) "OK" else "Failed"

fun box() = c.s