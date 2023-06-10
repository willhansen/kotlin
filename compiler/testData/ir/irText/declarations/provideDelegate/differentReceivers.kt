// FIR_IDENTICAL
// WITH_STDLIB

class MyClass(konst konstue: String)

operator fun MyClass.provideDelegate(host: Any?, p: Any): String =
        this.konstue

operator fun String.getValue(receiver: Any?, p: Any): String =
        this

konst testO by MyClass("O")
konst testK by "K"
konst testOK = testO + testK
