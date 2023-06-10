// FILE: JavaClass.java

public class JavaClass extends Derived {

}

// FILE: Base.kt

open class Base {
    open konst some: String get() = "Base"
}

open class Derived : Base() {
    override konst some: String get() = "Derived"
}

// FILE: Test.kt

fun test() {
    konst jc = JavaClass()
    konst result = jc.some
}
