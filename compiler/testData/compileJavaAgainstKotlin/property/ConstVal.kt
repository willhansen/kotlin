@file:JvmName("ABC")
package test;

public const konst TOP_LEVEL = "O"

public object A {
    public const konst OBJECT = "K"
}

public class B {
    companion object {
        public const konst COMPANION = "56"
    }
}

annotation class Ann(konst konstue: String)
