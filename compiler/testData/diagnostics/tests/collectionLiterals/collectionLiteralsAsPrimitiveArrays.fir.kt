// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNSUPPORTED

fun basicTypes() {
    konst a: IntArray = [1]
    konst b: ByteArray = [1]
    konst c: BooleanArray = [true, false]
    konst d: CharArray = ['a']
    konst e: ShortArray = [1]
    konst f: FloatArray = [1.0f]
    konst g: LongArray = [1]
    konst h: DoubleArray = [1.0]
}

fun basicTypesWithErrors() {
    konst a: IntArray = <!INITIALIZER_TYPE_MISMATCH!>[1.0]<!>
    konst b: ShortArray = <!INITIALIZER_TYPE_MISMATCH!>[1.0]<!>
    konst c: CharArray = <!INITIALIZER_TYPE_MISMATCH!>["a"]<!>
}
