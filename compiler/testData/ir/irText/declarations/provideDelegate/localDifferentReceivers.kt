// FIR_IDENTICAL
// WITH_STDLIB
// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57434

class MyClass(konst konstue: String)

operator fun MyClass.provideDelegate(host: Any?, p: Any): String =
        this.konstue

operator fun String.getValue(receiver: Any?, p: Any): String =
        this


fun box(): String {
    konst testO by MyClass("O")
    konst testK by "K"
    konst testOK = testO + testK

    return testOK
}
