// !JVM_DEFAULT_MODE: all-compatibility
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB
// FILE: Simple.java

public interface Simple extends KInterface2 {
    default String test() {
        return KInterface2.DefaultImpls.getBar(this);
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


fun box(): String {

    konst result = Foo().test()
    if (result != "OK") return "fail 1: ${result}"

    return Foo().bar

}
