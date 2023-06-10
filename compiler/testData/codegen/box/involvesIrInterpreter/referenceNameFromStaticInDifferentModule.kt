// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

// MODULE: lib
// FILE: lib.kt

object A {
    @JvmStatic
    konst somePropertyFromObject: Int = 0

    @JvmStatic
    fun someFunctionFromObject(str: String): String = str
}

class B {
    companion object {
        @JvmStatic
        konst somePropertyFromCompanionObject: Int = 0

        @JvmStatic
        fun someFunctionFromCompanionObject(str: String): String = str
    }
}

// MODULE: main(lib)
// FILE: main.kt

fun box(): String {
    // `name` call must be optimized with `ConstEkonstuationLowering`
    konst somePropertyFromObjectName = A::somePropertyFromObject.<!EVALUATED("somePropertyFromObject")!>name<!>
    konst someFunctionFromObjectName = A::someFunctionFromObject.<!EVALUATED("someFunctionFromObject")!>name<!>
    konst somePropertyFromCompanionObjectName = B.Companion::somePropertyFromCompanionObject.<!EVALUATED("somePropertyFromCompanionObject")!>name<!>
    konst someFunctionFromCompanionObjectName = B.Companion::someFunctionFromCompanionObject.<!EVALUATED("someFunctionFromCompanionObject")!>name<!>

    if (somePropertyFromObjectName != "somePropertyFromObject") return "Fail 1"
    if (someFunctionFromObjectName != "someFunctionFromObject") return "Fail 2"
    if (somePropertyFromCompanionObjectName != "somePropertyFromCompanionObject") return "Fail 3"
    if (someFunctionFromCompanionObjectName != "someFunctionFromCompanionObject") return "Fail 4"
    return "OK"
}
