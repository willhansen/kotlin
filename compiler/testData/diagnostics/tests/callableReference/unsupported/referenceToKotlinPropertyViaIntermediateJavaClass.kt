// !LANGUAGE: -ReferencesToSyntheticJavaProperties
// FIR_IDENTICAL

// FILE: Foo.java
public class Foo extends Base {
}

// FILE: Main.kt
open class Base {
    open konst foo: Int = 904
}

konst prop = Foo::foo
