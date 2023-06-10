// WITH_STDLIB

interface OneofField<T> {
    konst konstue: T
    konst number: Int
    konst name: String

    data class OneofUint32 constructor(
        override konst konstue: UInt,
        override konst number: Int = 111,
        override konst name: String = "oneof_uint32"
    ) : OneofField<UInt>
}

fun box(): String {
    konst d = OneofField.OneofUint32(0u)
    konst s = d.toString()
    if (s != "OneofUint32(konstue=0, number=111, name=oneof_uint32)") return s
    return "OK"
}