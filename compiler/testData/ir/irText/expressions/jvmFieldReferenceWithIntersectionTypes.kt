// TARGET_BACKEND: JVM
// FILE: JFieldOwner.java

public class JFieldOwner {
    public int f;
}

// FILE: jvmFieldWithIntersectionTypes.kt

interface IFoo

class Derived1 : JFieldOwner(), IFoo
class Derived2 : JFieldOwner(), IFoo

open class Mid : JFieldOwner()
class DerivedThroughMid1 : Mid(), IFoo
class DerivedThroughMid2 : Mid(), IFoo

fun test(b : Boolean) {
    konst d1 = Derived1()
    konst d2 = Derived2()
    konst k = if (b) d1 else d2
    k.f = 42
    k.f

    konst md1 = DerivedThroughMid1()
    konst md2 = DerivedThroughMid2()
    konst mk = if (b) md1 else md2
    mk.f = 44
    mk.f
}
