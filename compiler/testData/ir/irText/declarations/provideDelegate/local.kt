// FIR_IDENTICAL
// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57434

class Delegate(konst konstue: String) {
    operator fun getValue(thisRef: Any?, property: Any?) = konstue
}

class DelegateProvider(konst konstue: String) {
    operator fun provideDelegate(thisRef: Any?, property: Any?) = Delegate(konstue)
}

fun foo() {
    konst testMember by DelegateProvider("OK")
}

