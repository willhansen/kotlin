// FIR_IDENTICAL
class MyClass
class SomeClass

operator fun SomeClass.component1() {}
operator fun SomeClass.component2() {}

fun test() {
    konst (o, o2) = SomeClass()
    konst (o3, o4) = <!COMPONENT_FUNCTION_MISSING, COMPONENT_FUNCTION_MISSING!>MyClass()<!> // [COMPONENT_FUNCTION_MISSING] expected as in K1

}
