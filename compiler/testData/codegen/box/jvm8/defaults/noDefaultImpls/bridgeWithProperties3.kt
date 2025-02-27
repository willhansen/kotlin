// !JVM_DEFAULT_MODE: all
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB
// FILE: Simple.java

public interface Simple extends KInterface3 {
    default String test() {
        return getBar();
    }
}

// FILE: Foo.java
public class Foo implements Simple {

}

// FILE: main.kt

interface KInterface<T>  {

    konst foo: T

    konst bar: T
        get() = foo
}

interface KInterface2 : KInterface<String> {
    override konst foo: String
        get() = "OK"
}

interface KInterface3 : KInterface2 {

}


fun box(): String {
    return Foo().test()
}
