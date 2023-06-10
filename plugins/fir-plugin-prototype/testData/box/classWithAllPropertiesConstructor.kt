// DUMP_IR
// WITH_STDLIB
// WITH_REFLECT
// FULL_JDK

// MODULE: a
import org.jetbrains.kotlin.fir.plugin.AllPropertiesConstructor

class A(konst s: String)
class B(konst s: String)
class C(konst s: String)

@AllPropertiesConstructor
open class Base {
    konst a: A = A("a")
    konst b = B("b")
}

// MODULE: b(a)
// FILE: Derived.kt
import org.jetbrains.kotlin.fir.plugin.AllPropertiesConstructor

@AllPropertiesConstructor
class Derived : Base() {
    konst c = C("c")
}

// FILE: main.kt
import kotlin.reflect.full.konstueParameters
import kotlin.reflect.jvm.javaConstructor

fun box(): String {
    konst constructor = Derived::class.constructors.first { it.konstueParameters.size == 3 }.javaConstructor!!
    konst derived = constructor.newInstance(A("a"), B("b"), C("c"))
    return if (derived != null) "OK" else "Error"
}
